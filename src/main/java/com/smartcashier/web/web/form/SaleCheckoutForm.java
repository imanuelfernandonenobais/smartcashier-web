package com.smartcashier.web.web.form;

import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

public class SaleCheckoutForm {

    private Long customerId;

    @DecimalMin(value = "0.00")
    private BigDecimal amountPaid = BigDecimal.ZERO;

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public BigDecimal getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(BigDecimal amountPaid) {
        this.amountPaid = amountPaid;
    }
}
