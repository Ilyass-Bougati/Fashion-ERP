package com.sefault.server.security.properties;

import lombok.Getter;
import org.springframework.stereotype.Component;

@Getter
@Component("authorities")
public class ApplicationAuthorities {
    // Admin module authorities
    private final String manageAuthoritiesAuthority = "MANAGE_AUTHORITIES";
    private final String updateUsersAuthority = "UPDATE_USERS";
    private final String deleteUsersAuthority = "DELETE_USERS";
    private final String createUsersAuthority = "CREATE_USERS";
    private final String activateUsersAuthority = "ACTIVATE_USERS";
    private final String getUsersAuthority = "GET_USERS";
    private final String listUsersAuthority = "LIST_USERS";
}
