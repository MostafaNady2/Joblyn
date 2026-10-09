package com.example.jobboard.repository;

import com.example.jobboard.entity.CandidateProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface CandidateRepository extends JpaRepository<CandidateProfile, Integer> {

    public Optional<CandidateProfile> findById(int id);

    public List<CandidateProfile> findAll();

    public Optional<CandidateProfile> findByUserEmail(String email);

    public CandidateProfile save(CandidateProfile candidateProfile);
}
