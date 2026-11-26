package com.CS157A.FinalProject.models;

/**
 * A logged-in user. Deliberately has no password field, so the hash
 * never travels past the login code.
 *
 * restaurantId is the restaurant this staff member manages: set for STAFF, null for CUSTOMER.
 */
public record UserAccount(int userId, UserType userType, String name, String email, Integer restaurantId) {

    public boolean isCustomer() {
        return userType == UserType.CUSTOMER;
    }

    public boolean isStaff() {
        return userType == UserType.STAFF;
    }
}
