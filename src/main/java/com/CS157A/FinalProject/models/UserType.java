package com.CS157A.FinalProject.models;

/**
 * The kinds of user. The names match the user_type values stored in the users table,
 * so name() and valueOf() convert to and from the database.
 */
public enum UserType {
    CUSTOMER,
    STAFF
}
