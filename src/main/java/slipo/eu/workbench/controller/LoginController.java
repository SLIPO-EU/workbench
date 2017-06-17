package slipo.eu.workbench.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpSession;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.WebAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController
{
    private static Logger logger = LoggerFactory.getLogger(LoginController.class);
    
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String login(HttpSession session, Model model, @RequestParam(required = false) String error)
    {
        if (error != null) { 
            model.addAttribute("error", error);
            AuthenticationException ex = (AuthenticationException) 
                session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
            model.addAttribute("errorMessage", ex.getMessage());
        }
        
        return "login"; 
    }
    
    @RequestMapping(value = "/logged-out", method = RequestMethod.GET)
    public String loggedOut(HttpSession session, Model model)
    {
        return "logged-out"; 
    }
}
