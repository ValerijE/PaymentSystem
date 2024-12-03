package com.evv.service;

import com.evv.database.entity.Payment;
import com.evv.database.entity.PaymentAccount;
import com.evv.database.entity.PaymentCreditCard;
import com.evv.database.repository.PaymentRepository;
import com.evv.dto.PaymentAccountCreateEditDto;
import com.evv.dto.PaymentAccountReadDto;
import com.evv.dto.PaymentCreditCardCreateEditDto;
import com.evv.dto.PaymentCreditCardReadDto;
import com.evv.exception.UnableToChargeMoneyFromAccountException;
import com.evv.exception.UnableToChargeMoneyFromCreditCardException;
import com.evv.mapper.PaymentMapper;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Validated
public class PaymentService {

    private final PaymentRepository paymentRepository;

    private final PaymentMapper paymentMapper;

    private final CreditCardService creditCardService;

    private final AccountService accountService;

    @Transactional
    public Optional<PaymentCreditCardReadDto> createPaymentCreditCardWithBalanceReduce(
            @Validated PaymentCreditCardCreateEditDto createEditDto) {

        // Снятие денег с карты
        Long creditCardId = createEditDto.getCreditCard().getId();
        BigDecimal amount = createEditDto.getAmount();
        creditCardService.reduceBalance(
                        creditCardId,
                        amount)
                .orElseThrow(() -> new UnableToChargeMoneyFromCreditCardException(creditCardId));

        return Optional.of(createEditDto)
                .map(paymentMapper::paymentCreditCardCreateEditDtoToPaymentCreditCard)
                .map(paymentRepository::saveAndFlush)
                .map(paymentMapper::paymentCreditCardToPaymentCreditCardReadDto);
    }

    @Transactional
    public Optional<PaymentAccountReadDto> createPaymentAccountWithBalanceReduce(
            @Validated PaymentAccountCreateEditDto createEditDto) {

        // Снятие денег со счета
        Long accountId = createEditDto.getAccount().getId();
        BigDecimal amount = createEditDto.getAmount();
        accountService.reduceBalance(
                        accountId,
                        amount)
                .orElseThrow(() -> new UnableToChargeMoneyFromAccountException(accountId));

        return Optional.of(createEditDto)
                .map(paymentMapper::paymentAccountCreateEditDtoToPaymentAccount)
                .map(paymentRepository::saveAndFlush)
                .map(paymentMapper::paymentAccountToPaymentAccountReadDto);
    }

    @Transactional
    public void deletePaymentWithoutFundsReturn(Payment payment) {
        paymentRepository.delete(payment);
    }

    /**
     * Увеличивает баланс кредитной карты или счета в зависимости от способа платежа, содержащегося в аргументе
     * payment на величину amount содержащуюся в payment.
     */
    public void returnFunds(Payment payment) {
        Object p = payment;
        if (payment instanceof HibernateProxy) {
            p = Hibernate.unproxy(payment);
        }
        if (p instanceof PaymentCreditCard paymentCreditCard) {
            Long creditCardId = paymentCreditCard.getCreditCard().getId();
            creditCardService.increaseBalance(creditCardId, paymentCreditCard.getAmount());
        } else if (p instanceof PaymentAccount paymentAccount) {
            Long accountId = paymentAccount.getAccount().getId();
            accountService.increaseBalance(accountId, paymentAccount.getAmount());
        }
    }
}
