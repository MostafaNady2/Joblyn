package com.example.jobboard.controller;

import com.example.jobboard.entity.Job;
import com.example.jobboard.enums.JobStatus;
import com.example.jobboard.enums.Role;
import com.example.jobboard.security.ApplicationSecurityConfig;
import com.example.jobboard.security.CustomUserDetailsService;
import com.example.jobboard.security.LoginSuccessHandler;
import com.example.jobboard.service.JobService;
import com.example.jobboard.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@Import(ApplicationSecurityConfig.class)
class AdminControllerTest {
    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    private JobService jobService;
    @MockitoBean
    private UserService userService;
    // for security
    @MockitoBean
    CustomUserDetailsService customUserDetailsService;
    @MockitoBean
    LoginSuccessHandler loginSuccessHandler;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("should return dashboard page for admin user")
    void dashboard_admin() throws Exception {
        when(userService.getProfilePhotoUrl()).thenReturn(null);
        when(jobService.countJobsByStatus(JobStatus.PENDING)).thenReturn(0L);
        when(jobService.countJobsByStatus(JobStatus.OPEN)).thenReturn(0L);
        when(jobService.countJobsByStatus(JobStatus.REJECTED)).thenReturn(0L);
        when(userService.countByRole(Role.EMPLOYER)).thenReturn(0L);
        when(jobService.findByStatus(JobStatus.PENDING)).thenReturn(null);

        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"));
    }
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Approve redirects to the dashboard and calls the service")
    void approve_job() throws Exception {
        mockMvc.perform(post("/admin/jobs/1/approve").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/dashboard"));
        verify(jobService).approveJob(1);
    }
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Reject redirects to the dashboard and calls the service")
    void reject_job() throws Exception {
            mockMvc.perform(post("/admin/jobs/1/reject").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/dashboard"));
        verify(jobService).rejectJob(1);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Approve without CSRF token returns 403")
    void approve_noCsrf() throws Exception {
        mockMvc.perform(post("/admin/jobs/5/approve"))
                .andExpect(status().isForbidden());
        verify(jobService, never()).approveJob(5);
    }
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Approve with invalid ID returns 400")
    void approve_badId() throws Exception {
        mockMvc.perform(post("/admin/jobs/a7a/approve").with(csrf()))
                .andExpect(status().isBadRequest());
        verify(jobService, never()).approveJob(5);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Jobs page uses page 0, size 5, newest first by default")
    void jobs_page() throws Exception {
        Pageable pageable = PageRequest.of(0,5, Sort.by("postedAt").descending());
        when(jobService.findAll(pageable)).thenReturn(new PageImpl<>(List.of()));
        mockMvc.perform(get("/admin/jobs"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/jobs"));
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(jobService).findAll(captor.capture());
        assertThat(captor.getValue().getPageNumber()).isEqualTo(0);
        assertThat(captor.getValue().getPageSize()).isEqualTo(5);
        assertThat(captor.getValue().getSort().getOrderFor("postedAt").getDirection()).isEqualTo(Sort.Direction.DESC);
    }
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Jobs page uses page 5, newest first by default")
    void jobs_page_5() throws Exception {
        Pageable pageable = PageRequest.of(5,5, Sort.by("postedAt").descending());
        when(jobService.findAll(pageable)).thenReturn(new PageImpl<>(List.of()));
        mockMvc.perform(get("/admin/jobs").param("page", "5"))
                .andExpect(status().isOk());
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(jobService).findAll(captor.capture());
        assertThat(captor.getValue().getPageNumber()).isEqualTo(5);
    }
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Users page uses page 0, size 5, id ascending by default")
    void users_page() throws Exception {
        Pageable pageable = PageRequest.of(0,5,Sort.by("id"));
        when(userService.findAll(pageable)).thenReturn(new PageImpl<>(List.of()));
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/users"));
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(userService).findAll(captor.capture());
        assertThat(captor.getValue().getPageNumber()).isEqualTo(0);
        assertThat(captor.getValue().getPageSize()).isEqualTo(5);
    }
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Jobs page uses page 5, newest first by default")
    void users_page_5() throws Exception {
        Pageable pageable = PageRequest.of(5,5, Sort.by("id"));
        when(userService.findAll(pageable)).thenReturn(new PageImpl<>(List.of()));
        mockMvc.perform(get("/admin/users").param("page", "5"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/users"));
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(userService).findAll(captor.capture());
        assertThat(captor.getValue().getPageNumber()).isEqualTo(5);
        assertThat(captor.getValue().getPageSize()).isEqualTo(5);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/admin/dashboard", "/admin/jobs", "/admin/users"})
    @WithMockUser(roles = "CANDIDATE")
    @DisplayName("GET pages are forbidden for non-admins")
    void get_page_with_candidate(String url) throws Exception {
        mockMvc.perform(get(url))
                .andExpect(status().isForbidden());
        verify(userService, never()).findAll(any());
        verify(jobService, never()).findAll(any());
    }
    @ParameterizedTest
    @ValueSource(strings = {"/admin/dashboard", "/admin/jobs", "/admin/users"})
    @DisplayName("Anonymous users are redirected to the login page")
    void anonymous_user_redirected(String url) throws Exception {
        mockMvc.perform(get(url))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        verify(userService, never()).findAll(any());
        verify(jobService, never()).findAll(any());
    }
}