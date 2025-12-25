package com.projectnova.meridian.service;

import com.projectnova.meridian.dao.InvitationRepository;
import com.projectnova.meridian.dao.UserRepository;
import com.projectnova.meridian.dto.AcceptInvitationRequest;
import com.projectnova.meridian.dto.CreateInvitationRequest;
import com.projectnova.meridian.dto.InvitationResponse;
import com.projectnova.meridian.dto.ValidateTokenResponse;
import com.projectnova.meridian.exceptions.DuplicateResourceException;
import com.projectnova.meridian.exceptions.ResourceNotFoundException;
import com.projectnova.meridian.model.Invitation;
import com.projectnova.meridian.model.InvitationStatus;
import com.projectnova.meridian.model.User;
import com.projectnova.meridian.model.UserRole;
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
import java.util.stream.Collectors;

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

    public Page<InvitationResponse> getAllInvitations(Pageable pageable) {
        Page<Invitation> invitationPage = invitationRepository.findAll(pageable);
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

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setUsername(request.getUsername());
        user.setLastName(request.getLastName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(invitation.getEmail());
        user.setRole(invitation.getRole());
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
        if(existsByEmailAndStatus(createInvitationRequest.getEmail(), InvitationStatus.PENDING)) {
            throw new DuplicateResourceException("Invitation already pending for this email");
        }
        Invitation invitation = new Invitation();
        invitation.setEmail(createInvitationRequest.getEmail());
        invitation.setRole(createInvitationRequest.getRole());
        invitation.setToken(UUID.randomUUID().toString());
        invitation.setStatus(InvitationStatus.PENDING);
        invitation.setExpiresAt(LocalDateTime.now().plusDays(7));
        invitation.setInvitedBy(currentUser);
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
