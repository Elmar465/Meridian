package com.projectnova.meridian.dto;


import com.projectnova.meridian.model.ProjectMemberRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectMemberResponse {

    private Long id;
    private Long userId;
    private String username;
    private String email;
    private String fullName;
    private String avatar;
    private ProjectMemberRole role;
    private LocalDateTime joinedAt;
}
