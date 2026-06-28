package com.society.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProfileRequest {
    @Size(min = 2, max = 100)
    private String fullName;

    @Email
    private String email;

    private String profileImageUrl;
    private String apartmentNo;
    private Integer totalMembers;
    private String jobProfile;
    private String workingLocation;
    private String bloodGroup;


}
