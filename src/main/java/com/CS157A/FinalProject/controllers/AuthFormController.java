package com.CS157A.FinalProject.controllers;

import com.CS157A.FinalProject.dao.UserDao;
import com.CS157A.FinalProject.models.UserAccount;
import com.CS157A.FinalProject.services.AuthService;
import com.CS157A.FinalProject.util.AuthUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.Optional;

/**
 * Handles the login, sign-up, restaurant sign-up and logout forms.
 * The GET pages for these forms are in PageController.
 * On an error the same page is shown again with the "error" message and the user's input in "form".
 */
@Controller
public class AuthFormController {

    private static final String LOGIN_VIEW = "login";
    private static final String REGISTER_VIEW = "register";
    private static final String RESTAURANT_VIEW = "Register Restaurant";

    private final AuthService authService;

    public AuthFormController(AuthService authService) {
        this.authService = authService;
    }

    /** POST /login : checks email and password, then logs the user in. */
    @PostMapping("/login")
    public String login(@RequestParam Map<String, String> form, HttpSession session, Model model) {
        String email = form.get("email");
        String password = form.get("password");

        if (isBlank(email) || isEmpty(password)) {
            return loginError(model, email, form.get("next"), "Email and password are required");
        }

        Optional<UserAccount> result = authService.login(email, password);
        if (result.isEmpty()) {
            return loginError(model, email, form.get("next"), "Invalid email or password");
        }

        logIn(session, result.get());
        return "redirect:" + safeNext(form.get("next"));
    }

    /** POST /register : creates a customer account and logs them in. */
    @PostMapping("/register")
    public String registerCustomer(@RequestParam Map<String, String> form, HttpSession session, Model model) {
        String name = form.get("name");
        String email = form.get("email");
        String phone = form.get("phone");

        if (isBlank(name) || isBlank(email) || isBlank(phone) || isEmpty(form.get("password"))) {
            return showError(model, REGISTER_VIEW, form, "Name, email, phone number and password are required");
        }
        String passwordError = checkPasswords(form);
        if (passwordError != null) {
            return showError(model, REGISTER_VIEW, form, passwordError);
        }

        try {
            UserAccount user = authService.registerCustomer(name, email, form.get("password"),
                    phone.trim(), blankToNull(form.get("specialRequest")));
            logIn(session, user);
            return "redirect:/";
        } catch (UserDao.EmailAlreadyUsedException e) {
            return showError(model, REGISTER_VIEW, form, "That email is already registered");
        }
    }

    /** POST /register/restaurant : creates the staff member's account and their restaurant, then logs them in. */
    @PostMapping("/register/restaurant")
    public String registerRestaurant(@RequestParam Map<String, String> form, HttpSession session, Model model) {
        if (isBlank(form.get("name")) || isBlank(form.get("email")) || isEmpty(form.get("password"))
                || isBlank(form.get("restaurantName")) || isBlank(form.get("street"))
                || isBlank(form.get("city")) || isBlank(form.get("state")) || isBlank(form.get("zipCode"))) {
            return showError(model, RESTAURANT_VIEW, form,
                    "Your name, email, password, and the restaurant's name and full address are required");
        }
        String passwordError = checkPasswords(form);
        if (passwordError != null) {
            return showError(model, RESTAURANT_VIEW, form, passwordError);
        }

        UserDao.NewRestaurant restaurant = new UserDao.NewRestaurant(
                form.get("restaurantName"),
                blankToNull(form.get("restaurantPhone")),
                form.get("street"),
                form.get("city"),
                form.get("state"),
                form.get("zipCode"),
                blankToNull(form.get("description")),
                blankToNull(form.get("diningStyle")),
                blankToNull(form.get("dressCode")));

        try {
            UserAccount staff = authService.registerRestaurant(form.get("name"), form.get("email"),
                    form.get("password"), blankToNull(form.get("phone")), restaurant);
            logIn(session, staff);
            return "redirect:/";
        } catch (UserDao.EmailAlreadyUsedException e) {
            return showError(model, RESTAURANT_VIEW, form, "That email is already registered");
        }
    }

    /** POST /logout : ends the session. */
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    private static void logIn(HttpSession session, UserAccount user) {
        session.setAttribute(AuthUtil.SESSION_USER_ID, user.userId());
        session.setAttribute(AuthUtil.SESSION_ROLE, user.userType());
    }

    /** Returns an error message if the password and its confirmation differ, otherwise null. */
    private static String checkPasswords(Map<String, String> form) {
        String confirm = form.get("confirmPassword");
        if (confirm == null || !form.get("password").equals(confirm)) {
            return "Passwords do not match";
        }
        return null;
    }

    private static String loginError(Model model, String email, String next, String error) {
        model.addAttribute("email", email);
        model.addAttribute("next", next);
        model.addAttribute("error", error);
        return LOGIN_VIEW;
    }

    private static String showError(Model model, String view, Map<String, String> form, String error) {
        model.addAttribute("form", form);
        model.addAttribute("error", error);
        return view;
    }

    /** Only allows redirects to a path on this site, so a link can't send users elsewhere after login. */
    private static String safeNext(String next) {
        if (next != null && next.startsWith("/") && !next.startsWith("//") && !next.startsWith("/\\")) {
            return next;
        }
        return "/";
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }

    private static String blankToNull(String s) {
        return isBlank(s) ? null : s.trim();
    }
}
