package slipo.eu.workbench.controller;

import javax.servlet.http.HttpSession;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class UserController
{
    @RequestMapping("/user/me")
    public String showInfo(HttpSession session, Model model, Authentication authn)
    {
        Integer counter = (Integer) session.getAttribute("counter");
        counter = counter == null? 0 : counter + 1;

        session.setAttribute("counter", counter);
        model.addAttribute("counter", counter);

        model.addAttribute("username", authn.getName());
        model.addAttribute("roles", authn.getAuthorities());
        
        return "user-me";
    }
}
