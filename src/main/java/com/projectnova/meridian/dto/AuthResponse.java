package com.projectnova.meridian.dto;


import com.projectnova.meridian.model.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {


    Long  id;
    String token;
    String username;
    UserRole  role;

}
