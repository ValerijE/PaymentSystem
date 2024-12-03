package com.evv.database.repository;

import com.evv.TransactionalIT;
import com.evv.database.entity.*;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@RequiredArgsConstructor
class PaymentRepositoryIT extends TransactionalIT {

    private final PaymentRepository paymentRepository;

    private final CreditCardRepository creditCardRepository;

    private final UserRepository userRepository;

    @Test
    void save() {
        Client client = new Client(null, "saveClientTest@it.com", "{noop}000", Role.CLIENT,
                LocalDate.of(2003, 1, 1), ClientStatus.ACTIVE, null,
                Gender.FEMALE, null, null);

        Client savedClient = userRepository.save(client);

        CreditCard creditCard = new CreditCard(null,
                LocalDate.of(2026, 5, 3),
                BigDecimal.valueOf(-123_123_0500, 4),
                BigDecimal.valueOf(150_000_0500, 4),
                CreditCardStatus.ACTIVE, null, null, 0L);
        creditCard.setClient(savedClient);
        CreditCard savedCreditCard = creditCardRepository.save(creditCard);

        // Product нужны, чтобы получить стоимость покупки как сумму стоимости каждого product
        Product product1 = new Product(null, "Овсянка", BigDecimal.valueOf(1000_0000, 4));
        Product product2 = new Product(null, "Гречка", BigDecimal.valueOf(500_0000, 4));
        BigDecimal totalCost = product1.getCost().add(product2.getCost());

        Purchase purchase = new Purchase(null, client, Set.of(product1, product2), null);

        PaymentCreditCard payment = new PaymentCreditCard(null, totalCost, null, savedCreditCard);
        purchase.setPayment(payment);

        // When
        PaymentCreditCard savedPayment = paymentRepository.save(payment);

        // Then
        assertThat(savedPayment.getId()).isPositive();
        assertThat(savedPayment.getAmount()).isEqualTo(totalCost);
        assertThat(savedPayment.getPurchase()).isEqualTo(purchase);
        assertThat(savedPayment.getCreditCard()).isEqualTo(savedCreditCard);
    }
}