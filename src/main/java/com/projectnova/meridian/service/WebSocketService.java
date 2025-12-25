package com.projectnova.meridian.service;


import com.projectnova.meridian.dto.IssueUpdateMessage;
import com.projectnova.meridian.dto.WebSocketMessage;
import com.projectnova.meridian.model.Comment;
import com.projectnova.meridian.model.Issue;
import com.projectnova.meridian.model.IssueStatus;
import com.projectnova.meridian.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    // Broadcast issue creation to all users waiting the project
    public void notifyIssueCreated(Issue issue, User creator) {
        IssueUpdateMessage message = new IssueUpdateMessage(
                issue.getId(),
                issue.getProject().getKey() + "-" + issue.getIssueNumber(),
                issue.getTitle(),
                issue.getStatus(),
                issue.getPriority(),
                creator.getUsername(),
                "CHEATED"
        );

        WebSocketMessage wsMessage = new WebSocketMessage(
                "ISSUE_CREATED",
                issue.getId(),
                "New issue created " + message.getIssueKey(),
                message
        );

        //Broadcast to all users subscribed to this project
        messagingTemplate.convertAndSend("/topic/project/" + issue.getProject().getId(), wsMessage);
        log.info("WenSocket: Issue created notifications sent for {}", message.getIssueKey());
    }

    // Notify issue update
    public void notifyIssueUpdated(Issue issue, User updater) {
        IssueUpdateMessage message = new IssueUpdateMessage(
                issue.getId(),
                issue.getProject().getKey() + "-" + issue.getIssueNumber(),
                issue.getTitle(),
                issue.getStatus(),
                issue.getPriority(),
                updater.getUsername(),
                "UPDATED"
        );

        WebSocketMessage wsMessage = new WebSocketMessage(
                "ISSUE_UPDATED",
                issue.getId(),
                "Issue updated: " + message.getIssueKey(),
                message
        );

        messagingTemplate.convertAndSend("/topic/project/" + issue.getProject().getId(), wsMessage);
        log.info("WebSocket: Issue updated notification sent for {}", message.getIssueKey());
    }

    // Notify issue assignment
    public void notifyIssueAssigned(Issue issue, User assignee, User assigner) {
        IssueUpdateMessage message = new IssueUpdateMessage(
                issue.getId(),
                issue.getProject().getKey() + "-" + issue.getIssueNumber(),
                issue.getTitle(),
                issue.getStatus(),
                issue.getPriority(),
                assigner.getUsername(),
                "ASSIGNED"
        );

        WebSocketMessage wsMessage = new WebSocketMessage(
                "ISSUE_ASSIGNED",
                issue.getId(),
                issue.getIssueNumber() + " assigned to " + assignee.getUsername(),
                message
        );

        // Send to project topic
        messagingTemplate.convertAndSend("/topic/project/" + issue.getProject().getId(), wsMessage);

        // Send personal notification to assignee
        messagingTemplate.convertAndSendToUser(
                assignee.getUsername(),
                "/queue/notifications",
                wsMessage
        );

        log.info("WebSocket: Issue assignment notification sent");
    }

    // Notify status change
    public void notifyStatusChanged(Issue issue, IssueStatus oldStatus, IssueStatus newStatus, User updater) {
        IssueUpdateMessage message = new IssueUpdateMessage(
                issue.getId(),
                issue.getProject().getKey() + "-" + issue.getIssueNumber(),
                issue.getTitle(),
                newStatus,
                issue.getPriority(),
                updater.getUsername(),
                "STATUS_CHANGED"
        );

        WebSocketMessage wsMessage = new WebSocketMessage(
                "ISSUE_STATUS_CHANGED",
                issue.getId(),
                "Status changed: " + oldStatus + " → " + newStatus,
                message
        );

        messagingTemplate.convertAndSend("/topic/project/" + issue.getProject().getId(), wsMessage);
        log.info("WebSocket: Status change notification sent");
    }

    // Notify new comment
    public void notifyCommentAdded(Comment comment) {
        WebSocketMessage wsMessage = new WebSocketMessage(
                "COMMENT_ADDED",
                comment.getIssue().getId(),
                "New comment by " + comment.getUser().getUsername(),
                comment.getId()
        );

        messagingTemplate.convertAndSend(
                "/topic/issue/" + comment.getIssue().getId() + "/comments",
                wsMessage
        );

        log.info("WebSocket: Comment notification sent");
    }
}
