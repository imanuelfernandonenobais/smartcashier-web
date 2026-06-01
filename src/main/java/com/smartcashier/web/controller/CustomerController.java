package com.smartcashier.web.controller;

import com.smartcashier.web.service.CustomerService;
import com.smartcashier.web.web.form.CustomerForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("customers", customerService.findAll());
        model.addAttribute("pageTitle", "Pelanggan");
        model.addAttribute("activeNav", "customers");
        return "customers/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("customerForm", new CustomerForm());
        model.addAttribute("pageTitle", "Tambah Pelanggan");
        model.addAttribute("activeNav", "customers");
        return "customers/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("customerForm") CustomerForm form, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Tambah Pelanggan");
            model.addAttribute("activeNav", "customers");
            return "customers/form";
        }
        customerService.create(form);
        redirectAttributes.addFlashAttribute("successMessage", "Pelanggan berhasil ditambahkan.");
        return "redirect:/customers";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        var customer = customerService.getById(id);
        CustomerForm form = new CustomerForm();
        form.setName(customer.getName());
        form.setAddress(customer.getAddress());
        form.setPhoneNumber(customer.getPhoneNumber());
        model.addAttribute("customerForm", form);
        model.addAttribute("customerId", id);
        model.addAttribute("pageTitle", "Edit Pelanggan");
        model.addAttribute("activeNav", "customers");
        return "customers/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("customerForm") CustomerForm form, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("customerId", id);
            model.addAttribute("pageTitle", "Edit Pelanggan");
            model.addAttribute("activeNav", "customers");
            return "customers/form";
        }
        customerService.update(id, form);
        redirectAttributes.addFlashAttribute("successMessage", "Pelanggan berhasil diperbarui.");
        return "redirect:/customers";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        customerService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Pelanggan berhasil dihapus.");
        return "redirect:/customers";
    }
}
