package com.society.security;

import com.society.dto.request.UpdateProfileRequest;
import com.society.dto.response.UserResponse;
import com.society.entity.*;
import com.society.exception.BadRequestException;
import com.society.exception.ResourceNotFoundException;
import com.society.repository.RentalProfileRepository;
import com.society.repository.SecurityGuardRepository;
import com.society.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RentalProfileRepository rentalProfileRepository;
    private final SecurityGuardRepository securityGuardRepository;

    public UserResponse getUserById(Long id){
        User user = userRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("User not found"));
        return mapToResponse(user);
    }

    public UserResponse getUserByPhone(String phoneNo){
        User user = userRepository.findByPhoneNo(phoneNo)
                .orElseThrow(()->new ResourceNotFoundException("User not found"));
        return mapToResponse(user);
    }

    public List<UserResponse> getAllUsersByRole(Role role){
        return userRepository.findByRoleAndIsActiveTrue(role).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<UserResponse> getAllRentals(){
        return getAllUsersByRole(Role.RENTAL);
    }

    /**
     * Update user profile
     */
    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        log.info("Updating profile for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // Update basic info
        if (request.getFullName() != null && !request.getFullName().trim().isEmpty()) {
            user.setFullName(request.getFullName());
        }
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            user.setEmail(request.getEmail());
        }
        if (request.getProfileImageUrl() != null && !request.getProfileImageUrl().trim().isEmpty()) {
            user.setProfileImageUrl(request.getProfileImageUrl());
        }

        user = userRepository.save(user);

        // Update rental profile if user is rental
        if (user.getRole() == Role.RENTAL && request.getApartmentNo() != null) {
            RentalProfile profile = rentalProfileRepository.findByUserId(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Rental profile not found"));

            if (request.getApartmentNo() != null) profile.setApartmentNo(request.getApartmentNo());
            if (request.getTotalMembers() != null) profile.setTotalMembers(request.getTotalMembers());
            if (request.getJobProfile() != null) profile.setJobProfile(request.getJobProfile());
            if (request.getWorkingLocation() != null) profile.setWorkingLocation(request.getWorkingLocation());
            if (request.getBloodGroup() != null) profile.setBloodGroup(request.getBloodGroup());
            if (request.getMoveInDate() != null) profile.setMoveInDate(request.getMoveInDate());

            rentalProfileRepository.save(profile);
        }

        log.info("Profile updated for user ID: {}", userId);
        return mapToResponse(user);
    }

    /**
     * Get all security guards
     */
    public List<UserResponse> getAllSecurityGuards() {
        log.info("Fetching all security guards");
        return userRepository.findByRoleAndIsActiveTrue(Role.SECURITY_GUARD).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get users with pending approval status
     */
    public List<UserResponse> getPendingApprovals() {
        log.info("Fetching all pending approval users");
        return userRepository.findByApprovalStatus(ApprovalStatus.PENDING).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Approve user registration
     */
    @Transactional
    public UserResponse approveUser(Long userId, Long approverId, String comment) {
        log.info("Approving user ID: {} by approver: {}", userId, approverId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        if (user.getApprovalStatus() == ApprovalStatus.APPROVED) {
            throw new BadRequestException("User is already approved");
        }

        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new ResourceNotFoundException("Approver not found"));

        if (approver.getRole() != Role.SOCIETY_MANAGER && approver.getRole() != Role.SOCIETY_OWNER) {
            throw new BadRequestException("Only managers or owners can approve users");
        }

        user.setApprovalStatus(ApprovalStatus.APPROVED);
        user.setIsActive(true);
        user.setIsVerified(true);
        user.setApproveAt(java.time.LocalDateTime.now());
        user.setApprovedBy(approverId);
        user.setReviewerComment(comment);

        user = userRepository.save(user);
        log.info("User {} approved successfully", userId);

        return mapToResponse(user);
    }

    /**
     * Reject user registration
     */
    @Transactional
    public UserResponse rejectUser(Long userId, Long approverId, String reason) {
        log.info("Rejecting user ID: {} by approver: {}", userId, approverId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        if (user.getApprovalStatus() == ApprovalStatus.REJECTED) {
            throw new BadRequestException("User is already rejected");
        }

        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new ResourceNotFoundException("Approver not found"));

        if (approver.getRole() != Role.SOCIETY_MANAGER && approver.getRole() != Role.SOCIETY_OWNER) {
            throw new BadRequestException("Only managers or owners can reject users");
        }

        user.setApprovalStatus(ApprovalStatus.REJECTED);
        user.setIsActive(false);
        user.setApproveAt(java.time.LocalDateTime.now());
        user.setApprovedBy(approverId);
        user.setReviewerComment("Rejected: " + reason);

        user = userRepository.save(user);
        log.info("User {} rejected", userId);

        return mapToResponse(user);
    }


    @Transactional
    public void activateUser(Long userId) {
        log.info("Activating user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        user.setIsActive(true);
        userRepository.save(user);

        log.info("User {} activated", userId);
    }

    public boolean existsByPhone(String phoneNo) {
        return userRepository.existsByPhoneNo(phoneNo);
    }

    private UserResponse mapToResponse(User user) {
        UserResponse.UserResponseBuilder builder = UserResponse.builder()
                .id(user.getId())
                .phoneNo(user.getPhoneNo())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .profileImageUrl(user.getProfileImageUrl())
                .role(user.getRole().name())
                .isActive(user.getIsActive())
                .isVerified(user.getIsVerified())
                .approvalStatus(user.getApprovalStatus() != null ? user.getApprovalStatus().name() : null)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt());

        // Add rental-specific fields
        if (user.getRole() == Role.RENTAL) {
            // Fetch rental profile if not already loaded
            RentalProfile profile = rentalProfileRepository.findByUserId(user.getId())
                    .orElse(null);

            if (profile != null) {
                builder.apartmentNo(profile.getApartmentNo())
                        .totalMembers(profile.getTotalMembers())
                        .jobProfile(profile.getJobProfile())
                        .workingLocation(profile.getWorkingLocation())
                        .bloodGroup(profile.getBloodGroup())
                        .moveInDate(profile.getMoveInDate());
            }
        }

        // Add security guard fields
        if (user.getRole() == Role.SECURITY_GUARD) {
            if (user.getSecurityGuard() != null) {
                builder.employeeId(user.getSecurityGuard().getEmployeeId())
                        .shiftType(user.getSecurityGuard().getShiftType() != null ?
                                user.getSecurityGuard().getShiftType().name() : null)
                        .gateNumber(user.getSecurityGuard().getGateNumber());
            }
        }

        return builder.build();
    }

}
