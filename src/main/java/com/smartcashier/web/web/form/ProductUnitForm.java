package com.smartcashier.web.web.form;

import com.smartcashier.web.model.UnitType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class ProductUnitForm {

    @NotNull
    private UnitType unitType;

    @NotNull
    @DecimalMin(value = "0.00")
    private BigDecimal salePrice = BigDecimal.ZERO;

    @NotNull
    @DecimalMin(value = "0.00")
    private BigDecimal stockQuantity = BigDecimal.ZERO;

    public UnitType getUnitType() {
        return unitType;
    }

    public void setUnitType(UnitType unitType) {
        this.unitType = unitType;
    }

    public BigDecimal getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(BigDecimal salePrice) {
        this.salePrice = salePrice;
    }

    public BigDecimal getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(BigDecimal stockQuantity) {
        this.stockQuantity = stockQuantity;
    }
}
