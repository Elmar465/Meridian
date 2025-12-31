package com.projectnova.meridian.controller;


import com.projectnova.meridian.dto.OrganizationResponse;
import com.projectnova.meridian.dto.OrganizationStatsDTO;
import com.projectnova.meridian.dto.UpdateOrganizationRequest;
import com.projectnova.meridian.dto.UserResponse;
import com.projectnova.meridian.model.Organization;
import com.projectnova.meridian.model.User;
import com.projectnova.meridian.model.UserRole;
import com.projectnova.meridian.service.OrganizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;



    @PutMapping("/{id}/archive")
    public ResponseEntity<OrganizationResponse> archiveOrganization(@PathVariable Long id,
                                                                       @AuthenticationPrincipal User currentUser)
            throws AccessDeniedException {
        OrganizationResponse organizationResponse  = organizationService.archiveOrganization(id, currentUser);
        return new ResponseEntity<>(organizationResponse, HttpStatus.OK);
    }


    @PutMapping("/{id}/reactivate")
    public ResponseEntity<OrganizationResponse> reactivateOrganization(@PathVariable Long id,
                                                                    @AuthenticationPrincipal User currentUser)
            throws AccessDeniedException {
        OrganizationResponse organizationResponse  = organizationService.reactivateOrganization(id, currentUser);
        return new ResponseEntity<>(organizationResponse, HttpStatus.OK);
    }


    @PutMapping("/{id}/suspend")
    public ResponseEntity<OrganizationResponse> suspendOrganization(@PathVariable Long id,
                                                                    @AuthenticationPrincipal User currentUser)
            throws AccessDeniedException {
        OrganizationResponse organizationResponse  = organizationService.suspendOrganization(id, currentUser);
        return new ResponseEntity<>(organizationResponse, HttpStatus.OK);
    }

    @PutMapping("/{id}/transfer/{newOwnerId}")
    public ResponseEntity<OrganizationResponse> transferOwnership(@PathVariable Long id, @PathVariable Long newOwnerId,
                                                                  @AuthenticationPrincipal User currentUser)
            throws AccessDeniedException {
        OrganizationResponse response  = organizationService.transferOwnership(id, newOwnerId, currentUser);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<OrganizationStatsDTO> getOrganizationStats(@PathVariable long id,
                                                                     @AuthenticationPrincipal User currentUser)
            throws AccessDeniedException {
        OrganizationStatsDTO response = organizationService.getOrganizationStats(id, currentUser);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }



    @PutMapping("/{id}/members/{userId}/role")
    public ResponseEntity<UserResponse> changeMemberRole(@PathVariable Long id, @PathVariable Long userId,
                                                         @RequestBody UserRole role,
                                                         @AuthenticationPrincipal User currentUser)
            throws AccessDeniedException {
        UserResponse user = organizationService.changeMemberRole(id, userId, role, currentUser);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<Void> removeMember(@PathVariable Long id, @PathVariable Long userId,
                                             @AuthenticationPrincipal User currentUser) throws AccessDeniedException {
        organizationService.removeMember(id, userId, currentUser);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<Page<UserResponse>> getMembers(@PathVariable long id,
                                                         @AuthenticationPrincipal User currentUser,
                                                         Pageable pageable) throws AccessDeniedException {
       Page<UserResponse> members = organizationService.getMembers(id, currentUser, pageable);
       return new ResponseEntity<>(members, HttpStatus.OK);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrganization(@PathVariable long id, @AuthenticationPrincipal User currentUser)
            throws AccessDeniedException {
        organizationService.deleteOrganization(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrganizationResponse> updateOrganization(@PathVariable Long id,
                                                                   @RequestBody @Valid UpdateOrganizationRequest request,
                                                                   @AuthenticationPrincipal User currentUser)
            throws AccessDeniedException {
        OrganizationResponse update = organizationService.updateOrganization(id, request,currentUser);
        return ResponseEntity.ok(update);
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<OrganizationResponse> getOrganization(@PathVariable String slug,
                                                                @AuthenticationPrincipal User currentUser)
            throws AccessDeniedException {
        OrganizationResponse organizationResponse = organizationService.getOrganizationBySlug(slug, currentUser);
        return ResponseEntity.ok(organizationResponse);
    }


    @GetMapping("/{id}")
    public ResponseEntity<OrganizationResponse> getOrganizationById(@PathVariable long id,
                                                                    @AuthenticationPrincipal User currentUser)
            throws AccessDeniedException {
        OrganizationResponse organizationResponse   = organizationService.getOrganizationById(id, currentUser);
        return ResponseEntity.ok(organizationResponse);
    }

    @GetMapping("/current")
    public ResponseEntity<OrganizationResponse> getCurrentUserOrganization(@AuthenticationPrincipal User currentUser) {
        OrganizationResponse organization  = organizationService.getCurrentUserOrganization(currentUser);
        return ResponseEntity.ok(organization);
    }
}
