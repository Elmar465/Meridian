package com.projectnova.meridian.service;


import com.projectnova.meridian.dao.CommentRepository;
import com.projectnova.meridian.dao.IssueRepository;
import com.projectnova.meridian.dao.UserRepository;
import com.projectnova.meridian.dto.CommentResponse;
import com.projectnova.meridian.dto.CreateCommentRequest;
import com.projectnova.meridian.exceptions.ResourceNotFoundException;
import com.projectnova.meridian.exceptions.UnauthorizedException;
import com.projectnova.meridian.model.Comment;
import com.projectnova.meridian.model.Issue;
import com.projectnova.meridian.model.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final IssueRepository issueRepository;
    private final EmailService emailService;
    private final WebSocketService webSocketService;


    private Comment convertToEntity(CreateCommentRequest createCommentRequest, Issue issue, User user) {
        Comment comment = new Comment();
        comment.setUser(user);
        comment.setIssue(issue);
        comment.setContent(createCommentRequest.getContent());
        return comment;
    }


    private CommentResponse convertToResponse(Comment comment) {
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setIssueId(comment.getIssue().getId());  // ✅ Just the ID
        response.setUserId(comment.getUser().getId());    // ✅ User ID
        response.setUsername(comment.getUser().getUsername());
        response.setUserFullName(comment.getUser().getFirstName() + " " + comment.getUser().getLastName());
        response.setUserAvatar(comment.getUser().getAvatar());
        response.setContent(comment.getContent());
        response.setCreatedAt(comment.getCreatedAt());
        response.setUpdatedAt(comment.getUpdatedAt());
        return response;
    }

    private List<CommentResponse> convertToResponseList(List<Comment> comments) {
        return comments.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public Page<CommentResponse> getCommentsByIssueId(Long issueId, Pageable  pageable) {
        Page<Comment> comments = commentRepository.findByIssueId(issueId, pageable);
        return comments.map(this::convertToResponse);
    }

    public List<CommentResponse> getCommentsByIssueId(Long issueId) {
        List<Comment> comments = commentRepository.findByIssueId(issueId);
        return convertToResponseList(comments);
    }
    public Page<CommentResponse> getCommentsByUserId(Long userId, Pageable  pageable) {
        Page<Comment> comments = commentRepository.findByUserId(userId, pageable);
        return comments.map(this::convertToResponse);
    }

    public CommentResponse getCommentById(Long id) {
        return convertToResponse(commentRepository.findById(id).orElseThrow(()
                -> new ResourceNotFoundException("Comment not found" + id)));
    }

    @Transactional
    public CommentResponse createComment(CreateCommentRequest createCommentRequest, Long issueId, Long userId) {
        Issue existingIssue = issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found" + issueId));

        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User  not found" + userId));

        Comment comment =  convertToEntity(createCommentRequest, existingIssue, existingUser);
        Comment saveComment  = commentRepository.save(comment);
        emailService.sendNewCommentEmail(saveComment);
        webSocketService.notifyCommentAdded(saveComment);
        return convertToResponse(saveComment);
    }

    @Transactional
    public CommentResponse updateComment(Long id, String content, Long userId){
        Comment existingId =  commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found" + id));

        if(!existingId.getUser().getId().equals(userId)){
            throw new UnauthorizedException("You can only update your own comments");
        }
        existingId.setContent(content);
        Comment savedComment = commentRepository.save(existingId);
        return convertToResponse(savedComment);
    }

    @Transactional
    public void deleteComment(Long id, Long userId) {
        Comment existingComment =  commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found" + id));

        if(!existingComment.getUser().getId().equals(userId)){
            throw new UnauthorizedException("You can only delete your own comments");
        }
        commentRepository.delete(existingComment);
    }
}
