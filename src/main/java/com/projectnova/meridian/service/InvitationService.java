package com.projectnova.meridian.service;

import com.projectnova.meridian.dao.InvitationRepository;
import com.projectnova.meridian.dao.UserRepository;
import com.projectnova.meridian.dto.*;
import com.projectnova.meridian.exceptions.DuplicateResourceException;
import com.projectnova.meridian.exceptions.ResourceNotFoundException;
import com.projectnova.meridian.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvitationService {

    private final InvitationRepository invitationRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;


    @Scheduled(cron = "0 0 0 * * *")
    public void cleanupExpiredInvitations() {
        List<Invitation> expiredList = invitationRepository.findAllByExpiresAtBeforeAndStatus(LocalDateTime.now(),
                InvitationStatus.PENDING);

        if (expiredList.isEmpty()) {
            log.info("No expired invitations to clean up");
            return;
        }

        for (Invitation invitation : expiredList) {
            invitation.setStatus(InvitationStatus.EXPIRED);
        }

        invitationRepository.saveAll(expiredList);
        log.info("Cleaned up {} expired invitations", expiredList.size());
    }

    public Page<InvitationResponse> getPendingInvitations(Pageable pageable, User currentUser){
        Long orgId = currentUser.getOrganization().getId();
        Page<Invitation> invitationPage = invitationRepository.findByOrganizationIdAndStatus(orgId, InvitationStatus.PENDING, pageable);
        return convertToResponsePage(invitationPage);
    }

    public Page<InvitationResponse> getAllInvitations(Pageable pageable, User currentUser) {
        Long orgId = currentUser.getOrganization().getId();
        Page<Invitation> invitationPage = invitationRepository.findByOrganizationId(orgId,pageable);
        return convertToResponsePage(invitationPage);
    }

    public Page<InvitationResponse> getMyInvitations(User currentUser, Pageable pageable) {
        Page<Invitation> user =  invitationRepository.findByInvitedBy(currentUser, pageable);
        return convertToResponsePage(user);
    }


    public InvitationResponse resendInvitation(Long id, User currentUser) throws AccessDeniedException {
        Invitation invitation = invitationRepository.findById(id).orElseThrow(()
                -> new ResourceNotFoundException("Invitation not found with id: " + id));
        if(invitation.getStatus() ==  InvitationStatus.ACCEPTED || invitation.getStatus() == InvitationStatus.CANCELLED) {
            throw new ResourceNotFoundException("Cannot resend this invitation");
        }

        if(currentUser.getRole() != UserRole.ADMIN && !invitation.getInvitedBy().equals(currentUser)) {
            throw new AccessDeniedException("You are not allowed to resend this invitation");
        }
        Long orgId = currentUser.getOrganization().getId();
        if(!orgId.equals(invitation.getOrganization().getId())) {
            throw new AccessDeniedException("You are not allowed to resend this invitation");
        }
        invitation.setToken(UUID.randomUUID().toString());
        invitation.setExpiresAt(LocalDateTime.now().plusDays(7));
        invitation.setStatus(InvitationStatus.PENDING);
        invitationRepository.save(invitation);
        emailService.sendInvitationEmail(currentUser, invitation);
        return  convertToResponse(invitation);
    }



    public void cancelInvitation(Long id, User currentUser) throws AccessDeniedException {
        Invitation invitation = invitationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found with id " + id));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new ResourceNotFoundException("Cannot cancel - invitation already used/expired/cancelled");
        }

        if (currentUser.getRole() != UserRole.ADMIN && !invitation.getInvitedBy().equals(currentUser)) {
            throw new AccessDeniedException("You don't have permission to cancel this invitation");
        }
        Long orgId = currentUser.getOrganization().getId();
        if(!orgId.equals(invitation.getOrganization().getId())) {
            throw new AccessDeniedException("You are not allowed to cancel this invitation");
        }
        invitation.setStatus(InvitationStatus.CANCELLED);
        invitationRepository.save(invitation);
    }
    public boolean existsByEmailAndStatus(String email, InvitationStatus invitationStatus) {
        return invitationRepository.existsByEmailAndStatus(email, invitationStatus);
    }

    @Transactional
    public InvitationResponse acceptInvitation(AcceptInvitationRequest request) {
        Invitation invitation = invitationRepository.findByToken(request.getToken())
                .orElseThrow(() -> new ResourceNotFoundException("Invitation Token"));
        if(invitation.getStatus() != InvitationStatus.PENDING) {
            throw new DuplicateResourceException("Invitation already used");
        }
        if(invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResourceNotFoundException("Invitation expired");
        }
        if(userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already taken");
        }

        if(userRepository.existsByEmail(invitation.getEmail())) {
            throw new DuplicateResourceException("Email already taken");
        }
        if(invitation.getOrganization().getStatus() !=  OrganizationStatus.ACTIVE) {
            throw new ResourceNotFoundException("Organization is not active");
        }
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setUsername(request.getUsername());
        user.setLastName(request.getLastName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(invitation.getEmail());
        user.setRole(invitation.getRole());
        user.setOrganization(invitation.getOrganization());
        user.setIsActive(true);
        User savedUser = userRepository.save(user);
        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitation.setAcceptedAt(LocalDateTime.now());

        invitation.setAcceptedByUser(savedUser);
        Invitation save = invitationRepository.save(invitation);
        return convertToResponse(save);
    }

    public ValidateTokenResponse validateToken(String token) {
        Optional<Invitation>  optionalInvitation = invitationRepository.findByToken(token);
        if(optionalInvitation.isEmpty()) {
            return new ValidateTokenResponse(false, null, null, "Invalid token");
        }
        Invitation invitation = optionalInvitation.get();
        if(invitation.getStatus() != InvitationStatus.PENDING) {
            return new ValidateTokenResponse(false, null, null, "Invalid already used");
        }
        if(invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            return new ValidateTokenResponse(false, null, null, "Invalid expired");
        }

        return new ValidateTokenResponse(true, invitation.getEmail(), invitation.getRole()
                ,"Valid Invitation");
    }

    @Transactional
    public InvitationResponse createInvitation(CreateInvitationRequest createInvitationRequest, User currentUser) {
        Long orgId = currentUser.getOrganization().getId();
        if(invitationRepository.existsByEmailAndOrganizationIdAndStatus(createInvitationRequest.getEmail(),
                orgId, InvitationStatus.PENDING)) {
            throw new DuplicateResourceException("Invitation already pending for this email");
        }
        if(currentUser.getOrganization().getStatus() != OrganizationStatus.ACTIVE) {
            throw new IllegalStateException("Cannot invite to suspended organization");
        }

        // Check if already member
        if(userRepository.existsByEmailAndOrganizationId(createInvitationRequest.getEmail(), orgId)) {
            throw new DuplicateResourceException("User already member of organization");
        }
        Invitation invitation = new Invitation();
        invitation.setEmail(createInvitationRequest.getEmail());
        invitation.setRole(createInvitationRequest.getRole());
        invitation.setToken(UUID.randomUUID().toString());
        invitation.setStatus(InvitationStatus.PENDING);
        invitation.setExpiresAt(LocalDateTime.now().plusDays(7));
        invitation.setInvitedBy(currentUser);
        invitation.setOrganization(currentUser.getOrganization());
        Invitation saveUserInvitation = invitationRepository.save(invitation);
        emailService.sendInvitationEmail(currentUser, saveUserInvitation);
        return convertToResponse(saveUserInvitation);
    }

    private Page<InvitationResponse> convertToResponsePage(Page<Invitation> invitationPage) {
        return invitationPage.map(this::convertToResponse);
    }
    private Invitation convertToEntity(CreateInvitationRequest createInvitationRequest) {
        Invitation invitation = new Invitation();
        invitation.setEmail(createInvitationRequest.getEmail());
        invitation.setRole(createInvitationRequest.getRole());
        return invitation;

    }

    private InvitationResponse convertToResponse(Invitation invitation) {
        InvitationResponse response = new InvitationResponse();
        response.setId(invitation.getId());
        response.setEmail(invitation.getEmail());
        response.setRole(invitation.getRole());
        response.setStatus(invitation.getStatus());
        response.setInvitedByUsername(invitation.getInvitedBy().getUsername());
        response.setExpiresAt(invitation.getExpiresAt());
        response.setCreatedAt(invitation.getCreatedAt());
        return response;
    }

}
