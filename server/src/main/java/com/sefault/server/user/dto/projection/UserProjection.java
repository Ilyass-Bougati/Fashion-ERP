package com.sefault.server.user.dto.projection;

import com.sefault.server.user.entity.Session;
import com.sefault.server.user.entity.UserAuthority;
import com.sefault.server.user.entity.UserReport;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface UserProjection {
    UUID getId();

    String getFirstName();

    String getLastName();

    String getEmail();

    String getPassword();

    List<Session> getSessions();

    List<UserAuthority> getUserAuthorities();

    List<UserReport> getUserReports();

    List<UserAuthority> getGrantedAuthorities();

    String getPhoneNumber();

    Boolean getActive();

    LocalDateTime getCreatedAt();

    LocalDateTime getUpdatedAt();
}