package com.example.jobboard.service;

import com.example.jobboard.entity.*;
import com.example.jobboard.enums.ApplicationStatus;
import com.example.jobboard.exception.ApplicationNotFoundException;
import com.example.jobboard.repository.ApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {
    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private UserService userService;
    @InjectMocks
    private ApplicationService applicationService;

    private List<Application> applications;

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

        for(Application application : applications) {
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

        assertThatThrownBy(()->applicationService.decide(99,ApplicationStatus.ACCEPTED))
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

        assertThatThrownBy(()->applicationService.decide(1,ApplicationStatus.ACCEPTED))
                .isInstanceOf(AccessDeniedException.class);

        verify(applicationRepository,never()).save(application);
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

        applicationService.decide(1,ApplicationStatus.ACCEPTED);

        verify(applicationRepository,times(1)).save(application);
        assertThat(application.getStatus()).isEqualTo(ApplicationStatus.ACCEPTED);
    }


}