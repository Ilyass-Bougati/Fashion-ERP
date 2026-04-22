package com.sefault.server.user.entity;

import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;
import lombok.Setter;
import org.hibernate.annotations.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "erp_users")
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotEmpty
    private String firstName;

    @NotEmpty
    private String lastName;

    // This needs to be unique
    @Email
    @NotEmpty
    private String email;

    @NotEmpty
    private String password;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Session> sessions = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserAuthority> userAuthorities = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserReport> userReports = new ArrayList<>();

    @OneToMany(mappedBy = "grantedBy", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserAuthority> grantedAuthorities = new ArrayList<>();

    // This needs validation and to be unique
    @NotEmpty
    private String phoneNumber;

    @NotNull
    private Boolean active = true;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
