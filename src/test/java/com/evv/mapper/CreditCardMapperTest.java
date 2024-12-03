package com.evv.mapper;

import com.evv.database.entity.*;
import com.evv.database.repository.UserRepository;
import com.evv.dto.CreditCardDto;
import com.evv.exception.ClientByIdNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CreditCardMapperTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    CreditCardMapperImpl creditCardMapper;

    // GIVEN для всех тестов
    private static final Long CLIENT_ID = 111L;

    private static final Long CREDIT_CARD_ID = 222L;

    private static final String CLIENT_EMAIL = "some_client@test.com";

    private static final BigDecimal CREDIT_CARD_BALANCE = BigDecimal.valueOf(10_000_1230L, 4);

    private static final LocalDate EXPIRATION_DATE = LocalDate.of(2025, 5, 5);

    private static final BigDecimal CREDIT_LIMIT = BigDecimal.valueOf(50_000_0000, 4);

    private final Client client = new Client(CLIENT_ID, CLIENT_EMAIL, "{noop}000", Role.CLIENT,
            LocalDate.of(2000, 5, 5), ClientStatus.ACTIVE, null,
            Gender.MALE, null, null);

    private final CreditCardDto.CreateEdit.Public creditCardCreateEditDto = new CreditCardDto.CreateEdit.Public(
            EXPIRATION_DATE,
            CREDIT_CARD_BALANCE,
            CREDIT_LIMIT,
            CreditCardStatus.ACTIVE, CLIENT_ID);

    private final CreditCard creditCard = new CreditCard(
            CREDIT_CARD_ID,
            EXPIRATION_DATE,
            CREDIT_CARD_BALANCE,
            CREDIT_LIMIT,
            CreditCardStatus.ACTIVE, client, null, 0L);

    @Test
    void creditCardToCreditCardReadDto_NormalFlow() {
        // When
        CreditCardDto.Read.Public creditCardReadDto = creditCardMapper.creditCardToCreditCardReadDto(creditCard);

        // Then
        assertThat(creditCardReadDto.getId()).isEqualTo(CREDIT_CARD_ID);
        assertThat(creditCardReadDto.getExpirationDate()).isEqualTo(EXPIRATION_DATE);
        assertThat(creditCardReadDto.getBalance()).isEqualTo(CREDIT_CARD_BALANCE);
        assertThat(creditCardReadDto.getCreditLimit()).isEqualTo(CREDIT_LIMIT);
        assertThat(creditCardReadDto.getStatus()).isSameAs(CreditCardStatus.ACTIVE);
        assertThat(creditCardReadDto.getClientId()).isEqualTo(CLIENT_ID);
    }

    @Test
    void checkCreditCardCreateEditDtoToCreditCard() {
        // stub
        doReturn(Optional.of(client)).when(userRepository).findClientById(CLIENT_ID);

        // When
        CreditCard creditCard = creditCardMapper.creditCardCreateEditDtoToCreditCard(creditCardCreateEditDto);

        // Then
        assertThat(creditCard.getExpirationDate()).isEqualTo(EXPIRATION_DATE);
        assertThat(creditCard.getBalance()).isEqualTo(CREDIT_CARD_BALANCE);
        assertThat(creditCard.getCreditLimit()).isEqualTo(CREDIT_LIMIT);
        assertThat(creditCard.getStatus()).isSameAs(CreditCardStatus.ACTIVE);
        assertThat(creditCard.getClient().getId()).isEqualTo(CLIENT_ID);

        verify(userRepository).findClientById(CLIENT_ID);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void shouldThrow_ClientByIdNotFoundException() {
        // stub
        doReturn(Optional.empty()).when(userRepository).findClientById(CLIENT_ID);

        // When
        assertThatThrownBy(() -> creditCardMapper
                        .creditCardCreateEditDtoToCreditCard(creditCardCreateEditDto))
        // Then
                .isInstanceOf(ClientByIdNotFoundException.class)
                .hasMessageContaining("not found");

        verify(userRepository).findClientById(CLIENT_ID);
        verifyNoMoreInteractions(userRepository);
    }
}
