package com.projectnova.meridian.dao;

import com.projectnova.meridian.model.User;
import com.projectnova.meridian.model.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User,Long> {


    //Find User by userName
    Optional<User> findByUsername(String username);

    //Find User by email
    Optional<User> findByEmail(String email);

    //Check if username exist
    boolean existsByUsername(String username);

    //Find usersBy Role
    List<User> findByRole(UserRole role);

    //Find active Users only
    List<User> findByIsActiveTrue();

    boolean existsByEmail(String email);

    Page<User> findByIsActive(Boolean isActive, Pageable pageable);
    Page<User> findByRole(UserRole role, Pageable pageable);



    @Query("SELECT u FROM User  u WHERE " +
            "LOWER(u.firstName)  LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.lastName) LIKE  LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) ")
    Page<User> searchUsers(@Param("searchTerm")  String searchTerm, Pageable pageable);



    @Query("SELECT u FROM User u WHERE " +
            "(:role IS NULL OR u.role = :role) AND " +
            "(:isActive IS NULL OR u.isActive = :isActive)")
    Page<User> filterUsers(@Param("role") UserRole role ,
                           @Param("isActive") Boolean isActive,
                           Pageable pageable);
}
