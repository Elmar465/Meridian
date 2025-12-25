package com.projectnova.meridian.service;


import com.projectnova.meridian.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;



    @Async
    public void sendInvitationEmail(User user, Invitation invitation) {
        try{
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(invitation.getEmail());
            mailMessage.setSubject("Invitation Email");
            mailMessage.setText(String.format(
                    """
                            Hi there,
                            
                            %s has invited you to join Meridian as a %s.
                            
                            Click the link below to accept your invitation:
                            http://localhost:5173/accept-invite?token=%s
                            
                            This invitation expires in 7 days.
                            
                            Best regards,
                            Meridian Team
                            """,
                    user.getFirstName() + " " + user.getLastName(),  // who invited
                    invitation.getRole(),                              // role
                    invitation.getToken()
            ));
            mailSender.send(mailMessage);
            log.info("Sent email to invitation email to {}.", user.getEmail());
        } catch (Exception e) {
            log.error("Error sending email to invitation email to {}.", user.getEmail(), e);
        }
    }


    @Async
    public void sendWelcomeEmail(User user) {
        try{
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject("Welcome to Meridian");
            message.setText(String.format(
                    """
                            Hi %s ,
                            
                            Welcome to Meridian Project Management System
                            
                            Your account has been successfully created .
                            Username: %s
                            
                            Start managing your projects efficiently!
                            
                            Best Regards\s
                            Meridian Team""",
                    user.getFirstName(),
                    user.getUsername()
            ));
            mailSender.send(message);
            log.info("Welcome email sent to: {}",  user.getEmail());
        }catch (Exception e){
            log.error("Failed to send welcome email to: {}",  user.getEmail());
        }
    }

    @Async
    public void sendIssueAssignmentEmail(Issue issue, User assignee) {
        try{
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(assignee.getEmail());
            message.setSubject("New Issue Assigned: " + issue.getProject().getKey() + "-" + issue.getIssueNumber());
            message.setText(String.format(
                    """
                            Hi %s ,
                            
                            You have been assigned a new issue:
                            
                            Issue: %s
                            Title: %s
                            Priority: %s
                            Please review and start working on it .
                            
                            Best regards,\s
                            Meridian Team""",
                    assignee.getFirstName(),
                    issue.getProject() + "-" + issue.getIssueNumber(),
                    issue.getTitle(),
                    issue.getPriority(),
                    issue.getProject().getName()
            ));
            mailSender.send(message);
            log.info("Assignment email sent to: {}",  assignee.getEmail());
        } catch (Exception e){
            log.error("Failed to send assignment email to: {}",  assignee.getEmail());
        }
    }

    @Async
    public void sendIssueStatusChangeEmail(Issue issue, IssueStatus oldStatus, IssueStatus newStatus) {
        try{
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(issue.getAssignee().getEmail());
            message.setSubject("Issue Status Updated: " + issue.getProject().getKey() + "-" + issue.getIssueNumber());
            message.setText(String.format(
                    """
                            Hi %s ,
                           
                            Issue status has been updated: 
                            Issue: %s 
                            Title: %s
                            Status: %s
                            
                            Best  regards,\s
                            Meridian Team,  
                            """,
                    issue.getAssignee().getFirstName(),
                    issue.getProject().getKey() + "-" + issue.getIssueNumber(),
                    issue.getTitle(),
                    oldStatus,
                    newStatus
                    ));

            mailSender.send(message);
            log.info("Status change email sent to: {}",  issue.getAssignee().getEmail());
        }catch (Exception e){
            log.error("Failed to send status change email", e);
        }
    }


    @Async
    public void sendNewCommentEmail(Comment comment) {
        Issue issue = comment.getIssue();
        if (issue.getAssignee() != null && !issue.getAssignee().getId().equals(comment.getUser().getId())) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(issue.getAssignee().getEmail());
                message.setSubject("New Comment on Issue: " + issue.getProject() .getKey() + "-" + issue.getIssueNumber());
                message.setText(String.format(
                        "Hi %s,\n\n" +
                                "New comment added to issue:\n\n" +
                                "Issue: %s\n" +
                                "Title: %s\n" +
                                "Comment by: %s %s\n" +
                                "Comment: %s\n\n" +
                                "Best regards,\n" +
                                "Meridian Team",
                        issue.getAssignee().getFirstName(),
                        issue.getProject().getKey(),
                        issue.getTitle(),
                        comment.getUser().getFirstName(),
                        comment.getUser().getLastName(),
                        comment.getContent()
                ));

                mailSender.send(message);
                log.info("Comment notification sent to: {}", issue.getAssignee().getEmail());
            } catch (Exception e) {
                log.error("Failed to send comment notification", e);
            }
        }
    }

    @Async
    public void sendProjectMemberAddedEmail(Project project, User newMember) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(newMember.getEmail());
            message.setSubject("Added to Project: " + project.getName());
            message.setText(String.format(
                    "Hi %s,\n\n" +
                            "You have been added to a new project:\n\n" +
                            "Project: %s\n" +
                            "Description: %s\n\n" +
                            "You can now view and contribute to this project.\n\n" +
                            "Best regards,\n" +
                            "Meridian Team",
                    newMember.getFirstName(),
                    project.getName(),
                    project.getDescription()
            ));

            mailSender.send(message);
            log.info("Project addition email sent to: {}", newMember.getEmail());
        } catch (Exception e) {
            log.error("Failed to send project addition email", e);
        }
} }
