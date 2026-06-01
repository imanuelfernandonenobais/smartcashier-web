package com.smartcashier.web.web.view;

import java.math.BigDecimal;
import java.util.List;

public record DashboardView(
        BigDecimal todaySales,
        BigDecimal todayPurchases,
        long productCount,
        long customerCount,
        long supplierCount,
        long userCount,
        List<LowStockView> lowStockProducts,
        List<RecentTransactionView> recentSales,
        List<RecentTransactionView> recentPurchases
) {
}
