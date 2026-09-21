package com.example.jobboard.dto;

import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class JobFilter {
    private String keyword;
    private List<String> workNature;
    private double minSalary;
    private Set<String> skills;
}
