package com.smartcashier.web.web.view;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RecentTransactionView(
        String code,
        String counterpart,
        String userName,
        LocalDateTime transactionTime,
        BigDecimal totalAmount
) {
}
