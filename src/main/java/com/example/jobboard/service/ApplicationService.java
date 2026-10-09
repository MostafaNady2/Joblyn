package com.example.jobboard.service;

import com.example.jobboard.entity.Application;
import com.example.jobboard.enums.ApplicationStatus;
import com.example.jobboard.exception.ApplicationNotFoundException;
import com.example.jobboard.repository.ApplicationRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final UserService userService;

    @Autowired
    public ApplicationService(ApplicationRepository applicationRepository, UserService userService) {
        this.applicationRepository = applicationRepository;
        this.userService = userService;
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
}
