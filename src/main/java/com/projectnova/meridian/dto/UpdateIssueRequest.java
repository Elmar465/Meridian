package com.projectnova.meridian.dto;


import com.projectnova.meridian.model.IssueStatus;
import com.projectnova.meridian.model.IssueType;
import com.projectnova.meridian.model.Priority;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateIssueRequest {

    @Size(max = 200)
    private String title;
    private String description;
    private IssueType type;
    private IssueStatus status;
    private Priority priority;
    private Long assigneeId;
    private LocalDate dueDate;
}
