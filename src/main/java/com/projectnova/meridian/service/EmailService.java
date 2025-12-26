package com.projectnova.meridian.service;

import com.projectnova.meridian.model.*;
import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    private final Resend resend;
    private final String fromEmail;
    private final String frontendUrl;

    public EmailService(
            @Value("${resend.api-key}") String apiKey,
            @Value("${app.frontend-url:https://meridian-front-end.vercel.app}") String frontendUrl
    ) {
        this.resend = new Resend(apiKey);
        this.fromEmail = "Meridian <onboarding@resend.dev>";
        this.frontendUrl = frontendUrl;
    }

    @Async
    public void sendWelcomeEmail(User user) {
        try {
            CreateEmailOptions options = CreateEmailOptions.builder()
                    .from(fromEmail)
                    .to(user.getEmail())
                    .subject("Welcome to Meridian")
                    .html(String.format(
                            """
                            <h2>Hi %s,</h2>
                            <p>Welcome to <strong>Meridian Project Management System</strong>!</p>
                            <p>Your account has been successfully created.</p>
                            <p><strong>Username:</strong> %s</p>
                            <p>Start managing your projects efficiently!</p>
                            <br>
                            <p>Best Regards,<br>Meridian Team</p>
                            """,
                            user.getFirstName(),
                            user.getUsername()
                    ))
                    .build();

            CreateEmailResponse response = resend.emails().send(options);
            log.info("Welcome email sent to: {} - ID: {}", user.getEmail(), response.getId());
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", user.getEmail(), e);
        }
    }

    @Async
    public void sendInvitationEmail(User user, Invitation invitation) {
        try {
            CreateEmailOptions options = CreateEmailOptions.builder()
                    .from(fromEmail)
                    .to(invitation.getEmail())
                    .subject("Invitation to Join Meridian")
                    .html(String.format(
                            """
                            <h2>Hi there,</h2>
                            <p><strong>%s</strong> has invited you to join Meridian as a <strong>%s</strong>.</p>
                            <p>Click the link below to accept your invitation:</p>
                            <p><a href="%s/accept-invite?token=%s">Accept Invitation</a></p>
                            <p>This invitation expires in 7 days.</p>
                            <br>
                            <p>Best regards,<br>Meridian Team</p>
                            """,
                            user.getFirstName() + " " + user.getLastName(),
                            invitation.getRole(),
                            frontendUrl,
                            invitation.getToken()
                    ))
                    .build();

            CreateEmailResponse response = resend.emails().send(options);
            log.info("Invitation email sent to: {} - ID: {}", invitation.getEmail(), response.getId());
        } catch (Exception e) {
            log.error("Error sending invitation email to: {}", invitation.getEmail(), e);
        }
    }

    @Async
    public void sendIssueAssignmentEmail(Issue issue, User assignee) {
        try {
            CreateEmailOptions options = CreateEmailOptions.builder()
                    .from(fromEmail)
                    .to(assignee.getEmail())
                    .subject("New Issue Assigned: " + issue.getProject().getKey() + "-" + issue.getIssueNumber())
                    .html(String.format(
                            """
                            <h2>Hi %s,</h2>
                            <p>You have been assigned a new issue:</p>
                            <p><strong>Issue:</strong> %s-%s</p>
                            <p><strong>Title:</strong> %s</p>
                            <p><strong>Priority:</strong> %s</p>
                            <p><strong>Project:</strong> %s</p>
                            <p>Please review and start working on it.</p>
                            <br>
                            <p>Best regards,<br>Meridian Team</p>
                            """,
                            assignee.getFirstName(),
                            issue.getProject().getKey(),
                            issue.getIssueNumber(),
                            issue.getTitle(),
                            issue.getPriority(),
                            issue.getProject().getName()
                    ))
                    .build();

            CreateEmailResponse response = resend.emails().send(options);
            log.info("Assignment email sent to: {} - ID: {}", assignee.getEmail(), response.getId());
        } catch (Exception e) {
            log.error("Failed to send assignment email to: {}", assignee.getEmail(), e);
        }
    }

    @Async
    public void sendIssueStatusChangeEmail(Issue issue, IssueStatus oldStatus, IssueStatus newStatus) {
        try {
            CreateEmailOptions options = CreateEmailOptions.builder()
                    .from(fromEmail)
                    .to(issue.getAssignee().getEmail())
                    .subject("Issue Status Updated: " + issue.getProject().getKey() + "-" + issue.getIssueNumber())
                    .html(String.format(
                            """
                            <h2>Hi %s,</h2>
                            <p>Issue status has been updated:</p>
                            <p><strong>Issue:</strong> %s-%s</p>
                            <p><strong>Title:</strong> %s</p>
                            <p><strong>Old Status:</strong> %s</p>
                            <p><strong>New Status:</strong> %s</p>
                            <br>
                            <p>Best regards,<br>Meridian Team</p>
                            """,
                            issue.getAssignee().getFirstName(),
                            issue.getProject().getKey(),
                            issue.getIssueNumber(),
                            issue.getTitle(),
                            oldStatus,
                            newStatus
                    ))
                    .build();

            CreateEmailResponse response = resend.emails().send(options);
            log.info("Status change email sent to: {} - ID: {}", issue.getAssignee().getEmail(), response.getId());
        } catch (Exception e) {
            log.error("Failed to send status change email", e);
        }
    }

    @Async
    public void sendNewCommentEmail(Comment comment) {
        Issue issue = comment.getIssue();
        if (issue.getAssignee() != null && !issue.getAssignee().getId().equals(comment.getUser().getId())) {
            try {
                CreateEmailOptions options = CreateEmailOptions.builder()
                        .from(fromEmail)
                        .to(issue.getAssignee().getEmail())
                        .subject("New Comment on Issue: " + issue.getProject().getKey() + "-" + issue.getIssueNumber())
                        .html(String.format(
                                """
                                <h2>Hi %s,</h2>
                                <p>New comment added to issue:</p>
                                <p><strong>Issue:</strong> %s-%s</p>
                                <p><strong>Title:</strong> %s</p>
                                <p><strong>Comment by:</strong> %s %s</p>
                                <p><strong>Comment:</strong> %s</p>
                                <br>
                                <p>Best regards,<br>Meridian Team</p>
                                """,
                                issue.getAssignee().getFirstName(),
                                issue.getProject().getKey(),
                                issue.getIssueNumber(),
                                issue.getTitle(),
                                comment.getUser().getFirstName(),
                                comment.getUser().getLastName(),
                                comment.getContent()
                        ))
                        .build();

                CreateEmailResponse response = resend.emails().send(options);
                log.info("Comment notification sent to: {} - ID: {}", issue.getAssignee().getEmail(), response.getId());
            } catch (Exception e) {
                log.error("Failed to send comment notification", e);
            }
        }
    }

    @Async
    public void sendProjectMemberAddedEmail(Project project, User newMember) {
        try {
            CreateEmailOptions options = CreateEmailOptions.builder()
                    .from(fromEmail)
                    .to(newMember.getEmail())
                    .subject("Added to Project: " + project.getName())
                    .html(String.format(
                            """
                            <h2>Hi %s,</h2>
                            <p>You have been added to a new project:</p>
                            <p><strong>Project:</strong> %s</p>
                            <p><strong>Description:</strong> %s</p>
                            <p>You can now view and contribute to this project.</p>
                            <br>
                            <p>Best regards,<br>Meridian Team</p>
                            """,
                            newMember.getFirstName(),
                            project.getName(),
                            project.getDescription()
                    ))
                    .build();

            CreateEmailResponse response = resend.emails().send(options);
            log.info("Project addition email sent to: {} - ID: {}", newMember.getEmail(), response.getId());
        } catch (Exception e) {
            log.error("Failed to send project addition email", e);
        }
    }
}