package com.example.jobboard.controller;

import com.example.jobboard.security.ApplicationSecurityConfig;
import com.example.jobboard.security.CustomUserDetailsService;
import com.example.jobboard.security.LoginSuccessHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(LoginController.class)
@Import(ApplicationSecurityConfig.class)
class LoginControllerTest {

    @Autowired
    MockMvc mockMvc;
    // for security
    @MockitoBean
    CustomUserDetailsService customUserDetailsService;
    @MockitoBean
    LoginSuccessHandler loginSuccessHandler;

    @Test
    @DisplayName("GET /login returns the login page")
    void get_login_page() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }
}