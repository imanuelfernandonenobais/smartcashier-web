package com.smartcashier.web.controller;

import com.smartcashier.web.config.SecurityConfig;
import com.smartcashier.web.model.User;
import com.smartcashier.web.model.UserRole;
import com.smartcashier.web.security.AppUserDetailsService;
import com.smartcashier.web.service.AppFormattingService;
import com.smartcashier.web.service.CurrentUserService;
import com.smartcashier.web.service.DashboardService;
import com.smartcashier.web.service.UserService;
import com.smartcashier.web.web.view.DashboardView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {HomeController.class, LoginController.class, DashboardController.class, UserController.class})
@Import(SecurityConfig.class)
class WebLayerSecurityTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DashboardService dashboardService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private CurrentUserService currentUserService;

    @MockitoBean
    private AppUserDetailsService appUserDetailsService;

    @MockitoBean(name = "money")
    private AppFormattingService appFormattingService;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setFullName("Administrator");
        user.setUsername("admin");
        user.setRole(UserRole.ADMIN);

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(appFormattingService.format(any(BigDecimal.class))).thenAnswer(invocation -> "Rp " + invocation.getArgument(0));
        when(dashboardService.getDashboardView()).thenReturn(new DashboardView(
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                0,
                0,
                0,
                0,
                List.of(),
                List.of(),
                List.of()
        ));
        when(userService.findAll()).thenReturn(List.of());
    }

    @Test
    void dashboardRedirectsAnonymousUsersToLogin() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void dashboardAllowsAuthenticatedAdmin() throws Exception {
        mockMvc.perform(get("/dashboard").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void usersPageForbidsKasirRole() throws Exception {
        mockMvc.perform(get("/users").with(user("kasir").roles("KASIR")))
                .andExpect(status().isForbidden());
    }
}
