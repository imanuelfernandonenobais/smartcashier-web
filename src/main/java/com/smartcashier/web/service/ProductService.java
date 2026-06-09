package com.smartcashier.web.service;

import com.smartcashier.web.exception.BusinessException;
import com.smartcashier.web.exception.NotFoundException;
import com.smartcashier.web.model.Product;
import com.smartcashier.web.model.ProductCategory;
import com.smartcashier.web.model.ProductUnitValue;
import com.smartcashier.web.model.UnitType;
import com.smartcashier.web.repository.ProductCategoryRepository;
import com.smartcashier.web.repository.ProductRepository;
import com.smartcashier.web.repository.ProductUnitValueRepository;
import com.smartcashier.web.web.form.ProductForm;
import com.smartcashier.web.web.form.ProductUnitForm;
import com.smartcashier.web.web.view.LowStockView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;
    private final ProductUnitValueRepository productUnitValueRepository;

    public ProductService(
            ProductRepository productRepository,
            ProductCategoryRepository categoryRepository,
            ProductUnitValueRepository productUnitValueRepository
    ) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productUnitValueRepository = productUnitValueRepository;
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public Product getById(Long id) {
        return productRepository.findDetailedById(id)
                .orElseThrow(() -> new NotFoundException("Produk tidak ditemukan."));
    }

    @Transactional
    public Product create(ProductForm form) {
        if (productRepository.existsByNameIgnoreCase(form.getName())) {
            throw new BusinessException("Nama produk sudah digunakan.");
        }

        Product product = new Product();
        apply(product, form);

        return productRepository.save(product);
    }

    @Transactional
    public Product update(Long id, ProductForm form) {
        Product product = getById(id);

        if (productRepository.existsByNameIgnoreCaseAndIdNot(form.getName(), id)) {
            throw new BusinessException("Nama produk sudah digunakan.");
        }

        apply(product, form);

        return productRepository.save(product);
    }

    @Transactional
    public void delete(Long id) {
        productRepository.delete(getById(id));
    }

    public long countProducts() {
        return productRepository.count();
    }

    public List<LowStockView> findLowStockProducts(BigDecimal threshold) {
        return productUnitValueRepository
                .findTop5ByStockQuantityLessThanOrderByStockQuantityAsc(threshold)
                .stream()
                .map(value -> new LowStockView(
                        value.getProduct().getName(),
                        value.getUnitType().getLabel(),
                        value.getStockQuantity()
                ))
                .toList();
    }

    public ProductUnitValue getUnitValue(Product product, UnitType unitType) {
        return product.findUnitValue(unitType)
                .orElseThrow(() -> new BusinessException(
                        "Satuan tidak tersedia untuk produk " + product.getName() + "."
                ));
    }

    private void apply(Product product, ProductForm form) {
        ProductCategory category = categoryRepository.findById(form.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Kategori tidak ditemukan."));

        product.setName(form.getName().trim());
        product.setCategory(category);

        Map<UnitType, ProductUnitForm> valuesByUnit = new EnumMap<>(UnitType.class);

        for (ProductUnitForm unitForm : form.getUnitValues()) {
            if (unitForm.getUnitType() == null) {
                throw new BusinessException("Satuan produk tidak valid.");
            }

            valuesByUnit.put(unitForm.getUnitType(), unitForm);
        }

        for (UnitType unitType : UnitType.values()) {
            ProductUnitForm unitForm = valuesByUnit.get(unitType);

            if (unitForm == null) {
                throw new BusinessException("Semua satuan produk harus tersedia.");
            }

            if (unitForm.getSalePrice() == null) {
                throw new BusinessException("Harga jual untuk satuan " + unitType.getLabel() + " wajib diisi.");
            }

            if (unitForm.getStockQuantity() == null) {
                throw new BusinessException("Stok untuk satuan " + unitType.getLabel() + " wajib diisi.");
            }

            if (unitForm.getSalePrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException("Harga jual untuk satuan " + unitType.getLabel() + " tidak boleh minus.");
            }

            if (unitForm.getStockQuantity().compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException("Stok untuk satuan " + unitType.getLabel() + " tidak boleh minus.");
            }

            ProductUnitValue unitValue = product.findUnitValue(unitType)
                    .orElseGet(() -> {
                        ProductUnitValue newValue = new ProductUnitValue();
                        newValue.setUnitType(unitType);
                        product.addUnitValue(newValue);
                        return newValue;
                    });

            unitValue.setSalePrice(unitForm.getSalePrice());
            unitValue.setStockQuantity(unitForm.getStockQuantity());
        }
    }
}