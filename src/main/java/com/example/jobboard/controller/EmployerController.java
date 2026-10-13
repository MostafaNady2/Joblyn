package com.example.jobboard.controller;

import com.example.jobboard.dto.EmployerForm;
import com.example.jobboard.dto.JobForm;
import com.example.jobboard.entity.*;
import com.example.jobboard.enums.ApplicationStatus;
import com.example.jobboard.enums.JobStatus;
import com.example.jobboard.exception.ResourceNotFoundException;
import com.example.jobboard.service.ApplicationService;
import com.example.jobboard.service.CandidateService;
import com.example.jobboard.service.EmployerService;
import com.example.jobboard.service.JobService;
import com.example.jobboard.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/employer")
public class EmployerController {

    private final EmployerService employerService;
    private final JobService jobService;
    private final ApplicationService applicationService;
    private final CandidateService candidateService;
    private final UserService userService;

    @Autowired
    public EmployerController(EmployerService employerService, JobService jobService,
                              ApplicationService applicationService, CandidateService candidateService,
                              UserService userService) {
        this.employerService = employerService;
        this.jobService = jobService;
        this.applicationService = applicationService;
        this.candidateService = candidateService;
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        EmployerProfile employer = (EmployerProfile) userService.getCurrentUser();
        if (employer == null) {
            return "redirect:/login";
        }
        List<Job> empJobs = jobService.findByEmployerId(employer.getId());
        model.addAttribute("companyName", employer.getCompanyName());
        model.addAttribute("totalListingsCount", empJobs.size());
        model.addAttribute("pendingListingsCount",
                empJobs.stream().filter(job -> job.getStatus() == JobStatus.PENDING).count());
        model.addAttribute("publishedListingsCount",
                empJobs.stream().filter(job -> job.getStatus() == JobStatus.OPEN).count());
        model.addAttribute("rejectedListingsCount",
                empJobs.stream().filter(job -> job.getStatus() == JobStatus.REJECTED).count());
        model.addAttribute("notPublishedJobs",
                empJobs.stream().filter(j -> j.getStatus() != JobStatus.OPEN).toList());
        model.addAttribute("employerProfile", employer);
        model.addAttribute("image", userService.getProfilePhotoUrl());
        return "employer/dashboard";
    }

    @GetMapping("/applications/{id}")
    public String applications(@PathVariable("id") int jobId, Model model) {
        EmployerProfile employer = (EmployerProfile) userService.getCurrentUser();
        Job job = jobService.findOwnedJob(jobId, employer.getUser().getEmail());
        model.addAttribute("companyName", employer.getCompanyName());
        model.addAttribute("selectedJob", job);
        model.addAttribute("jobApplications", job.getApplications());
        model.addAttribute("employerProfile", employer);
        model.addAttribute("image", userService.getProfilePhotoUrl());
        return "employer/applications";
    }

    @PostMapping("/applications/{id}/accept")
    public String acceptApplication(@PathVariable("id") int applicationId) {
        Application application = applicationService.decide(applicationId, ApplicationStatus.ACCEPTED);
        return "redirect:/employer/applications/" + application.getJob().getId();
    }

    @PostMapping("/applications/{id}/reject")
    public String rejectApplication(@PathVariable("id") int applicationId) {
        Application application = applicationService.decide(applicationId, ApplicationStatus.REJECTED);
        return "redirect:/employer/applications/" + application.getJob().getId();
    }

    @GetMapping("/jobs/new")
    public String newJob(Model model, Authentication auth) {
        EmployerProfile employer = (EmployerProfile) userService.getCurrentUser();
        model.addAttribute("companyName", employer.getCompanyName());
        model.addAttribute("job", new JobForm());
        model.addAttribute("employerProfile", employer);
        model.addAttribute("image", userService.getProfilePhotoUrl());
        return "employer/new-job";
    }

    @PostMapping("/jobs/new")
    public String saveJob(@Valid @ModelAttribute("job") JobForm jobForm,
                          BindingResult bindingResult, Model model) {
        EmployerProfile employer = (EmployerProfile) userService.getCurrentUser();
        if (bindingResult.hasErrors()) {
            model.addAttribute("companyName", employer.getCompanyName());
            model.addAttribute("employerProfile", employer);
            model.addAttribute("image",userService.getProfilePhotoUrl());
            return "employer/new-job";
        }

        Job job = new Job();
        job.setTitle(jobForm.getTitle());
        job.setDeadline(jobForm.getDeadline());
        job.setLocation(jobForm.getLocation());
        job.setSalary(jobForm.getSalary());
        job.setWorkNature(jobForm.getWorkNature());
        job.setDescription(jobForm.getDescription());
        job.getSkills().addAll(jobForm.getSkills());

        jobService.createJob(job, employer);
        return "redirect:/employer/dashboard";
    }

    @PostMapping("/jobs/{id}/close")
    public String closeJob(@PathVariable("id") int jobId, Authentication auth) {
        EmployerProfile employer = (EmployerProfile) userService.getCurrentUser();
        Job job = jobService.findOwnedJob(jobId, employer.getUser().getEmail());
        jobService.closeJob(job, employer);
        return "redirect:/employer/jobs";
    }

    @GetMapping("/candidate/resume/{id}")
    public String candidateResume(@PathVariable("id") int candidateId) {
        CandidateProfile candidate = candidateService.findById(candidateId);
        if (candidate != null && candidate.getResume() != null) {
            return "redirect:/resumes/candidate/" + candidate.getId() + "/" + candidate.getResume();
        }else{
            throw new ResourceNotFoundException("candidate resume not found");
        }
    }

    @GetMapping("/profile")
    public String showProfile(Model model) {
        EmployerProfile employer = (EmployerProfile) userService.getCurrentUser();
        EmployerForm form = new EmployerForm();
        form.setFirstName(employer.getFirstName());
        form.setLastName(employer.getLastName());
        form.setCompanyName(employer.getCompanyName());
        form.setCity(employer.getLocation().getCity());
        form.setCountry(employer.getLocation().getCountry());
        form.setProfilePhoto(employer.getProfilePhoto());

        model.addAttribute("employerProfile", form);
        model.addAttribute("image", userService.getProfilePhotoUrl());
        return "employer/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@ModelAttribute("employerProfile") EmployerForm form) throws IOException {
        EmployerProfile employer = (EmployerProfile) userService.getCurrentUser();

        employer.setFirstName(form.getFirstName());
        employer.setLastName(form.getLastName());
        employer.setCompanyName(form.getCompanyName());
        Location location = new Location();
        location.setCity(form.getCity());
        location.setCountry(form.getCountry());
        employer.setLocation(location);

        employerService.saveProfilePhoto(form.getPhotoFile(), employer);
        employerService.save(employer);

        return "redirect:/employer/profile";
    }

    @GetMapping("/jobs")
    public String myJobs(Model model, Authentication auth) {
        EmployerProfile employer = (EmployerProfile) userService.getCurrentUser();
        model.addAttribute("employerProfile", employer);
        model.addAttribute("jobs", jobService.findByEmployerId(employer.getId()));
        model.addAttribute("image", userService.getProfilePhotoUrl());
        return "employer/jobs";
    }

    @GetMapping("/jobs/{id}")
    public String showJob(@PathVariable("id") int jobId, Model model, Authentication auth) {
        Job job = jobService.findOwnedJob(jobId, auth.getName());
        model.addAttribute("employer", job.getEmployer());
        model.addAttribute("job", job);
        model.addAttribute("image", userService.getProfilePhotoUrl());
        return "employer/job";
    }
}