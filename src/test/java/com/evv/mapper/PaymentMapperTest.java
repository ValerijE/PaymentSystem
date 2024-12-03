package com.evv.mapper;

import com.evv.database.entity.*;
import com.evv.database.repository.AccountRepository;
import com.evv.database.repository.CreditCardRepository;
import com.evv.dto.*;
import com.evv.exception.AccountNotFoundException;
import com.evv.exception.CreditCardNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentMapperTest {

    @Mock
    CreditCardRepository creditCardRepository;

    @Mock
    AccountRepository accountRepository;

    @Mock
    CreditCardMapper creditCardMapper;

    @Mock
    AccountMapper accountMapper;

    @InjectMocks
    PaymentMapperImpl paymentMapper;

    // GIVEN для всех тестов
    private static final Long CLIENT_ID = 111L;

    private static final Long CREDIT_CARD_ID = 222L;

    private static final Long ACCOUNT_ID = 333L;

    private static final Long PAYMENT_CREDIT_CARD_ID = 3332L;

    private static final Long PAYMENT_ACCOUNT_ID = 30332L;

    private static final String CLIENT_EMAIL = "some_client@test.com";

    private static final BigDecimal CREDIT_CARD_BALANCE = BigDecimal.valueOf(10_000_1230L, 4);

    private static final LocalDate EXPIRATION_DATE = LocalDate.of(2025, 5, 5);

    private static final BigDecimal CREDIT_LIMIT = BigDecimal.valueOf(50_000_0000, 4);

    private final Client client = new Client(CLIENT_ID, CLIENT_EMAIL, "{noop}000", Role.CLIENT,
            LocalDate.of(2000, 5, 5), ClientStatus.ACTIVE, null,
            Gender.MALE, null, null);

    private final CreditCardDto.Read.Public creditCardReadDto = new CreditCardDto.Read.Public(
            CREDIT_CARD_ID,
            EXPIRATION_DATE,
            CREDIT_CARD_BALANCE,
            CREDIT_LIMIT,
            CreditCardStatus.ACTIVE, CLIENT_ID);

    private final CreditCard creditCard = new CreditCard(
            CREDIT_CARD_ID,
            EXPIRATION_DATE,
            CREDIT_CARD_BALANCE,
            CREDIT_LIMIT,
            CreditCardStatus.ACTIVE, client, new ArrayList<>(), 0L);

    private final BigDecimal accountBalance = BigDecimal.valueOf(35_000_0000, 4);

    private final Account account = new Account(
            ACCOUNT_ID, accountBalance, client, new ArrayList<>(), 0L);

    private final AccountDto.Read.Public accountReadDto = new AccountDto.Read.Public(
            ACCOUNT_ID, accountBalance, CLIENT_ID);

    private final BigDecimal paymentAmount = BigDecimal.valueOf(5_000_0000, 4);

    @Test
    void paymentCreditCardCreateEditDtoToPaymentCreditCard_NormalFlow() {
        // stub
        doReturn(Optional.of(creditCard)).when(creditCardRepository).findById(CREDIT_CARD_ID);

        // given
        PaymentCreditCardCreateEditDto paymentCreditCardCreateEditDto = new PaymentCreditCardCreateEditDto(
                paymentAmount,
                creditCardReadDto
        );

        // when
        PaymentCreditCard paymentCreditCard = paymentMapper
                .paymentCreditCardCreateEditDtoToPaymentCreditCard(paymentCreditCardCreateEditDto);

        // then
        assertThat(paymentCreditCard.getCreditCard()).isEqualTo(creditCard);
        assertThat(paymentCreditCard.getAmount()).isEqualTo(paymentAmount);
        assertThat(paymentCreditCard.getPurchase()).isNull();

        verify(creditCardRepository).findById(CREDIT_CARD_ID);
        verifyNoMoreInteractions(creditCardRepository);
    }

    @Test
    void paymentCreditCardToPaymentCreditCardReadDto_NormalFlow() {
        // stub
        doReturn(creditCardReadDto).when(creditCardMapper).creditCardToCreditCardReadDto(creditCard);

        // given
        PaymentCreditCard paymentCreditCard = new PaymentCreditCard(
                PAYMENT_CREDIT_CARD_ID,
                paymentAmount,
                null,
                creditCard
        );

        // when
        PaymentCreditCardReadDto paymentCreditCardReadDto = paymentMapper
                .paymentCreditCardToPaymentCreditCardReadDto(paymentCreditCard);

        // then
        assertThat(paymentCreditCardReadDto.getId()).isEqualTo(PAYMENT_CREDIT_CARD_ID);
        assertThat(paymentCreditCardReadDto.getCreditCard()).isEqualTo(creditCardReadDto);
        assertThat(paymentCreditCardReadDto.getAmount()).isEqualTo(paymentAmount);

        verify(creditCardMapper).creditCardToCreditCardReadDto(creditCard);
        verifyNoMoreInteractions(creditCardMapper);
    }


    @Test
    void paymentAccountCreateEditDtoToPaymentAccount_NormalFlow() {
        // stub
        doReturn(Optional.of(account)).when(accountRepository).findById(ACCOUNT_ID);

        // given
        PaymentAccountCreateEditDto paymentAccountCreateEditDto = new PaymentAccountCreateEditDto(
                paymentAmount,
                accountReadDto
        );

        // when
        PaymentAccount paymentAccount = paymentMapper
                .paymentAccountCreateEditDtoToPaymentAccount(paymentAccountCreateEditDto);

        // then
        assertThat(paymentAccount.getAccount()).isEqualTo(account);
        assertThat(paymentAccount.getAmount()).isEqualTo(paymentAmount);
        assertThat(paymentAccount.getPurchase()).isNull();

        verify(accountRepository).findById(ACCOUNT_ID);
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    void paymentAccountToPaymentAccountReadDto_NormalFlow() {
        // stub
        doReturn(accountReadDto).when(accountMapper).accountToAccountReadDto(account);

        // given
        PaymentAccount paymentAccount = new PaymentAccount(
                PAYMENT_ACCOUNT_ID,
                paymentAmount,
                null,
                account
        );

        // when
        PaymentAccountReadDto paymentAccountReadDto = paymentMapper
                .paymentAccountToPaymentAccountReadDto(paymentAccount);

        // then
        assertThat(paymentAccountReadDto.getId()).isEqualTo(PAYMENT_ACCOUNT_ID);
        assertThat(paymentAccountReadDto.getAccount()).isEqualTo(accountReadDto);
        assertThat(paymentAccountReadDto.getAmount()).isEqualTo(paymentAmount);

        verify(accountMapper).accountToAccountReadDto(account);
        verifyNoMoreInteractions(accountMapper);
    }

    @Test
    void paymentCreditCardCreateEditDtoToPaymentCreditCard_CreditCardRepositoryReturnEmptyOptional_ThrowCreditCardNotFoundException() {
        // stub
        doReturn(Optional.empty()).when(creditCardRepository).findById(CREDIT_CARD_ID);

        // given
        PaymentCreditCardCreateEditDto paymentCreditCardCreateEditDto = new PaymentCreditCardCreateEditDto(
                paymentAmount,
                creditCardReadDto
        );

        // when
        assertThatThrownBy(() -> paymentMapper
                        .paymentCreditCardCreateEditDtoToPaymentCreditCard(paymentCreditCardCreateEditDto))
        // then
                .isInstanceOf(CreditCardNotFoundException.class)
                .hasMessageContaining("not found");

        verify(creditCardRepository).findById(CREDIT_CARD_ID);
        verifyNoMoreInteractions(creditCardRepository);
    }

    @Test
    void paymentAccountCreateEditDtoToPaymentAccount_AccountRepositoryReturnEmptyOptional_ThrowAccountNotFoundException() {
        // stub
        doReturn(Optional.empty()).when(accountRepository).findById(ACCOUNT_ID);

        // given
        PaymentAccountCreateEditDto paymentAccountCreateEditDto = new PaymentAccountCreateEditDto(
                paymentAmount,
                accountReadDto
        );

        // when
        assertThatThrownBy(() -> paymentMapper
                        .paymentAccountCreateEditDtoToPaymentAccount(paymentAccountCreateEditDto))
        // then
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessageContaining("not found");

        verify(accountRepository).findById(ACCOUNT_ID);
        verifyNoMoreInteractions(creditCardRepository);
    }
}
