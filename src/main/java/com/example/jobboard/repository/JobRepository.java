package com.example.jobboard.repository;

import com.example.jobboard.entity.Job;
import org.springframework.data.domain.Page;
import com.example.jobboard.enums.JobStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface JobRepository extends JpaRepository<Job, Integer> , JpaSpecificationExecutor<Job> {


    public Page<Job> findAll(Pageable pageable);
    public Job save(Job job);
    public Optional<Job> findById(int id);
    public List<Job> findByStatus(JobStatus status);
    public long countJobsByStatus(JobStatus status);
    @Query("SELECT j FROM Job j LEFT JOIN FETCH j.applications WHERE j.employer.id = :employerId")
    List<Job> findByEmployerIdWithApplications(@Param("employerId") int employerId);

    @Query("SELECT j FROM Job j LEFT JOIN FETCH j.applications WHERE j.id = :id")
    Job findByIdWithApplications(@Param("id") int theId);
    public List<Job> findByPostedAtAfterOrderByPostedAtDesc(LocalDateTime postedAt );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Job j SET j.status = :status WHERE j.deadline <= NOW() AND j.status != :status")
    public int closeExpiredJobs(@Param("status") JobStatus status);
}
