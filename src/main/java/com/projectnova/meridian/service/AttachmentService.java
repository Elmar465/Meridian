package com.projectnova.meridian.service;


import com.projectnova.meridian.dao.AttachmentRepository;
import com.projectnova.meridian.dao.IssueRepository;
import com.projectnova.meridian.dao.UserRepository;
import com.projectnova.meridian.dto.AttachmentResponse;
import com.projectnova.meridian.exceptions.ResourceNotFoundException;
import com.projectnova.meridian.exceptions.UnauthorizedException;
import com.projectnova.meridian.model.Attachment;
import com.projectnova.meridian.model.Issue;
import com.projectnova.meridian.model.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final IssueRepository issueRepository;
    private final UserRepository userRepository;
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    private static final List<String> ALLOWED_FILE_TYPES = List.of(
            "image/jpeg", "image/png", "image/gif", "image/webp",
            "application/pdf", "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "text/plain", "application/zip"
    );


    @Value("${file.upload-dir:uploads}")
    private String uploadDir;




    @Transactional
    public void deleteAttachment(Long id, Long  userId) {
        Attachment attachment = attachmentRepository.findById(id).orElseThrow(() -> new RuntimeException("Attachment not found"));

        if (!attachment.getUploadedBy().getId().equals(userId)) {
            throw new UnauthorizedException("You can only delete your own attachments");
        }
        attachmentRepository.delete(attachment);
    }



    public byte[] downloadAttachment(Long id) throws Exception {
        Attachment  attachment = attachmentRepository.findById(id).orElseThrow(()
                -> new ResourceNotFoundException("Attachment not found"));
        return  Files.readAllBytes(Paths.get(attachment.getFileUrl()));
    }

    @Transactional
    public AttachmentResponse uploadAttachment(Long issueId, MultipartFile file, Long userId) throws IOException {
        if(file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }
        if(file.getSize() > MAX_FILE_SIZE) {
            throw  new BadRequestException("File size records maximum limit of 10MB");
        }

        if(!ALLOWED_FILE_TYPES.contains(file.getContentType())) {
            throw new BadRequestException("File type is not allowed" + file.getContentType());
        }
        Issue issue = issueRepository.findById(issueId).orElseThrow(()
                -> new ResourceNotFoundException("Issue with id not found " + issueId));

        User user = userRepository.findById(userId).orElseThrow(()
                -> new ResourceNotFoundException("User id not found " + userId));
        String fileUrl =saveFile(file);
        Attachment attachment = convertToEntity(file, issue, fileUrl, user);
        Attachment savedAttachment = attachmentRepository.save(attachment);
        return convertToResponse(savedAttachment);
    }
    public List<AttachmentResponse> getAttachmentsByIssueId(Long issueId) {
        List<Attachment> attachments = attachmentRepository.findByIssueId(issueId);
        return convertToResponseList(attachments);
    }
    public List<AttachmentResponse> getAttachmentsByUploadedById(Long id) {
        return convertToResponseList(attachmentRepository.findByUploadedById(id));
    }

    public AttachmentResponse getAttachmentById(Long id) {
        return convertToResponse(attachmentRepository.findById(id).orElseThrow(()
                -> new ResourceNotFoundException("Attachment not found")));
    }


    // For internal use (IssueService)
    private String saveFile(MultipartFile file) throws IOException {


        Path uploadPath = Paths.get(uploadDir);
        if(!Files.exists(uploadPath)){
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String originalFilename  = file.getOriginalFilename();
        String fileName = System.currentTimeMillis() + "_"  +  originalFilename;

        // Save files
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream() , filePath, StandardCopyOption.REPLACE_EXISTING);

        return uploadDir + "/" + fileName;
    }

    private List<AttachmentResponse> convertToResponseList(List<Attachment> attachments) {
        return attachments.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private Attachment convertToEntity(MultipartFile file, Issue issue, String fileUrl, User user) {
        Attachment attachment = new Attachment();
        attachment.setIssue(issue);
        attachment.setFileUrl(fileUrl);
        attachment.setFileName(file.getOriginalFilename());
        attachment.setFileSize(file.getSize());
         attachment.setFileType(file.getContentType());
        attachment.setUploadedBy(user);
        return attachment;
    }

    private AttachmentResponse convertToResponse(Attachment attachment){
        AttachmentResponse attachmentResponse = new AttachmentResponse();

        attachmentResponse.setId(attachment.getId());
        attachmentResponse.setIssueId(attachment.getIssue().getId());
        attachmentResponse.setFileName(attachment.getFileName());
        attachmentResponse.setFileSize(attachment.getFileSize());
        attachmentResponse.setFileType(attachment.getFileType());
        attachmentResponse.setUploadedById(attachment.getUploadedBy().getId());
        attachmentResponse.setUploadedByName(attachment.getUploadedBy().getFirstName()
                + " " + attachment.getUploadedBy().getLastName());
        attachmentResponse.setFileUrl(attachment.getFileUrl());
        attachmentResponse.setUploadedAt(attachment.getUploadedAt());
        return attachmentResponse;
    }
}
