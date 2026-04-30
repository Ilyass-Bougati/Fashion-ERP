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

    // Finance module authorities
    private final String createTransactionAuthority = "CREATE_TRANSACTION";
    private final String readTransactionAuthority = "READ_TRANSACTION";
    private final String reverseTransactionAuthority = "REVERSE_TRANSACTION";
    private final String createFixedChargeAuthority = "CREATE_FIXED_CHARGE";
    private final String readFixedChargeAuthority = "READ_FIXED_CHARGE";
    private final String updateFixedChargeAuthority = "UPDATE_FIXED_CHARGE";
    private final String toggleFixedChargeAuthority = "TOGGLE_FIXED_CHARGE";
    private final String processPayrollAuthority = "PROCESS_PAYROLL";
    private final String readPayrollAuthority = "READ_PAYROLL";
}
