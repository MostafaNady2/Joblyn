package com.example.jobboard.controller;

import com.example.jobboard.dto.UserForm;
import com.example.jobboard.exception.EmailAlreadyExistException;
import com.example.jobboard.security.ApplicationSecurityConfig;
import com.example.jobboard.security.CustomUserDetailsService;
import com.example.jobboard.security.LoginSuccessHandler;
import com.example.jobboard.service.RegistrationService;
import com.example.jobboard.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RegisterController.class)
@Import(ApplicationSecurityConfig.class)
class RegisterControllerTest {
    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    private UserService userService;
    @MockitoBean
    private RegistrationService registrationService;
    // for security
    @MockitoBean
    CustomUserDetailsService customUserDetailsService;
    @MockitoBean
    LoginSuccessHandler loginSuccessHandler;

    @Test
    @DisplayName("Anonymous users get an empty registration form")
    void getRegister_noRole() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attribute("userForm", hasProperty("role", nullValue())));
    }
    @ParameterizedTest
    @CsvSource({"candidate,CANDIDATE", "Candidate,CANDIDATE", "employer,EMPLOYER", "EMPLOYER,EMPLOYER"})
    @DisplayName("The role parameter pre-selects the role, ignoring case")
    void getRegister_withRole(String param, String expected) throws Exception {
        mockMvc.perform(get("/register").param("role", param))
                .andExpect(status().isOk())
                .andExpect(model().attribute("userForm", hasProperty("role", is(expected))));
    }
    @Test
    @DisplayName("An unknown role parameter is ignored")
    void getRegister_unknownRole() throws Exception {
        mockMvc.perform(get("/register").param("role", "admin"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("userForm", hasProperty("role", nullValue())));
    }
    @Test
    @DisplayName("A valid candidate form registers the user and redirects to login")
    void post_validCandidate() throws Exception {
        mockMvc.perform(formPost("candidate"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        ArgumentCaptor<UserForm> captor = ArgumentCaptor.forClass(UserForm.class);
        verify(registrationService).register(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo("a@b.com");
        assertThat(captor.getValue().getRole()).isEqualTo("candidate");
    }

    @Test
    @DisplayName("A valid employer form with company details registers and redirects")
    void post_validEmployer() throws Exception {
        mockMvc.perform(formPost("employer")
                        .param("companyName", "Acme")
                        .param("country", "Egypt")
                        .param("city", "Cairo"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(registrationService).register(any());
    }
    @ParameterizedTest
    @ValueSource(strings = {"employer", "EMPLOYER", "Employer"})
    @DisplayName("Employers must give company name, country and city, whatever the role casing")
    void post_employerMissingCompanyDetails(String role) throws Exception {
        mockMvc.perform(formPost(role))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeHasFieldErrors("userForm", "companyName", "country", "city"));

        verify(registrationService, never()).register(any());
    }
    @Test
    @DisplayName("Candidates do not need company details")
    void post_candidateWithoutCompanyDetails() throws Exception {
        mockMvc.perform(formPost("candidate"))
                .andExpect(status().is3xxRedirection());
        verify(registrationService).register(any());
    }

    @Test
    @DisplayName("A duplicate email returns the form with an error on the email field")
    void post_duplicateEmail() throws Exception {
        doThrow(new EmailAlreadyExistException("exists"))
                .when(registrationService).register(any(UserForm.class));

        mockMvc.perform(formPost("candidate"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeHasFieldErrors("userForm", "email"));
    }
    @Test
    @DisplayName("A POST without a CSRF token is rejected")
    void post_noCsrf() throws Exception {
        mockMvc.perform(post("/register")
                        .param("email", "a@b.com")
                        .param("password", "Password123")
                        .param("role", "candidate"))
                .andExpect(status().isForbidden());

        verify(registrationService, never()).register(any());
    }






    private MockHttpServletRequestBuilder formPost(String role) {
        return post("/register").with(csrf())
                .param("firstName", "Mostafa")
                .param("lastName", "Nady")
                .param("email", "a@b.com")
                .param("password", "Password123")
                .param("role", role);
    }
}