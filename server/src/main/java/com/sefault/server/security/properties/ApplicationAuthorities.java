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

    // Reports module authorities
    private final String readReportsAuthority = "READ_REPORTS";

    // Sales module authorities
    private final String createSaleAuthority = "CREATE_SALE";
    private final String readSaleAuthority = "READ_SALE";
    private final String refundSaleAuthority = "REFUND_SALE";

    private final String createSaleLineAuthority = "CREATE_SALE_LINE";
    private final String readSaleLineAuthority = "READ_SALE_LINE";
    private final String updateSaleLineAuthority = "UPDATE_SALE_LINE";
    private final String deleteSaleLineAuthority = "DELETE_SALE_LINE";

    // Storage / Inventory module authorities
    private final String createProductAuthority = "CREATE_PRODUCT";
    private final String updateProductAuthority = "UPDATE_PRODUCT";
    private final String deleteProductAuthority = "DELETE_PRODUCT";
    private final String getProductAuthority = "GET_PRODUCT";
    private final String listProductsAuthority = "LIST_PRODUCTS";

    private final String createProductCategoryAuthority = "CREATE_PRODUCT_CATEGORY";
    private final String updateProductCategoryAuthority = "UPDATE_PRODUCT_CATEGORY";
    private final String deleteProductCategoryAuthority = "DELETE_PRODUCT_CATEGORY";
    private final String getProductCategoryAuthority = "GET_PRODUCT_CATEGORY";
    private final String listProductCategoriesAuthority = "LIST_PRODUCT_CATEGORIES";

    private final String createProductVariationAuthority = "CREATE_PRODUCT_VARIATION";
    private final String updateProductVariationAuthority = "UPDATE_PRODUCT_VARIATION";
    private final String deleteProductVariationAuthority = "DELETE_PRODUCT_VARIATION";
    private final String getProductVariationAuthority = "GET_PRODUCT_VARIATION";
    private final String listProductVariationsAuthority = "LIST_PRODUCT_VARIATIONS";

    private final String createVendorAuthority = "CREATE_VENDOR";
    private final String updateVendorAuthority = "UPDATE_VENDOR";
    private final String deleteVendorAuthority = "DELETE_VENDOR";
    private final String getVendorAuthority = "GET_VENDOR";
    private final String listVendorsAuthority = "LIST_VENDORS";

    // Dashboard module authorities
    private final String viewDashboardAuthority = "VIEW_DASHBOARD";
}
