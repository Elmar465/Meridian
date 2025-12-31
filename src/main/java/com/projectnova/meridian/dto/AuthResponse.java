package com.projectnova.meridian.dto;


import com.projectnova.meridian.model.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {


    private  Long  id;
    private String token;
    private   String username;
    private UserRole  role;
    private Long organizationId;
    private String organizationName;
    private String organizationSlug;
}
