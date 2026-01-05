package com.projectnova.meridian.model;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Data
@Table(name = "users")
@NoArgsConstructor
@ToString(exclude = {"organization", "ownedOrganizations", "sentInvitations"})
@EqualsAndHashCode(exclude = {"organization", "ownedOrganizations", "sentInvitations"})
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class User implements UserDetails {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(unique = true, length = 50)
    private String username;
    @Column(unique = true, length = 100)
    private String email;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false, length = 255)
    private String password;
    @Column(length = 50)
    private String firstName;
    @Column(length = 50)
    private String lastName;
    @Column(length = 500)
    private String avatar;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role = UserRole.MEMBER;
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT true")
    private Boolean isActive;
    @Column(nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;
    @Column(nullable = false)
    @LastModifiedDate
    private LocalDateTime updatedAt;
    @OneToMany(mappedBy = "invitedBy")
    private List<Invitation> sentInvitations;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "organization_id")
    private Organization organization;
    @OneToMany(mappedBy = "owner")
    private List<Organization> ownedOrganizations;



    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isActive != null && isActive;
    }

}

