package com.example.jobboard.entity;

import com.example.jobboard.enums.CandidateWorkType;
import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "candidate")
public class CandidateProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "first_name",nullable = false)
    private String firstName;

    @Column(name = "last_name",nullable = false)
    private String lastName;

    @Column(name = "profile_photo")
    private String profilePhoto;

    @Column(name = "resume")
    private String resume;

    @Enumerated(EnumType.STRING)
    @Column(name = "work_type",nullable = false)
    private CandidateWorkType workType;

    @ElementCollection
    @CollectionTable(name = "candidate_skills", joinColumns = @JoinColumn(name = "candidate_id"))
    private Set<CandidateSkill> skills = new HashSet<>();

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id",nullable = false)
    private User user;
}
