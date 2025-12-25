package com.projectnova.meridian.dto;


import com.projectnova.meridian.model.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateInvitationRequest {

    @NotBlank
    @Email
    private String email;

    @NotNull
    private UserRole role;
}
