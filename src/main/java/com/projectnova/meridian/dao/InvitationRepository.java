package com.projectnova.meridian.dao;

import com.projectnova.meridian.model.Invitation;
import com.projectnova.meridian.model.InvitationStatus;
import com.projectnova.meridian.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, Long> {

    Optional<Invitation> findByToken(String token);
    Optional<Invitation> findByEmailAndStatus(String email, InvitationStatus status);
    Page<Invitation> findByInvitedBy(User user, Pageable pageable);
    Page<Invitation> findByStatus(InvitationStatus status, Pageable pageable);
    Page<Invitation> findAll(Pageable pageable);
    List<Invitation> findAllByExpiresAtBeforeAndStatus(LocalDateTime dateTime, InvitationStatus status);
    boolean existsByEmailAndStatus(String email, InvitationStatus status);
}
