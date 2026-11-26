package com.CS157A.FinalProject.services;

import com.CS157A.FinalProject.dao.UserDao;
import com.CS157A.FinalProject.models.UserAccount;
import com.CS157A.FinalProject.models.UserType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UserDao userDao;
    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    // Checked when the email doesn't exist, so a wrong email takes as long
    // as a wrong password and can't be used to discover which emails exist.
    private final String dummyHash = encoder.encode("not-a-real-password");

    public AuthService(UserDao userDao) {
        this.userDao = userDao;
    }

    /**
     * Returns the account if the email and password match, otherwise empty.
     * For staff, the account includes the id of the restaurant they manage.
     * Callers should show one message for both failures: "Invalid email or password".
     */
    public Optional<UserAccount> login(String email, String rawPassword) {
        if (email == null || rawPassword == null) {
            return Optional.empty();
        }

        Optional<UserDao.Credentials> found = userDao.findCredentialsByEmail(email);

        if (found.isEmpty()) {
            encoder.matches(rawPassword, dummyHash);   // equalize timing
            return Optional.empty();
        }

        UserDao.Credentials creds = found.get();
        if (creds.passwordHash() == null || !encoder.matches(rawPassword, creds.passwordHash())) {
            return Optional.empty();
        }
        return Optional.of(creds.account());
    }

    /** Use this when changing a password. */
    public String hashPassword(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    /**
     * Registers a customer and returns the new account.
     * Throws UserDao.EmailAlreadyUsedException if the email is taken.
     */
    public UserAccount registerCustomer(String name, String email, String rawPassword,
                                        String phoneNumber, String profileSpecialRequest) {
        UserDao.NewUser user = new UserDao.NewUser(name, email, encoder.encode(rawPassword), phoneNumber);
        int userId = userDao.createCustomer(user, profileSpecialRequest);
        return new UserAccount(userId, UserType.CUSTOMER, name.trim(), email.trim(), null);
    }

    /**
     * Registers a restaurant together with the staff member who manages it and returns
     * the staff member's account, including the new restaurant's id.
     * Throws UserDao.EmailAlreadyUsedException if the email is taken.
     */
    public UserAccount registerRestaurant(String staffName, String email, String rawPassword,
                                          String staffPhone, UserDao.NewRestaurant restaurant) {
        UserDao.NewUser staff = new UserDao.NewUser(staffName, email, encoder.encode(rawPassword), staffPhone);
        UserDao.StaffIds ids = userDao.createStaffWithRestaurant(staff, restaurant);
        return new UserAccount(ids.userId(), UserType.STAFF, staffName.trim(), email.trim(), ids.restaurantId());
    }
}