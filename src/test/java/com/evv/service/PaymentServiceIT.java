package com.evv.service;

import com.evv.NonTransactionalIT;
import com.evv.database.entity.*;
import com.evv.dto.PaymentCreditCardCreateEditDto;
import com.evv.dto.PaymentCreditCardReadDto;
import com.evv.mapper.CreditCardMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@RequiredArgsConstructor
class PaymentServiceIT extends NonTransactionalIT {

    private final PaymentService paymentService;

    private final CreditCardMapper creditCardMapper;

    @Test
    void createPaymentCreditCardWithBalanceReduce_NormalFlow() {
        // given
        BigDecimal creditCardInitBalance = BigDecimal.valueOf(-1000_5000, 4);
        BigDecimal paymentAmount = BigDecimal.valueOf(5_000_0000, 4);
        BigDecimal creditCardExpectedBalance = creditCardInitBalance.subtract(paymentAmount);

        Client client = new Client(1L, "test-client-email1@gmail.com", "{noop}156", Role.CLIENT,
                LocalDate.of(2004, 1, 21), ClientStatus.ACTIVE, null, null,
                new ArrayList<>(), new ArrayList<>());

        CreditCard creditCard = new CreditCard(1L, LocalDate.of(2026, 1, 1),
                creditCardInitBalance, BigDecimal.valueOf(-30000_0000, 4),
                CreditCardStatus.ACTIVE, client, new ArrayList<>(), 0L);

        client.addCreditCard(creditCard);

        PaymentCreditCardCreateEditDto paymentCreditCardCreateEditDto =
                new PaymentCreditCardCreateEditDto(paymentAmount,
                        creditCardMapper.creditCardToCreditCardReadDto(creditCard));

        // when
        PaymentCreditCardReadDto paymentCreditCardReadDto =
                paymentService.createPaymentCreditCardWithBalanceReduce(paymentCreditCardCreateEditDto).get();

        // then
        assertThat(paymentCreditCardReadDto.getId()).isPositive();
        assertThat(paymentCreditCardReadDto.getAmount()).isEqualTo(paymentAmount);
        assertThat(paymentCreditCardReadDto.getCreditCard().getId()).isEqualTo(creditCard.getId());
        assertThat(paymentCreditCardReadDto.getCreditCard().getBalance()).isEqualTo(creditCardExpectedBalance);
    }
}