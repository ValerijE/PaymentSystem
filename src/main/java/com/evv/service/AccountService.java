package com.evv.service;

import com.evv.database.entity.Account;
import com.evv.database.repository.AccountRepository;
import com.evv.dto.AccountDto;
import com.evv.exception.AccountInsufficientFundsException;
import com.evv.mapper.AccountMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    private final AccountMapper accountMapper;

    public List<AccountDto.Read.Public> findAllByClientId(Long clientId) {
        return accountRepository.findAllByClientIdOrderById(clientId).stream()
                .map(accountMapper::accountToAccountReadDto)
                .toList();
    }

    public Optional<AccountDto.Read.Public> findById(Long id) {
        return accountRepository.findById(id)
                .map(accountMapper::accountToAccountReadDto);
    }

    @Transactional
    public Optional<AccountDto.Read.Public> reduceBalance(Long id, BigDecimal amount) {
        return accountRepository.findById(id)
                .map(account -> doReduceBalance(account, amount))
                .map(accountRepository::saveAndFlush)
                .map(accountMapper::accountToAccountReadDto);
    }

    private Account doReduceBalance(Account account, BigDecimal amount) {
        BigDecimal expectedBalance = account.getBalance().subtract(amount);
        if (expectedBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new AccountInsufficientFundsException(account.getId());
        }
        account.setBalance(expectedBalance);
        return account;
    }

    @Transactional
    public Optional<AccountDto.Read.Public> save(AccountDto.CreateEdit.Public accountDto) {
        return Optional.of(accountDto)
                .map(accountMapper::accountCreateEditDtoToAccount)
                .map(accountRepository::save)
                .map(accountMapper::accountToAccountReadDto);
    }

    @Transactional
    public Optional<AccountDto.Read.Public> increaseBalance(Long id, BigDecimal amount) {
        return accountRepository.findById(id)
                .map(account -> doIncreaseBalance(account, amount))
                .map(accountRepository::saveAndFlush)
                .map(accountMapper::accountToAccountReadDto);
    }

    private Account doIncreaseBalance(Account account, BigDecimal amount) {
        BigDecimal expectedBalance = account.getBalance().add(amount);
        account.setBalance(expectedBalance);
        return account;
    }
}
