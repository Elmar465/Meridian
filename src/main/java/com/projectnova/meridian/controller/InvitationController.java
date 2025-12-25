package com.projectnova.meridian.controller;


import com.projectnova.meridian.dao.UserRepository;
import com.projectnova.meridian.dto.AcceptInvitationRequest;
import com.projectnova.meridian.dto.CreateInvitationRequest;
import com.projectnova.meridian.dto.InvitationResponse;
import com.projectnova.meridian.dto.ValidateTokenResponse;
import com.projectnova.meridian.exceptions.ResourceNotFoundException;
import com.projectnova.meridian.model.User;
import com.projectnova.meridian.service.InvitationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/invitations")
public class InvitationController {


    private final InvitationService invitationService;
    private final UserRepository userRepository;



    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<InvitationResponse>> getAllInvitations(Pageable pageable) {
        Page<InvitationResponse> invitationResponses = invitationService.getAllInvitations(pageable);
        return new  ResponseEntity<>(invitationResponses, HttpStatus.OK);
    }


    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Page<InvitationResponse>> getMyInvitations(Pageable pageable,
                                                                     @AuthenticationPrincipal UserDetails userDetails)
    {
        User user  =  userRepository.findByUsername(userDetails.getUsername()).orElseThrow(()
                -> new UsernameNotFoundException("User not found with username " + userDetails.getUsername()));
        Page<InvitationResponse> invitationResponses = invitationService.getMyInvitations(user, pageable);
        return new  ResponseEntity<>(invitationResponses, HttpStatus.OK);
    }

    @PostMapping("/{id}/resend")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<InvitationResponse> resendInvitation(@PathVariable Long id,
                                                               @AuthenticationPrincipal UserDetails userDetails)
            throws AccessDeniedException {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(()
                -> new UsernameNotFoundException("User not found with username " + userDetails.getUsername()));
        InvitationResponse invitationResponse = invitationService.resendInvitation(id, user);
        return new ResponseEntity<>(invitationResponse, HttpStatus.OK);
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> cancelInvitation(@PathVariable Long id,
                                                 @AuthenticationPrincipal UserDetails userDetails)
            throws AccessDeniedException {
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        invitationService.cancelInvitation(id, currentUser);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/accept")
    public ResponseEntity<InvitationResponse> acceptInvitation(@Valid @RequestBody AcceptInvitationRequest request) {
        InvitationResponse invitationResponse = invitationService.acceptInvitation(request);
        return new  ResponseEntity<>(invitationResponse, HttpStatus.CREATED);
    }


    @GetMapping("/validate/{token}")
    public ResponseEntity<ValidateTokenResponse> validateToken(@PathVariable String token) {
        ValidateTokenResponse response = invitationService.validateToken(token);
        return ResponseEntity.ok(response);
    }


    @PreAuthorize("hasAnyRole('ADMIN' , 'MANAGER')")
    @PostMapping
    public ResponseEntity<InvitationResponse> createInvitation(@Valid @RequestBody
                                                                   CreateInvitationRequest request,
                                                                    @AuthenticationPrincipal UserDetails userDetails) {
     User user =  userRepository.findByUsername(userDetails.getUsername()).orElseThrow(()
             -> new UsernameNotFoundException("Username not found"));
        InvitationResponse invitationResponse = invitationService.createInvitation(request,user);
        return new  ResponseEntity<>(invitationResponse, HttpStatus.CREATED);
    }
}
