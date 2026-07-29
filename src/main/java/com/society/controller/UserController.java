package com.society.controller;

import com.society.dto.request.UpdateProfileRequest;
import com.society.dto.response.ApiResponse;
import com.society.dto.response.RegistrationStatusResponse;
import com.society.dto.response.UserResponse;
import com.society.entity.Role;
import com.society.entity.User;
import com.society.exception.ResourceNotFoundException;
import com.society.repository.UserRepository;
import com.society.security.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "2. User Management")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    @GetMapping("/get/current/user")
    @Operation(summary = "Get current logged-in user profile")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails){
        return ResponseEntity.ok(ApiResponse.success("Profile fetched",userService.getUserByPhone(userDetails.getUsername())));
    }

    @PutMapping("/me")
    @Operation(summary = "Update current user profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateMyProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByPhoneNo(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UserResponse updated = userService.updateProfile(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated", updated));
    }

    //New: Get registration/approval status
    @GetMapping("/me/registration-status")
    public ResponseEntity<ApiResponse<RegistrationStatusResponse>> getRegistrationStatus(@AuthenticationPrincipal UserDetails userDetails){
        User user = userRepository.findByPhoneNo(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        RegistrationStatusResponse status = RegistrationStatusResponse.builder()
                .userId(user.getId())
                .fullName(user.getFullName())
                .phoneNo(user.getPhoneNo())
                .email(user.getEmail())
                .approvalStatus(user.getApprovalStatus())
                .apartmentNo(user.getRentalProfile() != null ? user.getRentalProfile().getApartmentNo() : null)
                .submittedAt(user.getCreatedAt())
                .reviewedAt(user.getApproveAt())
                .build();

        return ResponseEntity.ok(ApiResponse.success("Status fetched", status));
    }

    @PutMapping("/update/current/user")
    @Operation(summary = "Update current user profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal UserDetails userDetails
            ){
        UserResponse current = userService.getUserByPhone(userDetails.getUsername());
        UserResponse updated = userService.updateProfile(current.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated", updated));
    }

    @GetMapping("/get/{id}")
    @PreAuthorize("hasAnyRole('SOCIETY_MANAGER','SOCIETY_OWNER')")
    @Operation(summary = "Get user by ID(Manager/Owner)")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id){
        return ResponseEntity.ok(ApiResponse.success("User fetched", userService.getUserById(id)));
    }

    @GetMapping("/get/rentals")
    @PreAuthorize("hasAnyRole('SOCIETY_MANAGER','SOCIETY_OWNER','SECURITY_GUARD')")
    @Operation(summary = "Get all rentals")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllRentals(){
        return ResponseEntity.ok(ApiResponse.success("Rentals fetched",userService.getAllRentals()));
    }

    @GetMapping("/security-guards")
    @PreAuthorize("hasAnyRole('SOCIETY_MANAGER','SOCIETY_OWNER')")
    @Operation(summary = "Get all security guards")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllSecurityGuards() {
        return ResponseEntity.ok(ApiResponse.success("Guards fetched", userService.getAllSecurityGuards()));
    }

    @GetMapping("/get/user/role")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getByRole(@RequestParam Role role){
        return ResponseEntity.ok(ApiResponse.success("User fetched",userService.getAllUsersByRole(role)));
    }

    @GetMapping("/pending-approvals")
    @PreAuthorize("hasAnyRole('SOCIETY_MANAGER','SOCIETY_OWNER')")
    @Operation(summary = "Get all users pending approval (Manager/Owner)")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getPendingApprovals() {
        return ResponseEntity.ok(ApiResponse.success("Pending users fetched",
                userService.getPendingApprovals()));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('SOCIETY_MANAGER','SOCIETY_OWNER')")
    @Operation(summary = "Approve user registration (Manager/Owner)")
    public ResponseEntity<ApiResponse<UserResponse>> approveUser(
            @PathVariable Long id,
            @RequestParam(required = false) String comment,
            @AuthenticationPrincipal UserDetails userDetails) {

        User manager = userRepository.findByPhoneNo(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));

        UserResponse approved = userService.approveUser(id, manager.getId(), comment);
        return ResponseEntity.ok(ApiResponse.success("User approved", approved));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('SOCIETY_MANAGER','SOCIETY_OWNER')")
    @Operation(summary = "Reject user registration (Manager/Owner)")
    public ResponseEntity<ApiResponse<UserResponse>> rejectUser(
            @PathVariable Long id,
            @RequestParam String reason,
            @AuthenticationPrincipal UserDetails userDetails) {

        User manager = userRepository.findByPhoneNo(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));

        UserResponse rejected = userService.rejectUser(id, manager.getId(), reason);
        return ResponseEntity.ok(ApiResponse.success("User rejected", rejected));
    }

}
