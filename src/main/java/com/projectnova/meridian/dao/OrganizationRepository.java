package com.projectnova.meridian.dao;

import com.projectnova.meridian.model.Organization;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {


    Optional<Organization> findBySlug(String slug);
    Boolean existsBySlug(String slug);
    Page<Organization> findByOwnerId(Long ownerId, Pageable pageable);
    Page<Organization> findByMembersId(Long userId, Pageable pageable);
}
