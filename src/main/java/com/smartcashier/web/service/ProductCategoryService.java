package com.smartcashier.web.service;

import com.smartcashier.web.exception.BusinessException;
import com.smartcashier.web.exception.NotFoundException;
import com.smartcashier.web.model.ProductCategory;
import com.smartcashier.web.repository.ProductCategoryRepository;
import com.smartcashier.web.web.form.ProductCategoryForm;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductCategoryService {

    private final ProductCategoryRepository categoryRepository;

    public ProductCategoryService(ProductCategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<ProductCategory> findAll() {
        return categoryRepository.findAll();
    }

    public ProductCategory getById(Long id) {
        return categoryRepository.findById(id).orElseThrow(() -> new NotFoundException("Kategori tidak ditemukan."));
    }

    @Transactional
    public ProductCategory create(ProductCategoryForm form) {
        if (categoryRepository.existsByNameIgnoreCase(form.getName())) {
            throw new BusinessException("Nama kategori sudah digunakan.");
        }
        ProductCategory category = new ProductCategory();
        category.setName(form.getName().trim());
        return categoryRepository.save(category);
    }

    @Transactional
    public ProductCategory update(Long id, ProductCategoryForm form) {
        ProductCategory category = getById(id);
        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(form.getName(), id)) {
            throw new BusinessException("Nama kategori sudah digunakan.");
        }
        category.setName(form.getName().trim());
        return categoryRepository.save(category);
    }

    @Transactional
    public void delete(Long id) {
        categoryRepository.delete(getById(id));
    }

    public long countCategories() {
        return categoryRepository.count();
    }
}
