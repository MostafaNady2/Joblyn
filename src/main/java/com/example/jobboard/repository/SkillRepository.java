package com.example.jobboard.repository;

import com.example.jobboard.entity.CandidateProfile;
import com.example.jobboard.entity.CandidateSkill;
import com.example.jobboard.entity.Job;
import com.example.jobboard.entity.JobSkill;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Set;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;
import java.util.LinkedHashSet;

@Repository
public class SkillRepository {

    EntityManager entityManager;

    public SkillRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Set<String> findAllSkillNames() {
        String sql = """
                SELECT DISTINCT skill_name FROM (
                    SELECT skill_name FROM candidate_skills
                    UNION
                    SELECT skill_name FROM job_skills
                ) combined_skills ORDER BY skill_name ASC
                """;

        return new LinkedHashSet<>(entityManager.createNativeQuery(sql, String.class).getResultList());
    }

    public Set<CandidateSkill> findByCandidateId(int id) {
        String sql = "SELECT c FROM CandidateProfile c LEFT JOIN FETCH c.skills WHERE c.id = :theId";
        return entityManager.createQuery(sql, CandidateProfile.class)
                .setParameter("theId", id)
                .getSingleResult().getSkills();
    }

    public Set<JobSkill> findByJobId(int id) {
        String sql = "SELECT j FROM Job j LEFT JOIN FETCH j.skills WHERE j.id = :theId";
        return entityManager.createQuery(sql, Job.class)
                .setParameter("theId", id)
                .getSingleResult().getSkills();
    }

    @Transactional
    public void addSkillToCandidate(int candidateId,CandidateSkill skill) {
        CandidateProfile candidate = entityManager.find(CandidateProfile.class, candidateId);
        candidate.getSkills().add(skill);
        entityManager.merge(candidate);
    }

}
