package com.projectnova.meridian.controller;


import com.projectnova.meridian.dto.*;
import com.projectnova.meridian.model.Project;
import com.projectnova.meridian.model.ProjectStatus;
import com.projectnova.meridian.service.ProjectService;
import com.projectnova.meridian.utils.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects")
public class ProjectController {

private final ProjectService projectService;

private final UserContext userContext;


    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @DeleteMapping("/{projectId}/members/{userId}")
    public ResponseEntity<Void> deleteMember(@PathVariable Long projectId, @PathVariable Long userId) {
        projectService.removeMember(projectId, userId);
        return ResponseEntity.noContent().build();
    }



    @GetMapping("/{projectId}/members")
    public ResponseEntity<List<ProjectMemberResponse>> getProjectMembers(@PathVariable Long projectId) {
        List<ProjectMemberResponse> projectMemberResponse  = projectService.getProjectMembers(projectId);
        return ResponseEntity.ok(projectMemberResponse);
    }


    @PostMapping("/{projectId}/members")
    public ResponseEntity<ProjectMemberResponse>  addMember(@PathVariable Long projectId, @RequestBody AddMemberRequest request ){
        ProjectMemberResponse projectResponse = projectService.addMember(projectId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(projectResponse);
    }


    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long projectId) {
        projectService.deleteProject(projectId);
        return ResponseEntity.noContent().build();
    }


    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> updateProject(@PathVariable Long id, @RequestBody
    @Valid UpdateProjectRequest request){
        ProjectResponse update = projectService.updateProject(id, request);
        return new  ResponseEntity<>(update, HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProjectResponse>> searchProjects(@RequestParam String searchTerm,
                                                                Pageable pageable) {
        Page<ProjectResponse> projectResponseList = projectService.searchProjects(searchTerm, pageable);
        return ResponseEntity.ok(projectResponseList);

    }

    @GetMapping("/filter")
    public ResponseEntity<Page<ProjectResponse>> filteProject(
            @RequestParam(required = false) ProjectStatus status,
            @RequestParam(required = false) Long ownerId,
            Pageable pageable
    ) {
        Page<ProjectResponse> filter = projectService.filterProjects(status, ownerId, pageable);
        return ResponseEntity.ok(filter);
    }

    @PostMapping()
    public ResponseEntity<ProjectResponse> createProject(@RequestBody @Valid CreateProjectRequest request) {
        Long userId = userContext.getCurrentUserId();
        ProjectResponse projectResponse = projectService.createProject(request, userId);
        return new  ResponseEntity<>(projectResponse, HttpStatus.CREATED);
    }


    @GetMapping("/status/{status}")
    public ResponseEntity<Page<ProjectResponse>> getProjectsByStatus(@PathVariable("status") ProjectStatus status,
                                                                     Pageable pageable) {
         Page<ProjectResponse> projectResponses = projectService.getProjectsByStatus(status, pageable);
         return ResponseEntity.ok(projectResponses);
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<Page<ProjectResponse>> getProjectsByOwnerId(@PathVariable Long ownerId, Pageable pageable) {

        Page<ProjectResponse> projectResponse = projectService.getProjectsByOwnerId(ownerId, pageable);
        return ResponseEntity.ok(projectResponse);
    }

    @GetMapping
    public ResponseEntity<Page<ProjectResponse>> getAllProjects(Pageable pageable) {
        return ResponseEntity.ok(projectService.getAllProjects(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectDetailResponse> getProjectById(@PathVariable Long id) {
        ProjectDetailResponse projectResponse = projectService.getProjectById(id);
        return ResponseEntity.ok(projectResponse);
    }

    @GetMapping("/key/{key}")
    public   ResponseEntity<ProjectResponse> getProjectByKey(@PathVariable String key) {
        ProjectResponse projectResponse = projectService.getProjectByKey(key);
        return ResponseEntity.ok(projectResponse);
    }
}
