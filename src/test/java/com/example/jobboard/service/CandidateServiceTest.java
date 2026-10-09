package com.example.jobboard.service;

import com.example.jobboard.entity.CandidateProfile;
import com.example.jobboard.entity.CandidateSkill;
import com.example.jobboard.enums.CandidateWorkType;
import com.example.jobboard.repository.CandidateRepository;
import com.example.jobboard.repository.SkillRepository;
import com.example.jobboard.util.FileUploadUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CandidateServiceTest {

    @Mock
    private CandidateRepository candidateRepository;
    @Mock
    private SkillRepository skillRepository;
    @InjectMocks
    private CandidateService candidateService;
    private List<CandidateProfile> candidates;

    @BeforeEach
    void setUp() {
        CandidateProfile c1 = new CandidateProfile();
        c1.setId(1);
        c1.setFirstName("mostafa");
        c1.setLastName("nady");
        c1.setWorkType(CandidateWorkType.HYBRID);

        CandidateProfile c2 = new CandidateProfile();
        c2.setId(2);
        c2.setFirstName("ahmed");
        c2.setLastName("ali");
        c2.setWorkType(CandidateWorkType.REMOTE);

        candidates = List.of(c1,c2);
    }

    @Test
    @DisplayName("Get all candidates")
    void getAllCandidates() {
        when(candidateRepository.findAll()).thenReturn(candidates);

        List<CandidateProfile> result = candidateService.findAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getFirstName()).isEqualTo("mostafa");
        assertThat(result.get(1).getFirstName()).isEqualTo("ahmed");
    }

    @Test
    @DisplayName("Get candidate by id")
    void getCandidateById() {
        when(candidateRepository.findById(1)).thenReturn(Optional.of(candidates.get(0)));

        CandidateProfile result = candidateService.findById(1);

        assertThat(result).isNotNull();
        assertThat(result.getFirstName()).isEqualTo("mostafa");
        assertThat(result.getWorkType()).isEqualTo(CandidateWorkType.HYBRID);
    }

    @Test
    @DisplayName("Get candidate by id not found")
    void getCandidateByIdNotFound() {
        when(candidateRepository.findById(999)).thenReturn(Optional.empty());

        CandidateProfile result = candidateService.findById(999);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Save candidate")
    void saveCandidate() {
        when(candidateRepository.save(any(CandidateProfile.class))).thenReturn(candidates.get(0));

        CandidateProfile result = candidateService.save(candidates.get(0));

        assertThat(result).isNotNull();
        assertThat(result.getFirstName()).isEqualTo("mostafa");
        assertThat(result.getWorkType()).isEqualTo(CandidateWorkType.HYBRID);
    }

    @Test
    @DisplayName("Should not save profile photo when file is empty")
    void shouldNotSaveProfilePhoto() throws IOException {
        MultipartFile photo = mock(MultipartFile.class);
        CandidateProfile candidate = candidates.get(0);

        when(photo.isEmpty()).thenReturn(true);

        try (MockedStatic<FileUploadUtil> util = mockStatic(FileUploadUtil.class)) {
            candidateService.saveProfilePhoto(photo, candidate);

            util.verify(() -> FileUploadUtil.saveFile(any(), any(), any()), never());
            assertThat(candidate.getProfilePhoto()).isNull();
        }
    }

    @Test
    @DisplayName("Should not save resume when file is empty")
    void shouldNotSaveResume() throws IOException {
        MultipartFile resume = mock(MultipartFile.class);
        CandidateProfile candidate = candidates.get(0);

        when(resume.isEmpty()).thenReturn(true);

        try (MockedStatic<FileUploadUtil> util = mockStatic(FileUploadUtil.class)) {
            candidateService.saveProfileResume(resume, candidate);

            util.verify(() -> FileUploadUtil.saveFile(any(), any(), any()), never());
            assertThat(candidate.getResume()).isNull();
        }
    }



















}