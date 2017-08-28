package eu.slipo.workbench.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HomeController {

    @RequestMapping("*")
    public String index(HttpSession session, HttpServletRequest request) {
        if (!request.getRequestURI().equalsIgnoreCase("/")) {
            return "redirect:/";
        }

        return "index";
    }

}
