package com.projectnova.meridian.dao;


import com.projectnova.meridian.model.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttachmentRepository  extends JpaRepository<Attachment,Long> {

    List<Attachment> findByIssueId(Long issueId);

    List<Attachment> findByUploadedById(Long uploadedId);

    Long countByIssueId(Long issueId);


}
