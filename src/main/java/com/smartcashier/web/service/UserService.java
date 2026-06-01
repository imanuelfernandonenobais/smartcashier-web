package com.smartcashier.web.service;

import com.smartcashier.web.exception.BusinessException;
import com.smartcashier.web.exception.NotFoundException;
import com.smartcashier.web.model.User;
import com.smartcashier.web.repository.UserRepository;
import com.smartcashier.web.web.form.UserForm;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User getById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException("User tidak ditemukan."));
    }

    @Transactional
    public User create(UserForm form) {
        if (userRepository.existsByUsernameIgnoreCase(form.getUsername())) {
            throw new BusinessException("Username sudah digunakan.");
        }
        if (form.getPassword() == null || form.getPassword().isBlank()) {
            throw new BusinessException("Password wajib diisi.");
        }

        User user = new User();
        apply(user, form, true);
        return userRepository.save(user);
    }

    @Transactional
    public User update(Long id, UserForm form) {
        User user = getById(id);
        if (userRepository.existsByUsernameIgnoreCaseAndIdNot(form.getUsername(), id)) {
            throw new BusinessException("Username sudah digunakan.");
        }
        apply(user, form, false);
        return userRepository.save(user);
    }

    @Transactional
    public void delete(Long id) {
        userRepository.delete(getById(id));
    }

    public long countUsers() {
        return userRepository.count();
    }

    private void apply(User user, UserForm form, boolean requirePassword) {
        user.setFullName(form.getFullName().trim());
        user.setUsername(form.getUsername().trim());
        user.setRole(form.getRole());
        user.setEnabled(form.isEnabled());

        if (form.getPassword() != null && !form.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        } else if (requirePassword) {
            throw new BusinessException("Password wajib diisi.");
        }
    }
}
