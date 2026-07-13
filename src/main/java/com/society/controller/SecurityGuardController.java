package com.society.controller;

import com.society.dto.response.ApiResponse;
import com.society.dto.response.SecurityGuardResponse;
import com.society.service.SecurityGuardService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/security-guards")
@RequiredArgsConstructor
@Tag(name = "Security Guard Controller")
public class SecurityGuardController {
    private final SecurityGuardService guardService;

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<SecurityGuardResponse>>> getAll(){
        return ResponseEntity.ok(ApiResponse.success("Guards", guardService.getAllSecurityGuards()));
    }

    public ResponseEntity<ApiResponse<SecurityGuardResponse>> getById(@PathVariable Long id){
        return ResponseEntity.ok(ApiResponse.success("Guard", guardService.getById(id)));
    }

}


