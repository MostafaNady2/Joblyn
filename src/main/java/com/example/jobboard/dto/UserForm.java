package com.example.jobboard.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserForm {

    @NotEmpty(message = "First name is required")
    @Size(min = 2, message = "First name must be at least 2 characters long")
    private String firstName;

    @NotEmpty(message = "Last name is required")
    @Size(min = 2, message = "Last name must be at least 2 characters long")
    private String lastName;

    @NotEmpty(message = "Email is required")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A_Za-z]{2,63}$",message = "Email must be valid")
    private String email;

    @NotEmpty(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    @NotEmpty(message = "Please choose candidate or employer")
    private String role;

    private String companyName;
    private String country;
    private String city;
}
