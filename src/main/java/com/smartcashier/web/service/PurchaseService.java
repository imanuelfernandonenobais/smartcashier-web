package com.smartcashier.web.service;

import com.smartcashier.web.exception.BusinessException;
import com.smartcashier.web.exception.NotFoundException;
import com.smartcashier.web.model.Product;
import com.smartcashier.web.model.ProductUnitValue;
import com.smartcashier.web.model.PurchaseTransaction;
import com.smartcashier.web.model.PurchaseTransactionItem;
import com.smartcashier.web.model.Supplier;
import com.smartcashier.web.model.User;
import com.smartcashier.web.repository.PurchaseTransactionRepository;
import com.smartcashier.web.repository.SupplierRepository;
import com.smartcashier.web.web.form.PurchaseCheckoutForm;
import com.smartcashier.web.web.form.PurchaseItemForm;
import com.smartcashier.web.web.view.PurchaseDraft;
import com.smartcashier.web.web.view.PurchaseDraftItemView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
public class PurchaseService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final ProductService productService;
    private final SupplierRepository supplierRepository;
    private final PurchaseTransactionRepository purchaseTransactionRepository;
    private final TransactionCodeService transactionCodeService;

    public PurchaseService(ProductService productService, SupplierRepository supplierRepository, PurchaseTransactionRepository purchaseTransactionRepository, TransactionCodeService transactionCodeService) {
        this.productService = productService;
        this.supplierRepository = supplierRepository;
        this.purchaseTransactionRepository = purchaseTransactionRepository;
        this.transactionCodeService = transactionCodeService;
    }

    public PurchaseDraft newDraft() {
        return new PurchaseDraft();
    }

    public void addItem(PurchaseDraft draft, PurchaseItemForm form) {
        Product product = productService.getById(form.getProductId());

        PurchaseDraftItemView existing = draft.getItems().stream()
                .filter(item -> item.getProductId().equals(product.getId()) && item.getUnitType() == form.getUnitType())
                .findFirst()
                .orElse(null);

        if (existing == null) {
            existing = new PurchaseDraftItemView();
            existing.setProductId(product.getId());
            existing.setProductName(product.getName());
            existing.setUnitType(form.getUnitType());
            draft.getItems().add(existing);
        }

        existing.setQuantity(existing.getQuantity().add(form.getQuantity()));
        existing.setUnitPrice(form.getUnitPrice());
        existing.setSubtotal(existing.getQuantity().multiply(existing.getUnitPrice()));
        recalculate(draft);
    }

    public void removeItem(PurchaseDraft draft, int index) {
        if (index >= 0 && index < draft.getItems().size()) {
            draft.getItems().remove(index);
            recalculate(draft);
        }
    }

    @Transactional
    public PurchaseTransaction checkout(PurchaseDraft draft, PurchaseCheckoutForm form, User currentUser) {
        if (draft.getItems().isEmpty()) {
            throw new BusinessException("Detail transaksi pembelian masih kosong.");
        }

        recalculate(draft);

        PurchaseTransaction transaction = new PurchaseTransaction();
        transaction.setCode(nextCode());
        transaction.setTransactionTime(LocalDateTime.now());
        transaction.setUser(currentUser);
        transaction.setSupplier(loadSupplier(form.getSupplierId()));
        transaction.setTotalAmount(draft.getTotalAmount());

        for (PurchaseDraftItemView draftItem : draft.getItems()) {
            Product product = productService.getById(draftItem.getProductId());
            ProductUnitValue unitValue = productService.getUnitValue(product, draftItem.getUnitType());
            unitValue.setStockQuantity(unitValue.getStockQuantity().add(draftItem.getQuantity()));
            unitValue.setSalePrice(draftItem.getUnitPrice());

            PurchaseTransactionItem item = new PurchaseTransactionItem();
            item.setProduct(product);
            item.setUnitType(draftItem.getUnitType());
            item.setQuantity(draftItem.getQuantity());
            item.setUnitPrice(draftItem.getUnitPrice());
            item.setSubtotal(draftItem.getSubtotal());
            transaction.addItem(item);
        }

        return purchaseTransactionRepository.save(transaction);
    }

    private Supplier loadSupplier(Long supplierId) {
        if (supplierId == null) {
            return null;
        }
        return supplierRepository.findById(supplierId)
                .orElseThrow(() -> new NotFoundException("Supplier tidak ditemukan."));
    }

    private String nextCode() {
        String prefix = "PB-" + LocalDate.now().format(DATE_FORMATTER) + "-";
        Optional<String> latestCode = purchaseTransactionRepository.findTopByCodeStartingWithOrderByCodeDesc(prefix).map(PurchaseTransaction::getCode);
        return transactionCodeService.nextPurchaseCode(latestCode);
    }

    private void recalculate(PurchaseDraft draft) {
        BigDecimal total = draft.getItems().stream()
                .map(PurchaseDraftItemView::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        draft.setTotalAmount(total);
    }
}
