package com.smartcashier.web.service;

import com.smartcashier.web.model.User;
import com.smartcashier.web.model.UserRole;
import com.smartcashier.web.repository.UserRepository;
import com.smartcashier.web.web.form.UserForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.smartcashier.web.exception.BusinessException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTests {

    @Mock
    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, new BCryptPasswordEncoder());
    }

    @Test
    void createEncodesPasswordAndPersistsUser() {
        UserForm form = new UserForm();
        form.setFullName("Admin Baru");
        form.setUsername("admin-baru");
        form.setPassword("secret123");
        form.setRole(UserRole.ADMIN);
        form.setEnabled(true);

        when(userRepository.existsByUsernameIgnoreCase("admin-baru")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User user = userService.create(form);

        assertThat(user.getPasswordHash()).isNotBlank();
        assertThat(user.getPasswordHash()).doesNotContain("secret123");
        assertThat(user.getRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    void createRejectsDuplicateUsername() {
        UserForm form = new UserForm();
        form.setFullName("Admin Baru");
        form.setUsername("admin");
        form.setPassword("secret123");
        form.setRole(UserRole.ADMIN);

        when(userRepository.existsByUsernameIgnoreCase("admin")).thenReturn(true);

        assertThatThrownBy(() -> userService.create(form))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Username sudah digunakan");
    }
}
