package com.example.jobboard.entity;

import com.example.jobboard.enums.SkillLevel;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Embeddable
public class JobSkill {

    @Column(name = "skill_name",nullable = false)
    private String skillName;

    @Enumerated(EnumType.STRING)
    @Column(name = "required_level" , nullable = false)
    private SkillLevel requiredLevel;
}
