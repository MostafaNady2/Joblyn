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
import com.example.jobboard.mapper.UserMapper;
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
    private final UserMapper userMapper;

    public RegistrationService(UserRepository userRepository, CandidateRepository candidateRepository,
                               EmployerRepository employerRepository, PasswordEncoder passwordEncoder, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.candidateRepository = candidateRepository;
        this.employerRepository = employerRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    @Transactional
    public void register(UserForm form) {
        if (userRepository.findByEmail(form.getEmail()).isPresent()) {
            throw new EmailAlreadyExistException("Email already registered: " + form.getEmail());
        }

        User user = userMapper.toUser(form, passwordEncoder.encode(form.getPassword()));

        switch (user.getRole()) {
            case CANDIDATE -> candidateRepository.save(userMapper.toCandidateProfile(form, user));
            case EMPLOYER -> employerRepository.save(userMapper.toEmployerProfile(form, user));
            default -> throw new BadRequestException("invalid role: " + user.getRole().name());
        }

    }
}
