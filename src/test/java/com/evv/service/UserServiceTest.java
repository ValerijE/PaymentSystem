package com.evv.service;

import com.evv.database.entity.*;
import com.evv.database.repository.UserRepository;
import com.evv.dto.ClientCreateEditDto;
import com.evv.exception.ClientCantBeSaveException;
import com.evv.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Класс для тестирования бросаемых исключений
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    UserMapper userMapper;

    @InjectMocks
    UserService userService;

    // GIVEN для всех тестов
    private static final Long CLIENT_ID = 111L;

    private static final Long CREDIT_CARD_ID = 222L;

    private static final Long ACCOUNT_ID = 333L;

    private static final String CLIENT_EMAIL = "some_client@test.com";

    private final CreditCard creditCard = new CreditCard(CREDIT_CARD_ID,
            LocalDate.of(2026, 5, 3),
            BigDecimal.valueOf(-123_123_0500, 4),
            BigDecimal.valueOf(150_000_0500, 4),
            CreditCardStatus.ACTIVE, null, null, 0L);

    private final Account account = new Account(
            ACCOUNT_ID, BigDecimal.valueOf(35_000_0000, 4), null, null, 0L);

    private final Client client = new Client(CLIENT_ID, CLIENT_EMAIL, "client_encoded_password", Role.CLIENT,
            LocalDate.of(2000, 5, 5), ClientStatus.ACTIVE, null, Gender.MALE,
            List.of(account), List.of(creditCard));

    private final ClientCreateEditDto clientCreateEditDto = new ClientCreateEditDto(CLIENT_EMAIL, "client_raw_password", Role.CLIENT,
            LocalDate.of(2000, 5, 5), ClientStatus.ACTIVE, null, Gender.MALE);

    @Test
    void create_UserRepositoryReturnNull_ThrowClientCantBeSaveException() {
        // stub
        doReturn(null)
                .when(userRepository).save(any(Client.class));
        doReturn(client)
                .when(userMapper).clientCreateEditDtoToClient(any(ClientCreateEditDto.class));

        // when
        assertThatThrownBy(() -> userService.create(clientCreateEditDto))

        //then
                .isInstanceOf(ClientCantBeSaveException.class)
                .hasMessage("The error occurred while trying to save client. Possible you trying to register client with existing email \"%s\", or there are some other database problems"
                        .formatted(clientCreateEditDto.getEmail()));
        verify(userRepository).save(any(Client.class));
        verifyNoMoreInteractions(userRepository);
    }
}