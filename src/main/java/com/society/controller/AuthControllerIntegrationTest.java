package com.society.controller;

import com.society.SocietyManagementApplication;
import com.society.dto.request.OtpVerificationRequest;
import com.society.dto.request.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
        classes = SocietyManagementApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    //Option 1: Create ObjectMapper manually (Recommended)
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper =
            new com.fasterxml.jackson.databind.ObjectMapper();

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
    }

    @Test
    void testRegister_Success() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("Test User")
                .phoneNo("9876543210")
                .email("test@test.com")
                .role("RENTAL")
                .apartmentNo("A-101")
                .totalMembers(4)
                .build();

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testSendOtp_Success() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("Test User")
                .phoneNo("9876543211")
                .role("RENTAL")
                .apartmentNo("A-102")
                .totalMembers(3)
                .build();

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/auth/login")
                        .param("phoneNo", "9876543211"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testVerifyOtp_Success() throws Exception {
        // First register
        RegisterRequest registerRequest = RegisterRequest.builder()
                .fullName("Test User")
                .phoneNo("9876543212")
                .role("RENTAL")
                .apartmentNo("A-103")
                .totalMembers(2)
                .build();

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Send OTP
        mockMvc.perform(post("/auth/login")
                        .param("phoneNo", "9876543212"))
                .andExpect(status().isOk());

        // Verify OTP (mock accepts "123456")
        OtpVerificationRequest verifyRequest = OtpVerificationRequest.builder()
                .phoneNo("9876543212")
                .otp("123456")
                .build();

        mockMvc.perform(post("/auth/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").exists());
    }
}
