package com.example.jobboard.service;

import com.example.jobboard.entity.EmployerProfile;
import com.example.jobboard.repository.EmployerRepository;
import com.example.jobboard.util.FileUploadUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Service
public class EmployerService {
    private final EmployerRepository employerRepository;

    @Autowired
    public EmployerService(EmployerRepository employerRepository) {
        this.employerRepository = employerRepository;
    }

    public List<EmployerProfile> findAll() {
        return employerRepository.findAll();
    }

    public EmployerProfile findById(int id) {
        return employerRepository.findById(id).orElse(null);
    }

    @Transactional
    public void save(EmployerProfile employer) {
        employerRepository.save(employer);
    }

    public void saveProfilePhoto(MultipartFile photoFile, EmployerProfile employer) throws IOException {
        if(photoFile != null && !photoFile.isEmpty()) {
            String photoName = StringUtils.cleanPath(Objects.requireNonNull(photoFile.getOriginalFilename()));
            employer.setProfilePhoto(photoName);
            String directory = "photos/employer/"+employer.getId();
            FileUploadUtil.saveFile(directory,employer.getProfilePhoto(),photoFile);
        }
    }
}
