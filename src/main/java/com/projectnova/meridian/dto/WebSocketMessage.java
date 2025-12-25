package com.projectnova.meridian.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketMessage {

    private String type; // "ISSUE_CREATED", "ISSUE_UPDATED", "COMMENT_ADDED", etc.
    private Long entityId; // ID issue, project etc
    private String message;
    private Object data; // Actual payload
    private LocalDateTime timeStamp;

    public WebSocketMessage(String type, Long entityId, String message, Object data) {
        this.type = type;
        this.entityId = entityId;
        this.message = message;
        this.data = data;
        this.timeStamp = LocalDateTime.now();
    }
}
