-- ============================================================
-- Schema: Job Board / Recruitment Platform
-- Target: MySQL 8+ (Spring Boot initialisation)
-- ============================================================

-- Drop tables in reverse dependency order
DROP TABLE IF EXISTS job_skills;
DROP TABLE IF EXISTS candidate_skills;
DROP TABLE IF EXISTS application;
DROP TABLE IF EXISTS job;
DROP TABLE IF EXISTS employer;
DROP TABLE IF EXISTS candidate;
DROP TABLE IF EXISTS users;

-- ------------------------------------------------------------
-- USERS
-- ------------------------------------------------------------
CREATE TABLE users (
                       id INT AUTO_INCREMENT PRIMARY KEY,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       active BOOLEAN NOT NULL DEFAULT TRUE,
                       created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       role VARCHAR(20) NOT NULL -- CANDIDATE, EMPLOYER, ADMIN
);

-- ------------------------------------------------------------
-- CANDIDATE (1:1 with USERS)
-- ------------------------------------------------------------
CREATE TABLE candidate (
                           id INT AUTO_INCREMENT PRIMARY KEY,
                           first_name VARCHAR(100) NOT NULL,
                           last_name VARCHAR(100) NOT NULL,
                           profile_photo VARCHAR(255),
                           resume VARCHAR(255),
                           work_type VARCHAR(20) NOT NULL, -- REMOTE, ONSITE, HYBRID
                           user_id INT NOT NULL UNIQUE,
                           CONSTRAINT fk_candidate_user FOREIGN KEY (user_id) REFERENCES users (id)
);

-- ------------------------------------------------------------
-- EMPLOYER (1:1 with USERS)
-- ------------------------------------------------------------
CREATE TABLE employer (
                          id INT AUTO_INCREMENT PRIMARY KEY,
                          first_name VARCHAR(100) NOT NULL,
                          last_name VARCHAR(100) NOT NULL,
                          company_name VARCHAR(150) NOT NULL,
                          country VARCHAR(100),
                          city VARCHAR(100),
                          profile_photo VARCHAR(255),
                          user_id INT NOT NULL UNIQUE,
                          CONSTRAINT fk_employer_user FOREIGN KEY (user_id) REFERENCES users (id)
);

-- ------------------------------------------------------------
-- JOB
-- ------------------------------------------------------------
CREATE TABLE job (
                     id INT AUTO_INCREMENT PRIMARY KEY,
                     title VARCHAR(150) NOT NULL,
                     description TEXT,
                     work_nature VARCHAR(20) NOT NULL, -- FULL_TIME, PART_TIME, CONTRACT, INTERNSHIP
                     country VARCHAR(100),
                     city VARCHAR(100),
                     salary DECIMAL(12, 2),
                     status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, OPEN, CLOSED
                     posted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                     deadline DATETIME,
                     employer_id INT NOT NULL,
                     CONSTRAINT fk_job_employer FOREIGN KEY (employer_id) REFERENCES employer (id)
);

-- ------------------------------------------------------------
-- APPLICATION
-- ------------------------------------------------------------
CREATE TABLE application (
                             id INT AUTO_INCREMENT PRIMARY KEY,
                             cover_letter TEXT,
                             applied_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, REVIEWED, ACCEPTED, REJECTED
                             candidate_id INT NOT NULL,
                             job_id INT NOT NULL,
                             CONSTRAINT fk_application_candidate FOREIGN KEY (candidate_id) REFERENCES candidate (id),
                             CONSTRAINT fk_application_job FOREIGN KEY (job_id) REFERENCES job (id),
                             CONSTRAINT uq_application_candidate_job UNIQUE (candidate_id, job_id)
);

-- ------------------------------------------------------------
-- CANDIDATE_SKILLS (junction table)
-- ------------------------------------------------------------
CREATE TABLE candidate_skills (
                                  candidate_id     INT NOT NULL,
                                  skill_name       VARCHAR(100) NOT NULL,
                                  experience_level VARCHAR(50) NOT NULL,
                                  PRIMARY KEY (candidate_id, skill_name, experience_level),
                                  CONSTRAINT fk_candidate_skills_candidate
                                      FOREIGN KEY (candidate_id) REFERENCES candidate (id) ON DELETE CASCADE
);

-- ------------------------------------------------------------
-- JOB_SKILLS (junction table)
-- ------------------------------------------------------------
CREATE TABLE job_skills (
                            job_id           INT NOT NULL,
                            skill_name       VARCHAR(100) NOT NULL,
                            required_level   VARCHAR(50) NOT NULL,
                            PRIMARY KEY (job_id, skill_name, required_level),
                            CONSTRAINT fk_job_skills_job
                                FOREIGN KEY (job_id) REFERENCES job (id) ON DELETE CASCADE
);

-- ------------------------------------------------------------
-- Helpful indexes
-- ------------------------------------------------------------
CREATE INDEX idx_job_employer_id ON job (employer_id);
CREATE INDEX idx_application_candidate_id ON application (candidate_id);
CREATE INDEX idx_application_job_id ON application (job_id);