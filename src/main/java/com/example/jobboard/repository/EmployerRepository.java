package com.example.jobboard.repository;

import com.example.jobboard.entity.EmployerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface EmployerRepository extends JpaRepository<EmployerProfile, Integer> {


    public Optional<EmployerProfile> findByUserEmail(String email);
}
