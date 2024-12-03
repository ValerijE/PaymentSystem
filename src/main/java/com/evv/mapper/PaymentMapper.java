package com.evv.mapper;

import com.evv.database.entity.Account;
import com.evv.database.entity.CreditCard;
import com.evv.database.entity.PaymentAccount;
import com.evv.database.entity.PaymentCreditCard;
import com.evv.database.repository.AccountRepository;
import com.evv.database.repository.CreditCardRepository;
import com.evv.dto.PaymentAccountCreateEditDto;
import com.evv.dto.PaymentAccountReadDto;
import com.evv.dto.PaymentCreditCardCreateEditDto;
import com.evv.dto.PaymentCreditCardReadDto;
import com.evv.exception.AccountNotFoundException;
import com.evv.exception.CreditCardNotFoundException;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {CreditCardMapper.class, AccountMapper.class}
)
public abstract class PaymentMapper {

    @Autowired
    private CreditCardRepository creditCardRepository;

    @Autowired
    private AccountRepository accountRepository;

    public abstract PaymentCreditCard paymentCreditCardCreateEditDtoToPaymentCreditCard(
            PaymentCreditCardCreateEditDto source);

    public abstract PaymentCreditCardReadDto paymentCreditCardToPaymentCreditCardReadDto(
            PaymentCreditCard source);

    public abstract PaymentAccount paymentAccountCreateEditDtoToPaymentAccount(
            PaymentAccountCreateEditDto source);

    public abstract PaymentAccountReadDto paymentAccountToPaymentAccountReadDto(
            PaymentAccount source);

    @AfterMapping
    protected void injectCreditCard(@MappingTarget PaymentCreditCard target,
                                    PaymentCreditCardCreateEditDto source) {
        CreditCard creditCard = creditCardRepository.findById(source.getCreditCard().getId())
                .orElseThrow(() -> new CreditCardNotFoundException(source.getCreditCard().getId()));
        target.setCreditCard(creditCard);
    }

    @AfterMapping
    protected void injectAccount(@MappingTarget PaymentAccount target,
                                    PaymentAccountCreateEditDto source) {
        Account account = accountRepository.findById(source.getAccount().getId())
                .orElseThrow(() -> new AccountNotFoundException(source.getAccount().getId()));
        target.setAccount(account);
    }
}
