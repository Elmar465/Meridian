package com.projectnova.meridian.controller;

import com.projectnova.meridian.service.ActivityLogService;
import com.projectnova.meridian.dto.ActivityLogResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
public class ActivityLogController {

    private final ActivityLogService activityLogService;



    @GetMapping("/{id}")
    public ResponseEntity<ActivityLogResponse> getActivityById(@PathVariable Long id){
        ActivityLogResponse activityLogResponse = activityLogService.getActivityById(id);
        return ResponseEntity.ok(activityLogResponse);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<ActivityLogResponse>> getActivityLogs(@PathVariable Long userId, Pageable pageable){
        Page<ActivityLogResponse> attachmentResponse = activityLogService.getActivitiesByUserId(userId, pageable);
        return ResponseEntity.ok(attachmentResponse);
    }

    @GetMapping("/issue/{issueId}")
    public ResponseEntity<Page<ActivityLogResponse>> getActivitiesByIssueId(@PathVariable Long issueId, Pageable pageable) {
        Page<ActivityLogResponse> activityLogResponses = activityLogService.getActivitiesByIssueId(issueId, pageable);
        return ResponseEntity.ok(activityLogResponses);
    }

}
