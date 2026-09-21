-- ============================================================
-- Data: Job Board / Recruitment Platform
-- All accounts below use the password: password123
-- BCrypt hashes generated for Spring Security's BCryptPasswordEncoder
-- ============================================================

USE job_board;

-- USERS ------------------------------------------------------------
INSERT INTO users (id, email, password_hash, active, role) VALUES
                                                               (1, 'john.doe@email.com', '$2b$10$oEFj8DdpHEzx0THopyfTm.NhjoYK65WTnr0dP4x8TTKXC2pmpzeeG', TRUE, 'CANDIDATE'),
                                                               (2, 'jane.smith@techcorp.com', '$2b$10$vteJF6bOHUeiaP6zPStMCusk2EBBvwaWTDMJcCovHrYdHxRvjjGMa', TRUE, 'EMPLOYER'),
                                                               (3, 'alice.johnson@email.com', '$2b$10$r5s/xRY.3MTF4bxiUej5d.7QOQ./bsv57dPVtR.hT.Kp.syLVJkeS', TRUE, 'CANDIDATE'),
                                                               (4, 'bob.wilson@startup.io', '$2b$10$0o6Ksit.AdW71aYJ70As9eS7UQhaArRFra6heOYX8z0kwWGNhsmZO', TRUE, 'EMPLOYER'),
                                                               (5, 'admin@jobboard.com', '$2b$10$.u24B0j1VyoBstcopWshQ.M3uksofLMCttZV4PU3ss662bwOUVTcC', TRUE, 'ADMIN');

-- CANDIDATE ----------------------------------------------------------
INSERT INTO candidate (id, first_name, last_name, profile_photo, resume, work_type, user_id) VALUES
                                                                                                 (1, 'John', 'Doe', 'john_photo.jpg', 'john_doe_resume.pdf', 'REMOTE', 1),
                                                                                                 (2, 'Alice', 'Johnson', 'alice_photo.jpg', 'alice_johnson_resume.pdf', 'HYBRID', 3);

-- EMPLOYER -----------------------------------------------------------
INSERT INTO employer (id, first_name, last_name, company_name, country, city, profile_photo, user_id) VALUES
                                                                                                          (1, 'Jane', 'Smith', 'TechCorp Inc.', 'USA', 'New York', 'jane_smith_photo.jpg', 2),
                                                                                                          (2, 'Bob', 'Wilson', 'Startup.io', 'Canada', 'Toronto', 'bob_wilson_photo.jpg', 4);

-- JOB ----------------------------------------------------------------
INSERT INTO job (id, title, description, work_nature, country, city, salary, status, posted_at, deadline, employer_id) VALUES
                                                                                                                           (1, 'Senior Software Engineer', 'Lead the development of scalable cloud-based microservices. Required: Java, Spring Boot, AWS.', 'FULL_TIME', 'USA', 'San Francisco', 135000.00, 'OPEN', NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), 1),
                                                                                                                           (2, 'Junior Data Analyst', 'Analyze business data, create dashboards, and support decision-making. SQL and Tableau skills required.', 'FULL_TIME', 'USA', 'New York', 65000.00, 'OPEN', NOW(), DATE_ADD(NOW(), INTERVAL 20 DAY), 1),
                                                                                                                           (3, 'DevOps Intern', 'Support CI/CD pipelines, monitor infrastructure, and automate deployment processes.', 'INTERNSHIP', 'Canada', 'Toronto', 35000.00, 'PENDING', NOW(), DATE_ADD(NOW(), INTERVAL 45 DAY), 2),
                                                                                                                           (4, 'Product Manager', 'Define product vision, gather requirements, and coordinate with engineering teams.', 'FULL_TIME', 'Canada', 'Toronto', 110000.00, 'REJECTED', NOW(), DATE_ADD(NOW(), INTERVAL -10 DAY), 2);

-- APPLICATION --------------------------------------------------------
INSERT INTO application (id, cover_letter, applied_at, status, candidate_id, job_id) VALUES
                                                                                         (1, 'I have 8 years of Java experience and have led multiple microservices projects.', NOW(), 'ACCEPTED', 1, 1),
                                                                                         (2, 'I am a recent graduate with strong SQL skills and a passion for data.', NOW(), 'PENDING', 1, 2),
                                                                                         (3, 'I am excited about the DevOps internship and have basic Docker and Linux knowledge.', NOW(), 'PENDING', 2, 3),
                                                                                         (4, 'I have 3 years of experience as a product owner in agile environments.', NOW(), 'REJECTED', 2, 4);

-- CANDIDATE_SKILLS ---------------------------------------------------
INSERT INTO candidate_skills (candidate_id, skill_name, experience_level) VALUES
                                                                              (1, 'Java', 'EXPERT'),
                                                                              (1, 'Spring Boot', 'ADVANCED'),
                                                                              (1, 'AWS', 'INTERMEDIATE'),
                                                                              (1, 'SQL', 'ADVANCED'),
                                                                              (2, 'Python', 'INTERMEDIATE'),
                                                                              (2, 'Tableau', 'BEGINNER'),
                                                                              (2, 'SQL', 'INTERMEDIATE');

-- JOB_SKILLS ---------------------------------------------------------
INSERT INTO job_skills (job_id, skill_name, required_level) VALUES
                                                                (1, 'Java', 'EXPERT'),
                                                                (1, 'Spring Boot', 'ADVANCED'),
                                                                (1, 'AWS', 'INTERMEDIATE'),
                                                                (2, 'SQL', 'INTERMEDIATE'),
                                                                (2, 'Excel', 'BEGINNER'),
                                                                (2, 'Tableau', 'BEGINNER'),
                                                                (3, 'Linux', 'BEGINNER'),
                                                                (3, 'Docker', 'BEGINNER'),
                                                                (4, 'Agile', 'ADVANCED'),
                                                                (4, 'Product Management', 'EXPERT');