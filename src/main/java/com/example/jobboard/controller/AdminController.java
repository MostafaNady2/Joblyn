package com.example.jobboard.controller;

import com.example.jobboard.enums.JobStatus;
import com.example.jobboard.enums.Role;
import com.example.jobboard.service.JobService;
import com.example.jobboard.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final JobService jobService;
    private final UserService userService;

    @Autowired
    public AdminController(JobService jobService, UserService userService) {
        this.jobService = jobService;
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("image",userService.getProfilePhotoUrl());
        model.addAttribute("pendingCount", jobService.countJobsByStatus(JobStatus.PENDING));
        model.addAttribute("publishedCount", jobService.countJobsByStatus(JobStatus.OPEN));
        model.addAttribute("rejectedCount", jobService.countJobsByStatus(JobStatus.REJECTED));
        model.addAttribute("employersCount", userService.countByRole(Role.EMPLOYER));
        model.addAttribute("pendingJobs", jobService.findByStatus(JobStatus.PENDING));

        return "admin/dashboard";
    }

    @PostMapping("/jobs/{id}/approve")
    public String approveJob(@PathVariable("id") int jobId) {
        jobService.approveJob(jobId);
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/jobs/{id}/reject")
    public String rejectJob(@PathVariable("id") int jobId) {
        jobService.rejectJob(jobId);
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/jobs")
    public String jobs(@RequestParam(name = "page",defaultValue = "0") int page, Model model) {
        Pageable pageable = PageRequest.of(page,5, Sort.by("postedAt").descending());
        model.addAttribute("jobs", jobService.findAll(pageable));
        model.addAttribute("image",userService.getProfilePhotoUrl());
        return "admin/jobs";
    }

    @GetMapping("/users")
    public String users(@RequestParam(name = "page",defaultValue = "0") int page,Model model) {
        Pageable pageable = PageRequest.of(page,5,Sort.by("id"));
        model.addAttribute("users", userService.findAll(pageable));
        model.addAttribute("image",userService.getProfilePhotoUrl());
        return "admin/users";
    }
}
