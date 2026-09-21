package com.example.jobboard.entity;

import com.example.jobboard.enums.Role;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;


@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "email",nullable = false,unique = true)
    private String email;

    @Column(name = "password_hash",nullable = false)
    private String password;

    @Column(name = "active",nullable = false)
    private boolean isActive;

    @Enumerated(EnumType.STRING)
    @Column(name = "role",nullable = false)
    private Role role;

    @Column(name = "created_at",nullable = false)
    private LocalDateTime createdAt;
}
