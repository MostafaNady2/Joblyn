package com.example.jobboard.service;

import com.example.jobboard.entity.Application;
import com.example.jobboard.entity.CandidateProfile;
import com.example.jobboard.entity.Job;
import com.example.jobboard.enums.ApplicationStatus;
import com.example.jobboard.enums.JobStatus;
import com.example.jobboard.exception.ApplicationNotFoundException;
import com.example.jobboard.exception.BadRequestException;
import com.example.jobboard.exception.JobNotFoundException;
import com.example.jobboard.repository.ApplicationRepository;
import com.example.jobboard.repository.JobRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final UserService userService;
    private final JobRepository jobRepository;

    @Autowired
    public ApplicationService(ApplicationRepository applicationRepository, UserService userService, JobRepository jobRepository) {
        this.applicationRepository = applicationRepository;
        this.userService = userService;
        this.jobRepository = jobRepository;
    }

    public List<Application> findAll() {
        return applicationRepository.findAll();
    }

    public Application findById(int id) {
        return applicationRepository.findById(id).orElse(null);
    }

    @Transactional
    public Application save(Application application) {
        return applicationRepository.save(application);
    }

    public List<Application> findByCandidateId(int candidateId) {
        return applicationRepository.findByCandidateId(candidateId);
    }

    @Transactional
    public Application decide(int applicationId, ApplicationStatus status) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ApplicationNotFoundException("Application not found"));

        String userEmail = (String) userService.getCurrentUser();
        if (!userEmail.equals(application.getJob().getEmployer().getUser().getEmail())) {
            throw new AccessDeniedException("You are not authorized to decide this application");
        }
        application.setStatus(status);
        return applicationRepository.save(application);
    }
    @Transactional
    public Application apply(int jobId, CandidateProfile candidate, String coverLetter) {
        if (coverLetter == null || coverLetter.strip().length() < 100) {
            throw new BadRequestException("Cover letter must be at least 100 characters long");
        }
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new JobNotFoundException("Job with id : " + jobId + " not found"));
        if (job.getStatus() != JobStatus.OPEN) {
            throw new JobNotFoundException("Job with id : " + jobId + " not found");
        }
        if (job.getDeadline() != null && job.getDeadline().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("The deadline for this job has passed");
        }
        if (applicationRepository.existsByJobIdAndCandidateId(jobId, candidate.getId())) {
            throw new BadRequestException("You have already applied to this job");
        }

        Application application = new Application();
        application.setCoverLetter(coverLetter);
        application.setStatus(ApplicationStatus.PENDING);
        application.setAppliedAt(LocalDateTime.now());
        application.setJob(job);
        application.setCandidate(candidate);
        return applicationRepository.save(application);
    }
}
