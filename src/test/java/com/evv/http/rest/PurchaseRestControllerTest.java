package com.evv.http.rest;

import com.evv.database.entity.ClientStatus;
import com.evv.database.entity.CreditCardStatus;
import com.evv.database.entity.Gender;
import com.evv.database.entity.Role;
import com.evv.dto.*;
import com.evv.exception.PurchaseForClientCantBeSavedException;
import com.evv.exception.PurchaseNotFoundException;
import com.evv.http.rest.payload.PurchaseCreatePayload;
import com.evv.service.PurchaseService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.DirectFieldBindingResult;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseRestControllerTest {

    @Mock
    PurchaseService purchaseService;

    @InjectMocks
    PurchaseRestController purchaseRestController;

    // GIVEN для всех тестов
    private static final Long CLIENT_ID = 111L;

    private static final Long ADMIN_ID = 112L;

    private static final Long CREDIT_CARD_ID = 222L;

    private static final Long ACCOUNT_ID = 333L;

    private static final Long PAYMENT_CREDIT_CARD_ID = 3332L;

    private static final Long PAYMENT_ACCOUNT_ID = 30332L;

    private static final String CLIENT_EMAIL = "some_client@test.com";

    private static final String ADMIN_EMAIL = "some_admin@test.com";

    private final ClientReadDto client = new ClientReadDto(CLIENT_ID, CLIENT_EMAIL, Role.CLIENT,
            LocalDate.of(2000, 5, 5), ClientStatus.ACTIVE, null, Gender.MALE);

    private final ClientReadDto otherClient = new ClientReadDto(CLIENT_ID + 1, "some_other_client@test.com", Role.CLIENT,
            LocalDate.of(2001, 9, 9), ClientStatus.ACTIVE, null, Gender.FEMALE);

    private final CustomUserDetails clientDetails = new CustomUserDetailsImpl(client.getEmail(),
            "client_encoded_pas", true, true, true, true,
            Collections.singleton(client.getRole()), client);

    private final AdminReadDto admin = new AdminReadDto(ADMIN_ID, ADMIN_EMAIL, Role.ADMIN, 999);

    private final CustomUserDetails adminDetails = new CustomUserDetailsImpl(admin.getEmail(),
            "admin_encoded_pas", true, true, true, true,
            Collections.singleton(admin.getRole()), admin);

    private final CreditCardDto.Read.Public creditCard = new CreditCardDto.Read.Public(CREDIT_CARD_ID,
            LocalDate.of(2026, 5, 3),
            BigDecimal.valueOf(-123_123_0500, 4),
            BigDecimal.valueOf(150_000_0500, 4),
            CreditCardStatus.ACTIVE, CLIENT_ID);

    private final AccountDto.Read.Public account = new AccountDto.Read.Public(ACCOUNT_ID, BigDecimal.valueOf(35_000_0000, 4), CLIENT_ID);

    private final BigDecimal paymentAmount1 = BigDecimal.valueOf(5_000_0000, 4);
    private final BigDecimal paymentAmount2 = BigDecimal.valueOf(25_000_0000, 4);
    private final BigDecimal paymentAmount3 = BigDecimal.valueOf(15_000_0000, 4);

    private final PaymentCreditCardReadDto paymentCreditCard = new PaymentCreditCardReadDto(PAYMENT_CREDIT_CARD_ID,
            paymentAmount1, creditCard);

    private final PaymentAccountReadDto paymentAccount = new PaymentAccountReadDto(PAYMENT_ACCOUNT_ID,
            paymentAmount2, account);

    private final PaymentAccountReadDto otherPaymentAccount = new PaymentAccountReadDto(PAYMENT_ACCOUNT_ID + 1,
            paymentAmount3, account);

    private final ProductReadDto product1 = new ProductReadDto(1L, "Овсянка", BigDecimal.valueOf(1000_0000, 4));
    private final ProductReadDto product2 = new ProductReadDto(2L, "Гречка", BigDecimal.valueOf(500_0000, 4));
    private final ProductReadDto product3 = new ProductReadDto(3L, "Вода", BigDecimal.valueOf(10500_0000, 4));

    private final PurchaseReadDto purchase1 = new PurchaseReadDto(1L, client, Set.of(product1, product2), paymentCreditCard);
    private final PurchaseReadDto purchase2 = new PurchaseReadDto(2L, client, Set.of(product2, product3), paymentAccount);
    private final PurchaseReadDto purchase3 = new PurchaseReadDto(3L, otherClient, Set.of(product1, product3), otherPaymentAccount);

    private final Pageable normPageable = PageRequest.of(0, 10);

    @Test
    void findAll_NormalFlow_ReturnPageResponse() {
        // stub
        Page<PurchaseReadDto> pageOf2Product =
                new PageImpl<>(List.of(purchase1, purchase2));
        doReturn(pageOf2Product).when(purchaseService).findAllByClientId(CLIENT_ID, normPageable);

        // when
        PageResponse<PurchaseReadDto> actualResult =
                purchaseRestController.findAll(null, normPageable, clientDetails);

        //then
        assertThat(actualResult.getContent()).hasSize(2);
        assertThat(actualResult.getContent().contains(purchase1)).isTrue();
        assertThat(actualResult.getContent().contains(purchase2)).isTrue();
        verify(purchaseService).findAllByClientId(CLIENT_ID, normPageable);
        verifyNoMoreInteractions(purchaseService);
    }

    @Test
    void findAll_ClientAskedForOtherClient_ReturnAccessDeniedException() {
        // when //then
        assertThatThrownBy(() -> {
            purchaseRestController.findAll(CLIENT_ID + 1, normPageable, clientDetails);
        }).isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Client id=%d attempted to get purchases data of client id=%d"
                        .formatted(CLIENT_ID, CLIENT_ID + 1));
    }

    @Test
    void findById_NormalFlow() {
        //stub
        doReturn(Optional.of(purchase1)).when(purchaseService).findById(purchase1.getId());

        //when
        PurchaseReadDto actualResult = purchaseRestController.findById(purchase1.getId());

        //then
        assertThat(actualResult).isEqualTo(purchase1);
        verify(purchaseService).findById(purchase1.getId());
        verifyNoMoreInteractions(purchaseService);
    }

    @Test
    void findById_AskedForNotExistedPurchase_ThrowPurchaseNotFoundException() {
        //stub
        Long notExistedPurchaseId = 1L;
        doReturn(Optional.empty()).when(purchaseService).findById(notExistedPurchaseId);

        //when  //then
        assertThatThrownBy(() -> {
            purchaseRestController.findById(notExistedPurchaseId);
        }).isInstanceOf(PurchaseNotFoundException.class)
                .hasMessageContaining("Purchase with id = %d not found for current user".formatted(notExistedPurchaseId));

        verify(purchaseService).findById(notExistedPurchaseId);
        verifyNoMoreInteractions(purchaseService);
    }

    @Test
    void create_PaymentByCreditCard_NormalFlow() throws BindException {
        // given
        PurchaseCreatePayload payload =
                new PurchaseCreatePayload(paymentAmount1, Set.of(1L, 2L), CREDIT_CARD_ID, null);
        // stub
        doReturn(Optional.of(purchase1)).when(purchaseService).createPurchaseCreditCardByIds(paymentAmount1, CLIENT_ID,
                Set.of(1L, 2L), CREDIT_CARD_ID);
        // when
        ResponseEntity<?> actualResult = purchaseRestController.create(payload, new DirectFieldBindingResult(null, ""),
                UriComponentsBuilder.newInstance(), clientDetails);
        // then
        assertThat(actualResult.getBody()).isEqualTo(purchase1);
        assertThat(actualResult.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(actualResult.getHeaders().size()).isEqualTo(1);
        assertThat(actualResult.getHeaders().getFirst("Location")).isEqualTo("/api/v1/purchase/1");

        verify(purchaseService).createPurchaseCreditCardByIds(paymentAmount1, CLIENT_ID,
                Set.of(1L, 2L), CREDIT_CARD_ID);
        verifyNoMoreInteractions(purchaseService);
    }

    @Test
    void create_PaymentByCreditCardAndPurchaseServiceReturnEmptyOptional_ThrowPurchaseForClientCantBeSavedException() {
        // given
        PurchaseCreatePayload payload =
                new PurchaseCreatePayload(paymentAmount1, Set.of(1L, 2L), CREDIT_CARD_ID, null);
        // stub
        doReturn(Optional.empty()).when(purchaseService).createPurchaseCreditCardByIds(paymentAmount1, CLIENT_ID,
                Set.of(1L, 2L), CREDIT_CARD_ID);

        // when //then
        assertThatThrownBy(() -> {
            purchaseRestController.create(payload, new DirectFieldBindingResult(null, ""),
                    UriComponentsBuilder.newInstance(), clientDetails);
        }).isInstanceOf(PurchaseForClientCantBeSavedException.class)
                .hasMessageContaining("Purchase for client id = %d can't be saved".formatted(CLIENT_ID));

        verify(purchaseService).createPurchaseCreditCardByIds(paymentAmount1, CLIENT_ID,
                Set.of(1L, 2L), CREDIT_CARD_ID);
        verifyNoMoreInteractions(purchaseService);
    }

    @Test
    void create_PaymentByAccount_NormalFlow() throws BindException {
        // given
        PurchaseCreatePayload payload =
                new PurchaseCreatePayload(paymentAmount2, Set.of(2L, 3L), null, ACCOUNT_ID);
        // stub
        doReturn(Optional.of(purchase2)).when(purchaseService).createPurchaseAccountByIds(paymentAmount2, CLIENT_ID,
                Set.of(2L, 3L), ACCOUNT_ID);
        // when
        ResponseEntity<?> actualResult = purchaseRestController.create(payload, new DirectFieldBindingResult(null, ""),
                UriComponentsBuilder.newInstance(), clientDetails);
        // then
        assertThat(actualResult.getBody()).isEqualTo(purchase2);
        assertThat(actualResult.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(actualResult.getHeaders().size()).isEqualTo(1);
        assertThat(actualResult.getHeaders().getFirst("Location")).isEqualTo("/api/v1/purchase/2");

        verify(purchaseService).createPurchaseAccountByIds(paymentAmount2, CLIENT_ID,
                Set.of(2L, 3L), ACCOUNT_ID);
        verifyNoMoreInteractions(purchaseService);
    }


    @Test
    void create_PaymentByAccountAndPurchaseServiceReturnEmptyOptional_ThrowPurchaseForClientCantBeSavedException() {
        // given
        PurchaseCreatePayload payload =
                new PurchaseCreatePayload(paymentAmount2, Set.of(2L, 3L), null, ACCOUNT_ID);
        // stub
        doReturn(Optional.empty()).when(purchaseService).createPurchaseAccountByIds(paymentAmount2, CLIENT_ID,
                Set.of(2L, 3L), ACCOUNT_ID);

        // when //then
        assertThatThrownBy(() -> {
            purchaseRestController.create(payload, new DirectFieldBindingResult(null, ""),
                    UriComponentsBuilder.newInstance(), clientDetails);
        }).isInstanceOf(PurchaseForClientCantBeSavedException.class)
                .hasMessageContaining("Purchase for client id = %d can't be saved".formatted(CLIENT_ID));

        verify(purchaseService).createPurchaseAccountByIds(paymentAmount2, CLIENT_ID,
                Set.of(2L, 3L), ACCOUNT_ID);
        verifyNoMoreInteractions(purchaseService);
    }

}