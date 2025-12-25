package com.projectnova.meridian.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentResponse {

    private Long id;
    private Long issueId;
    private String fileName;
    private String fileUrl;
    private Long fileSize;
    private String fileType;
    private Long uploadedById;
    private String uploadedByName;
    private LocalDateTime uploadedAt;
}
