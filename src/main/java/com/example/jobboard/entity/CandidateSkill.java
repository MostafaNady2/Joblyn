package com.example.jobboard.entity;

import com.example.jobboard.enums.SkillLevel;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Embeddable
public class CandidateSkill {

    @Column(name = "skill_name",nullable = false)
    private String skillName;

    @Enumerated(EnumType.STRING)
    @Column(name = "experience_level",nullable = false)
    private SkillLevel experienceLevel;
}
