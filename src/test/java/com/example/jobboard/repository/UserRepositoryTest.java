package com.example.jobboard.repository;

import com.example.jobboard.entity.User;
import com.example.jobboard.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User candidate;
    private User employer;


    @BeforeEach
    void setup() {
        candidate = new User();
        candidate.setEmail("candidate@example.com");
        candidate.setRole(Role.CANDIDATE);
        candidate.setPassword("$2a$encoded");

        employer = new User();
        employer.setEmail("employer@example.com");
        employer.setRole(Role.EMPLOYER);
        employer.setPassword("$2a$encoded");

        candidate = userRepository.save(candidate);
        employer = userRepository.save(employer);
    }

    @Test
    @DisplayName("Test finding user by email")
    void findByEmailFound() {
        Optional<User> found = userRepository.findByEmail("candidate@example.com");
        assertThat(found).isPresent();
        assertThat(found.get().getRole()).isEqualTo(Role.CANDIDATE);
    }

    @Test
    @DisplayName("Test finding user by email not found")
    void findByEmailNotFound() {
        Optional<User> found = userRepository.findByEmail("other@example.com");
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Test finding user by id found")
    void findByIdFound() {
        Optional<User> found = userRepository.findById(candidate.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getRole()).isEqualTo(Role.CANDIDATE);

        found = userRepository.findById(employer.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getRole()).isEqualTo(Role.EMPLOYER);
    }

    @Test
    @DisplayName("Test finding user by id not found")
    void findByIdNotFound() {
        Optional<User> found = userRepository.findById(300);
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Test counting users by role")
    void countByRole() {
        long count = userRepository.countByRole(Role.CANDIDATE);
        assertThat(count).isEqualTo(1);

        count = userRepository.countByRole(Role.EMPLOYER);
        assertThat(count).isEqualTo(1);
    }


    @Test
    @DisplayName("Test finding all users with pagination")
    void findAllPage() {
        Page<User> page = userRepository.findAll(Pageable.ofSize(10));
        assertThat(page.getTotalElements()).isEqualTo(2);
    }

}