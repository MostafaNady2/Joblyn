package com.example.jobboard.controller;

import com.example.jobboard.entity.Application;
import com.example.jobboard.entity.CandidateProfile;
import com.example.jobboard.entity.EmployerProfile;
import com.example.jobboard.entity.Job;
import com.example.jobboard.entity.Location;
import com.example.jobboard.entity.User;
import com.example.jobboard.enums.ApplicationStatus;
import com.example.jobboard.enums.JobStatus;
import com.example.jobboard.enums.SkillLevel;
import com.example.jobboard.exception.ApplicationNotFoundException;
import com.example.jobboard.security.ApplicationSecurityConfig;
import com.example.jobboard.security.CustomUserDetailsService;
import com.example.jobboard.security.LoginSuccessHandler;
import com.example.jobboard.service.ApplicationService;
import com.example.jobboard.service.CandidateService;
import com.example.jobboard.service.EmployerService;
import com.example.jobboard.service.JobService;
import com.example.jobboard.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployerController.class)
@Import(ApplicationSecurityConfig.class)
@WithMockUser(username = "owner@test.com", roles = "EMPLOYER")
class EmployerControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private EmployerService employerService;
    @MockitoBean
    private JobService jobService;
    @MockitoBean
    private ApplicationService applicationService;
    @MockitoBean
    private CandidateService candidateService;
    @MockitoBean
    private UserService userService;
    // for security
    @MockitoBean
    CustomUserDetailsService customUserDetailsService;
    @MockitoBean
    LoginSuccessHandler loginSuccessHandler;

    private EmployerProfile employer;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setEmail("owner@test.com");

        Location location = new Location();
        location.setCity("Cairo");
        location.setCountry("Egypt");

        employer = new EmployerProfile();
        employer.setId(3);
        employer.setFirstName("Mostafa");
        employer.setLastName("Nady");
        employer.setCompanyName("Acme");
        employer.setLocation(location);
        employer.setUser(user);

        when(userService.getCurrentUser()).thenReturn(employer);
        when(userService.getProfilePhotoUrl()).thenReturn("/images/user-icon.png");
    }

    // ---------- GET /employer/dashboard ----------

    @Test
    @DisplayName("Dashboard counts listings by status and lists the ones that are not published")
    void dashboard() throws Exception {
        Job pending = job(1, JobStatus.PENDING);
        Job open = job(2, JobStatus.OPEN);
        Job rejected = job(3, JobStatus.REJECTED);
        Job closed = job(4, JobStatus.CLOSED);
        when(jobService.findByEmployerId(3)).thenReturn(List.of(pending, open, rejected, closed));

        mockMvc.perform(get("/employer/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("employer/dashboard"))
                .andExpect(model().attribute("companyName", "Acme"))
                .andExpect(model().attribute("totalListingsCount", 4))
                .andExpect(model().attribute("pendingListingsCount", 1L))
                .andExpect(model().attribute("publishedListingsCount", 1L))
                .andExpect(model().attribute("rejectedListingsCount", 1L))
                .andExpect(model().attribute("notPublishedJobs", List.of(pending, rejected, closed)))
                .andExpect(model().attribute("employerProfile", employer))
                .andExpect(model().attribute("image", "/images/user-icon.png"));
    }

    @Test
    @DisplayName("Dashboard works for an employer with no jobs")
    void dashboard_noJobs() throws Exception {
        when(jobService.findByEmployerId(3)).thenReturn(List.of());

        mockMvc.perform(get("/employer/dashboard"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("totalListingsCount", 0))
                .andExpect(model().attribute("pendingListingsCount", 0L));
    }

    @Test
    @DisplayName("Dashboard redirects to login when there is no current employer")
    void dashboard_noCurrentUser() throws Exception {
        when(userService.getCurrentUser()).thenReturn(null);

        mockMvc.perform(get("/employer/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verifyNoInteractions(jobService);
    }

    // ---------- GET /employer/applications/{id} ----------

    @Test
    @DisplayName("Applications page shows the job and its applications for the owner")
    void applications_owner() throws Exception {
        Job job = job(5, JobStatus.OPEN);
        when(jobService.findOwnedJob(5, "owner@test.com")).thenReturn(job);

        mockMvc.perform(get("/employer/applications/5"))
                .andExpect(status().isOk())
                .andExpect(view().name("employer/applications"))
                .andExpect(model().attribute("companyName", "Acme"))
                .andExpect(model().attribute("selectedJob", job))
                .andExpect(model().attributeExists("jobApplications"))   // Job must initialize its applications list
                .andExpect(model().attribute("employerProfile", employer))
                .andExpect(model().attribute("image", "/images/user-icon.png"));
    }

    @Test
    @DisplayName("Applications page is forbidden when the job belongs to another employer")
    void applications_notOwner() throws Exception {
        when(jobService.findOwnedJob(5, "owner@test.com")).thenThrow(new AccessDeniedException("not yours"));

        mockMvc.perform(get("/employer/applications/5"))
                .andExpect(status().isForbidden());
    }

    // ---------- POST /employer/applications/{id}/accept|reject ----------

    @Test
    @DisplayName("Accepting an application redirects to the applications page of its job")
    void accept() throws Exception {
        Application application = new Application();
        application.setJob(job(5, JobStatus.OPEN));
        when(applicationService.decide(11, ApplicationStatus.ACCEPTED)).thenReturn(application);

        mockMvc.perform(post("/employer/applications/11/accept").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employer/applications/5"));

        verify(applicationService).decide(11, ApplicationStatus.ACCEPTED);
    }

    @Test
    @DisplayName("Rejecting an application redirects to the applications page of its job")
    void reject() throws Exception {
        Application application = new Application();
        application.setJob(job(5, JobStatus.OPEN));
        when(applicationService.decide(11, ApplicationStatus.REJECTED)).thenReturn(application);

        mockMvc.perform(post("/employer/applications/11/reject").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employer/applications/5"));

        verify(applicationService).decide(11, ApplicationStatus.REJECTED);
    }

    @ParameterizedTest
    @ValueSource(strings = {"accept", "reject"})
    @DisplayName("Deciding an application of another employer's job is forbidden")
    void decide_notOwner(String action) throws Exception {
        when(applicationService.decide(eq(11), any())).thenThrow(new AccessDeniedException("not yours"));

        mockMvc.perform(post("/employer/applications/11/" + action).with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deciding an unknown application returns 404")
    void decide_unknown() throws Exception {
        when(applicationService.decide(eq(99), any())).thenThrow(new ApplicationNotFoundException("not found"));

        mockMvc.perform(post("/employer/applications/99/accept").with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deciding without a CSRF token is rejected")
    void decide_noCsrf() throws Exception {
        mockMvc.perform(post("/employer/applications/11/accept"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(applicationService);
    }

    // ---------- GET/POST /employer/jobs/new ----------

    @Test
    @DisplayName("New job page shows an empty form")
    void newJob() throws Exception {
        mockMvc.perform(get("/employer/jobs/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("employer/new-job"))
                .andExpect(model().attributeExists("job"))
                .andExpect(model().attribute("companyName", "Acme"))
                .andExpect(model().attribute("employerProfile", employer))
                .andExpect(model().attribute("image", "/images/user-icon.png"));
    }

    @Test
    @DisplayName("A valid job form creates the job for the current employer")
    void saveJob_valid() throws Exception {
        mockMvc.perform(jobPost())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employer/dashboard"));

        ArgumentCaptor<Job> captor = ArgumentCaptor.forClass(Job.class);
        verify(jobService).createJob(captor.capture(), eq(employer));
        Job created = captor.getValue();
        assertThat(created.getTitle()).isEqualTo("Backend Developer");
        assertThat(created.getWorkNature()).hasToString("FULL_TIME");
        assertThat(new BigDecimal(created.getSalary().toString())).isEqualByComparingTo("85000");
        assertThat(created.getLocation().getCity()).isEqualTo("Cairo");
        assertThat(created.getLocation().getCountry()).isEqualTo("Egypt");
        assertThat(created.getDeadline()).isEqualTo(LocalDateTime.of(2027, 1, 1, 23, 59));
        assertThat(created.getDescription()).startsWith("We are looking for a backend engineer");
        assertThat(created.getSkills()).extracting("skillName").containsExactly("Java");
        assertThat(created.getSkills()).extracting("requiredLevel").containsExactly(SkillLevel.EXPERT);
    }

    // The next three tests assume these rules exist on JobForm: a minimum description length
    // (the helper comment says 50), a @Future deadline, and an enum workNature.
    // If one fails because no error was reported, the form is missing that rule.

    @Test
    @DisplayName("A short description returns the form with an error")
    void saveJob_shortDescription() throws Exception {
        mockMvc.perform(jobPost("description", "too short"))
                .andExpect(status().isOk())
                .andExpect(view().name("employer/new-job"))
                .andExpect(model().attributeHasFieldErrors("job", "description"));

        verify(jobService, never()).createJob(any(), any());
    }

    @Test
    @DisplayName("A deadline in the past returns the form with an error")
    void saveJob_pastDeadline() throws Exception {
        mockMvc.perform(jobPost("deadline", "2020-01-01T10:00"))
                .andExpect(status().isOk())
                .andExpect(view().name("employer/new-job"))
                .andExpect(model().attributeHasFieldErrors("job", "deadline"));

        verify(jobService, never()).createJob(any(), any());
    }

    @Test
    @DisplayName("An unknown work nature returns the form with an error")
    void saveJob_invalidWorkNature() throws Exception {
        mockMvc.perform(jobPost("workNature", "NOT_A_TYPE"))
                .andExpect(status().isOk())
                .andExpect(view().name("employer/new-job"))
                .andExpect(model().attributeHasFieldErrors("job", "workNature"));

        verify(jobService, never()).createJob(any(), any());
    }

    @Test
    @DisplayName("A job form without a title returns the form with an error")
    void saveJob_blankTitle() throws Exception {
        mockMvc.perform(jobPost("title", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("employer/new-job"))
                .andExpect(model().attributeHasFieldErrors("job", "title"))
                .andExpect(model().attribute("companyName", "Acme"))
                .andExpect(model().attribute("employerProfile", employer));

        verify(jobService, never()).createJob(any(), any());
    }

    @Test
    @DisplayName("Posting a job without a CSRF token is rejected")
    void saveJob_noCsrf() throws Exception {
        mockMvc.perform(post("/employer/jobs/new").param("title", "Backend Developer"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(jobService);
    }

    @Test
    @DisplayName("Closing an owned job closes it and redirects to the jobs list")
    void closeJob_owner() throws Exception {
        Job job = job(5, JobStatus.OPEN);
        when(jobService.findOwnedJob(5, "owner@test.com")).thenReturn(job);

        mockMvc.perform(post("/employer/jobs/5/close").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employer/jobs"));

        verify(jobService).closeJob(job, employer);
    }

    @Test
    @DisplayName("Closing another employer's job is forbidden and closes nothing")
    void closeJob_notOwner() throws Exception {
        when(jobService.findOwnedJob(5, "owner@test.com")).thenThrow(new AccessDeniedException("not yours"));

        mockMvc.perform(post("/employer/jobs/5/close").with(csrf()))
                .andExpect(status().isForbidden());

        verify(jobService, never()).closeJob(any(), any());
    }

    @Test
    @DisplayName("Resume link redirects to the stored resume file")
    void candidateResume_found() throws Exception {
        CandidateProfile candidate = new CandidateProfile();
        candidate.setId(9);
        candidate.setResume("cv.pdf");
        when(candidateService.findById(9)).thenReturn(candidate);

        mockMvc.perform(get("/employer/candidate/resume/9"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/resumes/candidate/9/cv.pdf"));
    }

    @Test
    @DisplayName("Resume link returns 404 when the candidate has no resume")
    void candidateResume_noResume() throws Exception {
        CandidateProfile candidate = new CandidateProfile();
        candidate.setId(9);
        when(candidateService.findById(9)).thenReturn(candidate);

        mockMvc.perform(get("/employer/candidate/resume/9"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Resume link returns 404 when the candidate does not exist")
    void candidateResume_unknownCandidate() throws Exception {
        when(candidateService.findById(99)).thenReturn(null);

        mockMvc.perform(get("/employer/candidate/resume/99"))
                .andExpect(status().isNotFound());
    }


    @Test
    @DisplayName("Profile page pre-fills the form from the current employer")
    void showProfile() throws Exception {
        mockMvc.perform(get("/employer/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("employer/profile"))
                .andExpect(model().attribute("employerProfile", allOf(
                        hasProperty("firstName", is("Mostafa")),
                        hasProperty("lastName", is("Nady")),
                        hasProperty("companyName", is("Acme")),
                        hasProperty("city", is("Cairo")),
                        hasProperty("country", is("Egypt")))))
                .andExpect(model().attribute("image", "/images/user-icon.png"));
    }

    @Test
    @DisplayName("Updating the profile saves the new data and the uploaded photo")
    void updateProfile_withPhoto() throws Exception {
        MockMultipartFile photo = new MockMultipartFile("photoFile", "logo.png", "image/png", "img".getBytes());

        mockMvc.perform(multipart("/employer/profile")
                        .file(photo)
                        .param("firstName", "Ahmed")
                        .param("lastName", "Ali")
                        .param("companyName", "Vodafone")
                        .param("city", "Alexandria")
                        .param("country", "Egypt")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employer/profile"));

        assertThat(employer.getFirstName()).isEqualTo("Ahmed");
        assertThat(employer.getLastName()).isEqualTo("Ali");
        assertThat(employer.getCompanyName()).isEqualTo("Vodafone");
        assertThat(employer.getLocation().getCity()).isEqualTo("Alexandria");
        assertThat(employer.getLocation().getCountry()).isEqualTo("Egypt");
        verify(employerService).saveProfilePhoto(any(MultipartFile.class), eq(employer));
        verify(employerService).save(employer);
    }

    @Test
    @DisplayName("Updating the profile works without a photo")
    void updateProfile_withoutPhoto() throws Exception {
        mockMvc.perform(multipart("/employer/profile")
                        .param("firstName", "Ahmed")
                        .param("lastName", "Ali")
                        .param("companyName", "Globex")
                        .param("city", "Alexandria")
                        .param("country", "Egypt")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        verify(employerService).saveProfilePhoto(any(), eq(employer));
        verify(employerService).save(employer);
    }

    @Test
    @DisplayName("My jobs page lists the jobs of the current employer")
    void myJobs() throws Exception {
        Job job = job(5, JobStatus.OPEN);
        when(jobService.findByEmployerId(3)).thenReturn(List.of(job));

        mockMvc.perform(get("/employer/jobs"))
                .andExpect(status().isOk())
                .andExpect(view().name("employer/jobs"))
                .andExpect(model().attribute("jobs", List.of(job)))
                .andExpect(model().attribute("employerProfile", employer))
                .andExpect(model().attribute("image", "/images/user-icon.png"));
    }

    @Test
    @DisplayName("Job page shows an owned job")
    void showJob_owner() throws Exception {
        Job job = job(5, JobStatus.OPEN);
        when(jobService.findOwnedJob(5, "owner@test.com")).thenReturn(job);

        mockMvc.perform(get("/employer/jobs/5"))
                .andExpect(status().isOk())
                .andExpect(view().name("employer/job"))
                .andExpect(model().attribute("job", job))
                .andExpect(model().attribute("employer", employer))
                .andExpect(model().attribute("image", "/images/user-icon.png"));
    }

    @Test
    @DisplayName("Job page is forbidden for another employer's job")
    void showJob_notOwner() throws Exception {
        when(jobService.findOwnedJob(5, "owner@test.com")).thenThrow(new AccessDeniedException("not yours"));

        mockMvc.perform(get("/employer/jobs/5"))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/employer/dashboard", "/employer/jobs", "/employer/jobs/new", "/employer/jobs/5",
            "/employer/profile", "/employer/applications/5", "/employer/candidate/resume/9"})
    @WithMockUser(roles = "CANDIDATE")
    @DisplayName("Employer pages are forbidden for candidates")
    void pages_forbiddenForCandidate(String url) throws Exception {
        mockMvc.perform(get(url))
                .andExpect(status().isForbidden());

        verifyNoInteractions(jobService, applicationService, employerService, candidateService);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/employer/jobs/5/close", "/employer/jobs/new",
            "/employer/applications/11/accept", "/employer/applications/11/reject"})
    @WithMockUser(roles = "CANDIDATE")
    @DisplayName("Employer actions are forbidden for candidates")
    void actions_forbiddenForCandidate(String url) throws Exception {
        mockMvc.perform(post(url).with(csrf()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(jobService, applicationService, employerService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/employer/dashboard", "/employer/jobs", "/employer/jobs/new", "/employer/profile"})
    @WithAnonymousUser
    @DisplayName("Anonymous users are redirected to the login page")
    void pages_anonymous(String url) throws Exception {
        mockMvc.perform(get(url))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verifyNoInteractions(jobService, applicationService, employerService);
    }

    private MockHttpServletRequestBuilder jobPost(String... overrides) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("title", "Backend Developer");
        params.put("workNature", "FULL_TIME");
        params.put("salary", "85000");
        params.put("location.city", "Cairo");
        params.put("location.country", "Egypt");
        params.put("deadline", "2027-01-01T23:59");
        params.put("description",
                "We are looking for a backend engineer to design, build and maintain "
                        + "distributed services. You will work with Java, Spring and SQL.");  // > 50 chars
        params.put("skills[0].skillName", "Java");
        params.put("skills[0].requiredLevel", "EXPERT");

        // overrides = ["title", "Senior Dev", "salary", "120000",..]
        for (int i = 0; i < overrides.length; i += 2) {
            if (overrides[i + 1] == null) {
                params.remove(overrides[i]);
            } else {
                params.put(overrides[i], overrides[i + 1]);
            }
        }

        MockHttpServletRequestBuilder request = post("/employer/jobs/new").with(csrf());
        params.forEach((name, value) -> request.param(name, value));
        return request;
    }

    private Job job(int id, JobStatus status) {
        Job job = new Job();
        job.setId(id);
        job.setStatus(status);
        job.setEmployer(employer);
        return job;
    }
}