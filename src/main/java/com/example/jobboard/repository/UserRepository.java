package com.example.jobboard.repository;

import com.example.jobboard.entity.User;
import com.example.jobboard.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    public List<User> findAll();
    public Page<User> findAll(Pageable pageable);
    public Optional<User> findById(int id);
    public Optional<User> findByEmail(String email);
    public User save(User user);
    public long count();
    public long countByRole(Role role);
}
