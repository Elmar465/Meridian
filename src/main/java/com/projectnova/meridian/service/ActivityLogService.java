package com.projectnova.meridian.service;


import com.projectnova.meridian.dao.ActivityLogRepository;
import com.projectnova.meridian.dao.IssueRepository;
import com.projectnova.meridian.dao.UserRepository;
import com.projectnova.meridian.dto.ActivityLogResponse;
import com.projectnova.meridian.exceptions.ResourceNotFoundException;
import com.projectnova.meridian.model.ActivityLog;
import com.projectnova.meridian.model.Issue;
import com.projectnova.meridian.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository  activityLogRepository;
    private final IssueRepository   issueRepository;
    private final UserRepository userRepository;




    @Transactional
    public ActivityLogResponse logSimpleActivity(Long issueId, Long userId, String action) {
        return logActivity(issueId, userId, action, null, null, null);
    }

    @Transactional
    public ActivityLogResponse logActivity(Long issueId, Long userId, String action,
                                           String fieldName, String oldValue, String newValue) {

        Issue issue =  issueRepository.findById(issueId).orElseThrow(()
                -> new ResourceNotFoundException("Issue with id not found " + issueId));
        User user = userRepository.findById(userId).orElseThrow(()
                -> new ResourceNotFoundException("User with id not found " + userId));

        ActivityLog activityLog = new ActivityLog();
        activityLog.setIssue(issue);
        activityLog.setUser(user);
        activityLog.setAction(action);
        activityLog.setFieldName(fieldName);
        activityLog.setOldValue(oldValue);
        activityLog.setNewValue(newValue);
        ActivityLog savedActivityLog = activityLogRepository.save(activityLog);
        return convertToResponse(savedActivityLog);
    }

    public ActivityLogResponse getActivityById(Long id){
        return convertToResponse(activityLogRepository.findById(id).orElseThrow(()
                -> new ResourceNotFoundException("Id not found!" + id)));
    }

    public Page<ActivityLogResponse> getActivitiesByUserId(Long userId, Pageable pageable) {
        Page<ActivityLog>  activityLogs = activityLogRepository.findByUserId(userId, pageable);
        return activityLogs.map(this::convertToResponse);
    }

    public Page<ActivityLogResponse> getActivitiesByIssueId(Long issueId, Pageable pageable){
        Page<ActivityLog>  activityLogs = activityLogRepository.findByIssueId(issueId, pageable);
        return activityLogs.map(this::convertToResponse);
    }

    public List<ActivityLogResponse> getActivitiesByIssueId(Long issueId) {
        List<ActivityLog>  activityLogs = activityLogRepository.findByIssueId(issueId);
        return convertToResponseList(activityLogs);
    }

    private List<ActivityLogResponse> convertToResponseList(List<ActivityLog> activityLogs) {
        return activityLogs.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private ActivityLogResponse convertToResponse(ActivityLog activityLog) {
        ActivityLogResponse activityLogResponse = new ActivityLogResponse();
        activityLogResponse.setId(activityLog.getId());
        activityLogResponse.setIssueId(activityLog.getIssue().getId());
        activityLogResponse.setUserId(activityLog.getUser().getId());
        activityLogResponse.setUsername(activityLog.getUser().getUsername());
        activityLogResponse.setUserFullName(activityLog.getUser().getFirstName() + " " + activityLog.getUser().getLastName());
        activityLogResponse.setAction(activityLog.getAction());
        activityLogResponse.setFieldName(activityLog.getFieldName());
        activityLogResponse.setOldValue(activityLog.getOldValue());
        activityLogResponse.setNewValue(activityLog.getNewValue());
        activityLogResponse.setCreatedAt(activityLog.getCreatedAt());
        return activityLogResponse;
    }
}
