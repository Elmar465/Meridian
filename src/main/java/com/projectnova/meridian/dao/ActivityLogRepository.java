package com.projectnova.meridian.dao;


import com.projectnova.meridian.model.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityLogRepository  extends JpaRepository<ActivityLog,Long> {

    List<ActivityLog> findByIssueId(Long  issueId);

    List<ActivityLog> findByUserId(Long userId);

    List<ActivityLog> findByIssueIdOrderByCreatedAtDesc(Long issueId);

    Page<ActivityLog> findByIssueId(Long issueId, Pageable pageable);

    Page<ActivityLog> findByUserId(Long userId, Pageable pageable);
}
