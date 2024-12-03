package com.evv.service;

import com.evv.database.entity.Payment;
import com.evv.database.entity.Purchase;
import com.evv.database.repository.PurchaseRepository;
import com.evv.dto.*;
import com.evv.exception.*;
import com.evv.mapper.PurchaseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PurchaseService {

    private final CreditCardService creditCardService;

    private final AccountService accountService;

    private final PurchaseRepository purchaseRepository;

    private final PaymentService paymentService;

    private final PurchaseMapper purchaseMapper;

    private final UserService userService;

    private final ProductService productService;

    /**
     * Метод для совершения покупки через контроллер, работающий с формами Thymeleaf.
     * Оплата покупки производится с кредитной карты.
     * Входными параметрами являются DTO сущностей.
     */
    @Transactional
    public Optional<PurchaseReadDto> createPurchaseCreditCard(BigDecimal totalCost,
                                                              ClientReadDto clientReadDto,
                                                              Set<ProductReadDto> chosenProducts,
                                                              CreditCardDto.Read.Public creditCardReadDto) {
        // Снятие денег с карты
        // Создание и сохранение платежа
        PaymentCreditCardCreateEditDto paymentCreditCardCreateEditDto =
                new PaymentCreditCardCreateEditDto(totalCost, creditCardReadDto);
        PaymentCreditCardReadDto paymentCreditCardReadDto =
                paymentService.createPaymentCreditCardWithBalanceReduce(paymentCreditCardCreateEditDto)
                        .orElseThrow(() -> new PaymentCreditCardCantBeSavedException(creditCardReadDto.getId()));

        // Сохранение покупки
        PurchaseCreateEditDto purchaseCreateEditDto =
                new PurchaseCreateEditDto(clientReadDto, chosenProducts, paymentCreditCardReadDto.getId());

        return Optional.of(purchaseCreateEditDto)
                .map(purchaseMapper::purchaseCreateEditDtoToPurchase)
                .map(purchaseRepository::saveAndFlush)
                .map(purchaseMapper::purchaseToPurchaseReadDto);
    }

    /**
     * Метод для совершения покупки через контроллер, работающий с формами Thymeleaf.
     * Оплата покупки производится со счета.
     * Входными параметрами являются DTO сущностей.
     */
    @Transactional
    public Optional<PurchaseReadDto> createPurchaseAccount(BigDecimal totalCost,
                                                           ClientReadDto clientReadDto,
                                                           Set<ProductReadDto> chosenProducts,
                                                           AccountDto.Read.Public accountReadDto) {
        // Снятие денег со счета
        // Создание и сохранение платежа
        PaymentAccountCreateEditDto paymentAccountCreateEditDto =
                new PaymentAccountCreateEditDto(totalCost, accountReadDto);
        PaymentAccountReadDto paymentAccountReadDto = paymentService
                .createPaymentAccountWithBalanceReduce(paymentAccountCreateEditDto)
                .orElseThrow(() -> new PaymentAccountCantBeSavedException(accountReadDto.getId()));

        // Сохранение покупки
        PurchaseCreateEditDto purchaseCreateEditDto =
                new PurchaseCreateEditDto(clientReadDto, chosenProducts, paymentAccountReadDto.getId());

        return Optional.of(purchaseCreateEditDto)
                .map(purchaseMapper::purchaseCreateEditDtoToPurchase)
                .map(purchaseRepository::saveAndFlush)
                .map(purchaseMapper::purchaseToPurchaseReadDto);
    }

    /**
     * Метод для совершения покупки через REST контроллер.
     * Оплата покупки производится с кредитной карты.
     * Через REST передаются только id сущностей.
     */
    @Transactional
    public Optional<PurchaseReadDto> createPurchaseCreditCardByIds(BigDecimal totalCost,
                                                                   Long clientId,
                                                                   Set<Long> chosenProductIds,
                                                                   Long creditCardId) {
        return createPurchaseCreditCard(
                totalCost,
                userService.findClientById(clientId).orElseThrow(() -> new ClientByIdNotFoundException(clientId)),
                chosenProductIds.stream()
                        .map(productId -> productService.findById(productId)
                                .orElseThrow(() -> new ProductByIdNotFoundException(productId)))
                        .collect(Collectors.toSet()),
                creditCardService.findById(creditCardId)
                        .orElseThrow(() -> new CreditCardNotFoundException(creditCardId))
        );
    }

    /**
     * Метод для совершения покупки через REST контроллер.
     * Оплата покупки производится со счета.
     * Через REST передаются только id сущностей.
     */
    @Transactional
    public Optional<PurchaseReadDto> createPurchaseAccountByIds(BigDecimal totalCost,
                                                                Long clientId,
                                                                Set<Long> chosenProductIds,
                                                                Long accountId) {
        return createPurchaseAccount(
                totalCost,
                userService.findClientById(clientId).orElseThrow(() -> new ClientByIdNotFoundException(clientId)),
                chosenProductIds.stream()
                        .map(productId -> productService.findById(productId)
                                .orElseThrow(() -> new ProductByIdNotFoundException(productId)))
                        .collect(Collectors.toSet()),
                accountService.findById(accountId)
                        .orElseThrow(() -> new AccountNotFoundException(accountId))
        );
    }

    public Optional<PurchaseReadDto> findById(Long id) {
        return purchaseRepository.findById(id)
                .map(purchaseMapper::purchaseToPurchaseReadDto);
    }

    public Page<PurchaseReadDto> findAllByClientId(Long clientId, Pageable pageable) {
        return purchaseRepository.findAllByClientId(clientId, pageable)
                .map(purchaseMapper::purchaseToPurchaseReadDto);
    }

    public Optional<PurchaseReadDto> findByIdAndClientId(Long id, Long clientId) {
        return purchaseRepository.findByIdAndClientId(id, clientId)
                .map(purchaseMapper::purchaseToPurchaseReadDto);
    }

    public Page<PurchaseReadDto> findAll(Pageable pageable) {
        return purchaseRepository.findAll(pageable)
                .map(purchaseMapper::purchaseToPurchaseReadDto);
    }

    @Transactional
    public void delete(Long purchaseId) {
        purchaseRepository.findById(purchaseId)
                .map(purchase -> {
                    // Сохранение ссылки на payment для последующего его удаления
                    Payment payment = purchase.getPayment();

                    // Возврат средств
                    paymentService.returnFunds(payment);

                    // Удаление покупки
                    purchaseRepository.delete(purchase);
                    purchaseRepository.flush();

                    // Удаление платежа
                    paymentService.deletePaymentWithoutFundsReturn(payment);

                    return purchase;
                })
                .orElseThrow(() -> new PurchaseNotFoundException(purchaseId));
    }

    /**
     * Обновляет покупку для тех случаев когда новый платеж производится с кредитной карты.
     * <p>Старый платеж удаляется. Денежная сумма возвращается на баланс кредитной карты или счета
     * с которых была произведена оплата.
     * <p>Новый платеж регистрируется с новым идентификатором.
     *
     */
    @Transactional
    public Optional<PurchaseReadDto> updatePurchaseCreditCardByIds(Long purchaseId, BigDecimal totalCost,
                                                                   Set<Long> newChosenProductIds, Long creditCardId) {

        return purchaseRepository.findById(purchaseId)
                .map(purchase -> {

                    // Сохранение ссылки на старый платеж для последующего его удаления
                    Payment oldPayment = purchase.getPayment();

                    // Возврат средств по старому платежу
                    paymentService.returnFunds(oldPayment);

                    // Создание нового платежа
                    var newCreditCardReadDto = creditCardService.findById(creditCardId)
                            .orElseThrow(() -> new CreditCardNotFoundException(creditCardId));
                    var newPaymentCreditCardCreateEditDto =
                            new PaymentCreditCardCreateEditDto(totalCost, newCreditCardReadDto);
                    var newPaymentCreditCardReadDto = paymentService
                            .createPaymentCreditCardWithBalanceReduce(newPaymentCreditCardCreateEditDto)
                            .orElseThrow(() -> new PaymentCreditCardCantBeSavedException(creditCardId));

                    // Список продуктов
                    Set<ProductReadDto> newChosenProducts = newChosenProductIds.stream()
                            .map(productId -> productService.findById(productId)
                                    .orElseThrow(() -> new ProductByIdNotFoundException(productId)))
                            .collect(Collectors.toSet());

                    // Обновление покупки
                    PurchaseCreateEditDto partiallyUpdatedCreateEditDto = // partially, т.к. изменение клиента не предусмотрено
                            new PurchaseCreateEditDto(null, newChosenProducts, newPaymentCreditCardReadDto.getId());
                    Purchase udatedPurchase = // замена полей products и payment в persistent entity purchase:
                            purchaseMapper.purchaseUpdateFromPurchaseCreateEditDto(purchase, partiallyUpdatedCreateEditDto);
                    purchaseRepository.flush();

                    // Удаление старого платежа
                    paymentService.deletePaymentWithoutFundsReturn(oldPayment);

                    return udatedPurchase;
                })
                .map(purchaseMapper::purchaseToPurchaseReadDto);
    }

    /**
     * Обновляет покупку для тех случаев когда новый платеж производится со счета.
     * <p>Старый платеж удаляется. Денежная сумма возвращается на баланс кредитной карты или счета
     * с которых была произведена оплата.
     * <p>Новый платеж регистрируется с новым идентификатором.
     *
     */
    @Transactional
    public Optional<PurchaseReadDto> updatePurchaseAccountByIds(Long purchaseId, BigDecimal totalCost,
                                                                   Set<Long> newChosenProductIds, Long accountId) {

        return purchaseRepository.findById(purchaseId)
                .map(purchase -> {

                    // Сохранение ссылки на старый платеж для последующего его удаления
                    Payment oldPayment = purchase.getPayment();

                    // Возврат средств по старому платежу
                    paymentService.returnFunds(oldPayment);

                    // Создание нового платежа
                    var newAccountReadDto = accountService.findById(accountId)
                            .orElseThrow(() -> new AccountNotFoundException(accountId));
                    var newPaymentAccountCreateEditDto =
                            new PaymentAccountCreateEditDto(totalCost, newAccountReadDto);
                    var newPaymentAccountReadDto = paymentService
                            .createPaymentAccountWithBalanceReduce(newPaymentAccountCreateEditDto)
                            .orElseThrow(() -> new PaymentAccountCantBeSavedException(accountId));

                    // Список продуктов
                    Set<ProductReadDto> newChosenProducts = newChosenProductIds.stream()
                            .map(productId -> productService.findById(productId)
                                    .orElseThrow(() -> new ProductByIdNotFoundException(productId)))
                            .collect(Collectors.toSet());

                    // Обновление покупки
                    PurchaseCreateEditDto partiallyUpdatedCreateEditDto = // partially, т.к. изменение клиента не предусмотрено
                            new PurchaseCreateEditDto(null, newChosenProducts, newPaymentAccountReadDto.getId());
                    Purchase udatedPurchase = // замена полей products и payment в persistent entity purchase:
                            purchaseMapper.purchaseUpdateFromPurchaseCreateEditDto(purchase, partiallyUpdatedCreateEditDto);
                    purchaseRepository.flush();

                    // Удаление старого платежа
                    paymentService.deletePaymentWithoutFundsReturn(oldPayment);

                    return udatedPurchase;
                })
                .map(purchaseMapper::purchaseToPurchaseReadDto);
    }
}
