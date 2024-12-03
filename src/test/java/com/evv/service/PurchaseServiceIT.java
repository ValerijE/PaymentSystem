package com.evv.service;

import com.evv.NonTransactionalIT;
import com.evv.TestUtils;
import com.evv.database.entity.CreditCardStatus;
import com.evv.dto.*;
import com.evv.exception.AccountInsufficientFundsException;
import com.evv.exception.AttemptToChargeMoneyFromBlockedCreditCardException;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@RequiredArgsConstructor
class PurchaseServiceIT extends NonTransactionalIT {

    private final UserService userService;

    private final CreditCardService creditCardService;

    private final AccountService accountService;

    private final PurchaseService purchaseService;

    private final ProductService productService;

    private final EntityManager entityManager;

    @Test
    void createPurchaseCreditCard_NormalFlow() {
        // Клиент и его кредитная карта берутся из @Sql
        ClientReadDto clientReadDto = userService.findClientByEmail(TestUtils.FIRST_CLIENT_EMAIL).get();

        CreditCardDto.Read.Public creditCardReadDto = creditCardService.findAllByClientId(clientReadDto.getId()).stream()
                .filter(dto -> dto.getStatus() != CreditCardStatus.BLOCKED)
                .findFirst()
                .get();

        BigDecimal initialBalance = creditCardReadDto.getBalance();

        ProductCreateEditDto productCreateEditDto1 =
                new ProductCreateEditDto("Овсянка", BigDecimal.valueOf(1000_0000, 4));
        ProductCreateEditDto productCreateEditDto2 =
                new ProductCreateEditDto("Гречка", BigDecimal.valueOf(500_0000, 4));
        BigDecimal totalCost = productCreateEditDto1.getCost().add(productCreateEditDto2.getCost());
        ProductReadDto productReadDto1 = productService.save(productCreateEditDto1).get();
        ProductReadDto productReadDto2 = productService.save(productCreateEditDto2).get();

        // When
        PurchaseReadDto purchaseReadDto = purchaseService.createPurchaseCreditCard(
                totalCost, clientReadDto, Set.of(productReadDto1, productReadDto2), creditCardReadDto).get();

        // Then
        assertThat(purchaseReadDto.getId()).isPositive();

        assertThat(purchaseReadDto.getClient().getId()).isEqualTo(clientReadDto.getId());

        PaymentCreditCardReadDto payment = (PaymentCreditCardReadDto) purchaseReadDto.getPayment();
        assertThat(payment.getId()).isPositive();
        assertThat(payment.getAmount()).isEqualTo(totalCost);

        assertThat(purchaseReadDto.getProducts()).hasSize(2);

        Iterator<ProductReadDto> purchaseIterator = purchaseReadDto.getProducts().iterator();
        ProductReadDto product2 = purchaseIterator.next();
        ProductReadDto product1 = purchaseIterator.next();

        assertThat(product1.getId()).isEqualTo(productReadDto1.getId());
        assertThat(product1.getName()).isEqualTo(productReadDto1.getName());
        assertThat(product1.getCost()).isEqualTo(productReadDto1.getCost());
        assertThat(product2.getId()).isEqualTo(productReadDto2.getId());
        assertThat(product2.getName()).isEqualTo(productReadDto2.getName());
        assertThat(product2.getCost()).isEqualTo(productReadDto2.getCost());

        // Проверка баланса карты после совершения покупки c извлечением данных через DTO
        assertThat(
                payment.getCreditCard().getBalance()
        ).isEqualTo(
                initialBalance.subtract(totalCost)
        );

        // Проверка баланса карты после совершения покупки c извлечением данных напрямую из БД.
        entityManager.clear();
        assertThat(
                creditCardService.findById(creditCardReadDto.getId()).get().getBalance()
        ).isEqualTo(
                initialBalance.subtract(totalCost)
        );
    }

    @Test
    void createPurchaseCreditCard_CardIsBlocked_ThrowAttemptToChargeMoneyFromBlockedCreditCardException() {
        // Клиент и его кредитная карта берутся из @Sql
        ClientReadDto clientReadDto = userService.findClientByEmail(TestUtils.SECOND_CLIENT_EMAIL).get();

        // фильтрация заблокированной карты
        CreditCardDto.Read.Public creditCardReadDto = creditCardService.findAllByClientId(clientReadDto.getId()).stream()
                .filter(dto -> dto.getStatus() == CreditCardStatus.BLOCKED)
                .findFirst()
                .get();

        BigDecimal initialBalance = creditCardReadDto.getBalance();

        ProductCreateEditDto productCreateEditDto1 =
                new ProductCreateEditDto("Овсянка", BigDecimal.valueOf(1000_0000, 4));
        ProductCreateEditDto productCreateEditDto2 =
                new ProductCreateEditDto("Гречка", BigDecimal.valueOf(500_0000, 4));
        BigDecimal totalCost = productCreateEditDto1.getCost().add(productCreateEditDto2.getCost());
        ProductReadDto productReadDto1 = productService.save(productCreateEditDto1).get();
        ProductReadDto productReadDto2 = productService.save(productCreateEditDto2).get();

        // when
        assertThatThrownBy(() -> {
            purchaseService.createPurchaseCreditCard(
                    totalCost, clientReadDto, Set.of(productReadDto1, productReadDto2), creditCardReadDto).get();

        // then
        }).isInstanceOf(AttemptToChargeMoneyFromBlockedCreditCardException.class)
                .hasMessageContaining("There was an attempt to charge money from blocked credit card id = %d"
                        .formatted(creditCardReadDto.getId()));
    }

    @Test
    void createPurchaseAccount_NormalFlow() {
        // Клиент и его счет берутся из @Sql
        ClientReadDto clientReadDto = userService.findClientByEmail(TestUtils.FIRST_CLIENT_EMAIL).get();

        ProductCreateEditDto productCreateEditDto1 =
                new ProductCreateEditDto("Овсянка", BigDecimal.valueOf(1000_0000, 4));
        ProductCreateEditDto productCreateEditDto2 =
                new ProductCreateEditDto("Гречка", BigDecimal.valueOf(500_0000, 4));
        BigDecimal totalCost = productCreateEditDto1.getCost().add(productCreateEditDto2.getCost());
        ProductReadDto productReadDto1 = productService.save(productCreateEditDto1).get();
        ProductReadDto productReadDto2 = productService.save(productCreateEditDto2).get();

        AccountDto.Read.Public accountReadDto = accountService.findAllByClientId(clientReadDto.getId()).stream()
                .filter(accountReadDto1 -> accountReadDto1.getBalance().compareTo(totalCost) >= 0)
                .findFirst()
                .get();
        BigDecimal initialBalance = accountReadDto.getBalance();

        // When
        PurchaseReadDto purchaseReadDto = purchaseService.createPurchaseAccount(
                totalCost, clientReadDto, Set.of(productReadDto1, productReadDto2), accountReadDto).get();

        // Then
        assertThat(purchaseReadDto.getId()).isPositive();

        assertThat(purchaseReadDto.getClient().getId()).isEqualTo(clientReadDto.getId());

        PaymentAccountReadDto payment = (PaymentAccountReadDto) purchaseReadDto.getPayment();
        assertThat(payment.getId()).isPositive();
        assertThat(payment.getAmount()).isEqualTo(totalCost);

        assertThat(purchaseReadDto.getProducts()).hasSize(2);
        Iterator<ProductReadDto> purchaseIterator = purchaseReadDto.getProducts().iterator();
        ProductReadDto product2 = purchaseIterator.next();
        ProductReadDto product1 = purchaseIterator.next();
        assertThat(product1.getId()).isEqualTo(productReadDto1.getId());
        assertThat(product1.getName()).isEqualTo(productReadDto1.getName());
        assertThat(product1.getCost()).isEqualTo(productReadDto1.getCost());
        assertThat(product2.getId()).isEqualTo(productReadDto2.getId());
        assertThat(product2.getName()).isEqualTo(productReadDto2.getName());
        assertThat(product2.getCost()).isEqualTo(productReadDto2.getCost());

        // Проверка баланса счета после совершения покупки c извлечением данных через DTO
        assertThat(
                payment.getAccount().getBalance()
        ).isEqualTo(
                initialBalance.subtract(totalCost)
        );

        // Проверка баланса карты после совершения покупки c извлечением данных напрямую из БД.
        entityManager.clear();
        assertThat(
                accountService.findById(accountReadDto.getId()).get().getBalance()
        ).isEqualTo(
                initialBalance.subtract(totalCost)
        );
    }

    @Test
    void createPurchaseAccount_ThrowAccountInsufficientFundsException() {
        // Клиент и его счет берутся из @Sql
        ClientReadDto clientReadDto = userService.findClientByEmail(TestUtils.FIRST_CLIENT_EMAIL).get();

        ProductCreateEditDto productCreateEditDto1 =
                new ProductCreateEditDto("Овсянка", BigDecimal.valueOf(1000_0000, 4));
        ProductCreateEditDto productCreateEditDto2 =
                new ProductCreateEditDto("Гречка", BigDecimal.valueOf(500_0000, 4));
        BigDecimal totalCost = productCreateEditDto1.getCost().add(productCreateEditDto2.getCost());
        ProductReadDto productReadDto1 = productService.save(productCreateEditDto1).get();
        ProductReadDto productReadDto2 = productService.save(productCreateEditDto2).get();

        AccountDto.Read.Public accountReadDto = accountService.findAllByClientId(clientReadDto.getId()).stream()
                .filter(accountReadDto1 -> accountReadDto1.getBalance().compareTo(totalCost) < 0)
                .findFirst()
                .get();

        // When Then
        assertThatThrownBy(() -> {
            purchaseService.createPurchaseAccount(
                    totalCost, clientReadDto, Set.of(productReadDto1, productReadDto2), accountReadDto).get();
        }).isInstanceOf(AccountInsufficientFundsException.class)
                .hasMessageContaining("Insufficient funds in account");
    }
}