package com.smartcashier.web.repository;

import com.smartcashier.web.model.SaleTransaction;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SaleTransactionRepository extends JpaRepository<SaleTransaction, Long> {
    @EntityGraph(attributePaths = {"customer", "user"})
    List<SaleTransaction> findTop5ByOrderByTransactionTimeDesc();

    @EntityGraph(attributePaths = {"customer", "user", "items", "items.product"})
    List<SaleTransaction> findAllByOrderByTransactionTimeDesc();

    Optional<SaleTransaction> findTopByCodeStartingWithOrderByCodeDesc(String prefix);

    @Query("select coalesce(sum(t.totalAmount), 0) from SaleTransaction t where t.transactionTime between :start and :end")
    BigDecimal sumTotalAmountBetween(LocalDateTime start, LocalDateTime end);
}
