package com.evv.mapper;

import com.evv.database.entity.*;
import com.evv.database.repository.UserRepository;
import com.evv.dto.AccountDto;
import com.evv.exception.ClientByIdNotFoundException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountMapperTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    AccountMapperImpl accountMapper;

    // GIVEN для всех тестов
    private static final Long CLIENT_ID = 111L;

    private static final Long ACCOUNT_ID = 333L;

    private static final String CLIENT_EMAIL = "some_client@test.com";

    private final Client client = new Client(CLIENT_ID, CLIENT_EMAIL, "{noop}000", Role.CLIENT,
            LocalDate.of(2000, 5, 5), ClientStatus.ACTIVE, null,
            Gender.MALE, null, null);

    private final BigDecimal accountBalance = BigDecimal.valueOf(35_000_0000, 4);

    private final Account account = new Account(
            ACCOUNT_ID, accountBalance, client, null, 0L);

    private final AccountDto.CreateEdit.Public accountCreateEditDto = new AccountDto.CreateEdit.Public(
            accountBalance, CLIENT_ID);

    @Test
    void accountToAccountReadDto_NormalFlow() {
        // When
        AccountDto.Read.Public accountReadDto = accountMapper.accountToAccountReadDto(account);

        // Then
        assertThat(accountReadDto.getId()).isEqualTo(ACCOUNT_ID);
        assertThat(accountReadDto.getBalance()).isEqualTo(accountBalance);
        assertThat(accountReadDto.getClientId()).isEqualTo(CLIENT_ID);
    }


    @Test
    void accountCreateEditDtoToAccount_NormalFlow() {
        // stub
        doReturn(Optional.of(client)).when(userRepository).findClientById(CLIENT_ID);

        // When
        Account account = accountMapper.accountCreateEditDtoToAccount(accountCreateEditDto);

        // Then
        assertThat(account.getBalance()).isEqualTo(accountBalance);
        assertThat(account.getClient().getId()).isEqualTo(CLIENT_ID);

        verify(userRepository).findClientById(CLIENT_ID);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void accountCreateEditDtoToAccount_UserRepositoryReturnEmptyOptional_ThrowClientByIdNotFoundException() {
        // stub
        doReturn(Optional.empty()).when(userRepository).findClientById(CLIENT_ID);

        // When
        Assertions.assertThatThrownBy(() -> accountMapper
                        .accountCreateEditDtoToAccount(accountCreateEditDto))
        // Then
                .isInstanceOf(ClientByIdNotFoundException.class)
                .hasMessageContaining("not found");

        verify(userRepository).findClientById(CLIENT_ID);
        verifyNoMoreInteractions(userRepository);
    }
}
