package com.CS157A.FinalProject.util;

import com.CS157A.FinalProject.models.UserType;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Shared login and role checks. Call one of these as the first line of an endpoint.
 *
 * Example:
 *   int userId = AuthUtil.requireCustomer(session);
 *
 * Covers two non-functional requirements:
 *   - Only authenticated customers can modify their own reservations.
 *   - Only authorized staff can update their own restaurant.
 */
public final class AuthUtil {

    /** Session attribute holding the logged-in user's id (Integer). Set when a user logs in or signs up. */
    public static final String SESSION_USER_ID = "userId";

    /** Session attribute holding the logged-in user's type (UserType). Set when a user logs in or signs up. */
    public static final String SESSION_ROLE = "role";

    private AuthUtil() {}

    /** Returns the logged-in user's ID, or throws 401 if not logged in. */
    public static int requireLogin(HttpSession session) {
        Object userId = session.getAttribute(SESSION_USER_ID);
        if (!(userId instanceof Integer id)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not logged in");
        }
        return id;
    }

    /** Returns the logged-in user's ID. Throws 401 if not logged in, 403 if the user isn't a customer. */
    public static int requireCustomer(HttpSession session) {
        int userId = requireLogin(session);
        if (session.getAttribute(SESSION_ROLE) != UserType.CUSTOMER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Customers only");
        }
        return userId;
    }

    /**
     * Returns the logged-in user's ID. Throws 401 if not logged in, 403 if the user isn't staff.
     * Use this when the restaurant comes from the staff member's own record, not from the request.
     */
    public static int requireStaff(HttpSession session) {
        int userId = requireLogin(session);
        if (session.getAttribute(SESSION_ROLE) != UserType.STAFF) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Staff only");
        }
        return userId;
    }

    /**
     * Returns the logged-in user's ID. Throws 401 if not logged in,
     * 403 unless the user is staff for this specific restaurant.
     */
    public static int requireStaffOf(HttpSession session, int restaurantId) {
        int userId = requireStaff(session);
        // TODO: once the schema is final, check with JDBC that this staff member works at restaurantId,
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED,
                "Staff restaurant check not implemented yet");
    }
}
