package com.example.jobboard.entity;

import com.example.jobboard.enums.ApplicationStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

@Data

@Entity
@Table(name = "application")
public class Application {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String coverLetter;

    @Column(name = "applied_at",nullable = false)
    private LocalDateTime appliedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status",nullable = false)
    private ApplicationStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id",nullable = false)
    @ToString.Exclude
    private CandidateProfile candidate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id",nullable = false)
    @ToString.Exclude
    private Job job;

}
