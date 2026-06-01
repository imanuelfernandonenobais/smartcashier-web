package com.smartcashier.web.controller;

import com.smartcashier.web.exception.BusinessException;
import com.smartcashier.web.service.ProductCategoryService;
import com.smartcashier.web.web.form.ProductCategoryForm;
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
@RequestMapping("/categories")
public class CategoryController {

    private final ProductCategoryService categoryService;

    public CategoryController(ProductCategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("pageTitle", "Kategori Produk");
        model.addAttribute("activeNav", "categories");
        return "categories/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("categoryForm", new ProductCategoryForm());
        model.addAttribute("pageTitle", "Tambah Kategori");
        model.addAttribute("activeNav", "categories");
        return "categories/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("categoryForm") ProductCategoryForm form, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Tambah Kategori");
            model.addAttribute("activeNav", "categories");
            return "categories/form";
        }
        try {
            categoryService.create(form);
            redirectAttributes.addFlashAttribute("successMessage", "Kategori berhasil ditambahkan.");
            return "redirect:/categories";
        } catch (BusinessException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("pageTitle", "Tambah Kategori");
            model.addAttribute("activeNav", "categories");
            return "categories/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        ProductCategoryForm form = new ProductCategoryForm();
        form.setName(categoryService.getById(id).getName());
        model.addAttribute("categoryForm", form);
        model.addAttribute("categoryId", id);
        model.addAttribute("pageTitle", "Edit Kategori");
        model.addAttribute("activeNav", "categories");
        return "categories/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("categoryForm") ProductCategoryForm form, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categoryId", id);
            model.addAttribute("pageTitle", "Edit Kategori");
            model.addAttribute("activeNav", "categories");
            return "categories/form";
        }
        try {
            categoryService.update(id, form);
            redirectAttributes.addFlashAttribute("successMessage", "Kategori berhasil diperbarui.");
            return "redirect:/categories";
        } catch (BusinessException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("categoryId", id);
            model.addAttribute("pageTitle", "Edit Kategori");
            model.addAttribute("activeNav", "categories");
            return "categories/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        categoryService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Kategori berhasil dihapus.");
        return "redirect:/categories";
    }
}
