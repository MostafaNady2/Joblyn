package com.example.jobboard.service;

import com.example.jobboard.dto.UserForm;
import com.example.jobboard.entity.CandidateProfile;
import com.example.jobboard.entity.EmployerProfile;
import com.example.jobboard.entity.Location;
import com.example.jobboard.entity.User;
import com.example.jobboard.enums.CandidateWorkType;
import com.example.jobboard.enums.Role;
import com.example.jobboard.exception.BadRequestException;
import com.example.jobboard.exception.EmailAlreadyExistException;
import com.example.jobboard.repository.CandidateRepository;
import com.example.jobboard.repository.EmployerRepository;
import com.example.jobboard.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class RegistrationService {

    private final UserRepository userRepository;
    private final CandidateRepository candidateRepository;
    private final EmployerRepository employerRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(UserRepository userRepository, CandidateRepository candidateRepository,
                               EmployerRepository employerRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.candidateRepository = candidateRepository;
        this.employerRepository = employerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void register(UserForm form) {
        if (userRepository.findByEmail(form.getEmail()).isPresent()) {
            throw new EmailAlreadyExistException("Email already registered: " + form.getEmail());
        }

        User user = new User();
        user.setEmail(form.getEmail());
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());

        if ("candidate".equalsIgnoreCase(form.getRole())) {
            user.setRole(Role.CANDIDATE);
            CandidateProfile candidate = new CandidateProfile();
            candidate.setFirstName(form.getFirstName());
            candidate.setLastName(form.getLastName());
            candidate.setWorkType(CandidateWorkType.HYBRID);
            candidate.setUser(user);
            candidateRepository.save(candidate); // cascade persists the user
        } else if ("employer".equalsIgnoreCase(form.getRole())) {
            user.setRole(Role.EMPLOYER);
            EmployerProfile employer = new EmployerProfile();
            employer.setFirstName(form.getFirstName());
            employer.setLastName(form.getLastName());
            employer.setCompanyName(form.getCompanyName());
            Location location = new Location();
            location.setCountry(form.getCountry());
            location.setCity(form.getCity());
            employer.setLocation(location);
            employer.setUser(user);
            employerRepository.save(employer); // cascade persists the user
        } else {
            throw new BadRequestException("Unknown role: " + form.getRole());
        }
    }
}
