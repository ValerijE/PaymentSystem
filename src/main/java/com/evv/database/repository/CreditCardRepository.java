package com.evv.database.repository;

import com.evv.database.entity.CreditCard;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CreditCardRepository extends JpaRepository<CreditCard, Long> {

    List<CreditCard> findAllByClientId(Long clientId);

    List<CreditCard> findAllByClientIdOrderByStatusAscIdAsc(Long clientId);

    List<CreditCard> findAllByClientId(Long clientId, Sort sort);

    Optional<CreditCard> findByIdAndClientId(Long id, Long clientId);
}
