package com.example.jobboard.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class Location {
    @Column(name = "city")
    private String city;

    @Column(name = "country")
    private String country;
}
