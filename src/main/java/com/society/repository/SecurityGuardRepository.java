package com.society.repository;

import com.society.entity.SecurityGuard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SecurityGuardRepository extends JpaRepository<SecurityGuard,Long> {
    Optional<SecurityGuard> findByUserId(Long userId);
    Optional<SecurityGuard> findByEmployeeId(String employeeId);
}
