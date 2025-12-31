package com.projectnova.meridian.dto;


import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrganizationRequest {


    @Size(min = 2, max = 100)
    private String name;
    @Size(max = 500)
    private String description;
    @Size(max = 500)
    private String logo;

}
