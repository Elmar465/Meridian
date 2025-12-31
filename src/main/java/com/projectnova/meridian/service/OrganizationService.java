package com.projectnova.meridian.service;


import com.projectnova.meridian.dao.InvitationRepository;
import com.projectnova.meridian.dao.OrganizationRepository;
import com.projectnova.meridian.dao.ProjectRepository;
import com.projectnova.meridian.dao.UserRepository;
import com.projectnova.meridian.dto.*;
import com.projectnova.meridian.exceptions.ResourceNotFoundException;
import com.projectnova.meridian.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final InvitationRepository invitationRepository;
    private final UserService userService;


    public OrganizationResponse archiveOrganization(Long orgId, User currentUser) throws AccessDeniedException {
        Organization organization  = getOrganizationOrThrow(orgId);
        validateOwnerAccess(organization, currentUser);
        organization.setStatus(OrganizationStatus.ARCHIVED);
        organizationRepository.save(organization);
        return convertToResponse(organization);
    }

    public OrganizationResponse reactivateOrganization(Long orgId, User currentUser) throws AccessDeniedException {
        Organization organization =  getOrganizationOrThrow(orgId);
        validateOwnerAccess(organization, currentUser);
        organization.setStatus(OrganizationStatus.ACTIVE);
        organizationRepository.save(organization);
        return convertToResponse(organization);
    }

    public OrganizationResponse suspendOrganization(Long orgId, User currentUser) throws AccessDeniedException {
        Organization organization = getOrganizationOrThrow(orgId);
        validateOwnerAccess(organization,currentUser);
        organization.setStatus(OrganizationStatus.SUSPENDED);
        organizationRepository.save(organization);
        return convertToResponse(organization);
    }

    public OrganizationResponse transferOwnership(Long orgId, Long newOwnerId, User currenUser)
            throws AccessDeniedException {
        Organization organization =  getOrganizationOrThrow(orgId);
        validateOwnerAccess(organization, currenUser);
        User user =  userRepository.findById(newOwnerId).orElseThrow(()
                -> new ResourceNotFoundException("User not found" + newOwnerId));
        if(!organization.getId().equals(user.getOrganization().getId())) {
            throw new AccessDeniedException("User not in this organization");
        }
        organization.setOwner(user);
        user.setRole(UserRole.ADMIN);
        userRepository.save(user);
        organizationRepository.save(organization);
        return convertToResponse(organization);
    }

    public OrganizationStatsDTO getOrganizationStats(Long orgId, User currentUser) throws AccessDeniedException {
        Organization organization =  getOrganizationOrThrow(orgId);
        validateMembership(organization, currentUser);
        Long count  = userRepository.countByOrganizationId(organization.getId());
        Long projects =  projectRepository.countByOrganizationId(organization.getId());
        Long invitations = invitationRepository.countByOrganizationIdAndStatus(orgId, InvitationStatus.PENDING);
        OrganizationStatsDTO stats =  new OrganizationStatsDTO();
        stats.setTotalMembers(count);
        stats.setTotalProjects(projects);
        stats.setPendingInvitations(invitations);
        return stats;
    }

    public UserResponse changeMemberRole(Long orgId, Long userId,  UserRole role, User currentUser)
            throws AccessDeniedException {
        Organization organization  = getOrganizationOrThrow(orgId);
        validateMembership(organization, currentUser);
        validateAdminAccess(organization, currentUser);
        User user = userRepository.findById(userId).orElseThrow(()
                -> new ResourceNotFoundException("User not found" + userId));
        if(!organization.getId().equals(user.getOrganization().getId())) {
            throw new AccessDeniedException("User not in this organization");
        }
        if(user.getId().equals(organization.getOwner().getId())) {
            throw new AccessDeniedException("Cannot change owner role");
        }
        user.setRole(role);
        userRepository.save(user);
        return userService.convertToUserResponse(user);
    }


    public void  removeMember(Long orgId, Long userId, User currentUser) throws AccessDeniedException {
        Organization organization  = getOrganizationOrThrow(orgId);
        validateMembership(organization, currentUser);
        validateAdminAccess(organization, currentUser);
        User user  = userRepository.findById(userId).orElseThrow(() -> new AccessDeniedException("User not found"));
        if(!organization.getId().equals(user.getOrganization().getId())) {
            throw new AccessDeniedException("User not in this organization");
        }
        if(user.getId().equals(organization.getOwner().getId())) {
            throw new AccessDeniedException("Cannot remove owner");
        }
        user.setOrganization(null);
        userRepository.save(user);
    }

    public Page<UserResponse> getMembers(Long orgId, User currentUser, Pageable pageable) throws AccessDeniedException {
        Organization organization = getOrganizationOrThrow(orgId);
        validateMembership(organization, currentUser);
        Page<User> users = userRepository.findByOrganizationId(organization.getId(), pageable);
        return users.map(userService::convertToUserResponse);
    }

    public void deleteOrganization(Long id, User currentUser) throws AccessDeniedException {
        Organization organization  =  getOrganizationOrThrow(id);
        validateOwnerAccess(organization, currentUser);
        organizationRepository.delete(organization);
    }

    public OrganizationResponse updateOrganization(Long orgId, UpdateOrganizationRequest request, User currentUser)
            throws AccessDeniedException {
        Organization organization = getOrganizationOrThrow(orgId);
        validateMembership(organization, currentUser);
        validateAdminAccess(organization, currentUser);
        if(request.getName() != null) {
            organization.setName(request.getName());
        }
        if(request.getDescription() != null) {
            organization.setDescription(request.getDescription());
        }
        if(request.getLogo() != null) {
            organization.setLogo(request.getLogo());
        }
        Organization updatedOrganization = organizationRepository.save(organization);
        return convertToResponse(updatedOrganization);
    }

    public OrganizationResponse getCurrentUserOrganization(User currentUser) {
        Organization organization = currentUser.getOrganization();
        if (organization == null) {
            throw new ResourceNotFoundException("Organization Not Found");
        }
        return convertToResponse(organization);
    }


    public OrganizationResponse getOrganizationBySlug(String slug, User currentUser) throws AccessDeniedException {
        Organization organization = organizationRepository.findBySlug(slug).orElseThrow(()
                ->  new ResourceNotFoundException("Organization Not Found"));
        validateMembership(organization, currentUser);
        return  convertToResponse(organization);
    }


    public OrganizationResponse getOrganizationById(Long id, User currentUser) throws AccessDeniedException {
        Organization organization =  getOrganizationOrThrow(id);
        validateMembership(organization,currentUser );
        return convertToResponse(organization);
    }


    public OrganizationResponse createOrganization(CreateOrganizationRequest request, User owner) {
        String slug = generateSlug(request.getName());
        String uniqueSlug = generateUniqueSlug(slug);

        Organization organization = convertEntity(request, owner);
        organization.setSlug(uniqueSlug);

        Organization savedOrg = organizationRepository.save(organization);

        owner.setOrganization(savedOrg);
        userRepository.save(owner);

        return convertToResponse(savedOrg);
    }


    private String generateUniqueSlug(String baseSlug) {
        String slug =  baseSlug;
        int counter = 2;
        while (organizationRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + counter;
            counter++;
        }
        return slug;
    }

    private String generateSlug(String name) {
        if(name == null || name.isEmpty()) {
            return "workspace";
        }
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .replace(" ", "-");
    }

    private void validateOrgIsActive(Organization organization) throws AccessDeniedException {
        if(organization.getStatus() != OrganizationStatus.ACTIVE) {
            throw new AccessDeniedException("Organization is not active");
        }
    }

    private void validateMembership(Organization organization, User user) throws AccessDeniedException {
        if(!user.getOrganization().getId().equals(organization.getId())) {
            throw new AccessDeniedException("not a member");
        }
    }


    private void validateAdminAccess(Organization organization, User user) throws AccessDeniedException {
        boolean isOwner = user.getId().equals(organization.getOwner().getId());
        boolean isAdmin = user.getRole() == UserRole.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("Access denied - admin only");
        }
    }

    private void validateOwnerAccess(Organization organization, User user) throws AccessDeniedException {
        if(!user.getId().equals(organization.getOwner().getId())) {
            throw new AccessDeniedException("Owner only");
        }
    }

    private Organization getOrganizationOrThrow(Long id) {
        return organizationRepository.findById(id).orElseThrow(()
                -> new ResourceNotFoundException("Organization Not Found"));
    }

    private Organization convertEntity(CreateOrganizationRequest createOrganizationRequest, User owner) {
        Organization organization = new Organization();
        organization.setName(createOrganizationRequest.getName());
        organization.setDescription(createOrganizationRequest.getDescription());
        organization.setOwner(owner);
        organization.setStatus(OrganizationStatus.ACTIVE);
        return organization;
    }


    private OrganizationResponse convertToResponse(Organization organization) {
        OrganizationResponse organizationResponse = new OrganizationResponse();
        organizationResponse.setId(organization.getId());
        organizationResponse.setName(organization.getName());
        organizationResponse.setSlug(organization.getSlug());
        organizationResponse.setDescription(organization.getDescription());
        organizationResponse.setLogo(organization.getLogo());
        organizationResponse.setOwnerId(organization.getOwner().getId());
        organizationResponse.setOwnerName(organization.getOwner().getFirstName());
        organizationResponse.setStatus(organization.getStatus());
        organizationResponse.setMemberCount(organization.getMembers().size());
        organizationResponse.setCreatedAt(organization.getCreatedDate());
        return organizationResponse;
    }
}
