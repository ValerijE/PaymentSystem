package com.evv.service;

import com.evv.NonTransactionalIT;
import com.evv.database.entity.ClientStatus;
import com.evv.database.entity.Gender;
import com.evv.database.entity.Role;
import com.evv.dto.ClientCreateEditDto;
import com.evv.dto.ClientReadDto;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;

import java.time.LocalDate;
import java.util.Locale;

import static com.evv.TestUtils.FIRST_CLIENT_EMAIL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@RequiredArgsConstructor
class UserServiceIT extends NonTransactionalIT {

    private final UserService userService;

    private static final LocalDate NOT_ENOUGH_AGE_BIRTHDATE = LocalDate.now().minusYears(17).minusDays(364);
    private static final LocalDate ENOUGH_AGE_BIRTHDATE = LocalDate.now().minusYears(17).minusDays(365);

    @Test
    void create_NormalFlow() {
        final String email = "existing.email@mail.ru";
        final Gender gender = Gender.FEMALE;
        ClientCreateEditDto clientCreateEditDto = new ClientCreateEditDto(email, "rawPassword", null,
                ENOUGH_AGE_BIRTHDATE, null, null, gender);

        ClientReadDto clientReadDto = userService.create(clientCreateEditDto);

        assertThat(clientReadDto.getId()).isPositive();
        assertThat(clientReadDto.getEmail()).isEqualTo(email);
        assertThat(clientReadDto.getRole()).isSameAs(Role.CLIENT);
        assertThat(clientReadDto.getBirthDate()).isEqualTo(ENOUGH_AGE_BIRTHDATE);
        assertThat(clientReadDto.getClientStatus()).isSameAs(ClientStatus.ACTIVE);
        assertThat(clientReadDto.getGender()).isSameAs(gender);
        assertThat(clientReadDto.getImage()).isNull();
    }

    @Test
    void createIfNotExistForOAuth_ClientNotExist_ReturnClientReadDto () {
        // given
        final String email = "not-existing.email@mail.ru";

        // when
        ClientReadDto clientReadDto = userService.createIfNotExistForOAuth(email, ENOUGH_AGE_BIRTHDATE);

        // then
        assertThat(clientReadDto.getId()).isPositive();
        assertThat(clientReadDto.getEmail()).isEqualTo(email);
        assertThat(clientReadDto.getRole()).isSameAs(Role.CLIENT);
        assertThat(clientReadDto.getBirthDate()).isEqualTo(ENOUGH_AGE_BIRTHDATE);
        assertThat(clientReadDto.getClientStatus()).isSameAs(ClientStatus.ACTIVE);
        assertThat(clientReadDto.getGender()).isNull();
        assertThat(clientReadDto.getImage()).isBlank();
    }

    @Test
    void createIfNotExistForOAuth_ClientExist_ReturnClientReadDto () {
        // when
        ClientReadDto clientReadDto = userService.createIfNotExistForOAuth(FIRST_CLIENT_EMAIL, ENOUGH_AGE_BIRTHDATE);

        // then
        assertThat(clientReadDto.getId()).isPositive();
        assertThat(clientReadDto.getEmail()).isEqualTo(FIRST_CLIENT_EMAIL);
        assertThat(clientReadDto.getRole()).isSameAs(Role.CLIENT);
        assertThat(clientReadDto.getBirthDate()).isEqualTo(LocalDate.of(1974, 6, 15));
        assertThat(clientReadDto.getClientStatus()).isSameAs(ClientStatus.ACTIVE);
        assertThat(clientReadDto.getGender()).isNull();
        assertThat(clientReadDto.getImage()).isBlank();
    }

    @Test
    void createIfNotExistForOAuth_ClientNotExistNotEnoughAge_ReturnClientReadDto () {
        // given
        final String email = "not-existing.email@mail.ru";
        LocaleContextHolder.setLocale(Locale.UK);

        // when
        assertThatThrownBy(() ->
                userService.createIfNotExistForOAuth(email, NOT_ENOUGH_AGE_BIRTHDATE))

        // then
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("Age must be over 18");
    }
}