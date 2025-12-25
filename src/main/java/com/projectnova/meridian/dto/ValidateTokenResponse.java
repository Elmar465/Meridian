package com.projectnova.meridian.dto;


import com.projectnova.meridian.model.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidateTokenResponse {

    private Boolean valid;
    private String email;
    private UserRole role;
    private String message;
}
