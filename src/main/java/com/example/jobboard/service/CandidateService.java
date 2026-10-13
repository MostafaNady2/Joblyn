package com.example.jobboard.service;


import com.example.jobboard.entity.Application;
import com.example.jobboard.entity.CandidateProfile;
import com.example.jobboard.entity.Job;
import com.example.jobboard.enums.ApplicationStatus;
import com.example.jobboard.enums.JobStatus;
import com.example.jobboard.exception.BadRequestException;
import com.example.jobboard.exception.JobNotFoundException;
import com.example.jobboard.repository.ApplicationRepository;
import com.example.jobboard.repository.CandidateRepository;
import com.example.jobboard.util.FileUploadUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class CandidateService {
    private final CandidateRepository candidateRepository;

    @Autowired
    public CandidateService(CandidateRepository candidateRepository) {
        this.candidateRepository = candidateRepository;
    }

    public List<CandidateProfile> findAll() {
        return candidateRepository.findAll();
    }

    public CandidateProfile findById(int id) {
        return candidateRepository.findById(id).orElse(null);
    }

    @Transactional
    public CandidateProfile save(CandidateProfile candidate) {
        return candidateRepository.save(candidate);
    }

    public void saveProfilePhoto(MultipartFile photo,CandidateProfile candidate) throws IOException {
        if(photo != null && !photo.isEmpty()){
            String photoName = StringUtils.cleanPath(Objects.requireNonNull(photo.getOriginalFilename()));
            candidate.setProfilePhoto(photoName);
            String directory="photos/candidate/"+candidate.getId();
            FileUploadUtil.saveFile(directory, candidate.getProfilePhoto(), photo);
        }
    }

    public void saveProfileResume(MultipartFile resume,CandidateProfile candidate) throws IOException {
        if(resume != null && !resume.isEmpty()){
            String resumeName = StringUtils.cleanPath(Objects.requireNonNull(resume.getOriginalFilename()));
            candidate.setResume(resumeName);
            String directory="resumes/candidate/"+candidate.getId();
            FileUploadUtil.saveFile(directory, candidate.getResume(), resume);
        }
    }

    public List<String> findAllSkills() {
        return candidateRepository.findAllSkills();
    }

}
