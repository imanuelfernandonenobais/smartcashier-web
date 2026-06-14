package com.smartcashier.web.controller;

import com.smartcashier.web.exception.BusinessException;
import com.smartcashier.web.model.Product;
import com.smartcashier.web.model.UnitType;
import com.smartcashier.web.service.ProductCategoryService;
import com.smartcashier.web.service.ProductService;
import com.smartcashier.web.web.form.ProductForm;
import com.smartcashier.web.web.form.ProductUnitForm;
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

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final ProductCategoryService categoryService;

    public ProductController(ProductService productService, ProductCategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("products", productService.findAll());
        model.addAttribute("pageTitle", "Produk Kain");
        model.addAttribute("activeNav", "products");
        return "products/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("productForm", createDefaultForm());
        populateForm(model, "Tambah Produk");
        return "products/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("productForm") ProductForm form, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            populateForm(model, "Tambah Produk");
            return "products/form";
        }
        try {
            productService.create(form);
            redirectAttributes.addFlashAttribute("successMessage", "Produk berhasil ditambahkan.");
            return "redirect:/products";
        } catch (BusinessException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            populateForm(model, "Tambah Produk");
            return "products/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Product product = productService.getById(id);
        model.addAttribute("productForm", toForm(product));
        model.addAttribute("productId", id);
        populateForm(model, "Edit Produk");
        return "products/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("productForm") ProductForm form, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("productId", id);
            populateForm(model, "Edit Produk");
            return "products/form";
        }
        try {
            productService.update(id, form);
            redirectAttributes.addFlashAttribute("successMessage", "Produk berhasil diperbarui.");
            return "redirect:/products";
        } catch (BusinessException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("productId", id);
            populateForm(model, "Edit Produk");
            return "products/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        productService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Produk berhasil dihapus.");
        return "redirect:/products";
    }

    private void populateForm(Model model, String pageTitle) {
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("activeNav", "products");
    }

    private ProductForm createDefaultForm() {
        ProductForm form = new ProductForm();
        form.setUnitValues(defaultUnitForms());
        return form;
    }

    private ProductForm toForm(Product product) {
        ProductForm form = new ProductForm();
        form.setName(product.getName());
        form.setCategoryId(product.getCategory().getId());
        List<ProductUnitForm> unitForms = new ArrayList<>();
        for (UnitType unitType : UnitType.values()) {
            var value = productService.getUnitValue(product, unitType);
            ProductUnitForm unitForm = new ProductUnitForm();
            unitForm.setUnitType(unitType);
            unitForm.setSalePrice(value.getSalePrice());
            unitForm.setStockQuantity(value.getStockQuantity());
            unitForms.add(unitForm);
        }
        form.setUnitValues(unitForms);
        return form;
    }

    private List<ProductUnitForm> defaultUnitForms() {
        List<ProductUnitForm> unitForms = new ArrayList<>();
        for (UnitType unitType : UnitType.values()) {
            ProductUnitForm unitForm = new ProductUnitForm();
            unitForm.setUnitType(unitType);
            unitForms.add(unitForm);
        }
        return unitForms;
    }
}
