package com.smartcashier.web.repository;

import com.smartcashier.web.model.ProductUnitValue;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;

public interface ProductUnitValueRepository extends JpaRepository<ProductUnitValue, Long> {
    @EntityGraph(attributePaths = {"product"})
    List<ProductUnitValue> findTop5ByStockQuantityLessThanOrderByStockQuantityAsc(BigDecimal threshold);
}
