package com.projectnova.meridian.dto;


import com.projectnova.meridian.model.ProjectStatus;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProjectRequest {

    @Size(max = 100)
    private String name;
    private String description;
    private ProjectStatus status;
}
