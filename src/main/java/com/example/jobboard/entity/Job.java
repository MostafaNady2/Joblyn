package com.example.jobboard.entity;

import com.example.jobboard.enums.JobStatus;
import com.example.jobboard.enums.JobWorkNature;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.List;


@Data
@Entity
@Table(name = "job")
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "title",nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "work_nature",nullable = false)
    private JobWorkNature workNature;

    @Embedded
    private Location location;

    @Column(name = "salary")
    private Double salary;

    @Enumerated(EnumType.STRING)
    @Column(name = "status",nullable = false)
    private JobStatus status;

    @ElementCollection
    @CollectionTable(name = "job_skills", joinColumns = @JoinColumn(name = "job_id"))
    @ToString.Exclude
    private Set<JobSkill> skills = new HashSet<>();

    @CreationTimestamp
    @Column(name = "posted_at",nullable = false)
    private LocalDateTime postedAt;

    @Column(name = "deadline")
    private LocalDateTime deadline;

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Application> applications = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employer_id",nullable = false)
    @ToString.Exclude
    private EmployerProfile employer;
}
