package com.evv.database.repository;

import com.evv.TestUtils;
import com.evv.TransactionalIT;
import com.evv.database.entity.Account;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RequiredArgsConstructor
class AccountRepositoryIT extends TransactionalIT {

    private final AccountRepository accountRepository;

    private final UserRepository userRepository;

    private final EntityManager entityManager;

    @Test
    void checkFindByClientId() {
        // Given, When
        Long clientId = userRepository.findClientByEmail(TestUtils.FIRST_CLIENT_EMAIL).get().getId();
        List<Account> accountsByClientId = accountRepository.findAllByClientIdOrderById(clientId);

        // Then
        assertThat(accountsByClientId).hasSize(3);

        Account account1 = accountsByClientId.get(0);
        assertThat(account1.getBalance()).isEqualTo(BigDecimal.valueOf(25_000_1000, 4));

        Account account2 = accountsByClientId.get(1);
        assertThat(account2.getBalance()).isEqualTo(BigDecimal.valueOf(0, 4));

        Long clientId1 = account1.getClient().getId();

        entityManager.clear();

        assertThat(account1.getClient().getId())
                .isEqualTo(account2.getClient().getId())
                .isEqualTo(clientId1);
    }
}