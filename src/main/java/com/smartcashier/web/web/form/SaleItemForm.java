package com.smartcashier.web.web.form;

import com.smartcashier.web.model.UnitType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class SaleItemForm {

    @NotNull
    private Long productId;

    @NotNull
    private UnitType unitType;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal quantity = BigDecimal.ONE;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public UnitType getUnitType() {
        return unitType;
    }

    public void setUnitType(UnitType unitType) {
        this.unitType = unitType;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }
}
