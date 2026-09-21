package com.example.jobboard.dto;

import com.example.jobboard.entity.CandidateSkill;
import com.example.jobboard.enums.CandidateWorkType;
import lombok.Data;
import org.springframework.util.AutoPopulatingList;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Web-bound form for candidate profile editing.
 * Carries no id and no User reference — the profile is always resolved
 * from the authenticated principal, never from client input.
 */
@Data
public class CandidateForm {
    private String firstName;
    private String lastName;
    private MultipartFile profilePhoto;
    private MultipartFile resume;
    private CandidateWorkType workType;
    private List<CandidateSkill> skills = new AutoPopulatingList<>(CandidateSkill.class);
}
