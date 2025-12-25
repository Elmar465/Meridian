package com.projectnova.meridian.service;


import com.projectnova.meridian.dao.IssueRepository;
import com.projectnova.meridian.dao.ProjectMemberRepository;
import com.projectnova.meridian.dao.ProjectRepository;
import com.projectnova.meridian.dao.UserRepository;
import com.projectnova.meridian.dto.*;
import com.projectnova.meridian.exceptions.DuplicateResourceException;
import com.projectnova.meridian.exceptions.ResourceNotFoundException;
import com.projectnova.meridian.model.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {


    private final UserService userService;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final IssueRepository  issueRepository;
    private final UserRepository  userRepository;
    private final EmailService emailService;


    private ProjectResponse convertToResponse(Project project) {
        ProjectResponse projectResponse = new ProjectResponse();
        projectResponse.setId(project.getId());
        projectResponse.setName(project.getName());
        projectResponse.setKey(project.getKey());
        projectResponse.setDescription(project.getDescription());
        projectResponse.setStatus(project.getStatus());
        projectResponse.setOwnerId(project.getOwner().getId());
        projectResponse.setOwnerName(project.getOwner().getFirstName() + " " + project.getOwner().getLastName());
        projectResponse.setCreatedAt(project.getCreatedAt());
        projectResponse.setUpdatedAt(project.getUpdatedAt());
        return projectResponse;
    }


    public Page<ProjectResponse> getProjectsByOwnerId(Long ownerId, Pageable pageable) {
        Page<Project>  projects = projectRepository.findByOwnerId(ownerId, pageable);
        return projects.map(this::convertToResponse);
    }

    private Project convertToEntity(CreateProjectRequest request, User owner) {
        Project project = new Project();
        project.setName(request.getName());
        project.setKey(request.getKey());
        project.setDescription(request.getDescription());
        project.setOwner(owner);
        project.setStatus(request.getStatus() != null ? request.getStatus() : ProjectStatus.ACTIVE);
        return project;
    }

    private Project updateEntity(Project project, UpdateProjectRequest request) {
        if(request.getName() != null) {
            project.setName(request.getName());
        }
        if(request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }
        if(request.getStatus() != null) {
            project.setStatus(request.getStatus());
        }
        return project;
    }

    private List<ProjectResponse> convertToResponseList(List<Project> projects) {
        return  projects.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private ProjectMemberResponse convertMemberToResponse(ProjectMember member) {
        ProjectMemberResponse response = new ProjectMemberResponse();

        // Set fields from ProjectMember:
        response.setId(member.getId());
        response.setUserId(member.getUser().getId());
        response.setUsername(member.getUser().getUsername());
        response.setEmail(member.getUser().getEmail());
        response.setFullName(member.getUser().getFirstName() + " " + member.getUser().getLastName());
        response.setAvatar(member.getUser().getAvatar());
        response.setRole(member.getRole());
        response.setJoinedAt(member.getJoinedAt());

        return response;
    }

    private List<ProjectMemberResponse> convertMembersToResponseList(List<ProjectMember> projectMembers) {
        return projectMembers.stream()
                .map(this::convertMemberToResponse)
                .collect(Collectors.toList());
    }

    private ProjectDetailResponse convertToDetailResponse(Project project) {
        ProjectDetailResponse response = new ProjectDetailResponse();

        response.setId(project.getId());;
        response.setName(project.getName());
        response.setKey(project.getKey());
        response.setDescription(project.getDescription());
        response.setStatus(project.getStatus());
        response.setOwnerId(project.getOwner().getId());
        response.setOwnerName(project.getOwner().getFirstName() + " " + project.getOwner().getLastName());
        response.setCreatedAt(project.getCreatedAt());
        response.setUpdatedAt(project.getUpdatedAt());
        List<ProjectMember> members = projectMemberRepository.findByProjectId(project.getId());
        response.setMembers(convertMembersToResponseList(members));

        // Count issue
        Long issue = issueRepository.countByProjectId(project.getId());
        response.setIssueCount(issue);

        return response;
    }

    public boolean existsByKey(String key) {
        return projectRepository.existsByKey(key);
    }

    public Page<ProjectResponse> getAllProjects(Pageable pageable) {
        Page<Project> projects = projectRepository.findAll(pageable);
        return projects.map(this::convertToResponse);
    }

    public Page<ProjectResponse> getProjectsByStatus(ProjectStatus status, Pageable pageable) {
        Page<Project> projects = projectRepository.findByStatus(status, pageable);
        return projects.map(this::convertToResponse);
    }

    public List<ProjectResponse> getProjectsByStatus(ProjectStatus projectStatus) {
        return convertToResponseList(projectRepository.findByStatus(projectStatus));
    }

    public ProjectResponse getProjectByKey(String key) {
        return convertToResponse(projectRepository.findByKey(key).orElseThrow(() ->
                new ResourceNotFoundException("Project not found" + key)));
    }

    public ProjectDetailResponse  getProjectById(Long id) {
        return convertToDetailResponse(projectRepository
                .findById(id).orElseThrow(() -> new ResourceNotFoundException("Project id not found" + id)));
    }

    @Transactional
    public ProjectResponse createProject(CreateProjectRequest createProjectRequest, Long ownerId) {
        if(existsByKey(createProjectRequest.getKey())) {
            throw  new DuplicateResourceException("Project key already exists");
        }

        User owner = userRepository.findById(ownerId).orElseThrow(()
                -> new ResourceNotFoundException("Owner not found" + ownerId));

        Project project = convertToEntity(createProjectRequest, owner);
        Project savedProject = projectRepository.save(project);
        return convertToResponse(savedProject);
    }


    public Page<ProjectResponse> searchProjects(String searchTerm, Pageable pageable) {
        Page<Project> project = projectRepository.searchProjects(searchTerm, pageable);
        return project.map(this::convertToResponse);
    }


    public Page<ProjectResponse>  filterProjects(ProjectStatus projectStatus, Long ownerId,  Pageable pageable) {
        Page<Project> projects = projectRepository.filterProjects(projectStatus, ownerId, pageable);
        return projects.map(this::convertToResponse);
    }

    @Transactional
    public ProjectResponse updateProject(Long id, UpdateProjectRequest updateProjectRequest) {
        Project existingProject  =  projectRepository.findById(id).orElseThrow(()
                -> new ResourceNotFoundException("Project  not found" + id));

        Project updatedProject  = updateEntity(existingProject, updateProjectRequest);

        Project saveProject =  projectRepository.save(updatedProject);
        return convertToResponse(saveProject);
    }

    @Transactional
    public void deleteProject(Long id) {
       Project existingProject = projectRepository.findById(id).orElseThrow(()
                -> new ResourceNotFoundException("Project  not found" + id));
       projectRepository.delete(existingProject);

    }

    public List<ProjectMemberResponse> getProjectMembers(Long projectId) {
        return convertMembersToResponseList(projectMemberRepository.findByProjectId(projectId));
    }

    @Transactional
    public ProjectMemberResponse addMember(Long projectId, AddMemberRequest request) {
        Project existingProject = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project  not found" + projectId));

        User existingUser = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found" + request.getUserId()));


        if(projectMemberRepository.existsByProjectIdAndUserId(projectId, request.getUserId())) {
            throw  new DuplicateResourceException("Project member already exists");
        }

        ProjectMember member = new ProjectMember();
        member.setProject(existingProject);
        member.setUser(existingUser);
        member.setRole(request.getRole() != null ? request.getRole() : ProjectMemberRole.MEMBER);

        ProjectMember savedMember = projectMemberRepository.save(member);
        emailService.sendProjectMemberAddedEmail(existingProject,existingUser);
        return convertMemberToResponse(savedMember);
    }




    @Transactional
    public void removeMember(Long projectId, Long userId) {
        if(!projectMemberRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw  new ResourceNotFoundException("Project member not found");
        }
        projectMemberRepository.deleteByProjectIdAndUserId(projectId, userId);
    }
}
