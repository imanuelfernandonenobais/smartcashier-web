package com.smartcashier.web.web.view;

import java.math.BigDecimal;

public record LowStockView(String productName, String unitLabel, BigDecimal stockQuantity) {
}
