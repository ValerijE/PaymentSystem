package com.evv.mapper;

import com.evv.database.entity.*;
import com.evv.database.repository.PaymentRepository;
import com.evv.dto.*;
import com.evv.exception.PaymentNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseMapperTest {

    @Mock
    PaymentRepository paymentRepository;

    @Mock
    ProductMapperImpl productMapper;

    @Mock
    UserMapperImpl userMapper;

    @Mock
    PaymentMapperImpl paymentMapper;

    @InjectMocks
    PurchaseMapperImpl purchaseMapper;

    // GIVEN для всех тестов
    private static final Long CLIENT_ID = 111L;

    private static final String CLIENT_EMAIL = "some_client@test.com";

    private static final Long CREDIT_CARD_ID = 222L;

    private static final Long ACCOUNT_ID = 333L;

    private static final Long PAYMENT_CREDIT_CARD_ID = 3332L;

    private static final Long PAYMENT_ACCOUNT_ID = 30332L;

    private static final Long PURCHASE_ID = 7770L;

    private static final BigDecimal CREDIT_CARD_BALANCE = BigDecimal.valueOf(10_000_1230L, 4);

    private static final LocalDate EXPIRATION_DATE = LocalDate.of(2025, 5, 5);

    private static final BigDecimal CREDIT_LIMIT = BigDecimal.valueOf(50_000_0000, 4);

    private final Client client = new Client(CLIENT_ID, CLIENT_EMAIL, "{noop}000", Role.CLIENT,
            LocalDate.of(2000, 5, 5), ClientStatus.ACTIVE, null,
            Gender.MALE, new ArrayList<>(), new ArrayList<>());

    private final ClientReadDto clientReadDto = new ClientReadDto(CLIENT_ID, CLIENT_EMAIL, Role.CLIENT,
            LocalDate.of(2000, 5, 5), ClientStatus.ACTIVE, null, Gender.MALE);

    private final CreditCard creditCard = new CreditCard(
            CREDIT_CARD_ID,
            EXPIRATION_DATE,
            CREDIT_CARD_BALANCE,
            CREDIT_LIMIT,
            CreditCardStatus.ACTIVE, client, new ArrayList<>(), 0L);

    private final CreditCardDto.Read.Public creditCardReadDto = new CreditCardDto.Read.Public(
            CREDIT_CARD_ID,
            EXPIRATION_DATE,
            CREDIT_CARD_BALANCE,
            CREDIT_LIMIT,
            CreditCardStatus.ACTIVE, CLIENT_ID);

    private final CreditCardDto.CreateEdit.Public creditCardCreateEditDto = new CreditCardDto.CreateEdit.Public(
            EXPIRATION_DATE,
            CREDIT_CARD_BALANCE,
            CREDIT_LIMIT,
            CreditCardStatus.ACTIVE, CLIENT_ID);

    private final BigDecimal accountBalance = BigDecimal.valueOf(35_000_0000, 4);

    private final Account account = new Account(
            ACCOUNT_ID, accountBalance, client, new ArrayList<>(), 0L);

    private final AccountDto.Read.Public accountReadDto = new AccountDto.Read.Public(
            ACCOUNT_ID, accountBalance, CLIENT_ID);

    private final BigDecimal paymentAmount = BigDecimal.valueOf(5_000_0000, 4);

    private final PaymentCreditCardReadDto paymentCreditCardReadDto = new PaymentCreditCardReadDto(PAYMENT_CREDIT_CARD_ID,
            paymentAmount, creditCardReadDto);

    private final PaymentCreditCard paymentCreditCard = new PaymentCreditCard(
            PAYMENT_CREDIT_CARD_ID,
            paymentAmount,
            null,
            creditCard
    );

    private final PaymentAccount paymentAccount = new PaymentAccount(
            PAYMENT_ACCOUNT_ID,
            paymentAmount,
            null,
            account
    );

    private final ProductReadDto productReadDto1 = new ProductReadDto(1L, "Овсянка", BigDecimal.valueOf(1000_0000, 4));
    private final Product product1 = new Product(1L, "Овсянка", BigDecimal.valueOf(1000_0000, 4));
    private final ProductReadDto productReadDto2 = new ProductReadDto(2L, "Гречка", BigDecimal.valueOf(500_0000, 4));
    private final Product product2 = new Product(2L, "Гречка", BigDecimal.valueOf(500_0000, 4));
    private final ProductReadDto productReadDto3 = new ProductReadDto(3L, "Перловка", BigDecimal.valueOf(500_0000, 4));
    private final Product product3 = new Product(3L, "Перловка", BigDecimal.valueOf(750_0000, 4));

    private final Purchase oldPurchase =
            new Purchase(PURCHASE_ID, client, new HashSet<>(List.of(product1, product2)), paymentCreditCard);

    private final PurchaseCreateEditDto newPurchaseCreateEditDto =
            new PurchaseCreateEditDto(clientReadDto, new HashSet<>(List.of(productReadDto3)), PAYMENT_ACCOUNT_ID);

    @Test
    void purchaseUpdateFromPurchaseCreateEditDto_NormalFlow() {
        // stub
        doReturn(Optional.of(paymentAccount)).when(paymentRepository).findById(PAYMENT_ACCOUNT_ID);
        doReturn(product3).when(productMapper).productReadDtoToProduct(productReadDto3);

        // when
        Purchase newPurchase =
                purchaseMapper.purchaseUpdateFromPurchaseCreateEditDto(oldPurchase, newPurchaseCreateEditDto);

        // then
        assertThat(newPurchase.getId()).isEqualTo(PURCHASE_ID);
        assertThat(newPurchase.getClient()).isEqualTo(client);
        assertThat(newPurchase.getPayment()).isEqualTo(paymentAccount);
        assertThat(newPurchase.getProducts()).hasSize(1);
        assertThat(newPurchase.getProducts()).contains(product3);

        verify(paymentRepository).findById(PAYMENT_ACCOUNT_ID);
        verifyNoMoreInteractions(paymentRepository);
    }

    @Test
    void purchaseCreateEditDtoToPurchase_PaymentRepositoryReturnEmptyOptional_ThrowPaymentNotFoundException() {
        doReturn(Optional.empty()).when(paymentRepository).findById(PAYMENT_ACCOUNT_ID);
        doReturn(product3).when(productMapper).productReadDtoToProduct(productReadDto3);
        doReturn(client).when(userMapper).clientReadDtoToClient(clientReadDto);

        assertThatThrownBy(() ->
                purchaseMapper.purchaseCreateEditDtoToPurchase(newPurchaseCreateEditDto))
                .isInstanceOf(PaymentNotFoundException.class)
                .hasMessage("Payment with id = %d not found".formatted(newPurchaseCreateEditDto.getPaymentId()));

        verify(paymentRepository).findById(PAYMENT_ACCOUNT_ID);
        verifyNoMoreInteractions(paymentRepository);
        verify(productMapper).productReadDtoToProduct(productReadDto3);
        verifyNoMoreInteractions(productMapper);
        verify(userMapper).clientReadDtoToClient(clientReadDto);
        verifyNoMoreInteractions(userMapper);
    }

    @Test
    void purchaseToPurchaseReadDto_NormalFlow() {
        // stub
        doReturn(paymentCreditCardReadDto).when(paymentMapper).paymentCreditCardToPaymentCreditCardReadDto(paymentCreditCard);
        doReturn(productReadDto1).when(productMapper).productToProductReadDto(product1);
        doReturn(productReadDto2).when(productMapper).productToProductReadDto(product2);
        doReturn(clientReadDto).when(userMapper).clientToClientReadDto(client);

        // when
        PurchaseReadDto purchaseReadDto = purchaseMapper.purchaseToPurchaseReadDto(oldPurchase);

        // then
        assertThat(purchaseReadDto.getId()).isEqualTo(PURCHASE_ID);
        assertThat(purchaseReadDto.getClient()).isEqualTo(clientReadDto);
        assertThat(purchaseReadDto.getPayment()).isEqualTo(paymentCreditCardReadDto);
        assertThat(purchaseReadDto.getProducts()).hasSize(2);
        assertThat(purchaseReadDto.getProducts()).contains(productReadDto1);
        assertThat(purchaseReadDto.getProducts()).contains(productReadDto2);

        verify(userMapper).clientToClientReadDto(client);
        verifyNoMoreInteractions(userMapper);
        verify(productMapper).productToProductReadDto(product1);
        verify(productMapper).productToProductReadDto(product2);
        verifyNoMoreInteractions(userMapper);
    }

}