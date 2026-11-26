package com.CS157A.FinalProject.dao;


import com.CS157A.FinalProject.db.DatabaseConnection;
import com.CS157A.FinalProject.models.UserAccount;
import com.CS157A.FinalProject.models.UserType;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

@Repository
public class UserDao {

    public record Credentials(UserAccount account, String passwordHash) {}

    public record NewUser(String name, String email, String passwordHash, String phoneNumber) {}

    public record NewRestaurant(String name, String phoneNumber,
                                String street, String city, String state, String zipCode,
                                String description, String diningStyle, String dressCode) {}

    /** The ids created by restaurant registration. */
    public record StaffIds(int userId, int restaurantId) {}

    public static class EmailAlreadyUsedException extends RuntimeException {
        public EmailAlreadyUsedException(String email) {
            super("Email already registered: " + email);
        }
    }

    private static final int MYSQL_DUPLICATE_KEY = 1062;

    // queries can be changed as our schema gets updated
    private static final String FIND_BY_EMAIL =
            "SELECT u.user_id, u.user_type, u.name, u.email, u.password_hash, r.restaurant_id " +
                    "FROM users u " +
                    "LEFT JOIN restaurant r ON r.manager_staff_id = u.user_id " +
                    "WHERE u.email = ?";

    private static final String INSERT_USER =
            "INSERT INTO users (user_type, name, email, password_hash, phone_number) " +
                    "VALUES (?, ?, ?, ?, ?)";

    private static final String INSERT_CUSTOMER =
            "INSERT INTO customer (user_id, profile_special_request) VALUES (?, ?)";

    private static final String INSERT_STAFF =
            "INSERT INTO restaurant_staff (user_id) VALUES (?)";

    // Adds the ZIP code if it's new; if it already exists, the stored city/state are kept.
    private static final String UPSERT_ZIP =
            "INSERT INTO zip_code (zip_code, city, state) VALUES (?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE zip_code = zip_code";

    private static final String INSERT_RESTAURANT =
            "INSERT INTO restaurant (name, description, dress_code, dining_style, street, zip_code, manager_staff_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String INSERT_RESTAURANT_PHONE =
            "INSERT INTO restaurant_phone (restaurant_id, phone_number) VALUES (?, ?)";


    /**
     * Looks up a user by email. Returns empty if no user has that email.
     * The database's default collation ignores case, so "Maya.Patel@Example.com"
     * finds "maya.patel@example.com".
     */
    public Optional<Credentials> findCredentialsByEmail(String email) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_BY_EMAIL)) {

            ps.setString(1, email.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                UserAccount account = new UserAccount(
                        rs.getInt("user_id"),
                        UserType.valueOf(rs.getString("user_type")),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getObject("restaurant_id", Integer.class));   // null for customers
                return Optional.of(new Credentials(account, rs.getString("password_hash")));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Database error while looking up user", e);
        }
    }

    /** Creates a users row + customer row in one transaction. Returns the new user_id. */
    public int createCustomer(NewUser user, String profileSpecialRequest) {
        return inTransaction(user.email(), conn -> {
            int userId = insertUser(conn, UserType.CUSTOMER, user);
            try (PreparedStatement ps = conn.prepareStatement(INSERT_CUSTOMER)) {
                ps.setInt(1, userId);
                ps.setString(2, profileSpecialRequest);
                ps.executeUpdate();
            }
            return userId;
        });
    }

    /**
     * Restaurant registration. In one transaction, creates the staff member's users row
     * (type STAFF), their restaurant_staff row, the ZIP code if it's new, the
     * restaurant with the staff member as its manager, and the restaurant's phone number.
     * Either all of it is saved or none of it is.
     */
    public StaffIds createStaffWithRestaurant(NewUser staff, NewRestaurant restaurant) {
        return inTransaction(staff.email(), conn -> {
            int userId = insertUser(conn, UserType.STAFF, staff);

            try (PreparedStatement ps = conn.prepareStatement(INSERT_STAFF)) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(UPSERT_ZIP)) {
                ps.setString(1, restaurant.zipCode().trim());
                ps.setString(2, restaurant.city().trim());
                ps.setString(3, restaurant.state().trim());
                ps.executeUpdate();
            }

            int restaurantId;
            try (PreparedStatement ps = conn.prepareStatement(INSERT_RESTAURANT, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, restaurant.name().trim());
                ps.setString(2, restaurant.description());
                ps.setString(3, restaurant.dressCode());
                ps.setString(4, restaurant.diningStyle());
                ps.setString(5, restaurant.street().trim());
                ps.setString(6, restaurant.zipCode().trim());
                ps.setInt(7, userId);
                ps.executeUpdate();
                restaurantId = generatedId(ps);
            }

            if (restaurant.phoneNumber() != null) {
                try (PreparedStatement ps = conn.prepareStatement(INSERT_RESTAURANT_PHONE)) {
                    ps.setInt(1, restaurantId);
                    ps.setString(2, restaurant.phoneNumber().trim());
                    ps.executeUpdate();
                }
            }
            return new StaffIds(userId, restaurantId);
        });
    }


    private int insertUser(Connection conn, UserType userType, NewUser user) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(INSERT_USER, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, userType.name());
            ps.setString(2, user.name().trim());
            ps.setString(3, user.email().trim());
            ps.setString(4, user.passwordHash());
            ps.setString(5, user.phoneNumber());
            ps.executeUpdate();
            return generatedId(ps);
        }
    }

    /** Reads the AUTO_INCREMENT id created by the last insert on this statement. */
    private static int generatedId(PreparedStatement ps) throws SQLException {
        try (ResultSet keys = ps.getGeneratedKeys()) {
            keys.next();
            return keys.getInt(1);
        }
    }

    @FunctionalInterface
    private interface TxWork<T> {
        T run(Connection conn) throws SQLException;
    }

    /** Runs the work as one transaction: everything is saved, or nothing is. */
    private <T> T inTransaction(String email, TxWork<T> work) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                T result = work.run(conn);
                conn.commit();
                return result;
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                if (e instanceof SQLException sql && sql.getErrorCode() == MYSQL_DUPLICATE_KEY) {
                    throw new EmailAlreadyUsedException(email);
                }
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Database error while creating user", e);
        }
    }

}