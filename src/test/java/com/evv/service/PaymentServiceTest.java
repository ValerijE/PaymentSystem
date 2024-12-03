package com.evv.service;

import com.evv.database.entity.CreditCardStatus;
import com.evv.dto.AccountDto;
import com.evv.dto.CreditCardDto;
import com.evv.dto.PaymentAccountCreateEditDto;
import com.evv.dto.PaymentCreditCardCreateEditDto;
import com.evv.exception.UnableToChargeMoneyFromAccountException;
import com.evv.exception.UnableToChargeMoneyFromCreditCardException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Класс для тестирования бросаемых исключений.
 */
@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @Mock
    CreditCardService creditCardService;

    @Mock
    AccountService accountService;

    @InjectMocks
    PaymentService paymentService;

    // GIVEN для всех тестов
    private static final Long CLIENT_ID = 111L;

    private static final Long CREDIT_CARD_ID = 222L;

    private static final Long ACCOUNT_ID = 333L;

    private final CreditCardDto.Read.Public creditCard = new CreditCardDto.Read.Public(CREDIT_CARD_ID,
            LocalDate.of(2026, 5, 3),
            BigDecimal.valueOf(-123_123_0500, 4),
            BigDecimal.valueOf(150_000_0500, 4),
            CreditCardStatus.ACTIVE, CLIENT_ID);

    private final AccountDto.Read.Public account =
            new AccountDto.Read.Public(ACCOUNT_ID, BigDecimal.valueOf(35_000_0000, 4), CLIENT_ID);

    private final BigDecimal paymentAmount1 = BigDecimal.valueOf(5_000_0000, 4);

    private final PaymentCreditCardCreateEditDto paymentCreditCardCreateEditDto = new PaymentCreditCardCreateEditDto(
            paymentAmount1, creditCard);

    private final PaymentAccountCreateEditDto paymentAccountCreateEditDto = new PaymentAccountCreateEditDto(
            paymentAmount1, account);

    @Test
    void createPaymentCreditCardWithBalanceReduce_CreditCardServiceReturnEmptyOptional_ThrowUnableToChargeMoneyFromCreditCardException() {
        // stub
        doReturn(Optional.empty())
                .when(creditCardService).reduceBalance(CREDIT_CARD_ID, paymentAmount1);

        // when
        assertThatThrownBy(() ->
                paymentService.createPaymentCreditCardWithBalanceReduce(paymentCreditCardCreateEditDto)

        // then
        ).isInstanceOf(UnableToChargeMoneyFromCreditCardException.class)
                .hasMessage("Unable to charge money from credit card id = %d".formatted(CREDIT_CARD_ID));

        verify(creditCardService).reduceBalance(CREDIT_CARD_ID, paymentAmount1);
        verifyNoMoreInteractions(creditCardService);
    }


    @Test
    void createPurchaseAccount_AccountServiceReturnEmptyOptional_ThrowUnableToChargeMoneyFromAccountException() {
        // stub
        doReturn(Optional.empty())
                .when(accountService).reduceBalance(ACCOUNT_ID, paymentAmount1);

        // when
        assertThatThrownBy(() ->
                paymentService.createPaymentAccountWithBalanceReduce(paymentAccountCreateEditDto)

        // then
        ).isInstanceOf(UnableToChargeMoneyFromAccountException.class)
                .hasMessage("Unable to charge money from account id = %d".formatted(ACCOUNT_ID));

        verify(accountService).reduceBalance(ACCOUNT_ID, paymentAmount1);
        verifyNoMoreInteractions(accountService);
    }
}
