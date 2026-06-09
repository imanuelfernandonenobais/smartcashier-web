package com.smartcashier.web.service;

import com.smartcashier.web.exception.NotFoundException;
import com.smartcashier.web.model.Customer;
import com.smartcashier.web.repository.CustomerRepository;
import com.smartcashier.web.web.form.CustomerForm;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    public Customer getById(Long id) {
        return customerRepository.findById(id).orElseThrow(() -> new NotFoundException("Pelanggan tidak ditemukan."));
    }

    @Transactional
    public Customer create(CustomerForm form) {
        Customer customer = new Customer();
        apply(customer, form);
        return customerRepository.save(customer);
    }

    @Transactional
    public Customer update(Long id, CustomerForm form) {
        Customer customer = getById(id);
        apply(customer, form);
        return customerRepository.save(customer);
    }

    @Transactional
    public void delete(Long id) {
        customerRepository.delete(getById(id));
    }

    public long countCustomers() {
        return customerRepository.count();
    }

    private void apply(Customer customer, CustomerForm form) {
        customer.setName(form.getName().trim());
        customer.setAddress(blankToNull(form.getAddress()));
        customer.setPhoneNumber(blankToNull(form.getPhoneNumber()));
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
