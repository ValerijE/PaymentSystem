package com.evv.service;

import com.evv.database.entity.CreditCard;
import com.evv.database.entity.CreditCardStatus;
import com.evv.database.repository.CreditCardRepository;
import com.evv.exception.AttemptToChargeMoneyFromBlockedCreditCardException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;

/**
 * Класс для тестирования бросаемых исключений.
 */
@ExtendWith(MockitoExtension.class)
public class CreditCardServiceTest {

    @Mock
    CreditCardRepository creditCardRepository;

    @InjectMocks
    CreditCardService creditCardService;

    // GIVEN для всех тестов
    private static final Long CREDIT_CARD_ID = 222L;

    @Test
    void doReduceBalance_CreditCardIsBlocked_ThrowAttemptToChargeMoneyFromBlockedCreditCardException() {
        // given
        CreditCard creditCard = new CreditCard(CREDIT_CARD_ID,
                LocalDate.of(2026, 5, 3),
                BigDecimal.valueOf(-123_123_0500, 4),
                BigDecimal.valueOf(150_000_0500, 4),
                CreditCardStatus.BLOCKED, null, new ArrayList<>(), 0L);

        // stub
        doReturn(Optional.of(creditCard)).when(creditCardRepository).findById(CREDIT_CARD_ID);

        // when
        assertThatThrownBy(() ->
                creditCardService.reduceBalance(CREDIT_CARD_ID, BigDecimal.valueOf(1000))

        // then
        ).isInstanceOf(AttemptToChargeMoneyFromBlockedCreditCardException.class)
                .hasMessageContaining(
                        "There was an attempt to charge money from blocked credit card id = %d"
                                .formatted(CREDIT_CARD_ID)
                );
    }
}
