package com.example.jobboard.util;

import com.example.jobboard.entity.Job;
import com.example.jobboard.dto.JobFilter;
import com.example.jobboard.enums.JobStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

@NoArgsConstructor
public class JobSpecification {

    public static Specification<Job> filter(JobFilter filter) {

        return (root, query, cb) -> {
            if (filter == null) {
                return cb.conjunction();
            }
            var predicates = cb.conjunction();
            predicates= cb.and(predicates,cb.equal(root.get("status"), JobStatus.OPEN));
            // keyword
            String keyword = filter.getKeyword();
            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                predicates = cb.and(
                        predicates,
                        cb.or(
                                cb.like(
                                        cb.lower(root.get("title")),
                                        pattern
                                ),
                                cb.like(
                                        cb.lower(root.get("description")),
                                        pattern
                                )
                        )
                );
            }
            // work Nature
            if (filter.getWorkNature() != null
                    && !filter.getWorkNature().isEmpty()) {

                predicates = cb.and(
                        predicates,
                        root.get("workNature")
                                .as(String.class)
                                .in(filter.getWorkNature())
                );
            }
            // minimum Salary
            if (filter.getMinSalary() > 0) {

                predicates = cb.and(
                        predicates,
                        cb.greaterThanOrEqualTo(
                                root.get("salary"),
                                filter.getMinSalary()
                        )
                );
            }
            // skills
            if (filter.getSkills() != null
                    && !filter.getSkills().isEmpty()) {

                Join<Object, Object> skills =
                        root.join("skills", JoinType.INNER);

                predicates = cb.and(
                        predicates,
                        skills.get("skillName")
                                .in(filter.getSkills())
                );

                query.distinct(true);
            }
            return predicates;
        };
    }
}