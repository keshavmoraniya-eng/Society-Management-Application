package com.society.repository;

import com.society.entity.Role;
import com.society.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByPhoneNo(String phoneNo);
    Optional<User> findByEmail(String email);
    Boolean existsByPhoneNo(String phoneNo);
    Boolean existsByEmail(String email);
    List<User> findByRole(Role role);
    List<User> findByRoleAndIsActiveTrue(Role role);
    Long countByRole(Role role);
}
