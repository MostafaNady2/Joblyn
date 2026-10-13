package com.example.jobboard.controller;

import com.example.jobboard.entity.Application;
import com.example.jobboard.entity.CandidateProfile;
import com.example.jobboard.entity.Job;
import com.example.jobboard.enums.ApplicationStatus;
import com.example.jobboard.enums.CandidateWorkType;
import com.example.jobboard.enums.JobStatus;
import com.example.jobboard.exception.BadRequestException;
import com.example.jobboard.exception.JobNotFoundException;
import com.example.jobboard.security.ApplicationSecurityConfig;
import com.example.jobboard.security.CustomUserDetailsService;
import com.example.jobboard.security.LoginSuccessHandler;
import com.example.jobboard.service.ApplicationService;
import com.example.jobboard.service.CandidateService;
import com.example.jobboard.service.JobService;
import com.example.jobboard.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(CandidateController.class)
@Import(ApplicationSecurityConfig.class)
@WithMockUser(roles = "CANDIDATE")
class CandidateControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private CandidateService candidateService;
    @MockitoBean
    private ApplicationService applicationService;
    @MockitoBean
    private JobService jobService;
    @MockitoBean
    private UserService userService;
    // for security
    @MockitoBean
    CustomUserDetailsService customUserDetailsService;
    @MockitoBean
    LoginSuccessHandler loginSuccessHandler;

    private CandidateProfile candidate;

    @BeforeEach
    void setUp() {
        candidate = new CandidateProfile();
        candidate.setId(7);
        candidate.setFirstName("Mostafa");
        candidate.setLastName("Nady");
        candidate.setWorkType(CandidateWorkType.HYBRID);
        when(userService.getCurrentUser()).thenReturn(candidate);
        when(userService.getProfilePhotoUrl()).thenReturn("/images/user-icon.png");
    }

    @Test
    @DisplayName("Dashboard counts applications by status and lists only the pending ones")
    void dashboard() throws Exception {
        Application pending1 = application(ApplicationStatus.PENDING);
        Application pending2 = application(ApplicationStatus.PENDING);
        Application accepted = application(ApplicationStatus.ACCEPTED);
        Application rejected = application(ApplicationStatus.REJECTED);
        when(applicationService.findByCandidateId(7))
                .thenReturn(List.of(pending1, accepted, pending2, rejected));

        mockMvc.perform(get("/candidate/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("candidate/dashboard"))
                .andExpect(model().attribute("underReviewCount", 2L))
                .andExpect(model().attribute("acceptedCount", 1L))
                .andExpect(model().attribute("rejectedCount", 1L))
                .andExpect(model().attribute("applications", List.of(pending1, pending2)))
                .andExpect(model().attribute("candidateName", "Mostafa Nady"))
                .andExpect(model().attribute("candidateProfile", candidate))
                .andExpect(model().attribute("image", "/images/user-icon.png"));
    }

    @Test
    @DisplayName("Dashboard works for a candidate with no applications")
    void dashboard_noApplications() throws Exception {
        when(applicationService.findByCandidateId(7)).thenReturn(List.of());

        mockMvc.perform(get("/candidate/dashboard"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("underReviewCount", 0L))
                .andExpect(model().attribute("applications", empty()));
    }

    @Test
    @DisplayName("Profile page pre-fills the form from the current candidate")
    void showProfile() throws Exception {
        when(candidateService.findAllSkills()).thenReturn(List.of("Java", "Spring"));

        mockMvc.perform(get("/candidate/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("candidate/profile"))
                .andExpect(model().attribute("candidateProfile", allOf(
                        hasProperty("firstName", is("Mostafa")),
                        hasProperty("lastName", is("Nady")),
                        hasProperty("workType", is(CandidateWorkType.HYBRID)))))
                .andExpect(model().attribute("profile", candidate))
                .andExpect(model().attribute("skillsSuggestions", List.of("Java", "Spring")))
                .andExpect(model().attribute("image", "/images/user-icon.png"));
    }

    // ---------- GET /candidate/applications ----------

    @Test
    @DisplayName("Applications page lists all applications and counts the accepted ones")
    void showApplications() throws Exception {
        Application pending = application(ApplicationStatus.PENDING);
        Application accepted = application(ApplicationStatus.ACCEPTED);
        when(applicationService.findByCandidateId(7)).thenReturn(List.of(pending, accepted));

        mockMvc.perform(get("/candidate/applications"))
                .andExpect(status().isOk())
                .andExpect(view().name("candidate/applications"))
                .andExpect(model().attribute("applications", List.of(pending, accepted)))
                .andExpect(model().attribute("acceptedApplications", 1L))
                .andExpect(model().attribute("candidateProfile", candidate));
    }

    // ---------- POST /candidate/profile ----------

    @Test
    @DisplayName("Updating the profile saves the new data and both uploaded files")
    void updateProfile_withFiles() throws Exception {
        MockMultipartFile photo = new MockMultipartFile("profilePhoto", "me.png", "image/png", "img".getBytes());
        MockMultipartFile resume = new MockMultipartFile("resume", "cv.pdf", "application/pdf", "pdf".getBytes());

        mockMvc.perform(multipart("/candidate/profile")
                        .file(photo).file(resume)
                        .param("firstName", "Ahmed")
                        .param("lastName", "Ali")
                        .param("workType", "REMOTE")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/candidate/dashboard"));

        assertThat(candidate.getFirstName()).isEqualTo("Ahmed");
        assertThat(candidate.getLastName()).isEqualTo("Ali");
        assertThat(candidate.getWorkType()).isEqualTo(CandidateWorkType.REMOTE);
        verify(candidateService).saveProfilePhoto(any(MultipartFile.class), eq(candidate));
        verify(candidateService).saveProfileResume(any(MultipartFile.class), eq(candidate));
        verify(candidateService).save(candidate);
    }

    @Test
    @DisplayName("Updating the profile works without any uploaded file")
    void updateProfile_withoutFiles() throws Exception {
        mockMvc.perform(multipart("/candidate/profile")
                        .param("firstName", "Ahmed")
                        .param("lastName", "Ali")
                        .param("workType", "REMOTE")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        verify(candidateService).save(candidate);
    }

    @Test
    @DisplayName("Apply passes the job id, current candidate and cover letter to the service")
    void apply_valid() throws Exception {
        String letter = "x".repeat(100);

        mockMvc.perform(post("/candidate/jobs/5/apply").with(csrf()).param("coverLetter", letter))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/candidate/dashboard"));

        verify(applicationService).apply(5, candidate, letter);
    }

    @Test
    @DisplayName("A missing cover letter parameter returns 400 and does not call the service")
    void apply_missingParam() throws Exception {
        mockMvc.perform(post("/candidate/jobs/5/apply").with(csrf()))
                .andExpect(status().isBadRequest());

        verify(applicationService, never()).apply(anyInt(), any(), any());
    }

    @Test
    @DisplayName("A JobNotFoundException from the service returns 404")
    void apply_jobNotFound() throws Exception {
        when(applicationService.apply(eq(99), any(), any()))
                .thenThrow(new JobNotFoundException("not found"));

        mockMvc.perform(post("/candidate/jobs/99/apply").with(csrf())
                        .param("coverLetter", "x".repeat(100)))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "The deadline for this job has passed",
            "You have already applied to this job",
            "Cover letter must be at least 100 characters long"})
    @DisplayName("A BadRequestException from the service returns 400 with its message")
    void apply_badRequestFromService(String message) throws Exception {
        when(applicationService.apply(eq(5), any(), any()))
                .thenThrow(new BadRequestException(message));

        mockMvc.perform(post("/candidate/jobs/5/apply").with(csrf())
                        .param("coverLetter", "x".repeat(100)))
                .andExpect(status().isBadRequest())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("status", 400))
                .andExpect(model().attribute("message", message));
    }
    @ParameterizedTest
    @ValueSource(strings = {"/candidate/dashboard", "/candidate/profile", "/candidate/applications"})
    @WithMockUser(roles = "EMPLOYER")
    @DisplayName("Candidate pages are forbidden for employers")
    void pages_forbiddenForEmployer(String url) throws Exception {
        mockMvc.perform(get(url))
                .andExpect(status().isForbidden());

        verifyNoInteractions(applicationService, candidateService);
    }

    @Test
    @WithMockUser(roles = "EMPLOYER")
    @DisplayName("Applying is forbidden for employers")
    void apply_forbiddenForEmployer() throws Exception {
        mockMvc.perform(post("/candidate/jobs/5/apply").with(csrf())
                        .param("coverLetter", "x".repeat(100)))
                .andExpect(status().isForbidden());

        verify(applicationService, never()).apply(anyInt(), any(), any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/candidate/dashboard", "/candidate/profile", "/candidate/applications"})
    @WithAnonymousUser
    @DisplayName("Anonymous users are redirected to the login page")
    void pages_anonymous(String url) throws Exception {
        mockMvc.perform(get(url))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verifyNoInteractions(applicationService, candidateService);
    }

    @Test
    @DisplayName("Applying without a CSRF token is rejected")
    void apply_noCsrf() throws Exception {
        mockMvc.perform(post("/candidate/jobs/5/apply").param("coverLetter", "x".repeat(100)))
                .andExpect(status().isForbidden());

        verify(applicationService, never()).apply(anyInt(), any(), any());
    }

    private Application application(ApplicationStatus status) {
        Application application = new Application();
        application.setStatus(status);
        application.setJob(job(JobStatus.OPEN));
        application.setCandidate(candidate);
        return application;
    }

    private Job job(JobStatus status) {
        Job job = new Job();
        job.setStatus(status);
        return job;
    }
}