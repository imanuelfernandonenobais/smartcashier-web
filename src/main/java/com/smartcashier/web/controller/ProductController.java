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
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
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
    public String create(
            @Valid @ModelAttribute("productForm") ProductForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMessage", getValidationMessage(bindingResult));
            populateForm(model, "Tambah Produk");
            return "products/form";
        }

        String manualValidation = validateProductForm(form);
        if (manualValidation != null) {
            model.addAttribute("errorMessage", manualValidation);
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

        } catch (Exception ex) {
            String detailError = getSimpleErrorMessage(ex, "disimpan");
            model.addAttribute("errorMessage", detailError);
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
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("productForm") ProductForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMessage", getValidationMessage(bindingResult));
            model.addAttribute("productId", id);
            populateForm(model, "Edit Produk");
            return "products/form";
        }

        String manualValidation = validateProductForm(form);
        if (manualValidation != null) {
            model.addAttribute("errorMessage", manualValidation);
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

        } catch (Exception ex) {
            String detailError = getSimpleErrorMessage(ex, "diperbarui");
            model.addAttribute("errorMessage", detailError);
            model.addAttribute("productId", id);
            populateForm(model, "Edit Produk");
            return "products/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Produk berhasil dihapus.");

        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());

        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Produk gagal dihapus. Produk kemungkinan masih digunakan pada transaksi."
            );
        }

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

    private String validateProductForm(ProductForm form) {
        if (form.getName() == null || form.getName().trim().isEmpty()) {
            return "Nama produk wajib diisi.";
        }

        if (form.getCategoryId() == null) {
            return "Kategori produk wajib dipilih.";
        }

        if (form.getUnitValues() == null || form.getUnitValues().isEmpty()) {
            return "Minimal satu satuan produk wajib diisi.";
        }

        for (ProductUnitForm unit : form.getUnitValues()) {
            String unitName = getUnitLabel(unit.getUnitType());

            if (unit.getUnitType() == null) {
                return "Satuan produk tidak valid.";
            }

            if (unit.getSalePrice() == null) {
                return "Harga jual pada satuan " + unitName + " wajib diisi.";
            }

            if (unit.getStockQuantity() == null) {
                return "Stok pada satuan " + unitName + " wajib diisi.";
            }

            if (unit.getSalePrice().compareTo(BigDecimal.ZERO) < 0) {
                return "Harga jual pada satuan " + unitName + " tidak boleh minus.";
            }

            if (unit.getStockQuantity().compareTo(BigDecimal.ZERO) < 0) {
                return "Stok pada satuan " + unitName + " tidak boleh minus.";
            }
        }

        return null;
    }

    private String getValidationMessage(BindingResult bindingResult) {
        FieldError error = bindingResult.getFieldErrors().get(0);

        String field = error.getField();
        Object rejectedValue = error.getRejectedValue();

        String fieldName = getReadableFieldName(field);
        String unitName = getUnitNameFromField(field);
        String example = getExampleValue(field);

        if (rejectedValue != null) {
            String inputValue = rejectedValue.toString();

            if (inputValue.contains(",")) {
                return fieldName + unitName + " tidak valid. Jangan pakai koma. Contoh benar: " + example;
            }

            if (inputValue.contains(".")) {
                return fieldName + unitName + " tidak valid. Jangan pakai titik. Contoh benar: " + example;
            }

            if (inputValue.toLowerCase().contains("rp")) {
                return fieldName + unitName + " tidak valid. Jangan pakai Rp. Contoh benar: " + example;
            }

            if (inputValue.matches(".*[a-zA-Z]+.*")) {
                return fieldName + unitName + " tidak valid. Jangan pakai huruf. Contoh benar: " + example;
            }

            if (inputValue.isBlank()) {
                return fieldName + unitName + " wajib diisi.";
            }
        }

        return fieldName + unitName + " tidak valid. Masukkan angka saja. Contoh benar: " + example;
    }

    private String getReadableFieldName(String field) {
        if (field.contains("salePrice")) {
            return "Harga jual";
        }

        if (field.contains("stockQuantity")) {
            return "Stok";
        }

        if (field.contains("name")) {
            return "Nama produk";
        }

        if (field.contains("categoryId")) {
            return "Kategori";
        }

        return "Input";
    }

    private String getUnitNameFromField(String field) {
        if (field.contains("unitValues[0]")) {
            return " pada satuan kg";
        }

        if (field.contains("unitValues[1]")) {
            return " pada satuan meter";
        }

        if (field.contains("unitValues[2]")) {
            return " pada satuan roll";
        }

        if (field.contains("unitValues[3]")) {
            return " pada satuan yard";
        }

        return "";
    }

    private String getExampleValue(String field) {
        if (field.contains("salePrice")) {
            return "45000";
        }

        if (field.contains("stockQuantity")) {
            return "1000";
        }

        return "123";
    }

    private String getUnitLabel(UnitType unitType) {
        if (unitType == null) {
            return "";
        }

        return unitType.getLabel();
    }

    private String getSimpleErrorMessage(Exception ex, String action) {
        String message = ex.getMessage();

        if (message != null && message.contains("Duplicate entry")) {
            return "Produk gagal " + action + ". Data satuan produk sudah ada. Coba refresh halaman lalu simpan ulang.";
        }

        return "Produk gagal " + action + ". Periksa kembali data yang dimasukkan.";
    }
}