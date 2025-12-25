package com.projectnova.meridian.dto;


import com.projectnova.meridian.model.IssueStatus;
import com.projectnova.meridian.model.IssueType;
import com.projectnova.meridian.model.Priority;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IssueResponse {


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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
