package com.projectnova.meridian.dto;


import com.projectnova.meridian.model.ProjectStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateProjectRequest {


    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "name cannot be more that 100 characters")
    private String name;
    @NotNull(message = "key must be provided")
    @Size(min = 2, max = 10, message = "Key must be between 2 and 10 characters")
    @Pattern(regexp = "^[A-Z]+$", message = "Key must contain only uppercase letters")
    private String key;
    private String description;
    private ProjectStatus status;
}
