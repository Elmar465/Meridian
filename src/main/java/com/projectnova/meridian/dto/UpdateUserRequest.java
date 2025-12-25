package com.projectnova.meridian.dto;


import com.projectnova.meridian.model.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserRequest {


    @Size(max = 50)
    private String firstName;
    @Size(max = 50)
    private String lastName;
    @Email(message = "Email must be valid")
    private String email;
    private String avatar;
    private UserRole role;

}
