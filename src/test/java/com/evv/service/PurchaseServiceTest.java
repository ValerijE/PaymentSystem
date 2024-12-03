package com.evv.service;

import com.evv.database.entity.ClientStatus;
import com.evv.database.entity.CreditCardStatus;
import com.evv.database.entity.Gender;
import com.evv.database.entity.Role;
import com.evv.dto.*;
import com.evv.exception.PaymentAccountCantBeSavedException;
import com.evv.exception.PaymentCreditCardCantBeSavedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Класс для тестирования бросаемых исключений.
 */
@ExtendWith(MockitoExtension.class)
class PurchaseServiceTest {

    @Mock
    PaymentService paymentService;

    @InjectMocks
    PurchaseService purchaseService;

    // GIVEN для всех тестов
    private static final Long CLIENT_ID = 111L;

    private static final Long CREDIT_CARD_ID = 222L;

    private static final Long ACCOUNT_ID = 333L;

    private static final String CLIENT_EMAIL = "some_client@test.com";

    private final ClientReadDto client = new ClientReadDto(CLIENT_ID, CLIENT_EMAIL, Role.CLIENT,
            LocalDate.of(2000, 5, 5), ClientStatus.ACTIVE, null, Gender.MALE);

    private final CreditCardDto.Read.Public creditCard = new CreditCardDto.Read.Public(CREDIT_CARD_ID,
            LocalDate.of(2026, 5, 3),
            BigDecimal.valueOf(-123_123_0500, 4),
            BigDecimal.valueOf(150_000_0500, 4),
            CreditCardStatus.ACTIVE, CLIENT_ID);

    private final AccountDto.Read.Public account = new AccountDto.Read.Public(ACCOUNT_ID, BigDecimal.valueOf(35_000_0000, 4), CLIENT_ID);

    private final BigDecimal paymentAmount1 = BigDecimal.valueOf(5_000_0000, 4);

    private final ProductReadDto product1 = new ProductReadDto(1L, "Овсянка", BigDecimal.valueOf(1000_0000, 4));
    private final ProductReadDto product2 = new ProductReadDto(2L, "Гречка", BigDecimal.valueOf(500_0000, 4));

    @Test
    void createPurchaseCreditCard_PaymentServiceReturnEmptyOptional_ThrowPaymentCreditCardCantBeSavedException() {
        doReturn(Optional.empty())
                .when(paymentService).createPaymentCreditCardWithBalanceReduce(any(PaymentCreditCardCreateEditDto.class));

        assertThatThrownBy(() ->
            purchaseService.createPurchaseCreditCard(paymentAmount1, client, Set.of(product1, product2), creditCard)
        ).isInstanceOf(PaymentCreditCardCantBeSavedException.class)
                        .hasMessage("Payment from Credit Card id = %d can't be saved".formatted(CREDIT_CARD_ID));

        verify(paymentService).createPaymentCreditCardWithBalanceReduce(any(PaymentCreditCardCreateEditDto.class));
        verifyNoMoreInteractions(paymentService);
    }

    @Test
    void createPurchaseAccount_PaymentServiceReturnEmptyOptional_ThrowPaymentAccountCantBeSavedException() {
        doReturn(Optional.empty())
                .when(paymentService).createPaymentAccountWithBalanceReduce(any(PaymentAccountCreateEditDto.class));

        assertThatThrownBy(() ->
                purchaseService.createPurchaseAccount(paymentAmount1, client, Set.of(product1, product2), account)
        ).isInstanceOf(PaymentAccountCantBeSavedException.class)
                .hasMessage("Payment from account id = %d can't be saved".formatted(ACCOUNT_ID));

        verify(paymentService).createPaymentAccountWithBalanceReduce(any(PaymentAccountCreateEditDto.class));
        verifyNoMoreInteractions(paymentService);
    }
}