package com.projectnova.meridian.dto;


import com.projectnova.meridian.model.InvitationStatus;
import com.projectnova.meridian.model.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvitationResponse {


    private Long id;
    private String email;
    private UserRole role;
    private InvitationStatus status;
    private String invitedByUsername;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
}
