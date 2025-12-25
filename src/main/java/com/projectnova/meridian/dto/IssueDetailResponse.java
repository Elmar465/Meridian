package com.projectnova.meridian.dto;


import com.projectnova.meridian.model.IssueStatus;
import com.projectnova.meridian.model.IssueType;
import com.projectnova.meridian.model.Priority;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IssueDetailResponse {

    private Long id;
    private Long projectId;
    private Integer issueNumber;
    private String issueKey;
    private String title;
    private String description;
    private IssueType type;
    private IssueStatus status;
    private Priority priority;
    private Long reporterId;
    private String reporterName;
    private Long assigneeId;
    private String assigneeName;
    private LocalDate dueDate;
    private List<CommentResponse> comments;
    private List<AttachmentResponse> attachments;
    private List<ActivityLogResponse> activities;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
