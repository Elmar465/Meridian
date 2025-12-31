package com.projectnova.meridian.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100)
    private String name;
    @Column(unique = true, nullable = false, length = 50)
    private String slug;
    @Column(length = 500)
    private String description;
    @Column(length = 500)
    private String logo;
    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;
    @OneToMany(mappedBy = "organization")
    private List<User> members;
    @OneToMany(mappedBy = "organization")
    private List<Project> projects;
    @OneToMany(mappedBy = "organization")
    private List<Invitation> invitations;
    @CreatedDate
    private LocalDateTime createdDate;
    @LastModifiedDate
    private LocalDateTime lastModifiedDate;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 20)
    private OrganizationStatus status;

}
