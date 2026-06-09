package com.smartcashier.web.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.smartcashier.web.model.Product;
import com.smartcashier.web.model.ProductCategory;
import com.smartcashier.web.model.ProductUnitValue;
import com.smartcashier.web.model.UnitType;
import com.smartcashier.web.repository.ProductCategoryRepository;
import com.smartcashier.web.repository.ProductRepository;
import com.smartcashier.web.repository.UserRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@Testcontainers(disabledWithoutDocker = true)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PersistenceIntegrationTests {

    @Container
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4.4")
            .withDatabaseName("smartcashier_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.datasource.driver-class-name", MYSQL::getDriverClassName);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.flyway.enabled", () -> true);
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductCategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void flywaySeedsInitialAdminUser() {
        assertThat(userRepository.findByUsernameIgnoreCase("admin")).isPresent();
    }

    @Test
    void persistsProductWithNormalizedUnitValues() {
        ProductCategory category = new ProductCategory();
        category.setName("Fabric");
        category = categoryRepository.save(category);

        Product product = new Product();
        product.setName("Cotton");
        product.setCategory(category);

        ProductUnitValue meter = new ProductUnitValue();
        meter.setUnitType(UnitType.METER);
        meter.setSalePrice(new BigDecimal("50000"));
        meter.setStockQuantity(new BigDecimal("25"));
        product.addUnitValue(meter);

        ProductUnitValue roll = new ProductUnitValue();
        roll.setUnitType(UnitType.ROLL);
        roll.setSalePrice(new BigDecimal("450000"));
        roll.setStockQuantity(new BigDecimal("4"));
        product.addUnitValue(roll);

        Product saved = productRepository.saveAndFlush(product);
        Optional<Product> reloaded = productRepository.findDetailedById(saved.getId());

        assertThat(reloaded).isPresent();
        assertThat(reloaded.orElseThrow().getUnitValues())
                .extracting(ProductUnitValue::getUnitType)
                .containsExactlyInAnyOrder(UnitType.METER, UnitType.ROLL);
    }
}
