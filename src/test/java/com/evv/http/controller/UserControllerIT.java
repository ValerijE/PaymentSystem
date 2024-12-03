package com.evv.http.controller;

import com.evv.NonTransactionalIT;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.FieldError;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static com.evv.TestUtils.FIRST_CLIENT_EMAIL;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@RequiredArgsConstructor
class UserControllerIT extends NonTransactionalIT {

    private final MockMvc mockMvc;

    private static final String NOT_ENOUGH_AGE_BIRTHDATE = LocalDate.now().minusYears(17).minusDays(364).toString();
    private static final String ENOUGH_AGE_BIRTHDATE = LocalDate.now().minusYears(17).minusDays(365).toString();

    private MultiValueMap<String, String> clientParams(String email, String age) {
        return new LinkedMultiValueMap<>(
                Map.of("email", singletonList(email),
                        "rawPassword", singletonList("test"),
                        "birthDate", singletonList(age),
                        "gender", singletonList("MALE"))
        );
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void findAllClients_AdminAskAdminPage_Return200() throws Exception {
        mockMvc.perform(get("/users/clients"))
                .andExpectAll(
                        status().is2xxSuccessful(),
                        view().name("user/clients"),
                        model().attributeExists("clients"),
                        model().attribute("clients", hasSize(2))
                );
    }

    @Test
    @WithAnonymousUser
    void findAllClients_AnonymAskAdminPage_RedirectToLogin() throws Exception {
        mockMvc.perform(get("/users/clients"))
                .andExpectAll(
                        status().is3xxRedirection(),
                        redirectedUrlPattern("**/users/login")
                );
    }

    @Test
    @WithMockUser(authorities = "CLIENT")
    void findAllClients_StrangeClientAskedAdminPage_ShouldRedirectToError() throws Exception {
        mockMvc.perform(get("/users/clients"))
                .andExpectAll(
                        status().isForbidden(),
                        content().string(containsString("You do not have permission to visit this page!"))
                );
    }

    @Test
    @WithUserDetails(value = FIRST_CLIENT_EMAIL)
    void findAllClients_ClientAskedAdminPage_ShouldRedirectToError() throws Exception {
        mockMvc.perform(get("/users/clients"))
                .andExpectAll(
                        status().isForbidden(),
                        content().string(containsString("You do not have permission to visit this page!"))
                        // Тут не получится сделать проверку view().name("/error/access-denied"), т.к. view=null, т.к. рендеринг этой страницы происходит вручную
                );
    }

    @Test
    @WithAnonymousUser
    void createClient_NormalFlow() throws Exception {
        mockMvc.perform(post("/users/clients")
                        .with(csrf())
                        .params(clientParams("test1@gmail.com", ENOUGH_AGE_BIRTHDATE))
                )
                .andExpectAll(
                        status().is3xxRedirection(),
                        redirectedUrl("/users/login")

                );
    }

    @Test
    @WithAnonymousUser
    @SuppressWarnings("unchecked")
    void createClient_TryingToCreateNotEnoughAgeClient_shouldRedirectRegistration() throws Exception {
        MvcResult mvcResult = mockMvc.perform(post("/users/clients")
                        .with(csrf())
                        .params(clientParams("test1@gmail.com", NOT_ENOUGH_AGE_BIRTHDATE))
                )
                .andExpectAll(
                        status().is3xxRedirection(),
                        redirectedUrl("/users/registration"),
                        flash().attributeCount(2),
                        flash().attributeExists("errors")
                ).andReturn();
        var errors = (List<FieldError>) mvcResult.getFlashMap().get("errors");
        assertThat(errors).hasSize(1);
        assertThat(
                errors.get(0).getDefaultMessage())
                .isEqualTo("Age must be over 18");
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    @ValueSource(strings = {"plainaddress",
            "#@%^%#$@#$@#.com", "@example.com",
            "Joe Smith <email@example.com>",
            "email.example.com",
            "email@example@example.com",
            ".email@example.com",
            "email.@example.com",
            "email..email@example.com",
            "email@example.com (Joe Smith)",
            "email@-example.com",
            "email@example..com",
            "Abc..123@example.com}",
            // Эти 3 проходят валидацию с дефолтным валидатором Hibernate, поэтому применен кастомный regexp:
            "あいうえお@example.com",
            "email@example",
            "email@111.222.333.44444",
            // Эти 3 заявлены как правильные, но не валидируются через кастомный regexp. Ну и хорошо, уж очень они причудливы:
            "email@123.123.123.123",
            "email@[123.123.123.123]",
            "\"email\"@example.com"
    })
    @WithAnonymousUser
    @SuppressWarnings("unchecked")
    void createClient_InvalidEmail_shouldRedirectRegistration(String email) throws Exception {
        MvcResult mvcResult = mockMvc.perform(post("/users/clients")
                        .with(csrf())
                        .params(clientParams(email, ENOUGH_AGE_BIRTHDATE))
                )
                .andExpectAll(
                        status().is3xxRedirection(),
                        redirectedUrl("/users/registration"),
                        flash().attributeCount(2),
                        flash().attributeExists("errors")
                ).andReturn();
        var errors = (List<FieldError>) mvcResult.getFlashMap().get("errors");
        assertThat(errors).hasSizeBetween(1, 2);
    }

    @ParameterizedTest
    @ValueSource(strings = {"email@example.com",
            "firstname.lastname@example.com",
            "email@subdomain.example.com",
            "firstname+lastname@example.com",
            "1234567890@example.com",
            "email@example-one.com",
            "_______@example.com",
            "email@example.name",
            "email@example.museum",
            "email@example.co.jp",
            "firstname-lastname@example.com"
    })
    @WithAnonymousUser
    void createClient_ValidEmail_shouldPassAndRiderectLogin(String email) throws Exception {
        mockMvc.perform(post("/users/clients")
                        .with(csrf())
                        .params(clientParams(email, ENOUGH_AGE_BIRTHDATE))
                )
                .andExpectAll(
                        status().is3xxRedirection(),
                        redirectedUrl("/users/login"),
                        flash().attributeCount(0),
                        model().hasNoErrors()
                );
    }
}