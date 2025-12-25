package com.projectnova.meridian.dto;

import com.projectnova.meridian.model.IssueStatus;
import com.projectnova.meridian.model.Priority;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IssueUpdateMessage {

    private Long issueId;
    private String issueKey;
    private String title;
    private IssueStatus status;
    private Priority priority;
    private String updatedBy;
    private String action;  // "CREATED", "UPDATED", "ASSIGNED", "STATUS_CHANGED"
}
