package com.smartcashier.web.web.form;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

public class ProductForm {

    @NotBlank
    @Size(max = 150)
    private String name;

    @NotNull
    private Long categoryId;

    @Valid
    private List<ProductUnitForm> unitValues = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public List<ProductUnitForm> getUnitValues() {
        return unitValues;
    }

    public void setUnitValues(List<ProductUnitForm> unitValues) {
        this.unitValues = unitValues;
    }
}
