package com.projectnova.meridian.controller;


import com.projectnova.meridian.dto.CommentResponse;
import com.projectnova.meridian.dto.CreateCommentRequest;
import com.projectnova.meridian.service.CommentService;
import com.projectnova.meridian.utils.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comments")
public class CommentController {


    private final CommentService commentService;
    private final UserContext userContext;


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCommentById(@PathVariable Long id){
        Long userId = userContext.getCurrentUserId();
        commentService.deleteComment(id, userId);
       return  ResponseEntity.noContent().build();
    }


    @PutMapping("/{id}")
    public ResponseEntity<CommentResponse> updateComment(@PathVariable Long id, @RequestParam String content)
                                                          {
        Long userId = userContext.getCurrentUserId();
        CommentResponse commentResponse = commentService.updateComment(id, content, userId);
        return new ResponseEntity<>(commentResponse, HttpStatus.OK);
    }


    @PostMapping("/issue/{issueId}")
    public ResponseEntity<CommentResponse> createComment(@PathVariable Long issueId,
                                                         @RequestBody @Valid CreateCommentRequest request
                                                         ){
        Long userId = userContext.getCurrentUserId();
        CommentResponse commentResponse = commentService.createComment(request, issueId,userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(commentResponse);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<CommentResponse>> getCommentsByUserId(@PathVariable Long userId, Pageable pageable){
        Page<CommentResponse> commentResponse = commentService.getCommentsByUserId(userId, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(commentResponse);
    }


    @GetMapping("/{id}")
    public ResponseEntity<CommentResponse> getCommentById(@PathVariable Long id) {
        CommentResponse commentResponse = commentService.getCommentById(id);
        return ResponseEntity.status(HttpStatus.OK).body(commentResponse);
    }

    @GetMapping("/issue/{issueId}")
    public ResponseEntity<Page<CommentResponse>>  getCommentsByIssueId(@PathVariable Long issueId, Pageable pageable){
        Page<CommentResponse> commentResponse = commentService.getCommentsByIssueId(issueId, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(commentResponse);
    }
}
