package com.smartcashier.web.controller;

import com.smartcashier.web.exception.BusinessException;
import com.smartcashier.web.service.CurrentUserService;
import com.smartcashier.web.service.ProductService;
import com.smartcashier.web.service.PurchaseService;
import com.smartcashier.web.service.SupplierService;
import com.smartcashier.web.web.form.PurchaseCheckoutForm;
import com.smartcashier.web.web.form.PurchaseItemForm;
import com.smartcashier.web.web.view.PurchaseDraft;
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
@RequestMapping("/purchases")
@SessionAttributes("purchaseDraft")
public class PurchaseController {

    private final PurchaseService purchaseService;
    private final ProductService productService;
    private final SupplierService supplierService;
    private final CurrentUserService currentUserService;

    public PurchaseController(PurchaseService purchaseService, ProductService productService, SupplierService supplierService, CurrentUserService currentUserService) {
        this.purchaseService = purchaseService;
        this.productService = productService;
        this.supplierService = supplierService;
        this.currentUserService = currentUserService;
    }

    @ModelAttribute("purchaseDraft")
    public PurchaseDraft purchaseDraft() {
        return purchaseService.newDraft();
    }

    @GetMapping
    public String page(Model model) {
        model.addAttribute("purchaseItemForm", new PurchaseItemForm());
        model.addAttribute("purchaseCheckoutForm", new PurchaseCheckoutForm());
        populate(model);
        return "purchases/index";
    }

    @PostMapping("/items")
    public String addItem(@Valid @ModelAttribute("purchaseItemForm") PurchaseItemForm form, BindingResult bindingResult, @ModelAttribute("purchaseDraft") PurchaseDraft draft, Model model) {
        if (!bindingResult.hasErrors()) {
            try {
                purchaseService.addItem(draft, form);
            } catch (BusinessException ex) {
                model.addAttribute("errorMessage", ex.getMessage());
            }
        }
        model.addAttribute("purchaseCheckoutForm", new PurchaseCheckoutForm());
        populate(model);
        return "purchases/index";
    }

    @PostMapping("/items/{index}/remove")
    public String removeItem(@PathVariable int index, @ModelAttribute("purchaseDraft") PurchaseDraft draft, RedirectAttributes redirectAttributes) {
        purchaseService.removeItem(draft, index);
        redirectAttributes.addFlashAttribute("successMessage", "Item berhasil dihapus.");
        return "redirect:/purchases";
    }

    @PostMapping("/checkout")
    public String checkout(@Valid @ModelAttribute("purchaseCheckoutForm") PurchaseCheckoutForm form, BindingResult bindingResult, @ModelAttribute("purchaseDraft") PurchaseDraft draft, Model model, SessionStatus sessionStatus, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("purchaseItemForm", new PurchaseItemForm());
            populate(model);
            return "purchases/index";
        }
        try {
            purchaseService.checkout(draft, form, currentUserService.getCurrentUser());
            sessionStatus.setComplete();
            redirectAttributes.addFlashAttribute("successMessage", "Transaksi pembelian berhasil disimpan.");
            return "redirect:/purchases";
        } catch (BusinessException ex) {
            model.addAttribute("purchaseItemForm", new PurchaseItemForm());
            model.addAttribute("errorMessage", ex.getMessage());
            populate(model);
            return "purchases/index";
        }
    }

    @PostMapping("/cancel")
    public String cancel(SessionStatus sessionStatus, RedirectAttributes redirectAttributes) {
        sessionStatus.setComplete();
        redirectAttributes.addFlashAttribute("successMessage", "Draft pembelian dibersihkan.");
        return "redirect:/purchases";
    }

    private void populate(Model model) {
        model.addAttribute("products", productService.findAll());
        model.addAttribute("suppliers", supplierService.findAll());
        model.addAttribute("pageTitle", "Transaksi Pembelian");
        model.addAttribute("activeNav", "purchases");
    }
}
