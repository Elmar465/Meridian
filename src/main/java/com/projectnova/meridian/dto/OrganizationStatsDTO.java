package com.projectnova.meridian.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationStatsDTO {

    private Long totalMembers;
    private Long totalProjects;
    private Long pendingInvitations;
}
