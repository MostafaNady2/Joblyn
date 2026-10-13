package com.example.jobboard.service;

import com.example.jobboard.entity.*;
import com.example.jobboard.enums.ApplicationStatus;
import com.example.jobboard.enums.JobStatus;
import com.example.jobboard.exception.ApplicationNotFoundException;
import com.example.jobboard.exception.BadRequestException;
import com.example.jobboard.exception.JobNotFoundException;
import com.example.jobboard.repository.ApplicationRepository;
import com.example.jobboard.repository.JobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {
    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private UserService userService;
    @Mock
    private JobRepository jobRepository;
    @InjectMocks
    private ApplicationService applicationService;

    private List<Application> applications;

    private static final String LETTER = "x".repeat(100);

    @BeforeEach
    void setUp() {
        Application a1 = new Application();
        a1.setId(1);
        a1.setCoverLetter("Sample cover letter");
        a1.setStatus(ApplicationStatus.PENDING);

        Application a2 = new Application();
        a2.setId(2);
        a2.setCoverLetter("Another cover letter");
        a2.setStatus(ApplicationStatus.ACCEPTED);

        Application a3 = new Application();
        a3.setId(3);
        a3.setCoverLetter("Third cover letter");
        a3.setStatus(ApplicationStatus.REJECTED);
        applications = List.of(a1, a2, a3);
    }

    // ---------- existing tests ----------

    @Test
    @DisplayName("Get all applications")
    void getAllApplications() {
        when(applicationRepository.findAll()).thenReturn(applications);
        List<Application> result = applicationService.findAll();

        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
        assertThat(result).hasSizeGreaterThan(0);
        assertThat(result).hasSizeLessThan(4);
        assertThat(result).hasSize(3);
    }

    @Test
    @DisplayName("Find application by id")
    void findApplicationById() {
        when(applicationRepository.findById(1)).thenReturn(Optional.of(applications.get(0)));
        Application result = applicationService.findById(1);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getCoverLetter()).isEqualTo("Sample cover letter");
        assertThat(result.getStatus()).isEqualTo(ApplicationStatus.PENDING);
    }

    @Test
    @DisplayName("Find application by id not found")
    void findApplicationByIdNotFound() {
        when(applicationRepository.findById(999)).thenReturn(Optional.empty());
        Application result = applicationService.findById(999);
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Save application")
    void saveApplication() {
        when(applicationRepository.save(any())).thenReturn(applications.get(1));

        Application result = applicationService.save(applications.get(1));

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2);
        assertThat(result.getCoverLetter()).isEqualTo("Another cover letter");
        assertThat(result.getStatus()).isEqualTo(ApplicationStatus.ACCEPTED);
    }

    @Test
    @DisplayName("Get applications by candidate id")
    void getByCandidateId() {
        CandidateProfile candidate = new CandidateProfile();
        candidate.setId(1);

        for (Application application : applications) {
            application.setCandidate(candidate);
        }
        when(applicationRepository.findByCandidateId(candidate.getId())).thenReturn(applications);

        List<Application> result = applicationService.findByCandidateId(candidate.getId());
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getCandidate().getId()).isEqualTo(candidate.getId());
    }

    @Test
    @DisplayName("Throw exception when application not found")
    void decideApplicationStatusThrowApplicationNotFoundException() {
        when(applicationRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> applicationService.decide(99, ApplicationStatus.ACCEPTED))
                .isInstanceOf(ApplicationNotFoundException.class);
    }

    @Test
    @DisplayName("Throw exception when current user does not own the job")
    void decideApplicationStatusThrowsAccessDeniedException() {
        String email = "authenticated@123gmail.com";

        User user = new User();
        user.setEmail(email);
        EmployerProfile employer = new EmployerProfile();
        employer.setUser(user);
        Job job = new Job();
        job.setEmployer(employer);
        Application application = applications.get(0);
        application.setJob(job);

        when(applicationRepository.findById(1)).thenReturn(Optional.of(application));
        when(userService.getCurrentUser()).thenReturn("anonymous@gmail.com");

        assertThatThrownBy(() -> applicationService.decide(1, ApplicationStatus.ACCEPTED))
                .isInstanceOf(AccessDeniedException.class);

        verify(applicationRepository, never()).save(application);
    }

    @Test
    @DisplayName("Decide application status successfully")
    void decideApplicationStatusSuccessfully() {
        String email = "authenticated@123gmail.com";

        User user = new User();
        user.setEmail(email);
        EmployerProfile employer = new EmployerProfile();
        employer.setUser(user);
        Job job = new Job();
        job.setEmployer(employer);
        Application application = applications.get(0);
        application.setJob(job);

        when(applicationRepository.findById(1)).thenReturn(Optional.of(application));
        when(userService.getCurrentUser()).thenReturn("authenticated@123gmail.com");
        when(applicationRepository.save(application)).thenReturn(application);

        applicationService.decide(1, ApplicationStatus.ACCEPTED);

        verify(applicationRepository, times(1)).save(application);
        assertThat(application.getStatus()).isEqualTo(ApplicationStatus.ACCEPTED);
    }

    // ---------- apply ----------
    @Test
    @DisplayName("Apply saves a PENDING application for an open job with no deadline")
    void apply_success() {
        CandidateProfile candidate = candidate();
        Job job = jobWith(JobStatus.OPEN, null);
        when(jobRepository.findById(5)).thenReturn(Optional.of(job));
        when(applicationRepository.existsByJobIdAndCandidateId(5, 7)).thenReturn(false);
        when(applicationRepository.save(any(Application.class))).thenAnswer(inv -> inv.getArgument(0));

        Application result = applicationService.apply(5, candidate, LETTER);

        ArgumentCaptor<Application> captor = ArgumentCaptor.forClass(Application.class);
        verify(applicationRepository).save(captor.capture());
        Application saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(ApplicationStatus.PENDING);
        assertThat(saved.getCoverLetter()).isEqualTo(LETTER);
        assertThat(saved.getJob()).isSameAs(job);
        assertThat(saved.getCandidate()).isSameAs(candidate);
        assertThat(saved.getAppliedAt()).isNotNull();
        assertThat(result).isSameAs(saved);
    }

    @Test
    @DisplayName("Apply works when the deadline is in the future")
    void apply_futureDeadline() {
        when(jobRepository.findById(5))
                .thenReturn(Optional.of(jobWith(JobStatus.OPEN, LocalDateTime.now().plusDays(3))));
        when(applicationRepository.save(any(Application.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThatCode(() -> applicationService.apply(5, candidate(), LETTER))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Apply throws JobNotFoundException when the job does not exist")
    void apply_jobNotFound() {
        when(jobRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> applicationService.apply(99, candidate(), LETTER))
                .isInstanceOf(JobNotFoundException.class);

        verifyNoInteractions(applicationRepository);
    }

    @ParameterizedTest
    @EnumSource(value = JobStatus.class, names = {"PENDING", "REJECTED", "CLOSED"})
    @DisplayName("Apply throws JobNotFoundException when the job is not open")
    void apply_jobNotOpen(JobStatus status) {
        when(jobRepository.findById(5)).thenReturn(Optional.of(jobWith(status, null)));

        assertThatThrownBy(() -> applicationService.apply(5, candidate(), LETTER))
                .isInstanceOf(JobNotFoundException.class);

        verifyNoInteractions(applicationRepository);
    }

    @Test
    @DisplayName("Apply throws BadRequestException when the deadline has passed")
    void apply_deadlinePassed() {
        when(jobRepository.findById(5))
                .thenReturn(Optional.of(jobWith(JobStatus.OPEN, LocalDateTime.now().minusDays(1))));

        assertThatThrownBy(() -> applicationService.apply(5, candidate(), LETTER))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("deadline");

        verifyNoInteractions(applicationRepository);
    }

    @Test
    @DisplayName("Apply throws BadRequestException when the candidate already applied")
    void apply_duplicate() {
        when(jobRepository.findById(5)).thenReturn(Optional.of(jobWith(JobStatus.OPEN, null)));
        when(applicationRepository.existsByJobIdAndCandidateId(5, 7)).thenReturn(true);

        assertThatThrownBy(() -> applicationService.apply(5, candidate(), LETTER))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already applied");

        verify(applicationRepository, never()).save(any());
    }
    @ParameterizedTest
    @ValueSource(ints = {0, 1, 99})
    @DisplayName("Apply rejects a cover letter shorter than 100 characters")
    void apply_letterTooShort(int length) {
        assertThatThrownBy(() -> applicationService.apply(5, candidate(), "x".repeat(length)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("100 characters");

        verifyNoInteractions(jobRepository, applicationRepository);
    }

    @Test
    @DisplayName("Apply rejects a cover letter that is only whitespace")
    void apply_letterBlank() {
        assertThatThrownBy(() -> applicationService.apply(5, candidate(), " ".repeat(150)))
                .isInstanceOf(BadRequestException.class);

        verifyNoInteractions(jobRepository, applicationRepository);
    }

    @Test
    @DisplayName("Apply rejects a null cover letter")
    void apply_letterNull() {
        assertThatThrownBy(() -> applicationService.apply(5, candidate(), null))
                .isInstanceOf(BadRequestException.class);

        verifyNoInteractions(jobRepository, applicationRepository);
    }

    @Test
    @DisplayName("Apply accepts a cover letter of exactly 100 characters")
    void apply_letterExactly100() {
        when(jobRepository.findById(5)).thenReturn(Optional.of(jobWith(JobStatus.OPEN, null)));
        when(applicationRepository.save(any(Application.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThatCode(() -> applicationService.apply(5, candidate(), "x".repeat(100)))
                .doesNotThrowAnyException();
    }
    private Job jobWith(JobStatus status, LocalDateTime deadline) {
        Job job = new Job();
        job.setId(5);
        job.setStatus(status);
        job.setDeadline(deadline);
        return job;
    }

    private CandidateProfile candidate() {
        CandidateProfile candidate = new CandidateProfile();
        candidate.setId(7);
        return candidate;
    }
}
