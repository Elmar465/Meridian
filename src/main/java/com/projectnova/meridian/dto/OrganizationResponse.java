package com.projectnova.meridian.dto;


import com.projectnova.meridian.model.OrganizationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationResponse {

    private Long id;
    private String name;
    private String slug;
    private String description;
    private String logo;
    private Long ownerId;
    private String ownerName;
    private Integer memberCount;
    private OrganizationStatus status;
    private Integer projectCount;
    private LocalDateTime createdAt;
}
