package com.smartcashier.web.controller;

import com.smartcashier.web.service.SupplierService;
import com.smartcashier.web.web.form.SupplierForm;
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
@RequestMapping("/suppliers")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("suppliers", supplierService.findAll());
        model.addAttribute("pageTitle", "Supplier");
        model.addAttribute("activeNav", "suppliers");
        return "suppliers/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("supplierForm", new SupplierForm());
        model.addAttribute("pageTitle", "Tambah Supplier");
        model.addAttribute("activeNav", "suppliers");
        return "suppliers/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("supplierForm") SupplierForm form, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Tambah Supplier");
            model.addAttribute("activeNav", "suppliers");
            return "suppliers/form";
        }
        supplierService.create(form);
        redirectAttributes.addFlashAttribute("successMessage", "Supplier berhasil ditambahkan.");
        return "redirect:/suppliers";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        var supplier = supplierService.getById(id);
        SupplierForm form = new SupplierForm();
        form.setName(supplier.getName());
        form.setAddress(supplier.getAddress());
        form.setPhoneNumber(supplier.getPhoneNumber());
        model.addAttribute("supplierForm", form);
        model.addAttribute("supplierId", id);
        model.addAttribute("pageTitle", "Edit Supplier");
        model.addAttribute("activeNav", "suppliers");
        return "suppliers/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("supplierForm") SupplierForm form, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("supplierId", id);
            model.addAttribute("pageTitle", "Edit Supplier");
            model.addAttribute("activeNav", "suppliers");
            return "suppliers/form";
        }
        supplierService.update(id, form);
        redirectAttributes.addFlashAttribute("successMessage", "Supplier berhasil diperbarui.");
        return "redirect:/suppliers";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        supplierService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Supplier berhasil dihapus.");
        return "redirect:/suppliers";
    }
}
