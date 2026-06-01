package com.smartcashier.web.controller;

import com.smartcashier.web.model.UserRole;
import com.smartcashier.web.exception.BusinessException;
import com.smartcashier.web.service.UserService;
import com.smartcashier.web.web.form.UserForm;
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
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("users", userService.findAll());
        model.addAttribute("pageTitle", "Manajemen User");
        model.addAttribute("activeNav", "users");
        return "users/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("userForm", new UserForm());
        populateForm(model, "Tambah User");
        return "users/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("userForm") UserForm form, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (form.getPassword() == null || form.getPassword().isBlank()) {
            bindingResult.rejectValue("password", "required", "Password wajib diisi.");
        }
        if (bindingResult.hasErrors()) {
            populateForm(model, "Tambah User");
            return "users/form";
        }
        try {
            userService.create(form);
            redirectAttributes.addFlashAttribute("successMessage", "User berhasil ditambahkan.");
            return "redirect:/users";
        } catch (BusinessException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            populateForm(model, "Tambah User");
            return "users/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        UserForm form = new UserForm();
        var user = userService.getById(id);
        form.setFullName(user.getFullName());
        form.setUsername(user.getUsername());
        form.setRole(user.getRole());
        form.setEnabled(user.isEnabled());
        model.addAttribute("userForm", form);
        model.addAttribute("userId", id);
        populateForm(model, "Edit User");
        return "users/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("userForm") UserForm form, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("userId", id);
            populateForm(model, "Edit User");
            return "users/form";
        }
        try {
            userService.update(id, form);
            redirectAttributes.addFlashAttribute("successMessage", "User berhasil diperbarui.");
            return "redirect:/users";
        } catch (BusinessException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("userId", id);
            populateForm(model, "Edit User");
            return "users/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        userService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "User berhasil dihapus.");
        return "redirect:/users";
    }

    private void populateForm(Model model, String pageTitle) {
        model.addAttribute("roles", UserRole.values());
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("activeNav", "users");
    }
}
