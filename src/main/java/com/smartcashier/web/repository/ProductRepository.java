package com.smartcashier.web.repository;

import com.smartcashier.web.model.Product;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @Override
    @EntityGraph(attributePaths = {"category", "unitValues"})
    List<Product> findAll();

    @Query("select p from Product p where p.id = :id")
    @EntityGraph(attributePaths = {"category", "unitValues"})
    Optional<Product> findDetailedById(Long id);

    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
