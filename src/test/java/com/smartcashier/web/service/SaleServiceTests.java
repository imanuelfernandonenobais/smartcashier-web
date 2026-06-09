package com.smartcashier.web.service;

import com.smartcashier.web.model.Product;
import com.smartcashier.web.model.ProductUnitValue;
import com.smartcashier.web.model.UnitType;
import com.smartcashier.web.repository.CustomerRepository;
import com.smartcashier.web.repository.SaleTransactionRepository;
import com.smartcashier.web.web.form.SaleItemForm;
import com.smartcashier.web.web.view.SaleDraft;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.smartcashier.web.exception.BusinessException;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SaleServiceTests {

    @Mock
    private ProductService productService;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private SaleTransactionRepository saleTransactionRepository;

    private SaleService saleService;

    private Product product;
    private ProductUnitValue unitValue;

    @BeforeEach
    void setUp() {
        saleService = new SaleService(productService, customerRepository, saleTransactionRepository, new TransactionCodeService());

        product = new Product();
        product.setName("Katun Premium");

        unitValue = new ProductUnitValue();
        unitValue.setUnitType(UnitType.METER);
        unitValue.setSalePrice(new BigDecimal("25000"));
        unitValue.setStockQuantity(new BigDecimal("10"));
    }

    @Test
    void addItemCalculatesSubtotalAndTotal() {
        SaleDraft draft = saleService.newDraft();
        SaleItemForm form = new SaleItemForm();
        form.setProductId(1L);
        form.setUnitType(UnitType.METER);
        form.setQuantity(new BigDecimal("2"));

        when(productService.getById(1L)).thenReturn(product);
        when(productService.getUnitValue(product, UnitType.METER)).thenReturn(unitValue);

        saleService.addItem(draft, form);

        assertThat(draft.getItems()).hasSize(1);
        assertThat(draft.getItems().get(0).getSubtotal()).isEqualByComparingTo("50000");
        assertThat(draft.getTotalAmount()).isEqualByComparingTo("50000");
    }

    @Test
    void addItemRejectsInsufficientStock() {
        SaleDraft draft = saleService.newDraft();
        SaleItemForm form = new SaleItemForm();
        form.setProductId(1L);
        form.setUnitType(UnitType.METER);
        form.setQuantity(new BigDecimal("20"));

        when(productService.getById(1L)).thenReturn(product);
        when(productService.getUnitValue(product, UnitType.METER)).thenReturn(unitValue);

        assertThatThrownBy(() -> saleService.addItem(draft, form))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("tidak mencukupi");
    }
}
