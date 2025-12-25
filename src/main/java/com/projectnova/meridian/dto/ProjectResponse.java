package com.projectnova.meridian.dto;


import com.projectnova.meridian.model.ProjectStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectResponse {


    private Long id;
    private String name;
    private String key;
    private String description;
    private Long ownerId;
    private String ownerName;
    private ProjectStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
