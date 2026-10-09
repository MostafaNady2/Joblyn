package com.example.jobboard.mapper;

import com.example.jobboard.dto.UserForm;
import com.example.jobboard.entity.CandidateProfile;
import com.example.jobboard.entity.EmployerProfile;
import com.example.jobboard.entity.Location;
import com.example.jobboard.entity.User;
import com.example.jobboard.enums.CandidateWorkType;
import com.example.jobboard.enums.Role;
import com.example.jobboard.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {
    public User toUser(UserForm form,String encodedPassword) {
        User user = new User();
        user.setEmail(form.getEmail());
        user.setPassword(encodedPassword);
        user.setRole(resolveRole(form.getRole()));
        return user;
    }

    public CandidateProfile toCandidateProfile(UserForm form,User user) {
        CandidateProfile c = new CandidateProfile();
        c.setFirstName(form.getFirstName());
        c.setLastName(form.getLastName());
        c.setWorkType(CandidateWorkType.HYBRID);
        c.setUser(user);
        return c;
    }

    public EmployerProfile toEmployerProfile(UserForm form, User user) {
        EmployerProfile e = new EmployerProfile();
        e.setFirstName(form.getFirstName());
        e.setLastName(form.getLastName());
        e.setCompanyName(form.getCompanyName());
        e.setLocation(toLocation(form.getCountry(), form.getCity()));
        e.setUser(user);
        return e;
    }

    private Location toLocation(String country, String city) {
        Location l = new Location();
        l.setCountry(country);
        l.setCity(city);
        return l;
    }

    private Role resolveRole(String role) {
        switch (role) {
            case "candidate" -> {
                return Role.CANDIDATE;
            }
            case "employer" -> {
                return Role.EMPLOYER;
            }
            default -> {
                throw new BadRequestException("Invalid role: " + role);
            }
        }
    }
}
