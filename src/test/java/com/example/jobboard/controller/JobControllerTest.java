package com.example.jobboard.controller;

import com.example.jobboard.dto.JobFilter;
import com.example.jobboard.entity.Job;
import com.example.jobboard.enums.JobStatus;
import com.example.jobboard.exception.JobNotFoundException;
import com.example.jobboard.security.ApplicationSecurityConfig;
import com.example.jobboard.security.CustomUserDetailsService;
import com.example.jobboard.security.LoginSuccessHandler;
import com.example.jobboard.service.JobService;
import com.example.jobboard.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(JobController.class)
@Import(ApplicationSecurityConfig.class)
class JobControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private JobService jobService;
    @MockitoBean
    private UserService userService;
    // for security
    @MockitoBean
    CustomUserDetailsService customUserDetailsService;
    @MockitoBean
    LoginSuccessHandler loginSuccessHandler;

    @BeforeEach
    void setUp() {
        when(jobService.findByFilter(any(JobFilter.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));
        when(jobService.findAllSkills()).thenReturn(List.of("Java", "Spring"));
        when(userService.getProfilePhotoUrl()).thenReturn("/images/user-icon.png");
    }

    @Test
    @DisplayName("Job list shows the page model and uses page 0, size 5, newest first")
    void list_defaults() throws Exception {
        mockMvc.perform(get("/jobs/list"))
                .andExpect(status().isOk())
                .andExpect(view().name("job/jobs"))
                .andExpect(model().attributeExists("jobs", "filter"))
                .andExpect(model().attribute("allSkills", List.of("Java", "Spring")))
                .andExpect(model().attribute("image", "/images/user-icon.png"))
                .andExpect(model().attribute("currentPage", "jobs"));

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(jobService).findByFilter(any(JobFilter.class), captor.capture());
        Pageable pageable = captor.getValue();
        assertThat(pageable.getPageNumber()).isEqualTo(0);
        assertThat(pageable.getPageSize()).isEqualTo(5);
        assertThat(pageable.getSort().getOrderFor("postedAt").getDirection())
                .isEqualTo(Sort.Direction.DESC);
    }
    @Test
    @DisplayName("The page 3 is passed to the service")
    void list_page_3() throws Exception {
        mockMvc.perform(get("/jobs/list").param("page", "3"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(jobService).findByFilter(any(JobFilter.class), captor.capture());
        assertThat(captor.getValue().getPageNumber()).isEqualTo(3);
    }

    @Test
    @DisplayName("Query parameters are bound into the filter, stored in session")
    void list_bindsFilterAndStoresInSession() throws Exception {
        mockMvc.perform(get("/jobs/list").param("keyword", "java")) // use a real JobFilter field
                .andExpect(status().isOk())
                .andExpect(request().sessionAttribute("filter", hasProperty("keyword", is("java"))));

        ArgumentCaptor<JobFilter> captor = ArgumentCaptor.forClass(JobFilter.class);
        verify(jobService).findByFilter(captor.capture(), any(Pageable.class));
        assertThat(captor.getValue().getKeyword()).isEqualTo("java");
    }
    @Test
    @DisplayName("An existing filter in the session is reused on the next request")
    void list_reusesSessionFilter() throws Exception {
        JobFilter saved = new JobFilter();
        saved.setKeyword("java");

        mockMvc.perform(get("/jobs/list").sessionAttr("filter", saved))
                .andExpect(status().isOk());

        ArgumentCaptor<JobFilter> captor = ArgumentCaptor.forClass(JobFilter.class);
        verify(jobService).findByFilter(captor.capture(), any(Pageable.class));
        assertThat(captor.getValue()).isSameAs(saved);
    }
    @Test
    @DisplayName("clear=true replaces the session filter with an empty one")
    void list_clearResetsFilter() throws Exception {
        JobFilter saved = new JobFilter();
        saved.setKeyword("java");

        mockMvc.perform(get("/jobs/list").param("clear", "true").sessionAttr("filter", saved))
                .andExpect(status().isOk());

        ArgumentCaptor<JobFilter> captor = ArgumentCaptor.forClass(JobFilter.class);
        verify(jobService).findByFilter(captor.capture(), any(Pageable.class));
        assertThat(captor.getValue()).isNotSameAs(saved);
        assertThat(captor.getValue().getKeyword()).isNull();
    }

    @Test
    @DisplayName("Anonymous users can browse jobs")
    void list_anonymous() throws Exception {
        mockMvc.perform(get("/jobs/list"))
                .andExpect(status().isOk());
    }
    @Test
    @WithMockUser(roles = "CANDIDATE")
    @DisplayName("Logged-in users can browse jobs")
    void list_loggedIn() throws Exception {
        mockMvc.perform(get("/jobs/list"))
                .andExpect(status().isOk())
                .andExpect(view().name("job/jobs"));
    }

    @Test
    @DisplayName("Applying a filter stores it in the session and redirects to the list")
    void applyFilter() throws Exception {
        mockMvc.perform(post("/jobs/list").with(csrf()).param("keyword", "java"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/jobs/list"))
                .andExpect(request().sessionAttribute("filter", hasProperty("keyword", is("java"))));

        verifyNoInteractions(jobService);
    }
    @Test
    @DisplayName("Applying a filter without a CSRF token is rejected")
    void applyFilter_noCsrf() throws Exception {
        mockMvc.perform(post("/jobs/list").param("keyword", "java"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Job details show the job")
    void job_found() throws Exception {
        Job job = job(JobStatus.OPEN);
        when(jobService.findById(5)).thenReturn(job);

        mockMvc.perform(get("/jobs/5"))
                .andExpect(status().isOk())
                .andExpect(view().name("job/job-details"))
                .andExpect(model().attribute("job", job))
                .andExpect(model().attribute("image", "/images/user-icon.png"))
                .andExpect(model().attribute("currentPage", "jobs"));
    }
    @Test
    @DisplayName("A non-numeric id returns 400")
    void job_badId() throws Exception {
        mockMvc.perform(get("/jobs/abc"))
                .andExpect(status().isBadRequest());

        verify(jobService, never()).findById(anyInt());
    }

    private Job job(JobStatus status) {
        Job job = new Job();
        job.setStatus(status);
        return job;
    }
}