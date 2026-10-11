package com.govfix.repository;

import com.govfix.entity.Role;
import com.govfix.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    List<User> findByRole(Role role);
    List<User> findByRoleAndIsActiveTrue(Role role);
}
