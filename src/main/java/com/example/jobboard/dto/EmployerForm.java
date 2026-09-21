package com.example.jobboard.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * Web-bound form for employer profile editing.
 * No id and no User reference — the profile is resolved from the
 * authenticated principal. profilePhoto only carries the current
 * filename for display; uploads arrive via photoFile.
 */
@Data
public class EmployerForm {
    private String firstName;
    private String lastName;
    private String companyName;
    private String city;
    private String country;
    private MultipartFile photoFile;
    private String profilePhoto;
}
