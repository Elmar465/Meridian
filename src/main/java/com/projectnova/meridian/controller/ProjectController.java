package com.projectnova.meridian.controller;


import com.projectnova.meridian.dto.*;
import com.projectnova.meridian.model.Project;
import com.projectnova.meridian.model.ProjectStatus;
import com.projectnova.meridian.model.User;
import com.projectnova.meridian.service.ProjectService;
import com.projectnova.meridian.utils.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects")
public class ProjectController {

private final ProjectService projectService;

private final UserContext userContext;


    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @DeleteMapping("/{projectId}/members/{userId}")
    public ResponseEntity<Void> deleteMember(@PathVariable Long projectId,
                                             @PathVariable Long userId
                                             ) throws AccessDeniedException {
        User currentUser = userContext.getCurrentUser();
        projectService.removeMember(projectId, userId, currentUser);
        return ResponseEntity.noContent().build();
    }



    @GetMapping("/{projectId}/members")
    public ResponseEntity<List<ProjectMemberResponse>> getProjectMembers(@PathVariable Long projectId
                                                                         )
            throws AccessDeniedException {
        User currentUser = userContext.getCurrentUser();
        List<ProjectMemberResponse> projectMemberResponse  = projectService.getProjectMembers(projectId, currentUser);
        return ResponseEntity.ok(projectMemberResponse);
    }


    @PostMapping("/{projectId}/members")
    public ResponseEntity<ProjectMemberResponse>  addMember(@PathVariable Long projectId,
                                                            @RequestBody AddMemberRequest request)
            throws AccessDeniedException {
        User currentUser = userContext.getCurrentUser();
        ProjectMemberResponse projectResponse = projectService.addMember(projectId, request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(projectResponse);
    }


    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long projectId)
            throws AccessDeniedException {
        User currentUser = userContext.getCurrentUser();
        projectService.deleteProject(projectId, currentUser);
        return ResponseEntity.noContent().build();
    }


    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> updateProject(@PathVariable Long id, @RequestBody
    @Valid UpdateProjectRequest request ) throws AccessDeniedException {
        User currentUser = userContext.getCurrentUser();
        ProjectResponse update = projectService.updateProject(id, request, currentUser);
        return new  ResponseEntity<>(update, HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProjectResponse>> searchProjects(@RequestParam String searchTerm,
                                                                Pageable pageable) {
        User currentUser = userContext.getCurrentUser();
        Page<ProjectResponse> projectResponseList = projectService.searchProjects(searchTerm, pageable, currentUser);
        return ResponseEntity.ok(projectResponseList);

    }

    @GetMapping("/filter")
    public ResponseEntity<Page<ProjectResponse>> filteProject(
            @RequestParam(required = false) ProjectStatus status,
            @RequestParam(required = false) Long ownerId,
            Pageable pageable
    ) {
        User currentUser = userContext.getCurrentUser();
        Page<ProjectResponse> filter = projectService.filterProjects(status, ownerId, pageable, currentUser);
        return ResponseEntity.ok(filter);
    }

    @PostMapping()
    public ResponseEntity<ProjectResponse> createProject(@RequestBody @Valid CreateProjectRequest request)
            throws AccessDeniedException {
        User currentUser = userContext.getCurrentUser();
        ProjectResponse projectResponse = projectService.createProject(request,currentUser);
        return new  ResponseEntity<>(projectResponse, HttpStatus.CREATED);
    }


    @GetMapping("/status/{status}")
    public ResponseEntity<Page<ProjectResponse>> getProjectsByStatus(@PathVariable("status") ProjectStatus status,
                                                                     Pageable pageable) {

         User currentUser1 = userContext.getCurrentUser();
         Page<ProjectResponse> projectResponses = projectService.getProjectsByStatus(status, pageable, currentUser1);
         return ResponseEntity.ok(projectResponses);
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<Page<ProjectResponse>> getProjectsByOwnerId(@PathVariable Long ownerId, Pageable pageable) {
        User currentUser1 = userContext.getCurrentUser();
        Page<ProjectResponse> projectResponse = projectService.getProjectsByOwnerId(ownerId, pageable,  currentUser1);
        return ResponseEntity.ok(projectResponse);
    }

    @GetMapping
    public ResponseEntity<Page<ProjectResponse>> getAllProjects(Pageable pageable) {
        System.out.println("=== getAllProjects called ===");
        try {
            User currentUser = userContext.getCurrentUser();
            System.out.println("User: " + currentUser.getUsername());
            System.out.println("Org: " + currentUser.getOrganization());
            return ResponseEntity.ok(projectService.getAllProjects(pageable, currentUser));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectDetailResponse> getProjectById(@PathVariable Long id
                                                                )
            throws AccessDeniedException {
        User currentUser = userContext.getCurrentUser();
        ProjectDetailResponse projectResponse = projectService.getProjectById(id, currentUser);
        return ResponseEntity.ok(projectResponse);
    }

    @GetMapping("/key/{key}")
    public   ResponseEntity<ProjectResponse> getProjectByKey(@PathVariable String key) {
        ProjectResponse projectResponse = projectService.getProjectByKey(key);
        return ResponseEntity.ok(projectResponse);
    }
}
