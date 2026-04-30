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

    // HR module authorities
    private final String createEmployeeAuthority = "CREATE_EMPLOYEE";
    private final String updateEmployeeAuthority = "UPDATE_EMPLOYEE";
    private final String deleteEmployeeAuthority = "DELETE_EMPLOYEE";
    private final String getEmployeeAuthority = "GET_EMPLOYEE";
    private final String listEmployeesAuthority = "LIST_EMPLOYEES";
    private final String terminateEmployeeAuthority = "TERMINATE_EMPLOYEE";

    private final String createIsleAuthority = "CREATE_ISLE";
    private final String updateIsleAuthority = "UPDATE_ISLE";
    private final String deleteIsleAuthority = "DELETE_ISLE";
    private final String getIsleAuthority = "GET_ISLE";
    private final String listIslesAuthority = "LIST_ISLES";
}
