package com.society.service;

import com.society.dto.request.AnnouncementRequest;
import com.society.dto.response.AnnouncementResponse;
import com.society.entity.Announcement;
import com.society.entity.Role;
import com.society.entity.User;
import com.society.exception.BadRequestException;
import com.society.exception.ResourceNotFoundException;
import com.society.repository.AnnouncementRepository;
import com.society.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final UserRepository userRepository;

    public AnnouncementResponse createAnnouncement(AnnouncementRequest request, Long managerId){
        User manager = userRepository.findById(managerId)
                .orElseThrow(()-> new ResourceNotFoundException("Manager not found"));

        if (manager.getRole() != Role.SOCIETY_MANAGER && manager.getRole() != Role.SOCIETY_OWNER){
            throw new BadRequestException("Only managers can post announcements");
        }

        Announcement announcement = Announcement.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .imageUrl(request.getImageUrl())
                .postedBy(manager)
                .build();

        announcement = announcementRepository.save(announcement);
        log.info("Announcement posted: {}", announcement.getId());
        return mapToResponse(announcement);
    }

    public List<AnnouncementResponse> getAllAnnouncement(){
        return announcementRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private AnnouncementResponse mapToResponse(Announcement announcement){
        return AnnouncementResponse.builder()
                .id(announcement.getId())
                .title(announcement.getTitle())
                .description(announcement.getDescription())
                .category(announcement.getCategory())
                .imageUrl(announcement.getImageUrl())
                .postedByName(announcement.getPostedBy().getFullName())
                .createdAt(announcement.getCreatedAt())
                .build();
    }
}
