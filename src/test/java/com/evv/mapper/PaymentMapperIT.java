package com.evv.mapper;

import com.evv.TransactionalIT;
import com.evv.database.entity.CreditCard;
import com.evv.database.entity.PaymentCreditCard;
import com.evv.database.repository.CreditCardRepository;
import com.evv.dto.PaymentCreditCardCreateEditDto;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@RequiredArgsConstructor
class PaymentMapperIT extends TransactionalIT {

    private final CreditCardRepository creditCardRepository;

    private final PaymentMapper paymentMapper;

    private final CreditCardMapper creditCardMapper;

    @Test
    void paymentCreditCardCreateEditDtoToPaymentCreditCard_NormalFlow() {

        // given
        CreditCard creditCard = creditCardRepository.findById(1L).get();

        BigDecimal paymentAmount = BigDecimal.valueOf(5_000_0000, 4);

        PaymentCreditCardCreateEditDto paymentCreditCardCreateEditDto = new PaymentCreditCardCreateEditDto(
                paymentAmount,
                creditCardMapper.creditCardToCreditCardReadDto(creditCard)
        );

        // when
        PaymentCreditCard paymentCreditCard = paymentMapper
                .paymentCreditCardCreateEditDtoToPaymentCreditCard(paymentCreditCardCreateEditDto);

        // then
        assertThat(paymentCreditCard.getCreditCard()).isEqualTo(creditCard);
        assertThat(paymentCreditCard.getAmount()).isEqualTo(paymentAmount);
        assertThat(paymentCreditCard.getPurchase()).isNull();
    }
}