package com.example.jobboard.service;

import com.example.jobboard.entity.EmployerProfile;
import com.example.jobboard.entity.Location;
import com.example.jobboard.repository.EmployerRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployerServiceTest {

    @Mock
    private EmployerRepository employerRepository;
    @InjectMocks
    private EmployerService employerService;

    private List<EmployerProfile> employers;

    @BeforeEach
    void setUp() {
        EmployerProfile e1 = new EmployerProfile();
        e1.setId(1);
        e1.setFirstName("ahmed");
        e1.setLastName("salah");
        e1.setCompanyName("vodafone");
        e1.setLocation(new Location());

        EmployerProfile e2 = new EmployerProfile();
        e2.setId(2);
        e2.setFirstName("mohamed");
        e2.setLastName("ahmed");
        e2.setCompanyName("orange");
        e2.setLocation(new Location());
        employers = List.of(e1, e2);
    }

    @Test
    @DisplayName("Get all employers")
    void getAllEmployers() {
        when(employerRepository.findAll()).thenReturn(employers);
        List<EmployerProfile> result = employerService.findAll();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getFirstName()).isEqualTo("ahmed");
        assertThat(result.get(1).getFirstName()).isEqualTo("mohamed");
    }

    @Test
    @DisplayName("Get employer by id")
    void getEmployerById() {
        when(employerRepository.findById(1)).thenReturn(Optional.of(employers.get(0)));
        EmployerProfile result = employerService.findById(1);
        assertThat(result).isNotNull();
        assertThat(result.getFirstName()).isEqualTo("ahmed");
        assertThat(result.getLastName()).isEqualTo("salah");
        assertThat(result.getCompanyName()).isEqualTo("vodafone");
    }

    @Test
    @DisplayName("Get employer by id not found")
    void getEmployerByIdNotFound() {
        when(employerRepository.findById(3)).thenReturn(Optional.empty());

        EmployerProfile result = employerService.findById(3);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Save employer")
    void saveEmployer() {

        when(employerRepository.save(employers.get(0))).thenReturn(employers.get(0));
        employerRepository.save(employers.get(0));
        verify(employerRepository, times(1)).save(employers.get(0));
    }

    @Test
    @DisplayName("Should not save profile photo when file is empty")
    void shouldNotSaveProfilePhoto() throws IOException {
        MultipartFile photo = mock(MultipartFile.class);
        EmployerProfile employer = employers.get(0);

        when(photo.isEmpty()).thenReturn(true);


        try(MockedStatic<FileUploadUtil> util = mockStatic(FileUploadUtil.class)){
            employerService.saveProfilePhoto(photo, employer);
            util.verify(()->FileUploadUtil.saveFile(any(),any(),any()),never());
            assertThat(employer.getProfilePhoto()).isNull();
        }
    }

}



