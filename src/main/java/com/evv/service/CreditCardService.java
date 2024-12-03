package com.evv.service;

import com.evv.database.entity.CreditCard;
import com.evv.database.entity.CreditCardStatus;
import com.evv.database.repository.CreditCardRepository;
import com.evv.dto.CreditCardDto;
import com.evv.exception.AttemptToChargeMoneyFromBlockedCreditCardException;
import com.evv.mapper.CreditCardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CreditCardService {

    private final CreditCardRepository creditCardRepository;

    private final CreditCardMapper creditCardMapper;

    public List<CreditCardDto.Read.Public> findAllByClientId(Long clientId) {
        return creditCardRepository.findAllByClientIdOrderByStatusAscIdAsc(clientId).stream()
                .map(creditCardMapper::creditCardToCreditCardReadDto)
                .toList();
    }

    public Optional<CreditCardDto.Read.Public> findById(Long id) {
        return creditCardRepository.findById(id)
                .map(creditCardMapper::creditCardToCreditCardReadDto);
    }

    public Optional<CreditCardDto.Read.Public> findByIdAndClientId(Long id, Long clientId) {
        return creditCardRepository.findByIdAndClientId(id, clientId)
                .map(creditCardMapper::creditCardToCreditCardReadDto);
    }

    @Transactional
    public Optional<CreditCardDto.Read.Public> reduceBalance(Long id, BigDecimal amount) {
        return creditCardRepository.findById(id)
                .map(creditCard -> doReduceBalance(creditCard, amount))
                .map(creditCardRepository::saveAndFlush)
                .map(creditCardMapper::creditCardToCreditCardReadDto);
    }

    private CreditCard doReduceBalance(CreditCard creditCard, BigDecimal amount) {
        if (creditCard.getStatus() == CreditCardStatus.BLOCKED) {
            throw new AttemptToChargeMoneyFromBlockedCreditCardException(creditCard.getId());
        }
        BigDecimal expectedBalance = creditCard.getBalance().subtract(amount);
        creditCard.setBalance(expectedBalance);
        if (expectedBalance.compareTo(creditCard.getCreditLimit()) < 0) {
            creditCard.setStatus(CreditCardStatus.LIMIT_EXCEEDED);
        }
        return creditCard;
    }

    @Transactional
    public Optional<CreditCardDto.Read.Public> save(CreditCardDto.CreateEdit.Public creditCardDto) {
        return Optional.of(creditCardDto)
                .map(creditCardMapper::creditCardCreateEditDtoToCreditCard)
                .map(creditCardRepository::save)
                .map(creditCardMapper::creditCardToCreditCardReadDto);
    }

    @Transactional
    public Optional<CreditCardDto.Read.Public> increaseBalance(Long id, BigDecimal amount) {
        return creditCardRepository.findById(id)
                .map(creditCard -> doIncreaseBalance(creditCard, amount))
                .map(creditCardRepository::saveAndFlush)
                .map(creditCardMapper::creditCardToCreditCardReadDto);
    }

    private CreditCard doIncreaseBalance(CreditCard creditCard, BigDecimal amount) {
        BigDecimal expectedBalance = creditCard.getBalance().add(amount);
        if (expectedBalance.compareTo(creditCard.getCreditLimit()) >= 0) {
            creditCard.setStatus(CreditCardStatus.ACTIVE);
        }
        creditCard.setBalance(expectedBalance);
        return creditCard;
    }
}
