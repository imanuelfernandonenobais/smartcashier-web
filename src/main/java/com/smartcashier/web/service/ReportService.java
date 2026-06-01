package com.smartcashier.web.service;

import com.smartcashier.web.model.PurchaseTransaction;
import com.smartcashier.web.model.SaleTransaction;
import com.smartcashier.web.repository.PurchaseTransactionRepository;
import com.smartcashier.web.repository.SaleTransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ReportService {

    private final SaleTransactionRepository saleTransactionRepository;
    private final PurchaseTransactionRepository purchaseTransactionRepository;

    public ReportService(SaleTransactionRepository saleTransactionRepository, PurchaseTransactionRepository purchaseTransactionRepository) {
        this.saleTransactionRepository = saleTransactionRepository;
        this.purchaseTransactionRepository = purchaseTransactionRepository;
    }

    public List<SaleTransaction> getSalesReport() {
        return saleTransactionRepository.findAllByOrderByTransactionTimeDesc();
    }

    public List<PurchaseTransaction> getPurchaseReport() {
        return purchaseTransactionRepository.findAllByOrderByTransactionTimeDesc();
    }

    public BigDecimal getSalesTotal() {
        return getSalesReport().stream().map(SaleTransaction::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getPurchaseTotal() {
        return getPurchaseReport().stream().map(PurchaseTransaction::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
