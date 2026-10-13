package com.example.jobboard.controller;

import com.example.jobboard.dto.CandidateForm;
import com.example.jobboard.entity.Application;
import com.example.jobboard.entity.CandidateProfile;
import com.example.jobboard.entity.Job;
import com.example.jobboard.enums.ApplicationStatus;
import com.example.jobboard.enums.JobStatus;
import com.example.jobboard.exception.BadRequestException;
import com.example.jobboard.exception.JobNotFoundException;
import com.example.jobboard.mapper.UserMapper;
import com.example.jobboard.service.ApplicationService;
import com.example.jobboard.service.CandidateService;
import com.example.jobboard.service.JobService;
import com.example.jobboard.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/candidate")
public class CandidateController {
    private final CandidateService candidateService;
    private final ApplicationService applicationService;
    private final JobService jobService;
    private final UserService userService;

    @Autowired
    public CandidateController(CandidateService candidateService, ApplicationService applicationService,
                               JobService jobService,UserService userService) {
        this.candidateService = candidateService;
        this.applicationService = applicationService;
        this.jobService = jobService;
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        CandidateProfile candidate = (CandidateProfile)userService.getCurrentUser();

        List<Application> applications = applicationService.findByCandidateId(candidate.getId());
        model.addAttribute("underReviewCount",
                applications.stream().filter(a -> a.getStatus() == ApplicationStatus.PENDING).count());
        model.addAttribute("acceptedCount",
                applications.stream().filter(a -> a.getStatus() == ApplicationStatus.ACCEPTED).count());
        model.addAttribute("rejectedCount",
                applications.stream().filter(a -> a.getStatus() == ApplicationStatus.REJECTED).count());
        model.addAttribute("applications",
                applications.stream().filter(a -> a.getStatus() == ApplicationStatus.PENDING).toList());
        model.addAttribute("candidateName", candidate.getFirstName() + " " + candidate.getLastName());
        model.addAttribute("candidateProfile", candidate);

        model.addAttribute("image", userService.getProfilePhotoUrl());
        return "candidate/dashboard";
    }

    @GetMapping("/profile")
    public String showProfile(Model model) {
        CandidateProfile profile = (CandidateProfile) userService.getCurrentUser();
        CandidateForm form = new CandidateForm();
        form.setFirstName(profile.getFirstName());
        form.setLastName(profile.getLastName());
        form.setWorkType(profile.getWorkType());
        if (profile.getSkills() != null) {
            form.getSkills().addAll(profile.getSkills());
        }

        model.addAttribute("candidateProfile", form); // the form backing bean
        model.addAttribute("profile", profile);       // the entity, for display-only data
        model.addAttribute("skillsSuggestions", candidateService.findAllSkills());
        model.addAttribute("image", userService.getProfilePhotoUrl());
        return "candidate/profile";
    }

    @GetMapping("/applications")
    public String showApplications(Model model) {
        CandidateProfile profile = (CandidateProfile) userService.getCurrentUser();

        List<Application> applications = applicationService.findByCandidateId(profile.getId());
        model.addAttribute("applications", applications);
        model.addAttribute("acceptedApplications",
                applications.stream().filter(a -> a.getStatus() == ApplicationStatus.ACCEPTED).count());
        model.addAttribute("candidateProfile", profile);
        model.addAttribute("image", userService.getProfilePhotoUrl());
        return "candidate/applications";
    }

    @PostMapping("/profile")
    public String updateProfile(@ModelAttribute("candidateProfile") CandidateForm form) throws IOException {
        CandidateProfile profile = (CandidateProfile) userService.getCurrentUser();

        profile.setFirstName(form.getFirstName());
        profile.setLastName(form.getLastName());
        profile.setWorkType(form.getWorkType());

        MultipartFile profilePhoto = form.getProfilePhoto();
        candidateService.saveProfilePhoto(profilePhoto, profile);

        MultipartFile resume = form.getResume();
        candidateService.saveProfileResume(resume, profile);

        profile.getSkills().clear();
        if (form.getSkills() != null) {
            profile.getSkills().addAll(form.getSkills());
        }

        candidateService.save(profile);
        return "redirect:/candidate/dashboard";
    }


    @PostMapping("/jobs/{id}/apply")
    public String applyForJob(@PathVariable("id") int jobId, @RequestParam("coverLetter") String coverLetter) {
        CandidateProfile candidate = (CandidateProfile) userService.getCurrentUser();
        applicationService.apply(jobId, candidate, coverLetter);
        return "redirect:/candidate/dashboard";
    }
}
