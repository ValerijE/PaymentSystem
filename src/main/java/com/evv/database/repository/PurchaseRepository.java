package com.evv.database.repository;

import com.evv.database.entity.Purchase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    Page<Purchase> findAllByClientId(Long clientId, Pageable pageable);

    Optional<Purchase> findByIdAndClientId(Long id, Long clientId);
}
