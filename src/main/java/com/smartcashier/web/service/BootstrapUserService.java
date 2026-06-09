package com.smartcashier.web.service;

import com.smartcashier.web.model.User;
import com.smartcashier.web.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BootstrapUserService implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public BootstrapUserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        userRepository.findByUsernameIgnoreCase("admin")
                .filter(user -> user.getPasswordHash() == null || user.getPasswordHash().isBlank())
                .ifPresent(this::initializeAdminPassword);
    }

    private void initializeAdminPassword(User user) {
        user.setPasswordHash(passwordEncoder.encode("admin123"));
        userRepository.save(user);
    }
}
