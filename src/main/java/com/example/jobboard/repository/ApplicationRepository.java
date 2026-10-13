package com.example.jobboard.repository;

import com.example.jobboard.entity.Application;
import com.example.jobboard.enums.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Integer> {

    public List<Application> findAll();
    public Optional<Application> findById(int id);
    public List<Application> findByCandidateId(int candidateId);
    @Query("SELECT COUNT(a) FROM Application a WHERE a.job.employer.id = :employerId")
    public long countApplicantsByEmployerId(@Param("employerId") int id);

    @Query("SELECT COUNT(a) FROM Application a WHERE a.job.id = :jobId AND a.candidate.id = :candidateId")
    boolean existsByJobIdAndCandidateId(int jobId, int candidateId);
}