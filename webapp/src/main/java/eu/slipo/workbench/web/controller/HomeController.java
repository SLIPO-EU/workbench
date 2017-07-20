package eu.slipo.workbench.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HomeController
{
    private static Logger logger = LoggerFactory.getLogger(HomeController.class);

    @RequestMapping({"/", "index"})
    public String index(HttpSession session, Model model) 
    {
        LocalDateTime now = LocalDateTime.now();
        
        logger.info("Generating an index page at {}", now);
        
        model.addAttribute("now", now.format(DateTimeFormatter.ISO_DATE_TIME));
        
        return "index";
    }
}
