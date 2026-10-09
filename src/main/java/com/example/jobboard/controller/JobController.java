package com.example.jobboard.controller;

import com.example.jobboard.dto.JobFilter;
import com.example.jobboard.entity.Job;
import com.example.jobboard.repository.SkillRepository;
import com.example.jobboard.service.JobService;
import com.example.jobboard.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;

@Controller
@RequestMapping("/jobs")
@SessionAttributes("filter")
public class JobController {
    private final JobService jobService;
    private final SkillRepository skillRepository;
    private final UserService userService;

    @Autowired
    public JobController(JobService jobService,SkillRepository skillRepository,UserService userService) {
        this.jobService = jobService;
        this.skillRepository = skillRepository;
        this.userService = userService;
    }

    @ModelAttribute("filter")
    public JobFilter initFilter(){
        return new JobFilter();
    }

    @GetMapping("/list")
    public String listJobs(@ModelAttribute("filter") JobFilter filter, @RequestParam(value = "page",defaultValue = "0") int page, @RequestParam(value = "clear",defaultValue = "false") boolean clear, Model model, SessionStatus sessionStatus) {
        if(clear){
            sessionStatus.isComplete();
            filter = new JobFilter();
            model.addAttribute("filter",filter);
        }
        String image = userService.getProfilePhotoUrl();
        Pageable pageable = PageRequest.of(page,5, Sort.by("postedAt").descending());
        Page<Job> openedJobs = jobService.findByFilter(filter, pageable);

        model.addAttribute("jobs", openedJobs);
        model.addAttribute("allSkills", skillRepository.findAllSkillNames());
        model.addAttribute("image", image);
        model.addAttribute("currentPage", "jobs");
        return "job/jobs";
    }
    @PostMapping("/list")
    public String applyFilter(@ModelAttribute("filter") JobFilter filter) {
        return "redirect:/jobs/list";
    }

    @GetMapping("/{id}")
    public String getJob(@PathVariable int id, Model model) {
        String image = userService.getProfilePhotoUrl();
        model.addAttribute("job", jobService.findById(id));
        model.addAttribute("image", image);
        model.addAttribute("currentPage", "jobs");
        return "job/job-details";
    }


}
