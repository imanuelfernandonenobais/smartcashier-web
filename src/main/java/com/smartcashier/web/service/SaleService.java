package com.smartcashier.web.service;

import com.smartcashier.web.exception.BusinessException;
import com.smartcashier.web.exception.NotFoundException;
import com.smartcashier.web.model.Customer;
import com.smartcashier.web.model.Product;
import com.smartcashier.web.model.ProductUnitValue;
import com.smartcashier.web.model.SaleTransaction;
import com.smartcashier.web.model.SaleTransactionItem;
import com.smartcashier.web.model.User;
import com.smartcashier.web.repository.CustomerRepository;
import com.smartcashier.web.repository.SaleTransactionRepository;
import com.smartcashier.web.web.form.SaleCheckoutForm;
import com.smartcashier.web.web.form.SaleItemForm;
import com.smartcashier.web.web.view.SaleDraft;
import com.smartcashier.web.web.view.SaleDraftItemView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
public class SaleService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final ProductService productService;
    private final CustomerRepository customerRepository;
    private final SaleTransactionRepository saleTransactionRepository;
    private final TransactionCodeService transactionCodeService;

    public SaleService(ProductService productService, CustomerRepository customerRepository, SaleTransactionRepository saleTransactionRepository, TransactionCodeService transactionCodeService) {
        this.productService = productService;
        this.customerRepository = customerRepository;
        this.saleTransactionRepository = saleTransactionRepository;
        this.transactionCodeService = transactionCodeService;
    }

    public SaleDraft newDraft() {
        return new SaleDraft();
    }

    public void addItem(SaleDraft draft, SaleItemForm form) {
        Product product = productService.getById(form.getProductId());
        ProductUnitValue unitValue = productService.getUnitValue(product, form.getUnitType());

        BigDecimal draftQuantity = draft.getItems().stream()
                .filter(item -> item.getProductId().equals(product.getId()) && item.getUnitType() == form.getUnitType())
                .map(SaleDraftItemView::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal requestedTotal = draftQuantity.add(form.getQuantity());
        if (requestedTotal.compareTo(unitValue.getStockQuantity()) > 0) {
            throw new BusinessException("Stok " + form.getUnitType().getLabel() + " untuk " + product.getName() + " tidak mencukupi.");
        }

        SaleDraftItemView existing = draft.getItems().stream()
                .filter(item -> item.getProductId().equals(product.getId()) && item.getUnitType() == form.getUnitType())
                .findFirst()
                .orElse(null);

        if (existing == null) {
            existing = new SaleDraftItemView();
            existing.setProductId(product.getId());
            existing.setProductName(product.getName());
            existing.setUnitType(form.getUnitType());
            draft.getItems().add(existing);
        }

        existing.setQuantity(existing.getQuantity().add(form.getQuantity()));
        existing.setUnitPrice(unitValue.getSalePrice());
        existing.setSubtotal(existing.getQuantity().multiply(existing.getUnitPrice()));

        recalculate(draft);
    }

    public void removeItem(SaleDraft draft, int index) {
        if (index >= 0 && index < draft.getItems().size()) {
            draft.getItems().remove(index);
            recalculate(draft);
        }
    }

    public void recalculatePayment(SaleDraft draft, BigDecimal amountPaid) {
        draft.setAmountPaid(amountPaid == null ? BigDecimal.ZERO : amountPaid);
        draft.setChangeAmount(draft.getAmountPaid().subtract(draft.getTotalAmount()));
    }

    @Transactional
    public SaleTransaction checkout(SaleDraft draft, SaleCheckoutForm form, User currentUser) {
        if (draft.getItems().isEmpty()) {
            throw new BusinessException("Detail transaksi penjualan masih kosong.");
        }

        recalculate(draft);
        recalculatePayment(draft, form.getAmountPaid());
        if (draft.getAmountPaid().compareTo(draft.getTotalAmount()) < 0) {
            throw new BusinessException("Pembayaran kurang dari total transaksi.");
        }

        SaleTransaction transaction = new SaleTransaction();
        transaction.setCode(nextCode());
        transaction.setTransactionTime(LocalDateTime.now());
        transaction.setUser(currentUser);
        transaction.setTotalAmount(draft.getTotalAmount());
        transaction.setAmountPaid(draft.getAmountPaid());
        transaction.setChangeAmount(draft.getChangeAmount());
        transaction.setCustomer(loadCustomer(form.getCustomerId()));

        for (SaleDraftItemView draftItem : draft.getItems()) {
            Product product = productService.getById(draftItem.getProductId());
            ProductUnitValue unitValue = productService.getUnitValue(product, draftItem.getUnitType());
            if (draftItem.getQuantity().compareTo(unitValue.getStockQuantity()) > 0) {
                throw new BusinessException("Stok " + draftItem.getUnitType().getLabel() + " untuk " + product.getName() + " tidak mencukupi.");
            }

            unitValue.setStockQuantity(unitValue.getStockQuantity().subtract(draftItem.getQuantity()));

            SaleTransactionItem item = new SaleTransactionItem();
            item.setProduct(product);
            item.setUnitType(draftItem.getUnitType());
            item.setQuantity(draftItem.getQuantity());
            item.setUnitPrice(draftItem.getUnitPrice());
            item.setSubtotal(draftItem.getSubtotal());
            transaction.addItem(item);
        }

        return saleTransactionRepository.save(transaction);
    }

    private Customer loadCustomer(Long customerId) {
        if (customerId == null) {
            return null;
        }
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new NotFoundException("Pelanggan tidak ditemukan."));
    }

    private String nextCode() {
        String prefix = "PJ-" + LocalDate.now().format(DATE_FORMATTER) + "-";
        Optional<String> latestCode = saleTransactionRepository.findTopByCodeStartingWithOrderByCodeDesc(prefix).map(SaleTransaction::getCode);
        return transactionCodeService.nextSaleCode(latestCode);
    }

    private void recalculate(SaleDraft draft) {
        BigDecimal total = draft.getItems().stream()
                .map(SaleDraftItemView::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        draft.setTotalAmount(total);
        draft.setChangeAmount(draft.getAmountPaid().subtract(total));
    }
}
