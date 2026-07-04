package com.society.security;

import com.society.dto.request.UpdateProfileRequest;
import com.society.dto.response.UserResponse;
import com.society.entity.RentalProfile;
import com.society.entity.Role;
import com.society.entity.SecurityGuard;
import com.society.entity.User;
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

    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request){
        User user = userRepository.findById(userId)
                .orElseThrow(()->new ResourceNotFoundException("User not found"));

        if (request.getFullName() != null){
            user.setFullName(request.getFullName());
        }

        if (request.getEmail() != null){
            user.setEmail(request.getEmail());
        }

        if (request.getProfileImageUrl() != null){
            user.setProfileImageUrl(request.getProfileImageUrl());
        }

        user = userRepository.save(user);

        if (user.getRole() == Role.RENTAL){
            RentalProfile profile = rentalProfileRepository.findByUserId(userId)
                    .orElseThrow(()->new ResourceNotFoundException("Rental profile not found"));
            if (request.getApartmentNo() != null){
                profile.setApartmentNo(request.getApartmentNo());
            }

            if (request.getTotalMembers() != null){
                profile.setTotalMembers(request.getTotalMembers());
            }

            if (request.getJobProfile() != null){
                profile.setJobProfile(request.getJobProfile());
            }

            if (request.getWorkingLocation() != null){
                profile.setWorkingLocation(request.getWorkingLocation());
            }

            if (request.getBloodGroup() != null){
                profile.setBloodGroup(request.getBloodGroup());
            }

            rentalProfileRepository.save(profile);
        }
        log.info("Profile updated for user: {}", userId);
        return mapToResponse(user);
    }

    public void deactivateUser(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(()->new ResourceNotFoundException("User not found"));
        user.setIsActive(false);
        userRepository.save(user);
        log.info("User deactivated: {}",userId);
    }

    private UserResponse mapToResponse(User user){
        UserResponse.UserResponseBuilder builder = UserResponse.builder()
                .id(user.getId())
                .phoneNo(user.getPhoneNo())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .profileImageUrl(user.getProfileImageUrl())
                .role(user.getRole().name())
                .isActive(user.getIsActive())
                .isVerified(user.getIsVerified());

        if(user.getRole() == Role.RENTAL && user.getRentalProfile() != null){
            RentalProfile rentalProfile = user.getRentalProfile();
            builder.apartmentNo(rentalProfile.getApartmentNo())
                    .totalMembers(rentalProfile.getTotalMembers())
                    .jobProfile(rentalProfile.getJobProfile())
                    .workingLocation(rentalProfile.getWorkingLocation())
                    .bloodGroup(rentalProfile.getBloodGroup())
                    .moveInDate(rentalProfile.getMoveInDate());

        }

        if (user.getRole() == Role.SECURITY_GUARD && user.getSecurityGuard() != null){
            SecurityGuard securityGuard = user.getSecurityGuard();
            builder.employeeId(securityGuard.getEmployeeId())
                    .shiftType(securityGuard.getShiftType() != null ? securityGuard.getShiftType().name() : null)
                    .gateNumber(securityGuard.getGateNumber());
        }

        return builder.build();
    }
}
