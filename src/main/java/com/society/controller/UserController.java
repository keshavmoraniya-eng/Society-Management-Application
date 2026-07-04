package com.society.controller;

import com.society.dto.request.UpdateProfileRequest;
import com.society.dto.response.ApiResponse;
import com.society.dto.response.UserResponse;
import com.society.entity.Role;
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

    @GetMapping("/get/current/user")
    @Operation(summary = "Get current logged-in user profile")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails){
        return ResponseEntity.ok(ApiResponse.success("Profile fetched",userService.getUserByPhone(userDetails.getUsername())));
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

    @GetMapping("/get/user/role")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getByRole(@RequestParam Role role){
        return ResponseEntity.ok(ApiResponse.success("User fetched",userService.getAllUsersByRole(role)));
    }

}
