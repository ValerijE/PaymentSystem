package com.evv.http.rest;

import com.evv.NonTransactionalIT;
import com.evv.database.entity.Account;
import com.evv.database.entity.CreditCard;
import com.evv.database.repository.AccountRepository;
import com.evv.database.repository.CreditCardRepository;
import lombok.RequiredArgsConstructor;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;

import static com.evv.TestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RequiredArgsConstructor
@AutoConfigureMockMvc(printOnlyOnFailure = false)
class PurchaseRestControllerIT extends NonTransactionalIT {

    private final MockMvc mockMvc;

    private final CreditCardRepository creditCardRepository;

    private final AccountRepository accountRepository;

    @Test
    @WithUserDetails(FIRST_CLIENT_EMAIL)
    void findAll_ClientAskedForHimselfWithoutClientId_ReturnClientPageResponse() throws Exception {
        // given
        MockHttpServletRequestBuilder requestBuilder = get("/api/v1/purchases")
                .queryParam("size", "2")
                .queryParam("page", "0");
        // when
        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
        // then
                        status().isOk(),
                        header().stringValues("content-type", "application/json"),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                        content().json(JSON_2_PURCHASES));
    }

    @Test
    @WithUserDetails(FIRST_CLIENT_EMAIL)
    void findAll_ClientAskedForHimselfWithClientId_ReturnClientPageResponse() throws Exception {
        // given
        MockHttpServletRequestBuilder requestBuilder = get("/api/v1/purchases")
                .queryParam("clientId", "1")
                .queryParam("size", "2")
                .queryParam("page", "0");
        // when
        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
        // then
                        status().isOk(),
                        header().stringValues("content-type", "application/json"),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                        content().json(JSON_2_PURCHASES));
    }

    @Test
    @WithUserDetails(FIRST_ADMIN_EMAIL)
    void findAll_AdminAskedEndpointWithoutClientId_ReturnAllClientsPageResponse() throws Exception {
        // given
        MockHttpServletRequestBuilder requestBuilder = get("/api/v1/purchases")
                .queryParam("size", "4")
                .queryParam("page", "0");
        // when
        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
        // then
                        status().isOk(),
                        header().stringValues("content-type", "application/json"),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                        content().json(JSON_4_PURCHASES));
    }

    @Test
    @WithUserDetails(FIRST_ADMIN_EMAIL)
    void findAll_AdminAskedEndpointWithClientId_ReturnClientPageResponse() throws Exception {
        // given
        MockHttpServletRequestBuilder requestBuilder = get("/api/v1/purchases")
                .queryParam("clientId", "1")
                .queryParam("size", "2")
                .queryParam("page", "0");
        // when
        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
        // then
                        status().isOk(),
                        header().stringValues("content-type", "application/json"),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                        content().json(JSON_2_PURCHASES));
    }

    @Test
    @WithUserDetails(FIRST_CLIENT_EMAIL)
    void findAll_ClientAskedForHimselfWithoutPageable_ReturnDefPageSizeClientPageResponse() throws Exception {
        // given
        MockHttpServletRequestBuilder requestBuilder = get("/api/v1/purchases");
        // when
        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
        // then
                        status().isOk(),
                        header().stringValues("content-type", "application/json"),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                        content().json(JSON_3_PURCHASES_WITH_DEF_PAGE_SIZE));
    }



    @Test
    @WithUserDetails(FIRST_CLIENT_EMAIL)
    void findAll_ClientAskedForSecondPage_ReturnSecondPageClientPageResponse() throws Exception {
        // given
        MockHttpServletRequestBuilder requestBuilder = get("/api/v1/purchases")
                .queryParam("size", "2")
                .queryParam("page", "1");
        // when
        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
        // then
                        status().isOk(),
                        header().stringValues("content-type", "application/json"),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                        content().json(JSON_SECOND_PAGE_1_PURCHASE_WITH_PAGE_SIZE_2));
    }

    @Test
    @WithUserDetails(FIRST_CLIENT_EMAIL)
    void findAll_ClientAskedForOtherClient_Return403() throws Exception {
        // given
        MockHttpServletRequestBuilder requestBuilder = get("/api/v1/purchases")
                .queryParam("clientId", "2");

        // when
        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
        // then
                        status().isForbidden(),
                        header().stringValues("content-type", "application/problem+json"),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                );
    }

    @Test
    @WithAnonymousUser
    void findAll_AnonymAskedForAllWithoutClientId_Return403() throws Exception {
        // given
        MockHttpServletRequestBuilder requestBuilder = get("/api/v1/purchases")
                .queryParam("size", "8")
                .queryParam("page", "0");
        // when
        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
        // then
                        status().isUnauthorized(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().string(containsString(
                                "Anonymous client ip =")),
                        content().string(containsString(
                                "attempted to access endpoint GET /api/v1/purchases without authentication"))

                );
    }

    @Test
    @WithUserDetails(FIRST_CLIENT_EMAIL)
    void findById_ClientAskedForHimself_ReturnDTO() throws Exception {
        // given
        MockHttpServletRequestBuilder requestBuilder = get("/api/v1/purchases/2");
        // when
        mockMvc.perform(requestBuilder)
                .andExpectAll(
        // then
                        status().isOk(),
                        header().stringValues("content-type", "application/json"),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                        content().json(JSON_1_PURCHASE)
                );
    }

    @Test
    @WithUserDetails(FIRST_CLIENT_EMAIL)
    void findById_ClientAskedForOtherClient_Return403() throws Exception {
        // given
        MockHttpServletRequestBuilder requestBuilder = get("/api/v1/purchases/3");
        // when
        mockMvc.perform(requestBuilder)
                .andExpectAll(
        // then
                        status().isForbidden(),
                        header().stringValues("content-type", "application/problem+json"),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                );
    }

    @Test
    @WithUserDetails(FIRST_CLIENT_EMAIL)
    void findById_ClientAskedForAbsentPurchase_Return404() throws Exception {
        // given
        MockHttpServletRequestBuilder requestBuilder = get("/api/v1/purchases/10");
        // when
        mockMvc.perform(requestBuilder)
                .andExpectAll(
        // then
                        status().isNotFound(),
                        header().stringValues("content-type", "application/problem+json"),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                );
    }

    @Test
    @WithUserDetails(FIRST_ADMIN_EMAIL)
    void findById_AdminAskedForExistentPurchase_ReturnDTO() throws Exception {
        // given
        MockHttpServletRequestBuilder requestBuilder = get("/api/v1/purchases/2");
        // when
        mockMvc.perform(requestBuilder)
                .andExpectAll(
        // then
                        status().isOk(),
                        header().stringValues("content-type", "application/json"),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                        content().json(JSON_1_PURCHASE)
                );
    }

    @Test
    @WithUserDetails(FIRST_ADMIN_EMAIL)
    void findById_AdminAskedForAbsentPurchase_Return404() throws Exception {
        // given
        MockHttpServletRequestBuilder requestBuilder = get("/api/v1/purchases/10");
        // when
        mockMvc.perform(requestBuilder)
                .andExpectAll(
        // then
                        status().isNotFound(),
                        header().stringValues("content-type", "application/problem+json"),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                );
    }

    @Test
    @WithUserDetails(FIRST_CLIENT_EMAIL)
    void create_ClientCreateByCreditCard_ReturnDTO() throws Exception {
        // given
        MockHttpServletRequestBuilder requestBuilder = post("/api/v1/purchases")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                        "totalCost": 8500,
                        "chosenProductIds": [4, 9, 12],
                        "creditCardId": 1,
                        "accountId": "null"
                        }
                        """);
        // when
        mockMvc.perform(requestBuilder)
                .andExpectAll(
        // then
                        status().isCreated(),
                        header().string(HttpHeaders.LOCATION,
                                "http://localhost/api/v1/purchase/6"),
                        header().stringValues("content-type", "application/json"),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                        content().json(JSON_NEW_CREDIT_CARD_PURCHASE)
                );
    }

    @Test
    @WithUserDetails(FIRST_CLIENT_EMAIL)
    void create_ClientCreateByAccount_ReturnDTO() throws Exception {
        // given
        MockHttpServletRequestBuilder requestBuilder = post("/api/v1/purchases")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                        "totalCost": 8500,
                        "chosenProductIds": [4, 9, 12],
                        "creditCardId": null,
                        "accountId": 1
                        }
                        """);
        // when
        mockMvc.perform(requestBuilder)
                .andExpectAll(
        // then
                        status().isCreated(),
                        header().string(HttpHeaders.LOCATION,
                                "http://localhost/api/v1/purchase/6"),
                        header().stringValues("content-type", "application/json"),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                        content().json(JSON_NEW_ACCOUNT_PURCHASE)
                );
    }

    @Test
    @WithUserDetails(FIRST_CLIENT_EMAIL)
    void create_ClientCreateContainAbsentProduct_Return404() throws Exception {
        // given
        MockHttpServletRequestBuilder requestBuilder = post("/api/v1/purchases")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                        "totalCost": 8500,
                        "chosenProductIds": [4, 50],
                        "creditCardId": 1,
                        "accountId": "null"
                        }
                        """);
        // when
        mockMvc.perform(requestBuilder)
                .andExpectAll(
        // then
                        status().isNotFound(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().string(containsString("Product with id = 50 not found"))
                );
    }

    @Test
    @WithUserDetails(FIRST_CLIENT_EMAIL)
    void create_ClientCreateByAbsentCreditCard_Return404() throws Exception {
        // given
        MockHttpServletRequestBuilder requestBuilder = post("/api/v1/purchases")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                        "totalCost": 8500,
                        "chosenProductIds": [4, 7],
                        "creditCardId": 50,
                        "accountId": "null"
                        }
                        """);
        // when
        mockMvc.perform(requestBuilder)
                .andExpectAll(
        // then
                        status().isNotFound(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().string(containsString("Credit card with id = 50 not found"))
                );
    }

    @Test
    @WithUserDetails(FIRST_CLIENT_EMAIL)
    void create_ClientCreateWithNoPaymentMethod_Return400() throws Exception {
        // given
        MockHttpServletRequestBuilder requestBuilder = post("/api/v1/purchases")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                        "totalCost": 8500,
                        "chosenProductIds": [4, 7],
                        "creditCardId": null,
                        "accountId": "null"
                        }
                        """);
        // when
        mockMvc.perform(requestBuilder)
                .andExpectAll(
        // then
                        status().isBadRequest(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().string(containsString(
                                "One of two reasons: 1. Account or Credit card must be selected, 2. Account and Credit card both selected"))
                );
    }

    @Test
    @WithUserDetails(FIRST_CLIENT_EMAIL)
    void create_ClientCreateWithBothPaymentMethods_Return400() throws Exception {
        // given
        MockHttpServletRequestBuilder requestBuilder = post("/api/v1/purchases")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                        "totalCost": 8500,
                        "chosenProductIds": [4, 7],
                        "creditCardId": 1,
                        "accountId": 1
                        }
                        """);
        // when
        mockMvc.perform(requestBuilder)
                .andExpectAll(
        // then
                        status().isBadRequest(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().string(containsString(
                                "One of two reasons: 1. Account or Credit card must be selected, 2. Account and Credit card both selected"))
                );
    }

    @Test
    @WithAnonymousUser
    void create_AnonymCreateCreditCard_Return403() throws Exception {
        // given
        MockHttpServletRequestBuilder requestBuilder = post("/api/v1/purchases")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                        "totalCost": 8500,
                        "chosenProductIds": [4, 9, 12],
                        "creditCardId": 1,
                        "accountId": "null"
                        }
                        """);
        // when
        mockMvc.perform(requestBuilder)
                .andExpectAll(
        // then
                        status().isUnauthorized(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().string(containsString(
                                "Anonymous client ip =")),
                        content().string(containsString(
                                "attempted to access endpoint POST /api/v1/purchases without authentication"))

                );
    }

    @Test
    @WithUserDetails(FIRST_ADMIN_EMAIL)
    void delete_AdminDeleteExistentPurchase_Return204() throws Exception {
        // given
        MockHttpServletRequestBuilder requestBuilder = delete("/api/v1/purchases/1");
        // when
        mockMvc.perform(requestBuilder)
                .andExpectAll(
        // then
                        status().isNoContent());
    }

    @Test
    @WithUserDetails(FIRST_ADMIN_EMAIL)
    void delete_AdminDeleteAbsentPurchase_Return404() throws Exception {
        // given
        MockHttpServletRequestBuilder requestBuilder = delete("/api/v1/purchases/10");
        // when
        mockMvc.perform(requestBuilder)
                .andExpectAll(
        // then
                        status().isNotFound(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().string(containsString(
                                "Purchase with id = 10 not found for current user"))
                );
    }

    @Test
    @WithUserDetails(FIRST_CLIENT_EMAIL)
    void delete_ClientAttemptedDelete_Return403() throws Exception {
        // given
        MockHttpServletRequestBuilder requestBuilder = delete("/api/v1/purchases/1");
        // when
        mockMvc.perform(requestBuilder)
                .andExpectAll(
        // then
                        status().isForbidden(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().string(containsString(
                                "Client id = 1 attempted to access endpoint DELETE /api/v1/purchases/1 without appropriate permissions"))
                );
    }

    @Test
    @WithAnonymousUser
    void delete_AnonymAttemptedDelete_Return403() throws Exception {
        // given
        MockHttpServletRequestBuilder requestBuilder = delete("/api/v1/purchases/1");
        // when
        mockMvc.perform(requestBuilder)
                .andExpectAll(
        // then
                        status().isUnauthorized(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().string(containsString(
                                "Anonymous client ip =")),
                        content().string(containsString(
                                "attempted to access endpoint DELETE /api/v1/purchases/1 without authentication"))

                );
    }

    @Test
    @WithUserDetails(FIRST_ADMIN_EMAIL)
    void update_WasPaymentByCreditCardAndBecomePaymentBySameCreditCard_Return200() throws Exception {
        // Данные из TestData.sql:
        BigDecimal ccInitBalance = BigDecimal.valueOf(-1000_5000, 4);
        BigDecimal oldPaymentAmount = BigDecimal.valueOf(5000_0000, 4);
        // Новые данные:
        BigDecimal newPaymentAmount = BigDecimal.valueOf(8500_0000, 4);
        // Расчетные данные
        BigDecimal ccExpectedBalance = ccInitBalance.add(oldPaymentAmount).subtract(newPaymentAmount);

        // given
        MockHttpServletRequestBuilder requestBuilder = put("/api/v1/purchases/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                        "totalCost": 8500,
                        "chosenProductIds": [9, 12],
                        "creditCardId": 1,
                        "accountId": "null"
                        }
                        """);
        // when
        mockMvc.perform(requestBuilder)
                .andExpectAll(
        // then
                        status().isOk(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                        content().json(JSON_UPDATED_PURCHASE_CC_TO_SAME_CC)
                );
        // Проверка баланса
        CreditCard creditCard = creditCardRepository.findById(1L).get();
        assertThat(creditCard.getBalance()).isEqualTo(ccExpectedBalance);
    }

    @Test
    @WithUserDetails(FIRST_ADMIN_EMAIL)
    void update_WasPaymentByCreditCardAndBecomePaymentByOtherCreditCard_Return200() throws Exception {
        // Данные из TestData.sql:
        BigDecimal oldCcInitBalance1 = BigDecimal.valueOf(-1000_5000, 4);
        BigDecimal newCcInitBalance2 = BigDecimal.valueOf(-50000_0000, 4);
        BigDecimal oldPaymentAmount = BigDecimal.valueOf(5000_0000, 4);
        // Новые данные:
        BigDecimal newPaymentAmount = BigDecimal.valueOf(8500_0000, 4);
        // Расчетные данные
        BigDecimal oldCcExpectedBalance = oldCcInitBalance1.add(oldPaymentAmount);
        BigDecimal newCcExpectedBalance = newCcInitBalance2.subtract(newPaymentAmount);

        // given
        MockHttpServletRequestBuilder requestBuilder = put("/api/v1/purchases/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                        "totalCost": 8500,
                        "chosenProductIds": [9, 12],
                        "creditCardId": 2,
                        "accountId": "null"
                        }
                        """);
        // when
        mockMvc.perform(requestBuilder)
                .andExpectAll(
        // then
                        status().isOk(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                        content().json(JSON_UPDATED_PURCHASE_CC_TO_OTHER_CC)
                );
        // Проверка баланса
        CreditCard oldCreditCard = creditCardRepository.findById(1L).get();
        assertThat(oldCreditCard.getBalance()).isEqualTo(oldCcExpectedBalance);
        CreditCard newCreditCard = creditCardRepository.findById(2L).get();
        assertThat(newCreditCard.getBalance()).isEqualTo(newCcExpectedBalance);
    }

    @Test
    @WithUserDetails(FIRST_ADMIN_EMAIL)
    void update_WasPaymentByCreditCardAndBecomePaymentByAccount_Return200() throws Exception {
        // Данные из TestData.sql:
        BigDecimal oldCcInitBalance1 = BigDecimal.valueOf(-1000_5000, 4);
        BigDecimal newAccountInitBalance = BigDecimal.valueOf(25000_1000, 4);
        BigDecimal oldPaymentAmount = BigDecimal.valueOf(5000_0000, 4);
        // Новые данные:
        BigDecimal newPaymentAmount = BigDecimal.valueOf(8500_0000, 4);
        // Расчетные данные
        BigDecimal oldCcExpectedBalance = oldCcInitBalance1.add(oldPaymentAmount);
        BigDecimal newAccountExpectedBalance = newAccountInitBalance.subtract(newPaymentAmount);

        // given
        MockHttpServletRequestBuilder requestBuilder = put("/api/v1/purchases/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                        "totalCost": 8500,
                        "chosenProductIds": [9, 12],
                        "creditCardId": "null",
                        "accountId": 1
                        }
                        """);
        // when
        mockMvc.perform(requestBuilder)
                .andExpectAll(
        // then
                        status().isOk(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                        content().json(JSON_UPDATED_PURCHASE_CC_TO_ACCOUNT)
                );
        // Проверка баланса
        CreditCard oldCreditCard = creditCardRepository.findById(1L).get();
        assertThat(oldCreditCard.getBalance()).isEqualTo(oldCcExpectedBalance);
        Account newAccount = accountRepository.findById(1L).get();
        assertThat(newAccount.getBalance()).isEqualTo(newAccountExpectedBalance);
    }

    @Test
    @WithUserDetails(FIRST_ADMIN_EMAIL)
    void update_WasPaymentByAccountAndBecomePaymentBySameAccount_Return200() throws Exception {
        // Данные из TestData.sql:
        BigDecimal accountInitBalance = BigDecimal.valueOf(25000_1000, 4);
        BigDecimal oldPaymentAmount = BigDecimal.valueOf(15000_0000, 4);
        // Новые данные:
        BigDecimal newPaymentAmount = BigDecimal.valueOf(8500_0000, 4);
        // Расчетные данные
        BigDecimal accountExpectedBalance = accountInitBalance.add(oldPaymentAmount).subtract(newPaymentAmount);

        // given
        MockHttpServletRequestBuilder requestBuilder = put("/api/v1/purchases/2")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                        "totalCost": 8500,
                        "chosenProductIds": [9, 12],
                        "creditCardId": "null",
                        "accountId": 1
                        }
                        """);
        // when
        mockMvc.perform(requestBuilder)
                .andExpectAll(
        // then
                        status().isOk(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                        content().json(JSON_UPDATED_PURCHASE_ACCOUNT_TO_SAME_ACCOUNT)
                );
        // Проверка баланса
        Account account = accountRepository.findById(1L).get();
        assertThat(account.getBalance()).isEqualTo(accountExpectedBalance);
    }

    @Test
    @WithUserDetails(FIRST_ADMIN_EMAIL)
    void update_WasPaymentByAccountAndBecomePaymentByOtherAccount_Return200() throws Exception {
        // Данные из TestData.sql:
        BigDecimal oldAccountInitBalance = BigDecimal.valueOf(25000_1000, 4);
        BigDecimal oldPaymentAmount = BigDecimal.valueOf(15000_0000, 4);
        BigDecimal newAccountInitBalance = BigDecimal.valueOf(50000_0000, 4);
        // Новые данные:
        BigDecimal newPaymentAmount = BigDecimal.valueOf(8500_0000, 4);
        // Расчетные данные
        BigDecimal oldAccountExpectedBalance = oldAccountInitBalance.add(oldPaymentAmount);
        BigDecimal newAccountExpectedBalance = newAccountInitBalance.subtract(newPaymentAmount);

        // given
        MockHttpServletRequestBuilder requestBuilder = put("/api/v1/purchases/2")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                        "totalCost": 8500,
                        "chosenProductIds": [9, 12],
                        "creditCardId": "null",
                        "accountId": 2
                        }
                        """);
        // when
        mockMvc.perform(requestBuilder)
                .andExpectAll(
        // then
                        status().isOk(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                        content().json(JSON_UPDATED_PURCHASE_ACCOUNT_TO_OTHER_ACCOUNT)
                );
        // Проверка баланса
        Account oldAccount = accountRepository.findById(1L).get();
        assertThat(oldAccount.getBalance()).isEqualTo(oldAccountExpectedBalance);
        Account newAccount = accountRepository.findById(2L).get();
        assertThat(newAccount.getBalance()).isEqualTo(newAccountExpectedBalance);
    }

    @Test
    @WithUserDetails(FIRST_ADMIN_EMAIL)
    void update_WasPaymentByAccountAndBecomePaymentByCreditCard_Return200() throws Exception {
        // Данные из TestData.sql:
        BigDecimal oldAccountInitBalance = BigDecimal.valueOf(25000_1000, 4);
        BigDecimal oldPaymentAmount = BigDecimal.valueOf(15000_0000, 4);
        BigDecimal newCcInitBalance = BigDecimal.valueOf(-1000_5000, 4);
        // Новые данные:
        BigDecimal newPaymentAmount = BigDecimal.valueOf(8500_0000, 4);
        // Расчетные данные
        BigDecimal oldAccountExpectedBalance = oldAccountInitBalance.add(oldPaymentAmount);
        BigDecimal newCcExpectedBalance = newCcInitBalance.subtract(newPaymentAmount);

        // given
        MockHttpServletRequestBuilder requestBuilder = put("/api/v1/purchases/2")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                        "totalCost": 8500,
                        "chosenProductIds": [9, 12],
                        "creditCardId": 1,
                        "accountId": "null"
                        }
                        """);
        // when
        mockMvc.perform(requestBuilder)
                .andExpectAll(
        // then
                        status().isOk(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                        content().json(JSON_UPDATED_PURCHASE_ACCOUNT_TO_CREDIT_CARD)
                );
        // Проверка баланса
        Account oldAccount = accountRepository.findById(1L).get();
        assertThat(oldAccount.getBalance()).isEqualTo(oldAccountExpectedBalance);
        CreditCard newCreditCard = creditCardRepository.findById(1L).get();
        assertThat(newCreditCard.getBalance()).isEqualTo(newCcExpectedBalance);
    }

    @Test
    @WithUserDetails(FIRST_ADMIN_EMAIL)
    void update_AbsentPurchase_Return404() throws Exception {

        // given
        MockHttpServletRequestBuilder requestBuilder = put("/api/v1/purchases/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                        "totalCost": 8500,
                        "chosenProductIds": [9, 12],
                        "creditCardId": 1,
                        "accountId": "null"
                        }
                        """);
        // when
        mockMvc.perform(requestBuilder)
                .andExpectAll(
        // then
                        status().isNotFound(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().string(Matchers.containsString(
                                "Purchase with id = 10 not found for current user"))
                );
    }

    @Test
    @WithUserDetails(FIRST_CLIENT_EMAIL)
    void update_ClientAttemptedUpdate_Return403() throws Exception {
        // given
        MockHttpServletRequestBuilder requestBuilder = put("/api/v1/purchases/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                        "totalCost": 8500,
                        "chosenProductIds": [9, 12],
                        "creditCardId": 1,
                        "accountId": "null"
                        }
                        """);
        // when
        mockMvc.perform(requestBuilder)
                .andExpectAll(
        // then
                        status().isForbidden(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().string(containsString(
                                "Client id = 1 attempted to access endpoint PUT /api/v1/purchases/1 without appropriate permissions"))
                );
    }

    @Test
    @WithAnonymousUser
    void update_AnonymAttemptedUpdate_Return403() throws Exception {
        // given
        MockHttpServletRequestBuilder requestBuilder = put("/api/v1/purchases/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                        "totalCost": 8500,
                        "chosenProductIds": [9, 12],
                        "creditCardId": 1,
                        "accountId": "null"
                        }
                        """);
        // when
        mockMvc.perform(requestBuilder)
                .andExpectAll(
        // then
                        status().isUnauthorized(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().string(containsString(
                                "Anonymous client ip =")),
                        content().string(containsString(
                                "attempted to access endpoint PUT /api/v1/purchases/1 without authentication"))

                );
    }
}
