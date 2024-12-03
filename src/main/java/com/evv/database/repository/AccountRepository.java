package com.evv.database.repository;

import com.evv.database.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {

//    @EntityGraph(attributePaths = {"client"})
    List<Account> findAllByClientIdOrderById(Long clientId);
}

