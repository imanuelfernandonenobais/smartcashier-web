package com.smartcashier.web.controller;

import com.smartcashier.web.exception.BusinessException;
import com.smartcashier.web.service.CurrentUserService;
import com.smartcashier.web.service.CustomerService;
import com.smartcashier.web.service.ProductService;
import com.smartcashier.web.service.SaleService;
import com.smartcashier.web.web.form.SaleCheckoutForm;
import com.smartcashier.web.web.form.SaleItemForm;
import com.smartcashier.web.web.view.SaleDraft;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/sales")
@SessionAttributes("saleDraft")
public class SaleController {

    private final SaleService saleService;
    private final ProductService productService;
    private final CustomerService customerService;
    private final CurrentUserService currentUserService;

    public SaleController(SaleService saleService, ProductService productService, CustomerService customerService, CurrentUserService currentUserService) {
        this.saleService = saleService;
        this.productService = productService;
        this.customerService = customerService;
        this.currentUserService = currentUserService;
    }

    @ModelAttribute("saleDraft")
    public SaleDraft saleDraft() {
        return saleService.newDraft();
    }

    @GetMapping
    public String page(Model model) {
        model.addAttribute("saleItemForm", new SaleItemForm());
        model.addAttribute("saleCheckoutForm", new SaleCheckoutForm());
        populate(model);
        return "sales/index";
    }

    @PostMapping("/items")
    public String addItem(@Valid @ModelAttribute("saleItemForm") SaleItemForm form, BindingResult bindingResult, @ModelAttribute("saleDraft") SaleDraft draft, Model model) {
        if (!bindingResult.hasErrors()) {
            try {
                saleService.addItem(draft, form);
            } catch (BusinessException ex) {
                model.addAttribute("errorMessage", ex.getMessage());
            }
        }
        model.addAttribute("saleCheckoutForm", new SaleCheckoutForm());
        populate(model);
        return "sales/index";
    }

    @PostMapping("/items/{index}/remove")
    public String removeItem(@PathVariable int index, @ModelAttribute("saleDraft") SaleDraft draft, RedirectAttributes redirectAttributes) {
        saleService.removeItem(draft, index);
        redirectAttributes.addFlashAttribute("successMessage", "Item berhasil dihapus.");
        return "redirect:/sales";
    }

    @PostMapping("/checkout")
    public String checkout(@Valid @ModelAttribute("saleCheckoutForm") SaleCheckoutForm form, BindingResult bindingResult, @ModelAttribute("saleDraft") SaleDraft draft, Model model, SessionStatus sessionStatus, RedirectAttributes redirectAttributes) {
        saleService.recalculatePayment(draft, form.getAmountPaid());
        if (bindingResult.hasErrors()) {
            model.addAttribute("saleItemForm", new SaleItemForm());
            populate(model);
            return "sales/index";
        }
        try {
            saleService.checkout(draft, form, currentUserService.getCurrentUser());
            sessionStatus.setComplete();
            redirectAttributes.addFlashAttribute("successMessage", "Transaksi penjualan berhasil disimpan.");
            return "redirect:/sales";
        } catch (BusinessException ex) {
            model.addAttribute("saleItemForm", new SaleItemForm());
            model.addAttribute("errorMessage", ex.getMessage());
            populate(model);
            return "sales/index";
        }
    }

    @PostMapping("/cancel")
    public String cancel(SessionStatus sessionStatus, RedirectAttributes redirectAttributes) {
        sessionStatus.setComplete();
        redirectAttributes.addFlashAttribute("successMessage", "Draft penjualan dibersihkan.");
        return "redirect:/sales";
    }

    private void populate(Model model) {
        model.addAttribute("products", productService.findAll());
        model.addAttribute("customers", customerService.findAll());
        model.addAttribute("pageTitle", "Transaksi Penjualan");
        model.addAttribute("activeNav", "sales");
    }
}
