package com.projectnova.meridian.service;


import com.projectnova.meridian.dao.*;
import com.projectnova.meridian.dto.*;
import com.projectnova.meridian.exceptions.ResourceNotFoundException;
import com.projectnova.meridian.model.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IssueService {

    private final AttachmentService attachmentService;
    private final ActivityLogService activityLogService;
    private final CommentService commentService;
    private final UserService userService;
    private final EmailService emailService;
    private final IssueRepository issueRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final AttachmentRepository attachmentRepository;
    private final ActivityLogRepository activityLogRepository;
    private final WebSocketService webSocketService;


    @Transactional
    public IssueResponse updateIssuePriority(Long issueId, Priority priority, Long userId) {
        Issue issue = issueRepository.findById(issueId).orElseThrow(()
                -> new ResourceNotFoundException("Issue with id not found " + issueId));
        issue.setPriority(priority);
        Issue savedIssue = issueRepository.save(issue);
        activityLogService.logSimpleActivity(savedIssue.getId(), userId, "updateIssuePriority");
        return convertToResponse(savedIssue);
    }
    public Page<IssueResponse> getAllIssues(Pageable pageable, User currentUser) {
        Long orgId = currentUser.getOrganization().getId();
        Page<Issue> issues = issueRepository.findByOrganizationId(orgId, pageable);
        return issues.map(this::convertToResponse);
    }

    @Transactional
    public IssueResponse updateIssueStatus(Long issueId, IssueStatus issueStatus, Long userId) {
        Issue issue = issueRepository.findById(issueId).orElseThrow(()
                -> new ResourceNotFoundException("Issue Not Found" + issueId));
        issue.setStatus(issueStatus);
        IssueStatus oldStatus = issue.getStatus();
        User updated = userRepository.findById(userId).orElseThrow(()
                -> new ResourceNotFoundException("User not found" + userId));
        Issue saveIssue = issueRepository.save(issue);
        activityLogService.logSimpleActivity(saveIssue.getId(), userId, "updated");
        webSocketService.notifyStatusChanged(saveIssue, oldStatus, issueStatus, updated);
        emailService.sendIssueStatusChangeEmail(saveIssue, oldStatus, issueStatus);
        return convertToResponse(saveIssue);
    }


    @Transactional
    public IssueResponse assignIssue(Long issueId, Long assigneeId, Long userId){
        Issue issue = issueRepository.findById(issueId).orElseThrow(()
                -> new ResourceNotFoundException("Issue not found" + issueId));
        User assignee  =  userRepository.findById(assigneeId).orElseThrow(()
                -> new ResourceNotFoundException("User not found" + assigneeId));
        User assigner  =  userRepository.findById(userId).orElseThrow(()
                -> new ResourceNotFoundException("User not found" + assigneeId));
        issue.setAssignee(assignee);
        Issue savedIssue = issueRepository.save(issue);
        activityLogService.logSimpleActivity(savedIssue.getId(), userId, "assigned");
        webSocketService.notifyIssueAssigned(savedIssue, assignee,assigner);
        emailService.sendIssueAssignmentEmail(savedIssue, assignee);
        return convertToResponse(savedIssue);
    }

    @Transactional
    public void deleteIssue(Long id) {
        Issue issue = issueRepository.findById(id).orElseThrow(() -> new
                ResourceNotFoundException("Issue not found" + id));
        issueRepository.delete(issue);
    }


    @Transactional
    public IssueResponse updateIssue(Long id, UpdateIssueRequest updateIssueRequest, Long userId) {
        Issue issue = issueRepository.findById(id).orElseThrow(()
                -> new ResourceNotFoundException("Issue not found" + id));
        User updated = userRepository.findById(userId).orElseThrow(()
                -> new ResourceNotFoundException("User not found" + userId));
        Issue updateIssue = updateEntity(issue, updateIssueRequest);
        Issue save =  issueRepository.save(updateIssue);
        activityLogService.logSimpleActivity(save.getId(), userId, "updated");
        webSocketService.notifyIssueUpdated(save, updated);
        return convertToResponse(save);
    }

    @Transactional
    public IssueResponse createIssue(Long projectId, CreateIssueRequest request, Long reporterId)  {
        Project project =  projectRepository.findById(projectId).orElseThrow(()
                -> new ResourceNotFoundException("Project Not Found" + projectId));
        User user = userRepository.findById(reporterId).orElseThrow(()
                -> new ResourceNotFoundException("User Not Found" + reporterId));
        Issue issue = convertToEntity(request, project, user);
        Integer issueNumber = generateIssueNumber(projectId);
        issue.setIssueNumber(issueNumber);
        Issue savedIssue = issueRepository.save(issue);
        webSocketService.notifyIssueCreated(savedIssue,user);
        activityLogService.logSimpleActivity(savedIssue.getId(), reporterId, "created");
        return convertToResponse(savedIssue);
    }

    public Long getIssueCount(Long projectId) {
        return issueRepository.countByProjectId(projectId);
    }

    public IssueDetailResponse getIssueById(Long issueId) {
        return convertToDetailResponse(issueRepository.findById(issueId).orElseThrow(()
                -> new ResourceNotFoundException("Issue with id: " + issueId + " not found!")));
    }


    public Page<IssueResponse> getIssuesByProjectIdAndAssigneeId(Long projectId, Long assigneeId, Pageable pageable) {
        Page<Issue>  issues = issueRepository.findByProjectIdAndAssigneeId(projectId, assigneeId, pageable);
        return issues.map(this::convertToResponse);
    }

    public Page<IssueResponse> getIssuesByReporterId(Long reporterId, Pageable pageable) {
        Page<Issue> issues = issueRepository.findByReporterId(reporterId, pageable);
        return issues.map(this::convertToResponse);
    }

    public Page<IssueResponse> filterIssues(    Long projectId,
                                                IssueStatus status,
                                                Priority priority,
                                                IssueType type,
                                                Long assigneeId,
                                                Long reporterId,
                                                Pageable pageable,
                                                User currentUser) {
      Long orgId = currentUser.getOrganization().getId();
      Page<Issue> issues =  issueRepository.filterIssuesByOrganization(orgId, projectId, status,
                priority,
                type,
                assigneeId,
                reporterId,
                pageable);
        return issues.map(this::convertToResponse);
    }

    public Page<IssueResponse> searchIssues(String searchTerm, Pageable pageable, User currentUser) {
        Long orgId = currentUser.getOrganization().getId();
        Page<Issue> issues = issueRepository.searchIssuesByOrganization(orgId, searchTerm, pageable);
        return issues.map(this::convertToResponse);
    }

    public Page<IssueResponse> getIssuesByAssigneeId(Long assigneeId, Pageable pageable) {
        Page<Issue> issuePage = issueRepository.findByAssigneeId(assigneeId, pageable);
        return issuePage.map(this::convertToResponse);
    }

    public Page<IssueResponse> getIssuesByProjectIdAndStatus(Long projectId, IssueStatus status, Pageable pageable) {
        Page<Issue> issues = issueRepository.findByProjectIdAndStatus(projectId, status, pageable);
        return issues.map(this::convertToResponse);
    }

    public Page<IssueResponse> getIssuesByProjectId(Long projectId, Pageable pageable) {
        Page<Issue> issuePage = issueRepository.findByProjectId(projectId, pageable);
        return issuePage.map(this::convertToResponse);
    }

//    public Page<IssueResponse> getAllIssues(Pageable pageable) {
//        Page<Issue> issues = issueRepository.findAll(pageable);
//        return  issues.map(this::convertToResponse);
//    }

    private Integer generateIssueNumber(Long projectId) {
        Optional<Issue> lastIssue = issueRepository.findTopByProjectIdOrderByIssueNumberDesc(projectId);
        return lastIssue.map(issue -> issue.getIssueNumber() + 1).orElse(1);

    }

    private Issue convertToEntity(CreateIssueRequest request, Project project, User reporter) {
        Issue issue = new Issue();
        issue.setProject(project);
        issue.setReporter(reporter);
        issue.setTitle(request.getTitle());
        issue.setDescription(request.getDescription());
        issue.setType(request.getType() != null ? request.getType() : IssueType.TASK);
        issue.setPriority(request.getPriority() != null ? request.getPriority() : Priority.MEDIUM);
        issue.setStatus(IssueStatus.TODO);

        if (request.getAssigneeId() != null) {
            User assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assignee not found"));
            issue.setAssignee(assignee);
        }

        issue.setDueDate(request.getDueDate());

        return issue;
    }


    public Issue updateEntity(Issue issue, UpdateIssueRequest request) {
        if(request.getTitle() != null) {
            issue.setTitle(request.getTitle());
        }

        if(request.getDescription() != null) {
            issue.setDescription(request.getDescription());
        }

        if(request.getType() != null) {
            issue.setType(request.getType());
        }

        if(request.getPriority() != null) {
            issue.setPriority(request.getPriority());
        }

        if(request.getAssigneeId() != null) {
            issue.setAssignee(userRepository.findById(request.getAssigneeId()).orElseThrow(()
                    -> new RuntimeException("Assignee not found")));
        }

        if (request.getDueDate() != null) {
            issue.setDueDate(request.getDueDate());
        }
        if(request.getStatus() != null) {
            issue.setStatus(request.getStatus());
        }
        return issue;
    }

    private IssueResponse convertToResponse(Issue issue) {
        IssueResponse issueResponse = new IssueResponse();
        issueResponse.setId(issue.getId());
        issueResponse.setProjectId(issue.getProject().getId());
        issueResponse.setIssueNumber(issue.getIssueNumber());
        issueResponse.setIssueKey(issue.getProject().getKey() + "-" + issue.getIssueNumber());
        issueResponse.setTitle(issue.getTitle());
        issueResponse.setDescription(issue.getDescription());
        issueResponse.setPriority(issue.getPriority());
        issueResponse.setReporterId(issue.getReporter().getId());
        issueResponse.setStatus(issue.getStatus());
        issueResponse.setReporterName(issue.getReporter().getFirstName() + " " + issue.getReporter().getLastName());
        issueResponse.setType(issue.getType());
        issueResponse.setCreatedAt(issue.getCreatedAt());
        issueResponse.setUpdatedAt(issue.getUpdatedAt());
        if(issue.getAssignee() != null) {
            issueResponse.setAssigneeId(issue.getAssignee().getId());
            issueResponse.setAssigneeName(issue.getAssignee().getFirstName() + " " + issue.getAssignee().getLastName());
        }
        issueResponse.setDueDate(issue.getDueDate());
        return  issueResponse;
    }

    // i will continue after finishing attachment service
    private IssueDetailResponse convertToDetailResponse(Issue issue) {  // ← Remove Pageable!
        IssueDetailResponse issueDetailResponse = new IssueDetailResponse();
        issueDetailResponse.setId(issue.getId());
        issueDetailResponse.setProjectId(issue.getProject().getId());
        issueDetailResponse.setIssueNumber(issue.getIssueNumber());
        issueDetailResponse.setIssueKey(issue.getProject().getKey() + "-" + issue.getIssueNumber());
        issueDetailResponse.setTitle(issue.getTitle());
        issueDetailResponse.setDescription(issue.getDescription());
        issueDetailResponse.setPriority(issue.getPriority());
        issueDetailResponse.setReporterId(issue.getReporter().getId());
        issueDetailResponse.setStatus(issue.getStatus());
        issueDetailResponse.setReporterName(issue.getReporter().getFirstName() + " " + issue.getReporter().getLastName());
        issueDetailResponse.setType(issue.getType());
        issueDetailResponse.setCreatedAt(issue.getCreatedAt());
        issueDetailResponse.setUpdatedAt(issue.getUpdatedAt());

        if(issue.getAssignee() != null) {
            issueDetailResponse.setAssigneeId(issue.getAssignee().getId());
            issueDetailResponse.setAssigneeName(issue.getAssignee().getFirstName() + " " + issue.getAssignee().getLastName());
        }

        issueDetailResponse.setDueDate(issue.getDueDate());

        List<CommentResponse> commentResponses = commentService.getCommentsByIssueId(issue.getId());
        List<AttachmentResponse> attachments = attachmentService.getAttachmentsByIssueId(issue.getId());
        List<ActivityLogResponse> activities = activityLogService.getActivitiesByIssueId(issue.getId());

        issueDetailResponse.setComments(commentResponses);
        issueDetailResponse.setAttachments(attachments);
        issueDetailResponse.setActivities(activities);

        return issueDetailResponse;
    }
    private List<IssueResponse> convertToResponseList(List<Issue> issues) {
        return issues
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
}
