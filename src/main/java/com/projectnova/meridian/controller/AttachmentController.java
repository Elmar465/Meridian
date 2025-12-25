package com.projectnova.meridian.controller;


import com.projectnova.meridian.dto.AttachmentResponse;
import com.projectnova.meridian.service.AttachmentService;
import com.projectnova.meridian.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/attachments")
public class AttachmentController {


    private final AttachmentService  attachmentService;
    private final UserContext userContext;

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadAttachment(@PathVariable Long id) throws Exception {
        byte[] attachmentResponse = attachmentService.downloadAttachment(id);
        return  ResponseEntity.ok().body(attachmentResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAttachment(@PathVariable Long id){
        Long userId = userContext.getCurrentUserId();
        attachmentService.deleteAttachment(id, userId);
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/issue/{issueId}")
    public ResponseEntity<AttachmentResponse> uploadAttachment(@PathVariable Long issueId,
                                                               @RequestParam("file") MultipartFile file
                                                               ) throws IOException {
        Long userId= userContext.getCurrentUserId();
        AttachmentResponse attachmentResponse = attachmentService.uploadAttachment(issueId,file, userId);
        return new ResponseEntity<>(attachmentResponse, HttpStatus.CREATED);
    }


    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AttachmentResponse>>   getAttachmentsByUploadedById(@PathVariable Long userId) {
        List<AttachmentResponse> attachmentResponse = attachmentService.getAttachmentsByUploadedById(userId);
        return ResponseEntity.ok(attachmentResponse);
    }


    @GetMapping("/{id}")
    public ResponseEntity<AttachmentResponse> getAttachmentById(@PathVariable Long id){
        return ResponseEntity.ok(attachmentService.getAttachmentById(id));
    }


    @GetMapping("/issue/{issueId}")
    public ResponseEntity<List<AttachmentResponse>> getAttachmentsByIssueId(@PathVariable Long issueId) {
        List<AttachmentResponse> attachmentResponses = attachmentService.getAttachmentsByIssueId(issueId);
        return ResponseEntity.ok(attachmentResponses);
    }
}
