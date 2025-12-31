package com.projectnova.meridian.dao;


import com.projectnova.meridian.model.Issue;
import com.projectnova.meridian.model.IssueStatus;
import com.projectnova.meridian.model.IssueType;
import com.projectnova.meridian.model.Priority;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IssueRepository extends JpaRepository<Issue,Long> {

    List<Issue> findByProjectId (Long projectId);

    List<Issue> findByProjectIdAndStatus(Long projectId, IssueStatus status);

    List<Issue> findByProjectIdAndAssigneeId(Long projectId,Long assigneeId);

    List<Issue> findByReporterId(Long reporterId);

    List<Issue> findByAssigneeId(Long assigneeId);

    Long countByProjectId(Long projectId);

    Optional<Issue> findTopByProjectIdOrderByIssueNumberDesc(Long projectId);

    Page<Issue> findByProjectId(Long projectId, Pageable pageable);

    Page<Issue> findByProjectIdAndStatus(Long projectId, IssueStatus status, Pageable pageable);

    Page<Issue> findByAssigneeId(Long assigneeId, Pageable pageable);

    Page<Issue> findByReporterId(Long reporterId, Pageable pageable);

    Page<Issue> findByProjectIdAndAssigneeId(Long projectId, Long assigneeId, Pageable pageable);


    // Search issues by title or description (case-insensitive)
    @Query("SELECT i FROM Issue i WHERE " +
            "LOWER(i.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(i.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Issue> searchIssues(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT i FROM Issue i WHERE " +
            "(:projectId IS NULL OR i.project.id = :projectId) AND " +
            "(:status IS NULL OR i.status = :status) AND " +
            "(:priority IS NULL OR i.priority = :priority) AND " +
            "(:type IS NULL OR i.type = :type) AND " +
            "(:assigneeId IS NULL OR i.assignee.id = :assigneeId) AND " +
            "(:reporterId IS NULL OR i.reporter.id = :reporterId)")
    Page<Issue> filterIssues(
            @Param("projectId") Long projectId,
            @Param("status") IssueStatus status,
            @Param("priority") Priority priority,
            @Param("type") IssueType type,
            @Param("assigneeId") Long assigneeId,
            @Param("reporterId") Long reporterId,
            Pageable pageable
    );

    @Query("SELECT i FROM Issue i WHERE i.project.organization.id = :orgId")
    Page<Issue> findByOrganizationId(@Param("orgId") Long orgId, Pageable pageable);
}
