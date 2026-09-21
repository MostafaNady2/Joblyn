# Joblyn: Job Board Platform

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Security](https://img.shields.io/badge/Security-Spring%20Security-blue.svg)](https://spring.io/projects/spring-security)
[![Database](https://img.shields.io/badge/Database-MySQL-blue.svg)](https://www.mysql.com/)

A robust, full-stack Spring Boot platform designed to bridge the gap between top talent and leading recruiters. **Joblyn** provides a seamless, role-based experience for Candidates, Employers, and Administrators, featuring secure authentication, real-time job moderation, and advanced filtering.

---

## Table of Contents
- [Key Features](#key-features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Getting Started](#getting-started)
- [Security & Roles](#security--roles)
- [Project Organization](#project-organization)
- [Future Roadmap](#future-roadmap)

---

## Key Features

### For Candidates (Job Seekers)
*   **Smart Dashboard:** Track application status (Pending, Accepted, Rejected) in real-time.
*   **Dynamic Profile:** Show-case skills, work preferences, and professional experience.
*   **Document Management:** Integrated resume and profile photo upload system.
*   **One-Click Apply:** Easy application process with custom cover letter validation.
*   **Advanced Discovery:** Search jobs by keywords, salary range, work nature, and required skills.

### For Employers (Recruiters)
*   **Company Branding:** Manage company profile, logo, and location.
*   **Streamlined Recruitment:** Post detailed job listings with specific skill requirements.
*   **Applicant Tracking (ATS):** Review candidate profiles, download resumes, and manage the hiring pipeline.
*   **Manual & Automated Closing:** Manually close job listings or rely on scheduled tasks for expired postings.
*   **Insightful Analytics:** Dashboard metrics for job listing performance and applicant counts.

### Automation & Reliability
*   **Automated Lifecycle:** Scheduled tasks to automatically close expired job postings daily.
*   **Data Integrity:** Robust business logic ensuring consistent job states and ownership checks.

### For Administrators (Moderators)
*   **Quality Control:** Review and approve/reject job postings to ensure platform integrity.
*   **Global Oversight:** Manage all users (Candidates & Employers) and monitor platform health.
*   **Platform Stats:** High-level overview of system-wide job and user distribution.

---

## Tech Stack

| Layer | Technology |
| :--- | :--- |
| **Backend** | Java 21, Spring Boot 4.1.0 |
| **Security** | Spring Security (RBAC, Custom Success Handlers) |
| **Persistence** | Spring Data JPA, Hibernate |
| **Database** | MySQL 8.0+ |
| **Frontend** | Thymeleaf, Bootstrap 5, JavaScript |
| **Validation** | Jakarta Bean Validation |
| **Build Tool** | Maven |

---

## Architecture

The application follows a clean, layered architecture to ensure maintainability and scalability:

1.  **Controller Layer:** MVC controllers handling role-specific routing and UI rendering.
2.  **Service Layer:** Encapsulates business logic, transactional boundaries, and security checks.
3.  **Data Access Layer (Repository):** Leverages Spring Data JPA for efficient MySQL interaction.
4.  **Security Layer:** Custom `UserDetailsService` and `SuccessHandler` for tailored role-based redirection.
5.  **Task Scheduling:** Spring `@Scheduled` tasks for automated job status management.
6.  **Util Layer:** Custom `Specifications` for complex, dynamic job filtering.

---

## Getting Started

### Prerequisites
*   **JDK 21** or higher
*   **Maven 3.8+**
*   **MySQL 8.0+**

### 1. Database Setup
Create a MySQL database:
```sql
CREATE DATABASE job_board;
```

### 2. Configuration
Copy `src/main/resources/application.properties.example` to `src/main/resources/application.properties` and update it with your credentials:
```properties
# Automatically create the database if it does not exist on the MySQL server
spring.datasource.url=jdbc:mysql://localhost:3306/job_board?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=your_db_user
spring.datasource.password=your_db_password

# Automatically create or update database tables based on your JPA Entities
spring.jpa.hibernate.ddl-auto=update

# Optional: Show generated SQL in the console to verify table creation
spring.jpa.show-sql=true
```

### 3. Installation & Run
```bash
# Clone the repository
git clone https://github.com/yourusername/jobboard.git
cd jobboard

# Build and run
./mvnw spring-boot:run
```
Access the application at: `http://localhost:8080`

---

## Security & Roles

| Role | Access Level |
| :--- | :--- |
| **ROLE_CANDIDATE** | Profile management, job search, application submission. |
| **ROLE_EMPLOYER** | Job posting, applicant review, company profile management. |
| **ROLE_ADMIN** | User management, job moderation, platform analytics. |

---

## Project Organization

```text
jobboard/
├── src/main/java/com/example/jobboard/
│   ├── config/      # Security & Infrastructure configs
│   ├── controller/  # Role-based Web Controllers
│   ├── dto/         # Data Transfer Objects & Filters
│   ├── entity/      # JPA Domain Models
│   ├── repository/  # Spring Data Repositories
│   ├── service/     # Business Logic
│   └── util/        # Dynamic Query Specifications
├── src/main/resources/
│   ├── static/      # CSS, JS, and UI Assets
│   └── templates/   # Thymeleaf HTML Templates
├── photos/          # User-uploaded Profile Photos
└── resumes/         # Candidate-uploaded Resumes
```

---

## Future Roadmap
- [ ] Dockerization: Containerize the app and DB for easy deployment.
- [ ] Email Notifications: Automatic alerts for application status changes.
- [ ] Social Login: Integration with LinkedIn/GitHub OAuth2.
- [ ] REST API: Expose endpoints for mobile application integration.


