package com.projectnova.meridian.dao;

import com.projectnova.meridian.model.Project;
import com.projectnova.meridian.model.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;


@Repository
public interface ProjectRepository extends JpaRepository<Project,Long> {

    //
    List<Project> findByOwnerId(Long ownerId);


    List<Project> findByStatus(ProjectStatus status);

    boolean existsByKey (String key);

    Optional<Project> findByKey(String key);

    Page<Project> findByOwnerId(Long ownerId, Pageable pageable);
    Page<Project> findByStatus(ProjectStatus status, Pageable pageable);

    @Query("SELECT p FROM Project p WHERE " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Project> searchProjects(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Filter projects
    @Query("SELECT p FROM Project p WHERE " +
            "(:status IS NULL OR p.status = :status) AND " +
            "(:ownerId IS NULL OR p.owner.id = :ownerId)")
    Page<Project> filterProjects(
            @Param("status") ProjectStatus status,
            @Param("ownerId") Long ownerId,
            Pageable pageable
    );

    Page<Project> findByOrganizationId(Long orgId, Pageable pageable);
    Page<Project> findByOrganizationIdAndStatus(Long orgId, ProjectStatus status, Pageable pageable);
    Long countByOrganizationId(Long orgId);
    Boolean existsByKeyAndOrganizationId(String key, Long orgId);


    // 1. searchProjectsByOrganization
    @Query("SELECT p FROM Project p WHERE p.organization.id = :orgId AND (" +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Project> searchProjectsByOrganization(@Param("orgId") Long orgId,
                                               @Param("searchTerm") String searchTerm,
                                               Pageable pageable);


    // 2. filterProjectsByOrganization
    @Query("SELECT p FROM Project p WHERE p.organization.id = :orgId AND " +
            "(:status IS NULL OR p.status = :status) AND " +
            "(:ownerId IS NULL OR p.owner.id = :ownerId)")
    Page<Project> filterProjectsByOrganization(@Param("orgId") Long orgId,
                                               @Param("status") ProjectStatus status,
                                               @Param("ownerId") Long ownerId,
                                               Pageable pageable);

    Page<Project> findByOrganizationIdAndOwnerId(Long orgId, Long ownerId, Pageable pageable);
}
