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
    @Mock
    private UserMapper mockedMapper;
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
    }

    @Test
    @DisplayName("Should register new candidate successfully")
    public void registerNewCandidate() {
        User user = new User();
        CandidateProfile candidate = new CandidateProfile();
        candidate.setUser(user);
        user.setRole(Role.CANDIDATE);

        when(passwordEncoder.encode("mostafa123")).thenReturn("$2a$encoded");
        when(mockedMapper.toUser(userForm, "$2a$encoded")).thenReturn(user);
        when(mockedMapper.toCandidateProfile(userForm, user)).thenReturn(candidate);

        registrationService.register(userForm);

        verify(mockedMapper).toUser(userForm, "$2a$encoded");
        verify(passwordEncoder).encode("mostafa123");

        assertThat(user.isActive()).isTrue();
//        assertThat(user.getCreatedAt()).isNotNull();

        verify(mockedMapper).toCandidateProfile(userForm, user);
        verify(candidateRepository).save(candidate);
    }

    @Test
    @DisplayName("Should register new employer successfully")
    public void registerNewEmployer() {
        User user = new User();
        EmployerProfile employer = new EmployerProfile();
        employer.setUser(user);
        user.setRole(Role.EMPLOYER);

        when(passwordEncoder.encode("mostafa123")).thenReturn("$2a$encoded");
        when(mockedMapper.toUser(userForm, "$2a$encoded")).thenReturn(user);
        when(mockedMapper.toEmployerProfile(userForm, user)).thenReturn(employer);

        registrationService.register(userForm);

        verify(mockedMapper).toUser(userForm, "$2a$encoded");
        verify(passwordEncoder).encode("mostafa123");

        assertThat(user.isActive()).isTrue();
//        assertThat(user.getCreatedAt()).isNotNull();

        verify(mockedMapper).toEmployerProfile(userForm, user);
        verify(employerRepository).save(employer);
    }

    @Test
    @DisplayName("Should throw exception when role is invalid")
    void registerInvalidRole() {
        RegistrationService service = new RegistrationService(userRepository, candidateRepository, employerRepository, passwordEncoder,
        new UserMapper());
        userForm.setRole("invalid");

        assertThatThrownBy(() -> service.register(userForm))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid role");

        verify(candidateRepository, never()).save(any());
        verify(employerRepository, never()).save(any());
    }


}