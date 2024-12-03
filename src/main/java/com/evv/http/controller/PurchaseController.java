package com.evv.http.controller;

import com.evv.database.entity.CreditCardStatus;
import com.evv.dto.*;
import com.evv.dto.validation.PaymentChoiceHolder;
import com.evv.exception.FaultChoosingPaymentMethodException;
import com.evv.exception.PurchaseForClientCantBeSavedException;
import com.evv.service.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.*;

@Controller
@RequestMapping("/purchase")
@SessionAttributes({"products", "chosenProducts", "totalCost", "creditCards",
        "accounts", "chosenCreditCard", "chosenAccount"})
@RequiredArgsConstructor
public class PurchaseController {

    private final ProductService productService;

    private final CreditCardService creditCardService;

    private final AccountService accountService;

    private final PurchaseService purchaseService;

    private final MessageSource messageSource;

    @GetMapping("/products")
    public String productChoice(Model model, ProductFilter filter,
                                @PageableDefault(size = 12) Pageable pageable,
                                Locale locale) {

        Page<ProductReadDto> productPage = productService.findAll(filter, pageable);

        if (productPage.getTotalElements() == 0) {
            String message = messageSource.getMessage("object_error.too_narrow-filter", null, locale);
            model.addAttribute("errors",
                    new ObjectError("TooNarrowFilter", message));
        }

        model.addAttribute("products", PageResponse.of(productPage));
        model.addAttribute("filter", filter);

        ProductIdsHolder productIdsHolder = new ProductIdsHolder(new ArrayList<>());
        model.addAttribute("productIdsHolder", productIdsHolder); // Надо положить этот пустой объект в модель и потом в Thymeleaf исп его в th:object, тогда в @PostMapping("/process") попадет готовая ProductIdsHolder из формы.

        return "purchase/product_choice";
    }

    @PostMapping("/payment")
    public String processProductChoice(Model model,
                                       @Validated ProductIdsHolder chosenProductIdsHolder,
                                       BindingResult bindingResult, RedirectAttributes redirectAttributes,
                                       @SessionAttribute("products") PageResponse<ProductReadDto> products,
                                       @AuthenticationPrincipal CustomUserDetails client
    ) {

        if (bindingResult.hasErrors()) {
            redirectAttributes
                    .addFlashAttribute("errors", bindingResult.getAllErrors());
            return "redirect:/purchase/products";
        }

        List<Long> chosenProductIds = chosenProductIdsHolder.getProductIds();
        List<ProductReadDto> chosenProducts = products.getContent().stream()
                .filter(productReadDto -> chosenProductIds.contains(productReadDto.getId()))
                .toList();
        BigDecimal totalCost = chosenProducts.stream()
                .map(ProductReadDto::getCost)
                .reduce(BigDecimal::add)
                .get();

        List<CreditCardDto.Read.Public> creditCards = creditCardService.findAllByClientId(client.getClient().getId());
        List<AccountDto.Read.Public> accounts = accountService.findAllByClientId(client.getClient().getId());

        model.addAttribute("chosenProducts", chosenProducts);
        model.addAttribute("totalCost", totalCost);
        model.addAttribute("creditCards", creditCards);
        model.addAttribute("accounts", accounts);
        model.addAttribute("blockedCreditCardStatus", CreditCardStatus.BLOCKED);
        model.addAttribute("paymentChoiceHolder", new PaymentChoiceHolder(null, null));

        return "purchase/payment_choice";
    }

    @PostMapping("/confirm-credit-card")
    public String processCreditCardChoice(Model model,
                                          @Validated PaymentChoiceHolder paymentChoiceHolder,
                                          BindingResult bindingResult, RedirectAttributes redirectAttributes,
                                          @SessionAttribute("creditCards") List<CreditCardDto.Read.Public> creditCards,
                                          @AuthenticationPrincipal CustomUserDetails client
    ) {

        if (bindingResult.hasErrors()) {
            redirectAttributes
                    .addFlashAttribute("errors", bindingResult.getAllErrors());
            return "redirect:/purchase/payment";
        }

        model.addAttribute("chosenAccount", null);
        CreditCardDto.Read.Public chosenCreditCard = creditCards.stream()
                .filter(creditCardReadDto ->
                        Objects.equals(creditCardReadDto.getId(), paymentChoiceHolder.getCreditCardId()))
                .findFirst()
                .orElseThrow(() -> new FaultChoosingPaymentMethodException(client.getClient().getId()));

        model.addAttribute("chosenCreditCard", chosenCreditCard);

        return "purchase/confirm_purchase";
    }

    @PostMapping("/confirm-account")
    public String processAccountChoice(Model model,
                                       @Validated PaymentChoiceHolder paymentChoiceHolder,
                                       BindingResult bindingResult, RedirectAttributes redirectAttributes,
                                       @SessionAttribute("accounts") List<AccountDto.Read.Public> accounts,
                                       @AuthenticationPrincipal CustomUserDetails client
    ) {

        if (bindingResult.hasErrors()) {
            redirectAttributes
                    .addFlashAttribute("errors", bindingResult.getAllErrors());
            return "redirect:/purchase/payment";
        }

        model.addAttribute("chosenCreditCard", null);
        AccountDto.Read.Public chosenAccount = accounts.stream()
                .filter(accountReadDto ->
                        Objects.equals(accountReadDto.getId(), paymentChoiceHolder.getAccountId()))
                .findFirst()
                .orElseThrow(() -> new FaultChoosingPaymentMethodException(client.getClient().getId()));

        model.addAttribute("chosenAccount", chosenAccount);

        return "purchase/confirm_purchase";
    }

    @PostMapping("/complete")
    public String processPurchaseComplete(Model model,
                                          @SessionAttribute("totalCost") BigDecimal totalCost,
                                          @SessionAttribute("chosenProducts") Set<ProductReadDto> chosenProducts,
                                          @AuthenticationPrincipal CustomUserDetails client,
                                          SessionStatus sessionStatus) {

        Object maybeChosenCreditCard = model.getAttribute("chosenCreditCard");
        CreditCardDto.Read.Public chosenCreditCard = maybeChosenCreditCard != null ?
                (CreditCardDto.Read.Public) maybeChosenCreditCard : null;
        Object maybeChosenAccount = model.getAttribute("chosenAccount");
        AccountDto.Read.Public chosenAccount = maybeChosenAccount != null ?
                (AccountDto.Read.Public) maybeChosenAccount : null;

        if (chosenCreditCard != null) {
            PurchaseReadDto prd = purchaseService.createPurchaseCreditCard(totalCost, client.getClient(), chosenProducts, chosenCreditCard)
                    .orElseThrow(() -> new PurchaseForClientCantBeSavedException(client.getClient().getId()));
            model.addAttribute("purchaseReadDto", prd);
            model.addAttribute("chosenCreditCard", ((PaymentCreditCardReadDto) prd.getPayment()).getCreditCard());
        } else if (chosenAccount != null) {
            PurchaseReadDto prd = purchaseService.createPurchaseAccount(totalCost, client.getClient(), chosenProducts, chosenAccount)
                    .orElseThrow(() -> new PurchaseForClientCantBeSavedException(client.getClient().getId()));
            model.addAttribute("purchaseReadDto", prd);
            model.addAttribute("chosenAccount", ((PaymentAccountReadDto) prd.getPayment()).getAccount());
        } else {
            throw new FaultChoosingPaymentMethodException(client.getClient().getId());
        }

        sessionStatus.setComplete();
        return "purchase/purchase_complete";
    }

    @Value
    public static class ProductIdsHolder {
        @NotEmpty(message = "{validation.NotEmpty.ProductIdsHolder}")
        List<Long> productIds;
    }

//    @Value
//    @PaymentIdPresent
//    public static class PaymentChoiceHolder {
//        Long creditCardId;
//        Long accountId;
//    }
}

