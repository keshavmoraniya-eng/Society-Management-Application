package com.society.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String token;
    private String tokenType;
    private Long expiresIn;
    private Long userId;
    private String phoneNo;
    private String fullName;
    private String email;
    private String role;
    private String profileImageUrl;
}
