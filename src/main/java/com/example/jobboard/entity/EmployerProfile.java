package com.example.jobboard.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "employer")
public class EmployerProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "first_name",nullable = false)
    private String firstName;

    @Column(name = "last_name",nullable = false)
    private String lastName;

    @Column(name = "company_name",nullable = false)
    private String companyName;

    @Embedded
    private Location location;

    @Column(name = "profile_photo")
    private String profilePhoto;

    @OneToOne(cascade =  CascadeType.ALL)
    @JoinColumn(name = "user_id",nullable = false)
    private User user;
}
