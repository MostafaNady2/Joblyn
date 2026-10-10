package com.example.jobboard.repository;

import com.example.jobboard.entity.CandidateProfile;
import com.example.jobboard.entity.CandidateSkill;
import com.example.jobboard.entity.User;
import com.example.jobboard.enums.CandidateWorkType;
import com.example.jobboard.enums.Role;
import com.example.jobboard.enums.SkillLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CandidateRepositoryTest {

    @Autowired
    private CandidateRepository candidateRepository;
    @Autowired
    private TestEntityManager entityManager;
    private CandidateProfile candidate;

    @BeforeEach
    void setup() {
        User user = new User();
        user.setEmail("ali@gmail.com");
        user.setPassword("$2a$encoded");
        user.setRole(Role.CANDIDATE);

        candidate = new CandidateProfile();
        candidate.setUser(user);
        candidate.setFirstName("ali");
        candidate.setLastName("ahmed");
        candidate.setWorkType(CandidateWorkType.HYBRID);

        CandidateSkill s1 = new CandidateSkill();
        s1.setSkillName("Java");
        s1.setExperienceLevel(SkillLevel.ADVANCED);

        CandidateSkill s2 = new CandidateSkill();
        s2.setSkillName("Python");
        s2.setExperienceLevel(SkillLevel.INTERMEDIATE);

        candidate.getSkills().add(s1);
        candidate.getSkills().add(s2);

        candidate = candidateRepository.save(candidate);
    }

    @Test
    @DisplayName("should persist candidate with user and skills")
    void shouldPersistCandidateWithUserAndSkills() {
        entityManager.flush();
        entityManager.clear();

        CandidateProfile saved = candidateRepository.findById(candidate.getId())
                .orElseThrow();

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUser().getId()).isNotNull();
        assertThat(saved.getSkills()).hasSize(2);
    }

    @Test
    @DisplayName("should get all skills")
    void getAllSkills() {
        List<String> skills = candidateRepository.findAllSkills();
        assertThat(skills).hasSize(2)
                .containsExactlyInAnyOrder("Java", "Python");

    }

}