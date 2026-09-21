package com.example.jobboard.service;

import com.example.jobboard.dto.JobFilter;
import com.example.jobboard.entity.EmployerProfile;
import com.example.jobboard.entity.Job;
import com.example.jobboard.enums.JobStatus;
import com.example.jobboard.exception.JobNotFoundException;
import com.example.jobboard.repository.JobRepository;
import com.example.jobboard.repository.SkillRepository;
import com.example.jobboard.util.JobSpecification;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class JobService {
    private final JobRepository jobRepository;
    private final SkillRepository skillRepository;

    @Autowired
    public JobService(JobRepository jobRepository, SkillRepository skillRepository) {
        this.jobRepository = jobRepository;
        this.skillRepository = skillRepository;
    }

    public Page<Job> findAll(Pageable pageable) {
        return jobRepository.findAll(pageable);
    }

    public Job findById(int id) {
        return jobRepository.findById(id).orElse(null);
    }

    public List<Job> findByStatus(JobStatus status) {
        return jobRepository.findByStatus(status);
    }

    public Page<Job> findByFilter(JobFilter filter, Pageable pageable) {
        Specification<Job> spec = JobSpecification.filter(filter);
        return jobRepository.findAll(spec, pageable);

    }

    @Transactional
    public Job save(Job job) {
        return jobRepository.save(job);
    }

    @Transactional
    public void delete(Job job) {
        jobRepository.delete(job);
    }

    @Transactional
    public void deleteById(int id) {
        jobRepository.deleteById(id);
    }

    @Transactional
    public void createJob(Job job, EmployerProfile employer) {
        job.setEmployer(employer);
        job.setPostedAt(LocalDateTime.now());
        job.setStatus(JobStatus.PENDING);
        jobRepository.save(job);
    }

    @Transactional
    public void closeJob(Job job,EmployerProfile employer) {
        job.setStatus(JobStatus.CLOSED);
        jobRepository.save(job);
    }

    @Transactional
    public void approveJob(int jobId) {
        transition(jobId, JobStatus.OPEN);
    }

    @Transactional
    public void rejectJob(int jobId) {
        transition(jobId, JobStatus.REJECTED);
    }

    private void transition(int jobId, JobStatus status) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new JobNotFoundException("Job with id : " + jobId +" not found"));
        job.setStatus(status);
    }

    public Job findOwnedJob(int jobId, String employerEmail) {
        Job job = jobRepository.findByIdWithApplications(jobId);
        if (job == null || job.getEmployer() == null
                || !job.getEmployer().getUser().getEmail().equals(employerEmail)) {
            throw new AccessDeniedException("You do not own this job");
        }
        return job;
    }

    public long countJobsByStatus(JobStatus status) {
        return jobRepository.countJobsByStatus(status);
    }

    public List<Job> findByEmployerId(int employerId) {
        return jobRepository.findByEmployerIdWithApplications(employerId);
    }

    public List<Job> findRecentJobs(int daysLeft) {
        return jobRepository.findByPostedAtAfterOrderByPostedAtDesc(LocalDateTime.now().minusDays(daysLeft));
    }

    @Scheduled(cron = "0 0 12 * * *")
    @Transactional
    public void closeExpiredJobs() {
        int count = jobRepository.closeExpiredJobs(JobStatus.CLOSED);
        // log.info("Closed {} expired jobs", count);
    }

}
