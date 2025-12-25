package com.projectnova.meridian.dto;


import com.projectnova.meridian.model.ProjectMemberRole;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddMemberRequest {


    @NotNull(message = "User ID is required")
    private Long userId;
    private ProjectMemberRole role;
}
