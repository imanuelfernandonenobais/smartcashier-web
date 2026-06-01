package com.smartcashier.web.repository;

import com.smartcashier.web.model.PurchaseTransaction;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PurchaseTransactionRepository extends JpaRepository<PurchaseTransaction, Long> {
    @EntityGraph(attributePaths = {"supplier", "user"})
    List<PurchaseTransaction> findTop5ByOrderByTransactionTimeDesc();

    @EntityGraph(attributePaths = {"supplier", "user", "items", "items.product"})
    List<PurchaseTransaction> findAllByOrderByTransactionTimeDesc();

    Optional<PurchaseTransaction> findTopByCodeStartingWithOrderByCodeDesc(String prefix);

    @Query("select coalesce(sum(t.totalAmount), 0) from PurchaseTransaction t where t.transactionTime between :start and :end")
    BigDecimal sumTotalAmountBetween(LocalDateTime start, LocalDateTime end);
}
