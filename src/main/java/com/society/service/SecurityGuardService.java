package com.society.service;

import com.society.dto.response.SecurityGuardResponse;
import com.society.entity.Role;
import com.society.entity.SecurityGuard;
import com.society.entity.User;
import com.society.exception.ResourceNotFoundException;
import com.society.repository.SecurityGuardRepository;
import com.society.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class SecurityGuardService {

    private final SecurityGuardRepository securityGuardRepository;
    private final UserRepository userRepository;

    public List<SecurityGuardResponse> getAllSecurityGuards(){
        return userRepository.findByRoleAndIsActiveTrue(Role.SECURITY_GUARD).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

    }

    public SecurityGuardResponse getById(Long id){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Security guard not found with id: " + id));
        if (user.getRole() != Role.SECURITY_GUARD){
            throw new ResourceNotFoundException("User with id: " + id + " is not a security guard.");
        }
        return mapToResponse(user);
    }

    private SecurityGuardResponse mapToResponse(User user){
        SecurityGuardResponse.SecurityGuardResponseBuilder builder = SecurityGuardResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .phoneNo(user.getPhoneNo())
                .email(user.getEmail())
                .profileImage(user.getProfileImageUrl());

        if (user.getSecurityGuard() != null){
            SecurityGuard guard = user.getSecurityGuard();
            builder.employeeId(guard.getEmployeeId())
                    .shiftType(guard.getShiftType() != null ? guard.getShiftType().name() : null)
                    .gateNumber(guard.getGateNumber())
                    .emergencyContact(guard.getEmergencyContact())
                    .joiningDate(guard.getJoiningDate());
        }

        return builder.build();
    }
}
