package com.smartcashier.web.web.view;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class PurchaseDraft implements Serializable {

    private final List<PurchaseDraftItemView> items = new ArrayList<>();
    private BigDecimal totalAmount = BigDecimal.ZERO;

    public List<PurchaseDraftItemView> getItems() {
        return items;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
}
