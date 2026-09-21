package com.example.jobboard.controller;

import com.example.jobboard.dto.JobFilter;
import com.example.jobboard.entity.CandidateProfile;
import com.example.jobboard.entity.EmployerProfile;
import com.example.jobboard.entity.Job;
import com.example.jobboard.entity.User;
import com.example.jobboard.enums.JobStatus;
import com.example.jobboard.enums.Role;
import com.example.jobboard.service.CandidateService;
import com.example.jobboard.service.EmployerService;
import com.example.jobboard.service.JobService;
import com.example.jobboard.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Controller
public class HomeController {

    private final UserService userService;
    private final JobService jobService;
    private final CandidateService candidateService;
    private final EmployerService employerService;

    @Autowired
    public HomeController(UserService userService, JobService jobService,CandidateService candidateService,EmployerService employerService) {
        this.userService = userService;
        this.jobService = jobService;
        this.candidateService = candidateService;
        this.employerService = employerService;
    }

    @GetMapping({"", "/", "/index", "/home"})
    public String home(Model model,Authentication auth) {
        List<Job> recentJobs = jobService.findRecentJobs(3).stream().filter(j->j.getStatus().equals(JobStatus.OPEN)).toList();
        String image = userService.getProfilePhotoUrl();
        model.addAttribute("image", image);
        model.addAttribute("recentJobs", recentJobs);
        model.addAttribute("rolesCount", jobService.countJobsByStatus(JobStatus.OPEN));
        model.addAttribute("employersCount", userService.countByRole(Role.EMPLOYER));
        model.addAttribute("currentPage", "home");

        return "index";
    }
}