package com.example.jobboard.dto;

import com.example.jobboard.entity.JobSkill;
import com.example.jobboard.entity.Location;
import com.example.jobboard.enums.JobWorkNature;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class JobForm {

    @NotBlank(message = "Job title is required")
    @Size(min = 10, message = "Job title must be at least 10 characters long")
    private String title;

    @NotNull(message = "Description is required")
    @Size(min = 50, message = "Job description must be at least 50 characters long")
    private String description;

    @NotNull(message = "Work nature is required")
    private JobWorkNature workNature;

    private Location location;

    @Positive(message = "Salary must be greater than zero")
    private Double salary;

    @NotNull(message = "Deadline is required")
    @FutureOrPresent(message = "Deadline must be in the future or present")
    private LocalDateTime deadline;

    @Valid
    private List<JobSkill> skills = new ArrayList<>();
}
