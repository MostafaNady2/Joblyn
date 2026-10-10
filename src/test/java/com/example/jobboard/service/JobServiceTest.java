package com.example.jobboard.service;

import com.example.jobboard.dto.JobFilter;
import com.example.jobboard.entity.*;
import com.example.jobboard.enums.JobStatus;
import com.example.jobboard.enums.SkillLevel;
import com.example.jobboard.exception.JobNotFoundException;
import com.example.jobboard.repository.JobRepository;
import com.example.jobboard.util.JobSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {
    @Mock
    private JobRepository jobRepository;
    @InjectMocks
    private JobService jobService;

    private List<Job> jobs;

    @BeforeEach
    void setUp() {
        Location location = new Location();
        location.setCity("Cairo");
        location.setCountry("Egypt");

        JobSkill skill = new JobSkill();
        skill.setSkillName("Java");
        skill.setRequiredLevel(SkillLevel.INTERMEDIATE);

        Job j1= new Job();
        j1.setId(1);
        j1.setTitle("Java Developer");
        j1.setDescription("Java Developer");
        j1.setSalary(10000d);
        j1.setStatus(JobStatus.OPEN);
        j1.setEmployer(null);
        j1.setLocation(location);
        j1.setSkills(Set.of(skill));

        Job j2 = new Job();
        j2.setId(2);
        j2.setTitle("Python Developer");
        j2.setDescription("Python Developer");
        j2.setSalary(12000d);
        j2.setStatus(JobStatus.PENDING);
        j2.setEmployer(null);
        j2.setLocation(location);
        j2.setSkills(Set.of(skill));

        Job j3 = new Job();
        j3.setId(3);
        j3.setTitle("IOS Developer");
        j3.setDescription("IOS Developer");
        j3.setSalary(10000d);
        j3.setStatus(JobStatus.CLOSED);
        j3.setEmployer(null);
        j3.setLocation(location);
        j3.setSkills(Set.of(skill));

        jobs = List.of(j1, j2, j3);
    }

    @Test
    @DisplayName("Get job by id")
    void getJobById() {
        when(jobRepository.findById(1)).thenReturn(Optional.of(jobs.get(0)));
        Job job = jobService.findById(1);
        assertThat(job).isNotNull();
        assertThat(job).isEqualTo(jobs.get(0));
    }


    @Test
    @DisplayName("Get non-existent job")
    void getNonExistentJob() {
        when(jobRepository.findById(999)).thenReturn(Optional.empty());
        Job job = jobService.findById(999);
        assertThat(job).isNull();
    }

    @Test
    @DisplayName("Get jobs by status")
    void getJobsByStatus() {
        when(jobRepository.findByStatus(JobStatus.PENDING)).thenReturn(List.of(jobs.get(1)));
        List<Job> jobsByStatus = jobService.findByStatus(JobStatus.PENDING);
        assertThat(jobsByStatus).isNotNull();
        assertThat(jobsByStatus).hasSize(1);
        assertThat(jobsByStatus).isEqualTo(List.of(jobs.get(1)));
    }

    @Test
    @DisplayName("Get all jobs")
    void getAllJobs() {
        Page<Job> page = new PageImpl<>(jobs);
        when(jobRepository.findAll(PageRequest.of(0,5))).thenReturn(page);
        Page<Job> allJobs = jobService.findAll(PageRequest.of(0,5));

        assertThat(allJobs).isNotNull();
        assertThat(allJobs).hasSize(3);
        assertThat(allJobs.getTotalElements()).isEqualTo(3);
        assertThat(allJobs.getTotalPages()).isEqualTo(1);
        assertThat(allJobs.getNumber()).isEqualTo(0);
    }

    @Test
    @DisplayName("Get jobs by filter")
    void getJobsByFilter() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<Job> page = new PageImpl<>(jobs, pageable, jobs.size());
        Specification<Job> spec = (root,query,cb)->null;

        when(jobRepository.findAll(spec, pageable)).thenReturn(page);
        try(MockedStatic<JobSpecification> mock = mockStatic(JobSpecification.class)) {
            mock.when(()->JobSpecification.filter(any())).thenReturn(spec);
            Page<Job> result = jobService.findByFilter(new JobFilter(), pageable);
            assertThat(result.getTotalElements()).isEqualTo(jobs.size());
        }
    }

    @Test
    @DisplayName("should create a new job")
    void createJob() {
        EmployerProfile employer = new EmployerProfile();
        Job job = new Job();
        job.setTitle("Software Engineer");
        job.setDescription("We are looking for a software engineer to join our team.");
        job.setLocation(jobs.get(0).getLocation());
        job.setStatus(JobStatus.PENDING);
        job.setSalary(100000d);

        jobService.createJob(job,employer);

        verify(jobRepository,times(1)).save(job);
    }

    @Test
    @DisplayName("should change job status to CLOSED")
    void closeJob() {
        EmployerProfile employer = new EmployerProfile();
        Job job = new Job();
        job.setTitle("Software Engineer");
        job.setDescription("We are looking for a software engineer to join our team.");
        job.setLocation(jobs.get(0).getLocation());
        job.setStatus(JobStatus.PENDING);
        job.setSalary(100000d);

        jobService.closeJob(job, employer);

        verify(jobRepository,times(1)).save(job);
    }

    @Test
    @DisplayName("should throw JobNotFoundException when job is not found")
    void approveNonExistingJob() {
        Job job = new Job();
        job.setId(100);
        job.setStatus(JobStatus.PENDING);

        when(jobRepository.findById(100)).thenReturn(Optional.empty());

        assertThatThrownBy(()->jobService.approveJob(100)).isInstanceOf(JobNotFoundException.class);

        assertThat(job.getStatus()).isEqualTo(JobStatus.PENDING);

    }

    @Test
    @DisplayName("should change job status to OPEN")
    void approveExistingJob() {
        Job job = new Job();
        job.setId(100);
        job.setStatus(JobStatus.PENDING);

        when(jobRepository.findById(100)).thenReturn(Optional.of(job));
        jobService.approveJob(100);

        assertThat(job.getStatus()).isEqualTo(JobStatus.OPEN);
    }

    @Test
    @DisplayName("should change job status to REJECTED")
    void rejectJob() {
        Job job = new Job();
        job.setId(100);
        job.setStatus(JobStatus.PENDING);

        when(jobRepository.findById(100)).thenReturn(Optional.of(job));
        jobService.rejectJob(100);

        assertThat(job.getStatus()).isEqualTo(JobStatus.REJECTED);
    }

    @Test
    @DisplayName("should throw AccessDeniedException when job is null")
    void getOwnJobsIfJobISNull() {
        String email = "authenticated@gmail.com";
        User user = new User();
        EmployerProfile employer = new EmployerProfile();
        Job job = new Job();
        job.setId(100);
        user.setEmail(email);
        employer.setUser(user);
        job.setEmployer(employer);

        when(jobRepository.findByIdWithApplications(100)).thenReturn(null);

        assertThatThrownBy(() -> jobService.findOwnedJob(100, "authenticated@gmail.com")).isInstanceOf(AccessDeniedException.class);

    }

    @Test
    @DisplayName("should throw AccessDeniedException when job employer is null")
    void getOwnJobsIfEmployerIsNull() {
        String email = "authenticated@gmail.com";
        User user = new User();
        EmployerProfile employer = new EmployerProfile();
        Job job = new Job();
        job.setId(100);
        user.setEmail(email);
        employer.setUser(user);
        job.setEmployer(null);

        when(jobRepository.findByIdWithApplications(100)).thenReturn(job);

        assertThatThrownBy(() -> jobService.findOwnedJob(100, "authenticated@gmail.com")).isInstanceOf(AccessDeniedException.class);

    }

    @Test
    @DisplayName("should throw AccessDeniedException when user is not authenticated")
    void getOwnJobsIfUserNotAuthenticated() {
        String email = "authenticated@gmail.com";
        User user = new User();
        EmployerProfile employer = new EmployerProfile();
        Job job = new Job();
        job.setId(100);
        user.setEmail(email);
        employer.setUser(user);
        job.setEmployer(employer);

        when(jobRepository.findByIdWithApplications(100)).thenReturn(job);

        assertThatThrownBy(() -> jobService.findOwnedJob(100, "anonymous@gmail.com")).isInstanceOf(AccessDeniedException.class);
    }
    @Test
    @DisplayName("should return job when user is authenticated")
    void getOwnJobsSuccessfully() {
        String email = "authenticated@gmail.com";
        User user = new User();
        EmployerProfile employer = new EmployerProfile();
        Job job = new Job();
        job.setId(100);
        user.setEmail(email);
        employer.setUser(user);
        job.setEmployer(employer);

        when(jobRepository.findByIdWithApplications(100)).thenReturn(job);

       Job result = jobService.findOwnedJob(100, "authenticated@gmail.com");

       assertThat(result).isEqualTo(job);
       assertThat(result.getEmployer().getUser().getEmail()).isEqualTo(email);
       assertThat(result.getId()).isEqualTo(100);
    }

    @Test
    @DisplayName("should count jobs by status")
    void countByStatus() {
        jobs.get(0).setStatus(JobStatus.OPEN);
        jobs.get(1).setStatus(JobStatus.OPEN);
        jobs.get(2).setStatus(JobStatus.CLOSED);

        when(jobRepository.countJobsByStatus(JobStatus.OPEN)).thenReturn(2L);
        when(jobRepository.countJobsByStatus(JobStatus.CLOSED)).thenReturn(1L);

        long openCount = jobService.countJobsByStatus(JobStatus.OPEN);
        long closedCount = jobService.countJobsByStatus(JobStatus.CLOSED);

        assertThat(openCount).isEqualTo(2L);
        assertThat(closedCount).isEqualTo(1L);
    }

    @Test
    @DisplayName("should get jobs by employer")
    void getByEmployer() {
        User user = new User();
        EmployerProfile employer = new EmployerProfile();
        Job job = new Job();
        job.setId(100);
        employer.setId(1);
        employer.setUser(user);
        job.setEmployer(employer);

        when(jobRepository.findByEmployerIdWithApplications(1)).thenReturn(List.of(job));

        List<Job> result = jobService.findByEmployerId(1);

        assertThat(result).contains(job);
        assertThat(result.get(0).getId()).isEqualTo(100);
    }

    @Test
    @DisplayName("should find recent jobs")
    void findRecentJobs() {
        when(jobRepository.findByPostedAtAfterOrderByPostedAtDesc(any())).thenReturn(jobs);

        List<Job> result = jobService.findRecentJobs(3);

        assertThat(result).hasSize(3);
        assertThat(result).containsAll(jobs);
    }

    @Test
    @DisplayName("should find all skills")
    void findAllSkills() {
        when(jobRepository.findAllSkills()).thenReturn(List.of("Java","C++"));

        List<String> result = jobService.findAllSkills();
        assertThat(result).hasSize(2);
        assertThat(result).contains("Java","C++");
    }

}