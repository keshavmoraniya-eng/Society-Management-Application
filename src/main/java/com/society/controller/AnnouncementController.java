package com.society.controller;

import com.society.dto.request.AnnouncementRequest;
import com.society.dto.response.AnnouncementResponse;
import com.society.dto.response.ApiResponse;
import com.society.service.AnnouncementService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/announcement")
@RequiredArgsConstructor
@Tag(name = "Announcement Controller")
public class AnnouncementController {
    private final AnnouncementService announcementService;

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('SOCIETY_MANAGER', 'SOCIETY_OWNER')")
    public ResponseEntity<ApiResponse<AnnouncementResponse>> create(@RequestBody AnnouncementRequest request, @AuthenticationPrincipal UserDetails userDetails){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Posted", announcementService.createAnnouncement(request,1L)));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<AnnouncementResponse>>> getAll(){
        return ResponseEntity.ok(ApiResponse.success("Announcement", announcementService.getAllAnnouncement()));
    }


}
