package com.example.jobboard.service;

import com.example.jobboard.dto.UserForm;
import com.example.jobboard.entity.CandidateProfile;
import com.example.jobboard.entity.EmployerProfile;
import com.example.jobboard.entity.User;
import com.example.jobboard.enums.Role;
import com.example.jobboard.exception.BadRequestException;
import com.example.jobboard.exception.EmailAlreadyExistException;
import com.example.jobboard.mapper.UserMapper;
import com.example.jobboard.repository.CandidateRepository;
import com.example.jobboard.repository.EmployerRepository;
import com.example.jobboard.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private CandidateRepository candidateRepository;
    @Mock
    private EmployerRepository employerRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Spy
    private UserMapper mapper;
    @InjectMocks
    private RegistrationService registrationService;

    private UserForm userForm;

    @BeforeEach
    public void setUp() {
        userForm = new UserForm();
        userForm.setFirstName("mostafa");
        userForm.setLastName("nady");
        userForm.setEmail("mostafa2nady@gmail.com");
        userForm.setPassword("mostafa123");
        userForm.setRole("candidate");
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    public void registerExistingUser() {
        User user = new User();
        user.setEmail("mostafa2nady@gmail.com");
        when(userRepository.findByEmail("mostafa2nady@gmail.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> registrationService.register(userForm))
                .isInstanceOf(EmailAlreadyExistException.class);
        verify(candidateRepository, never()).save(any());
        verify(employerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should register new candidate successfully")
    public void registerNewCandidate() {
        userForm.setRole("candidate");

        when(userRepository.findByEmail("mostafa2nady@gmail.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("mostafa123")).thenReturn("$2a$encoded");

        registrationService.register(userForm);

        ArgumentCaptor<CandidateProfile> captor = ArgumentCaptor.forClass(CandidateProfile.class);

        verify(candidateRepository).save(captor.capture());
        CandidateProfile candidate = captor.getValue();

        assertThat(candidate.getFirstName()).isEqualTo("mostafa");
        assertThat(candidate.getUser().getEmail()).isEqualTo("mostafa2nady@gmail.com");
        assertThat(candidate.getUser().getPassword()).isEqualTo("$2a$encoded");
        assertThat(candidate.getUser().getRole()).isEqualTo(Role.CANDIDATE);
        verify(employerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should register new employer successfully")
    public void registerNewEmployer() {
        userForm.setRole("employer");
        userForm.setCompanyName("vodafone");

        when(userRepository.findByEmail("mostafa2nady@gmail.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("mostafa123")).thenReturn("$2a$encoded");

        registrationService.register(userForm);

        ArgumentCaptor<EmployerProfile> captor = ArgumentCaptor.forClass(EmployerProfile.class);

        verify(employerRepository).save(captor.capture());
        EmployerProfile employer = captor.getValue();

        assertThat(employer.getFirstName()).isEqualTo("mostafa");
        assertThat(employer.getCompanyName()).isEqualTo("vodafone");
        assertThat(employer.getUser().getEmail()).isEqualTo("mostafa2nady@gmail.com");
        assertThat(employer.getUser().getPassword()).isEqualTo("$2a$encoded");
        assertThat(employer.getUser().getRole()).isEqualTo(Role.EMPLOYER);
        verify(candidateRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when role is invalid")
    void registerInvalidRole() {
        userForm.setRole("invalid");

        assertThatThrownBy(() -> registrationService.register(userForm))
                .isInstanceOf(BadRequestException.class);

        verify(candidateRepository, never()).save(any());
        verify(employerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when role is null")
    void registerNullRole() {
        userForm.setRole(null);

        assertThatThrownBy(() -> registrationService.register(userForm))
                .isInstanceOf(BadRequestException.class);

        verify(candidateRepository, never()).save(any());
        verify(employerRepository, never()).save(any());
    }


}