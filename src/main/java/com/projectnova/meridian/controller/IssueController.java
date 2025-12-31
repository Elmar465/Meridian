package com.projectnova.meridian.controller;


import com.projectnova.meridian.dto.CreateIssueRequest;
import com.projectnova.meridian.dto.IssueDetailResponse;
import com.projectnova.meridian.dto.IssueResponse;
import com.projectnova.meridian.dto.UpdateIssueRequest;
import com.projectnova.meridian.model.IssueStatus;
import com.projectnova.meridian.model.IssueType;
import com.projectnova.meridian.model.Priority;
import com.projectnova.meridian.model.User;
import com.projectnova.meridian.service.EmailService;
import com.projectnova.meridian.service.IssueService;
import com.projectnova.meridian.utils.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/issues")
public class IssueController {

    private final IssueService issueService;
    private final UserContext userContext;
    private final EmailService emailService;


    @PutMapping("/{id}/priority")
    public ResponseEntity<IssueResponse> updateIssuePriority(@PathVariable Long id,
                                                             @RequestParam Priority priority
                                                             ){
        Long userId = userContext.getCurrentUser().getId();
        IssueResponse issueResponse = issueService.updateIssuePriority(id, priority, userId);
        return ResponseEntity.status(HttpStatus.OK).body(issueResponse);
    }

    @GetMapping()
    public ResponseEntity<Page<IssueResponse>> getAllIssues(Pageable pageable,
                                                            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(issueService.getAllIssues(pageable, currentUser));
    }


    @PutMapping("/{id}/status")
    public ResponseEntity<IssueResponse> updateIssueStatus(@PathVariable Long id,
                                                           @RequestParam IssueStatus status
                                                           ){
        Long userId = userContext.getCurrentUserId();
        IssueResponse issueResponse = issueService.updateIssueStatus(id, status, userId);
        return ResponseEntity.status(HttpStatus.OK).body(issueResponse);
    }



    @PutMapping("/{id}/assign")
    public ResponseEntity<IssueResponse> assignIssue(@PathVariable Long id, @RequestParam Long assigneeId)
                                                     {
         Long userId = userContext.getCurrentUserId();
        IssueResponse issueResponse = issueService.assignIssue(id, assigneeId, userId);

        return ResponseEntity.status(HttpStatus.OK).body(issueResponse);
    }



    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void>  deleteIssue(@PathVariable Long id){
        issueService.deleteIssue(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<IssueResponse>  updateIssue(@PathVariable Long id,
                                                      @RequestBody @Valid UpdateIssueRequest request
                                                      ){
        Long userId = userContext.getCurrentUserId();
        IssueResponse issueResponse = issueService.updateIssue(id,request,userId);
        return ResponseEntity.status(HttpStatus.OK).body(issueResponse);
    }

    @PostMapping("/project/{projectId}")
    public ResponseEntity<IssueResponse>   createIssue(@PathVariable Long projectId,
                                                       @RequestBody @Valid CreateIssueRequest request
                                                        ){
        Long reporterId = userContext.getCurrentUserId();
        IssueResponse issueResponse = issueService.createIssue(projectId, request,reporterId);
        return ResponseEntity.status(HttpStatus.CREATED).body(issueResponse);
    }

    @GetMapping("/project/{projectId}/count")
    public ResponseEntity<Long>  getIssueCount(@PathVariable Long projectId) {
        Long issueCount = issueService.getIssueCount(projectId);
        return new ResponseEntity<>(issueCount, HttpStatus.OK);
    }


    @GetMapping("/project/{projectId}/assignee/{assigneeId}")
    public ResponseEntity<Page<IssueResponse>>  getIssuesByProjectIdAndAssigneeId(@PathVariable Long projectId,
                                                                                  @PathVariable Long assigneeId,
                                                                                  Pageable pageable) {
        Page<IssueResponse> ids =  issueService.getIssuesByProjectIdAndAssigneeId(projectId, assigneeId, pageable);
        return new ResponseEntity<>(ids, HttpStatus.OK);
    }

    @GetMapping("/reporter/{reporterId}")
    public ResponseEntity<Page<IssueResponse>>  getIssuesByReporterId(@PathVariable Long reporterId, Pageable pageable){
        Page<IssueResponse> reporters = issueService.getIssuesByReporterId(reporterId, pageable);
        return new ResponseEntity<>(reporters, HttpStatus.OK);
    }

    @GetMapping("/assignee/{assigneeId}")
    public ResponseEntity<Page<IssueResponse>>  getIssuesByAssigneeId(@PathVariable Long assigneeId, Pageable pageable){
        Page<IssueResponse> assignees = issueService.getIssuesByAssigneeId(assigneeId, pageable);
        return new ResponseEntity<>(assignees, HttpStatus.OK);
    }

    @GetMapping("/project/{projectId}/status/{status}")
    public ResponseEntity<Page<IssueResponse>> getIssuesByProjectIdAndStatus(@PathVariable Long projectId,
                                                                             @PathVariable IssueStatus status,
                                                                             Pageable pageable){
        Page<IssueResponse> issueResponse = issueService.getIssuesByProjectIdAndStatus(projectId,status,pageable);
        return ResponseEntity.status(HttpStatus.OK).body(issueResponse);
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<Page<IssueResponse>>  getIssuesByProjectId(@PathVariable Long projectId, Pageable pageable){
        Page<IssueResponse> issueResponseList = issueService.getIssuesByProjectId(projectId, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(issueResponseList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<IssueDetailResponse>  getIssue(@PathVariable Long id){
        IssueDetailResponse issueResponse = issueService.getIssueById(id);
        return ResponseEntity.ok(issueResponse);
    }


    @GetMapping("/search")
    public ResponseEntity<Page<IssueResponse>> searchIssues(@RequestParam String query,
                                                            Pageable pageable,
                                                            @AuthenticationPrincipal User currentUser){
        Page<IssueResponse> issueResponseList = issueService.searchIssues(query, pageable, currentUser);
        return ResponseEntity.ok(issueResponseList);
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<IssueResponse>> filterIssues(@RequestParam(required = false) Long projectId,
                                                            @RequestParam(required = false) IssueStatus status,
                                                            @RequestParam(required = false) Priority priority,
                                                            @RequestParam(required = false) IssueType type,
                                                            @RequestParam(required = false) Long assigneeId,
                                                            @RequestParam(required = false) Long reporterId,
                                                            Pageable pageable,
                                                            @AuthenticationPrincipal User currentUser){
        return ResponseEntity.ok(issueService.filterIssues(
                projectId, status, priority, type, assigneeId, reporterId, pageable, currentUser
        ));
    }

//    @GetMapping()
//    public ResponseEntity<Page<IssueResponse>> getAllIssues(Pageable pageable) {
//        return ResponseEntity.ok(issueService.getAllIssues(pageable));
//    }
}
