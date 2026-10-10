package com.example.jobboard.repository;

import com.example.jobboard.entity.*;
import com.example.jobboard.enums.JobStatus;
import com.example.jobboard.enums.JobWorkNature;
import com.example.jobboard.enums.Role;
import com.example.jobboard.enums.SkillLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
 @AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // remove if using embedded H2
class JobRepositoryTest {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private TestEntityManager em;

    private Job job;

    @BeforeEach
    void setup() {
        User user = new User();
        user.setEmail("ali@gmail.com");
        user.setPassword("$2a$encoded");
        user.setRole(Role.EMPLOYER);
        em.persist(user);

        EmployerProfile employer = new EmployerProfile();
        employer.setUser(user);
        employer.setCompanyName("a7a");
        employer.setFirstName("fathy");
        employer.setLastName("3ashor");
        em.persist(employer);

        job = new Job();
        job.setEmployer(employer);
        job.setTitle("java developer");
        job.setWorkNature(JobWorkNature.FULL_TIME);
        job.setStatus(JobStatus.OPEN);
        em.persist(job);

        em.flush();
    }

    @Test
    @DisplayName("should find jobs by employer with applications")
    void findByEmployerWithApplications() {
        List<Job> result = jobRepository.findByEmployerIdWithApplications(job.getEmployer().getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getApplications()).hasSize(0);
        assertThat(result.get(0).getStatus()).isEqualTo(JobStatus.OPEN);
    }

    @Test
    @DisplayName("should find job by id with applications")
    void findByIdWithApplications() {
        Job result = jobRepository.findByIdWithApplications(job.getId());

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(JobStatus.OPEN);
    }

    @Test
    @DisplayName("should find jobs by posted at desc")
    void findByPostedAtDesc() {
        List<Job> result = jobRepository.findByPostedAtAfterOrderByPostedAtDesc(LocalDateTime.now().minusDays(1));

        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(JobStatus.OPEN);
    }

    @Test
    @DisplayName("should close expired jobs")
    void closeExpiredJobs() {
        job.setDeadline(LocalDateTime.now().minusDays(1));
        em.merge(job);
        em.flush();

        int count = jobRepository.closeExpiredJobs(JobStatus.CLOSED);
        assertThat(count).isEqualTo(1);

        Job result = jobRepository.findById(job.getId()).orElseThrow();
        assertThat(result.getStatus()).isEqualTo(JobStatus.CLOSED);
    }

    @Test
    @DisplayName("should find all skills")
    void findAllSkills() {
        JobSkill s1 = new JobSkill();
        s1.setSkillName("Java");
        s1.setRequiredLevel(SkillLevel.ADVANCED);
        job.setSkills(Set.of(s1));
        em.persist(job);
        em.flush();

        List<String> result = jobRepository.findAllSkills();
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo("Java");
    }
}