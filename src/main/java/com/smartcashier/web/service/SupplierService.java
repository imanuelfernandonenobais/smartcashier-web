package com.smartcashier.web.service;

import com.smartcashier.web.exception.NotFoundException;
import com.smartcashier.web.model.Supplier;
import com.smartcashier.web.repository.SupplierRepository;
import com.smartcashier.web.web.form.SupplierForm;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;

    public SupplierService(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    public List<Supplier> findAll() {
        return supplierRepository.findAll();
    }

    public Supplier getById(Long id) {
        return supplierRepository.findById(id).orElseThrow(() -> new NotFoundException("Supplier tidak ditemukan."));
    }

    @Transactional
    public Supplier create(SupplierForm form) {
        Supplier supplier = new Supplier();
        apply(supplier, form);
        return supplierRepository.save(supplier);
    }

    @Transactional
    public Supplier update(Long id, SupplierForm form) {
        Supplier supplier = getById(id);
        apply(supplier, form);
        return supplierRepository.save(supplier);
    }

    @Transactional
    public void delete(Long id) {
        supplierRepository.delete(getById(id));
    }

    public long countSuppliers() {
        return supplierRepository.count();
    }

    private void apply(Supplier supplier, SupplierForm form) {
        supplier.setName(form.getName().trim());
        supplier.setAddress(blankToNull(form.getAddress()));
        supplier.setPhoneNumber(blankToNull(form.getPhoneNumber()));
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
