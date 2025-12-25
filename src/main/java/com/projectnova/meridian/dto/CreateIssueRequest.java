package com.projectnova.meridian.dto;

import com.projectnova.meridian.model.IssueType;
import com.projectnova.meridian.model.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateIssueRequest {


    @NotBlank(message = "title is required")
    @Size(max = 200, message = "title cannot be more that 200 characters")
    private String title;
    @NotBlank(message = "Description is required")
    @Size(min =  10, message = "Description must not be at least 10 characters")
    private String description;
    @NotNull(message = "Type is required")
    private IssueType type;
    @NotNull(message = "Priority is required")
    private Priority priority;
    private Long assigneeId;
    private LocalDate dueDate;

}
