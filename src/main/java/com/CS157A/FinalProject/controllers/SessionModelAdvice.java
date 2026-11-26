package com.CS157A.FinalProject.controllers;

import com.CS157A.FinalProject.models.UserType;
import com.CS157A.FinalProject.util.AuthUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Adds "loggedIn" and "role" to every page, so the nav bar can show the right links. */
@ControllerAdvice
public class SessionModelAdvice {

    @ModelAttribute
    public void addSessionInfo(HttpSession session, Model model) {
        model.addAttribute("loggedIn", session.getAttribute(AuthUtil.SESSION_USER_ID) != null);
        // The templates compare role with plain text, such as role == 'CUSTOMER', so pass the name.
        Object role = session.getAttribute(AuthUtil.SESSION_ROLE);
        model.addAttribute("role", role instanceof UserType type ? type.name() : null);
    }
}
