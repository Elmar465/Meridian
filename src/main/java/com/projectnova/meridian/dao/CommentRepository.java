package com.projectnova.meridian.dao;


import com.projectnova.meridian.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository  extends JpaRepository<Comment,Long> {

    List<Comment> findByIssueId(Long  issueId);

    List<Comment> findByUserId (Long  userId);

    Long countByIssueId(Long  issueId);

    Page<Comment> findByIssueId(Long issueId, Pageable pageable);

    Page<Comment> findByUserId(Long userId, Pageable pageable);

}
