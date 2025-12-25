package com.projectnova.meridian.dto;


import com.projectnova.meridian.model.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateUserRequest  {


    @NotBlank(message = "username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;
    @NotBlank(message = "email is required")
    @Email(message = "email must be valid")
    private String email;
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    private String firstName;
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    private String lastName;
    private UserRole role;
}
