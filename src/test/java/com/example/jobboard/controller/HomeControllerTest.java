package com.example.jobboard.controller;

import com.example.jobboard.entity.Job;
import com.example.jobboard.enums.JobStatus;
import com.example.jobboard.enums.Role;
import com.example.jobboard.security.ApplicationSecurityConfig;
import com.example.jobboard.security.CustomUserDetailsService;
import com.example.jobboard.security.LoginSuccessHandler;
import com.example.jobboard.service.CandidateService;
import com.example.jobboard.service.EmployerService;
import com.example.jobboard.service.JobService;
import com.example.jobboard.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.hamcrest.Matchers.contains;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebMvcTest(HomeController.class)
@Import(ApplicationSecurityConfig.class)
class HomeControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private UserService userService;
    @MockitoBean
    private JobService jobService;
    @MockitoBean
    private CandidateService candidateService;
    @MockitoBean
    private EmployerService employerService;

    // for security
    @MockitoBean
    CustomUserDetailsService customUserDetailsService;
    @MockitoBean
    LoginSuccessHandler loginSuccessHandler;

    @Test
    @DisplayName("Anonymous users can see the home page with its statistics")
    void home_anonymous() throws Exception {
        when(jobService.findRecentJobs(3)).thenReturn(List.of(job(JobStatus.OPEN)));
        when(userService.getProfilePhotoUrl()).thenReturn("/static/images/user-icon.png");
        when(jobService.countJobsByStatus(JobStatus.OPEN)).thenReturn(1L);
        when(userService.countByRole(Role.EMPLOYER)).thenReturn(1L);

        mockMvc.perform(get("/"))
                .andExpect(status().isOk()) // 200
                .andExpect(view().name("index"))
                .andExpect(model().attribute("recentJobs", List.of(job(JobStatus.OPEN))))
                .andExpect(model().attribute("image", "/static/images/user-icon.png"))
                .andExpect(model().attribute("rolesCount", 1L))
                .andExpect(model().attribute("employersCount", 1L))
                .andExpect(model().attribute("currentPage","home"));
    }

    @Test
    @DisplayName("Only OPEN jobs are shown in recent jobs")
    void home_filtersNonOpenedJobs() throws Exception {
        Job open = job(JobStatus.OPEN);
        Job closed = job(JobStatus.CLOSED);
        Job pending = job(JobStatus.PENDING);

        when(jobService.findRecentJobs(3)).thenReturn(List.of(open, closed, pending));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("recentJobs", contains(open)));
    }

    @Test
    @DisplayName("No jobs are shown when there are no jobs")
    void home_noJobs() throws Exception {
        when(jobService.findRecentJobs(3)).thenReturn(List.of());

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("recentJobs", List.of()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/", "/home", "/index"})
    @DisplayName("All home URLs map to the same page")
    void home_allMappings(String url) throws Exception {
        when(jobService.findRecentJobs(3)).thenReturn(List.of());

        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    @WithMockUser(roles = "CANDIDATE")
    @DisplayName("Logged-in users can see the home page too")
    void home_loggedIn() throws Exception {
        when(jobService.findRecentJobs(3)).thenReturn(List.of());

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }
    private Job job(JobStatus status) {
        Job job = new Job();
        job.setStatus(status);
        return job;
    }

}