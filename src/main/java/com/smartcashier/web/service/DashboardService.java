package com.smartcashier.web.service;

import com.smartcashier.web.repository.CustomerRepository;
import com.smartcashier.web.repository.ProductRepository;
import com.smartcashier.web.repository.PurchaseTransactionRepository;
import com.smartcashier.web.repository.SaleTransactionRepository;
import com.smartcashier.web.repository.SupplierRepository;
import com.smartcashier.web.repository.UserRepository;
import com.smartcashier.web.web.view.DashboardView;
import com.smartcashier.web.web.view.RecentTransactionView;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DashboardService {

    private final SaleTransactionRepository saleTransactionRepository;
    private final PurchaseTransactionRepository purchaseTransactionRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final SupplierRepository supplierRepository;
    private final UserRepository userRepository;
    private final ProductService productService;

    public DashboardService(SaleTransactionRepository saleTransactionRepository, PurchaseTransactionRepository purchaseTransactionRepository, ProductRepository productRepository, CustomerRepository customerRepository, SupplierRepository supplierRepository, UserRepository userRepository, ProductService productService) {
        this.saleTransactionRepository = saleTransactionRepository;
        this.purchaseTransactionRepository = purchaseTransactionRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.supplierRepository = supplierRepository;
        this.userRepository = userRepository;
        this.productService = productService;
    }

    public DashboardView getDashboardView() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1).minusNanos(1);

        BigDecimal todaySales = saleTransactionRepository.sumTotalAmountBetween(start, end);
        BigDecimal todayPurchases = purchaseTransactionRepository.sumTotalAmountBetween(start, end);

        List<RecentTransactionView> recentSales = saleTransactionRepository.findTop5ByOrderByTransactionTimeDesc().stream()
                .map(trx -> new RecentTransactionView(
                        trx.getCode(),
                        trx.getCustomer() == null ? "-" : trx.getCustomer().getName(),
                        trx.getUser().getFullName(),
                        trx.getTransactionTime(),
                        trx.getTotalAmount()))
                .toList();

        List<RecentTransactionView> recentPurchases = purchaseTransactionRepository.findTop5ByOrderByTransactionTimeDesc().stream()
                .map(trx -> new RecentTransactionView(
                        trx.getCode(),
                        trx.getSupplier() == null ? "-" : trx.getSupplier().getName(),
                        trx.getUser().getFullName(),
                        trx.getTransactionTime(),
                        trx.getTotalAmount()))
                .toList();

        return new DashboardView(
                todaySales,
                todayPurchases,
                productRepository.count(),
                customerRepository.count(),
                supplierRepository.count(),
                userRepository.count(),
                productService.findLowStockProducts(new BigDecimal("10")),
                recentSales,
                recentPurchases
        );
    }
}
