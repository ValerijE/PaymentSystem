package com.evv.http.controller;

import com.evv.NonTransactionalIT;
import com.evv.database.entity.CreditCardStatus;
import com.evv.dto.AccountDto;
import com.evv.dto.CreditCardDto;
import com.evv.dto.PageResponse;
import com.evv.dto.ProductReadDto;
import com.evv.exception.FaultChoosingPaymentMethodException;
import com.evv.service.AccountService;
import com.evv.service.CreditCardService;
import com.evv.dto.CustomUserDetails;
import com.evv.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.List;

import static com.evv.TestUtils.FIRST_CLIENT_EMAIL;
import static com.evv.TestUtils.MESSAGE_PRODUCTS_NOT_EMPTY;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@RequiredArgsConstructor
class PurchaseControllerIT extends NonTransactionalIT {

    private final MockMvc mockMvc;

    private final ProductService productService;

    private final CreditCardService creditCardService;

    private final AccountService accountService;

    private static final List<Long> someProductIds = List.of(4L, 7L, 11L);

    @Test
    @WithMockUser(authorities = "CLIENT")
    void productChoice_NormalFlow() throws Exception {
        mockMvc.perform(get("/purchase/products"))
                .andExpectAll(
                        status().is2xxSuccessful(),
                        view().name("purchase/product_choice"),
                        model().attributeExists("products"),
                        model().attribute("products", hasProperty("content", hasSize(12))), // Дефолтная величина установленная над параметром в @PageableDefault(size = 12)
                        model().attributeExists("filter"),
                        model().attributeExists("productIdsHolder")
                );
    }

    @Test
    @WithMockUser(authorities = "CLIENT")
    void productChoice_FilterTooNarrow_shouldContainErrors() throws Exception {
        mockMvc.perform(get("/purchase/products")
                        .param("minCost", "1000000")) // Очень большая минимальная стоимость, так, что после фильтра список продуктов будет пустой.
                .andExpectAll(
                        status().is2xxSuccessful(),
                        view().name("purchase/product_choice"),
                        model().attributeExists("errors"),
                        model().attribute("errors", hasProperty("defaultMessage", is("Change the filtering options to expand the filter"))),
                        model().attributeExists("products"),
                        model().attribute("products", hasProperty("content", hasSize(0))),
                        model().attributeExists("filter"),
                        model().attributeExists("productIdsHolder")
                );
    }

    @Test
    @WithUserDetails(value = FIRST_CLIENT_EMAIL)
    void processProductChoice_NormalFlow() throws Exception {
        mockMvc.perform(post("/purchase/payment")
                        .with(csrf())
                        .sessionAttr("products", products())
                        .param("productIds",
                                String.valueOf(someProductIds.get(0)),
                                String.valueOf(someProductIds.get(1)),
                                String.valueOf(someProductIds.get(2))))
                .andExpectAll(
                        status().is2xxSuccessful(),
                        view().name("purchase/payment_choice"),
                        model().attributeExists("chosenProducts"),
                        model().attribute("chosenProducts", hasSize(3)),
                        model().attributeExists("totalCost"),
                        model().attribute("totalCost", greaterThan(BigDecimal.ZERO)),
                        model().attributeExists("creditCards"),
                        model().attribute("creditCards", hasSize(2)),
                        model().attributeExists("accounts"),
                        model().attribute("accounts", hasSize(3)),
                        model().attributeExists("blockedCreditCardStatus"),
                        model().attribute("blockedCreditCardStatus", is(CreditCardStatus.BLOCKED)),
                        model().attributeExists("paymentChoiceHolder")
                );
    }

    @Test
    @WithUserDetails(value = FIRST_CLIENT_EMAIL)
    void processProductChoice_ProductListEmpty_shouldContainErrors() throws Exception {
        mockMvc.perform(post("/purchase/payment")
                        .with(csrf())
                        .sessionAttr("products", products()))
                // В этой строке намерено отсутствует метод .param("productIds"..), а значит не выбран ни один товар.
                .andExpectAll(
                        status().is3xxRedirection(),
                        redirectedUrl("/purchase/products"),
                        flash().attributeExists("errors"),
                        flash().attribute("errors", hasItem(hasProperty("defaultMessage", is(MESSAGE_PRODUCTS_NOT_EMPTY)))),
                        model().attributeDoesNotExist("chosenProducts"),
                        model().attributeDoesNotExist("totalCost"),
                        model().attributeDoesNotExist("creditCards"),
                        model().attributeDoesNotExist("accounts"),
                        model().attributeDoesNotExist("blockedCreditCardStatus"),
                        model().attributeDoesNotExist("paymentChoiceHolder")
                );
    }

    @Test
    @WithUserDetails(value = FIRST_CLIENT_EMAIL)
    void processCreditCardChoice_NormalFlow() throws Exception {
        CustomUserDetails client = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        mockMvc.perform(post("/purchase/confirm-credit-card")
                        .with(csrf())
                        .sessionAttr("creditCards", creditCards(client))
                        .param("creditCardId", String.valueOf(chosenCreditCard(client).getId())))
                .andExpectAll(
                        status().is2xxSuccessful(),
                        view().name("purchase/confirm_purchase"),
                        model().attributeExists("chosenCreditCard"),
                        model().attribute("chosenCreditCard", hasProperty("id", is(chosenCreditCard(client).getId()))),
                        model().attributeDoesNotExist("chosenAccount")
                );
    }

    @Test
    @WithUserDetails(value = FIRST_CLIENT_EMAIL)
    void processCreditCardChoice_PaymentDidNotChoose_shouldContainErrors() throws Exception {
        CustomUserDetails client = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        mockMvc.perform(post("/purchase/confirm-credit-card")
                        .with(csrf())
                        .sessionAttr("creditCards", creditCards(client)))
                // В этой строке намерено отсутствует метод .param("creditCardId"..), а значит не выбрана ни одна карта.
                .andExpectAll(
                        status().is3xxRedirection(),
                        redirectedUrl("/purchase/payment"),
                        flash().attributeExists("errors"),
                        flash().attribute("errors", hasItem(hasProperty("defaultMessage",
                                is("One of two reasons: 1. Account or Credit card must be selected, 2. Account and Credit card both selected")))),
                        model().attributeDoesNotExist("chosenCreditCard")
                );
    }

    @Test
    @WithUserDetails(value = FIRST_CLIENT_EMAIL)
    void processCreditCardChoice_BothPaymentsChose_shouldContainErrors() throws Exception {
        CustomUserDetails client = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        mockMvc.perform(post("/purchase/confirm-credit-card")
                        .with(csrf())
                        .sessionAttr("creditCards", creditCards(client))
                        // В следующих строках выбираются и счет и кредитная карта, а такого быть не должно:
                        .param("accountId", chosenAccount(client).getId().toString())
                        .param("creditCardId", chosenCreditCard(client).getId().toString()))
                .andExpectAll(
                        status().is3xxRedirection(),
                        redirectedUrl("/purchase/payment"),
                        flash().attributeExists("errors"),
                        flash().attribute("errors", hasItem(hasProperty("defaultMessage",
                                is("One of two reasons: 1. Account or Credit card must be selected, 2. Account and Credit card both selected")))),
                        model().attributeDoesNotExist("chosenCreditCard")
                );
    }

    @Test
    @WithUserDetails(value = FIRST_CLIENT_EMAIL)
    void processCreditCardChoice_CreditCardNotFound_ThrowFaultChoosingPaymentMethodException() throws Exception {
        CustomUserDetails client = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        MvcResult mvcResult = mockMvc.perform(post("/purchase/confirm-credit-card")
                        .with(csrf())
                        .sessionAttr("creditCards", creditCards(client))
                        .param("creditCardId", "1000000")) // Несуществующий id кредитной карты
                .andExpectAll(
                        status().is2xxSuccessful(),
                        view().name("error/common_error_page"),
                        model().attributeExists("message"),
                        model().attribute("message",
                                is("Error choosing payment method for client id = %d".formatted(client.getClient().getId())))
                )
                .andReturn();
        assertInstanceOf(FaultChoosingPaymentMethodException.class, mvcResult.getResolvedException());
    }

    @Test
    @WithUserDetails(value = FIRST_CLIENT_EMAIL)
    void processAccountChoice_NormalFlow() throws Exception {
        CustomUserDetails client = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long chosenAccountId = chosenAccount(client).getId();
        mockMvc.perform(post("/purchase/confirm-account")
                        .with(csrf())
                        .sessionAttr("accounts", accounts(client))
                        .param("accountId", String.valueOf(chosenAccountId)))
                .andExpectAll(
                        status().is2xxSuccessful(),
                        view().name("purchase/confirm_purchase"),
                        model().attributeExists("chosenAccount"),
                        model().attribute("chosenAccount", hasProperty("id", is(chosenAccountId))),
                        model().attributeDoesNotExist("chosenCreditCard")
                );
    }

    @Test
    @WithUserDetails(value = FIRST_CLIENT_EMAIL)
    void processAccountChoice_PaymentDidNotChoose_shouldContainErrors() throws Exception {
        CustomUserDetails client = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        mockMvc.perform(post("/purchase/confirm-account")
                        .with(csrf())
                        .sessionAttr("accounts", accounts(client)))
                // В этой строке намерено отсутствует метод .param("accountId"..), а значит не выбран ни один счет.
                .andExpectAll(
                        status().is3xxRedirection(),
                        redirectedUrl("/purchase/payment"),
                        flash().attributeExists("errors"),
                        flash().attribute("errors", hasItem(hasProperty("defaultMessage",
                                is("One of two reasons: 1. Account or Credit card must be selected, 2. Account and Credit card both selected")))),
                        model().attributeDoesNotExist("chosenAccount")
                );
    }

    @Test
    @WithUserDetails(value = FIRST_CLIENT_EMAIL)
    void processAccountChoice_BothPaymentsChose_shouldContainErrors() throws Exception {
        CustomUserDetails client = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        mockMvc.perform(post("/purchase/confirm-account")
                .with(csrf())
                .sessionAttr("accounts", accounts(client))
                // В следующих строках выбираются и счет и кредитная карта, а такого быть не должно:
                .param("accountId", chosenAccount(client).getId().toString())
                .param("creditCardId", chosenCreditCard(client).getId().toString()))
                .andExpectAll(
                        status().is3xxRedirection(),
                        redirectedUrl("/purchase/payment"),
                        flash().attributeExists("errors"),
                        flash().attribute("errors", hasItem(hasProperty("defaultMessage",
                                is("One of two reasons: 1. Account or Credit card must be selected, 2. Account and Credit card both selected")))),
                        model().attributeDoesNotExist("chosenAccount")
                );
    }

    @Test
    @WithUserDetails(value = FIRST_CLIENT_EMAIL)
    void processAccountChoice_AccountNotFound_ThrowErrorChoosingPaymentMethodException() throws Exception {
        CustomUserDetails client = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        MvcResult mvcResult = mockMvc.perform(post("/purchase/confirm-account")
                        .with(csrf())
                        .sessionAttr("accounts", accounts(client))
                        .param("accountId", "1000000")) // Несуществующий id банковского счета
                .andExpectAll(
                        status().is2xxSuccessful(),
                        view().name("error/common_error_page"),
                        model().attributeExists("message"),
                        model().attribute("message",
                                is("Error choosing payment method for client id = %d".formatted(client.getClient().getId())))
                )
                .andReturn();
        assertInstanceOf(FaultChoosingPaymentMethodException.class, mvcResult.getResolvedException());
    }

    @Test
    @WithUserDetails(value = FIRST_CLIENT_EMAIL)
    void processPurchaseComplete_PaymentWasFromCreditCard_NormalFlow() throws Exception {
        CustomUserDetails client = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long chosenCreditCardId = chosenCreditCard(client).getId();
        mockMvc.perform(post("/purchase/complete")
                        .with(csrf())
                        .sessionAttr("chosenProducts", chosenProducts())
                        .sessionAttr("totalCost", totalCost())
                        .sessionAttr("chosenCreditCard", chosenCreditCard(client)))
                .andExpectAll(
                        status().is2xxSuccessful(),
                        view().name("purchase/purchase_complete"),
                        model().attributeExists("purchaseReadDto"),
                        model().attribute("purchaseReadDto",
                                hasProperty("id", greaterThan(0L))),
                        model().attributeExists("chosenCreditCard"),
                        model().attribute("chosenCreditCard",
                                hasProperty("id", is(chosenCreditCardId)))
                );
    }

    @Test
    @WithUserDetails(value = FIRST_CLIENT_EMAIL)
    void processPurchaseComplete_PaymentDidNotChoose_ThrowFaultChoosingPaymentMethodException() throws Exception {
        CustomUserDetails client = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        MvcResult mvcResult = mockMvc.perform(post("/purchase/complete")
                        .with(csrf())
                        .sessionAttr("chosenProducts", chosenProducts())
                        .sessionAttr("totalCost", totalCost()))
                // Тут должны были бы быть или chosenCreditCard или chosenAccount
                .andExpectAll(
                        status().is2xxSuccessful(),
                        view().name("error/common_error_page"),
                        model().attributeExists("message"),
                        model().attribute("message",
                                is("Error choosing payment method for client id = %d".formatted(client.getClient().getId())))
                )
                .andReturn();
        assertInstanceOf(FaultChoosingPaymentMethodException.class, mvcResult.getResolvedException());
    }

    @Test
    @WithUserDetails(value = FIRST_CLIENT_EMAIL)
    void processPurchaseComplete_PaymentWasFromFromAccount_NormalFlow() throws Exception {
        CustomUserDetails client = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long chosenAccountId = chosenAccount(client).getId();
        mockMvc.perform(post("/purchase/complete")
                        .with(csrf())
                        .sessionAttr("chosenProducts", chosenProducts())
                        .sessionAttr("totalCost", totalCost())
                        .sessionAttr("chosenAccount", chosenAccount(client)))
                .andExpectAll(
                        status().is2xxSuccessful(),
                        view().name("purchase/purchase_complete"),
                        model().attributeExists("purchaseReadDto"),
                        model().attribute("purchaseReadDto",
                                hasProperty("id", greaterThan(0L))),
                        model().attributeExists("chosenAccount"),
                        model().attribute("chosenAccount",
                                hasProperty("id", is(chosenAccountId)))
                );
    }

    private PageResponse<ProductReadDto> products() {
        return PageResponse.of(productService.findAll(PageRequest.of(0, 12)));
    }

    private List<CreditCardDto.Read.Public> creditCards(CustomUserDetails client) {
        return creditCardService.findAllByClientId(client.getClient().getId());
    }

    private CreditCardDto.Read.Public chosenCreditCard(CustomUserDetails client) {
        return creditCardService.findAllByClientId(client.getClient().getId()).stream()
                .filter(cc -> cc.getStatus() != CreditCardStatus.BLOCKED)
                .findFirst().get();
    }

    private List<AccountDto.Read.Public> accounts(CustomUserDetails client) {
        return accountService.findAllByClientId(client.getClient().getId());
    }

    private AccountDto.Read.Public chosenAccount(CustomUserDetails client) {
        return accountService.findAllByClientId(client.getClient().getId()).stream()
                .filter(account -> account.getBalance().compareTo(totalCost()) >= 0)
                .findFirst().get();
    }

    private List<ProductReadDto> chosenProducts() {
        List<ProductReadDto> allProducts = productService.findAll(PageRequest.of(0, 12)).toList();
        return allProducts.stream()
                .filter(product -> someProductIds.contains(product.getId()))
                .toList();
    }

    private BigDecimal totalCost() {
        return chosenProducts().stream()
                .map(ProductReadDto::getCost)
                .reduce(BigDecimal::add)
                .get();
    }
}