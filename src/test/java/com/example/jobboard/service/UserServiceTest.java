package com.example.jobboard.service;

import com.example.jobboard.entity.User;
import com.example.jobboard.enums.Role;
import com.example.jobboard.repository.CandidateRepository;
import com.example.jobboard.repository.EmployerRepository;
import com.example.jobboard.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private CandidateRepository candidateRepository;
    @Mock
    private EmployerRepository employerRepository;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    public void setup() {
        user = new User();
        user.setId(1);
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setRole(Role.CANDIDATE);
    }

    @Test
    @DisplayName("Should create user")
    public void shouldCreateUser() {
        when(userRepository.save(user)).thenReturn(user);
        User result = userService.save(user);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getPassword()).isEqualTo("password");
        assertThat(result.getRole().name()).isEqualTo("CANDIDATE");

        user.setRole(Role.EMPLOYER);
        when(userRepository.save(user)).thenReturn(user);
        User result2 = userService.save(user);
        assertThat(result2.getRole().name()).isEqualTo("EMPLOYER");

        user.setRole(Role.ADMIN);
        when(userRepository.save(user)).thenReturn(user);
        User result3 = userService.save(user);
        assertThat(result3.getRole().name()).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("Should count users based on roles")
    public void countBasedOnRoles() {
        when(userRepository.countByRole(Role.CANDIDATE)).thenReturn(3L);
        when(userRepository.countByRole(Role.EMPLOYER)).thenReturn(2L);
        when(userRepository.countByRole(Role.ADMIN)).thenReturn(1L);

        assertThat(userService.countByRole(Role.CANDIDATE)).isEqualTo(3L);
        assertThat(userService.countByRole(Role.EMPLOYER)).isEqualTo(2L);
        assertThat(userService.countByRole(Role.ADMIN)).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should find all users")
    public void findAllUsers() {
        Page<User> page = new PageImpl<>(List.of(user));
        when(userRepository.findAll(PageRequest.of(0,5))).thenReturn(page);
        Page<User> result = userService.findAll(PageRequest.of(0, 5));
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0)).isEqualTo(user);
    }

    @Test
    @DisplayName("Should find user by id")
    public void findUserById() {
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        assertThat(userService.findById(1).get()).isNotNull();
        assertThat(userService.findById(1).get()).isEqualTo(user);
    }



}
