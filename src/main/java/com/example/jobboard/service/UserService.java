package com.example.jobboard.service;

import com.example.jobboard.entity.CandidateProfile;
import com.example.jobboard.entity.EmployerProfile;
import com.example.jobboard.entity.User;
import com.example.jobboard.enums.Role;
import com.example.jobboard.exception.UserNotFoundException;
import com.example.jobboard.repository.CandidateRepository;
import com.example.jobboard.repository.EmployerRepository;
import com.example.jobboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final CandidateRepository candidateRepository;
    private final EmployerRepository employerRepository;

    @Autowired
    public UserService(UserRepository userRepository, CandidateRepository candidateRepository,
                       EmployerRepository employerRepository) {
        this.userRepository = userRepository;
        this.candidateRepository = candidateRepository;
        this.employerRepository = employerRepository;
    }

    public long countByRole(Role role) {
        return userRepository.countByRole(role);
    }

    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public Optional<User> findById(int id) {
        return userRepository.findById(id);
    }


    public User save(User user) {
        return userRepository.save(user);
    }

    private Authentication currentAuthentication() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return auth;
    }

    public Object getCurrentUser() {
        Authentication auth = currentAuthentication();
        if (auth == null) {
            return null;
        }
        String email = auth.getName();
        String role = auth.getAuthorities().stream().findFirst().map(GrantedAuthority::getAuthority).orElse("");
        return switch (role) {
            case "ROLE_CANDIDATE" -> candidateRepository.findByUserEmail(email)
                    .orElseThrow(() -> new UserNotFoundException("Candidate profile not found"));
            case "ROLE_EMPLOYER" -> employerRepository.findByUserEmail(email)
                    .orElseThrow(() -> new UserNotFoundException("Employer profile not found"));
            case "ROLE_ADMIN" -> userRepository.findByEmail(email)
                    .orElseThrow(() -> new UserNotFoundException("Admin profile not found"));
            default -> null;
        };
    }

    public String getProfilePhotoUrl() {
        Object user = getCurrentUser();
        if (user instanceof CandidateProfile candidate) {
            String photo = candidate.getProfilePhoto();
            return (photo == null || photo.isBlank())
                    ? "/images/user-icon.png"
                    : "/photos/candidate/" + candidate.getId() + "/" + photo;
        }
        if (user instanceof EmployerProfile employer) {
            String photo = employer.getProfilePhoto();
            return (photo == null || photo.isBlank())
                    ? "/images/user-icon.png"
                    : "/photos/employer/" + employer.getId() + "/" + photo;
        }
        return "/images/user-icon.png";
    }
}
