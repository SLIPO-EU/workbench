package eu.slipo.workbench.controller.action;

import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import eu.slipo.workbench.model.Account;
import eu.slipo.workbench.model.Error;
import eu.slipo.workbench.model.RestResponse;
import eu.slipo.workbench.service.UserService;

@RestController
public class UserController
{
    private static Logger logger = LoggerFactory.getLogger(UserController.class);
    
    @Autowired
    UserService userService;
    
    @RequestMapping(
        value = "/action/user/profile", method = RequestMethod.GET, produces = "application/json")
    public RestResponse<Account> getProfile(Authentication authentication)
    {
        String username = authentication.getName();

        Account account = userService.findOneByUsername(username);
        Assert.state(account != null,
            "Expected to find a user with authenticated username!");
        
        return RestResponse.result(account);
    }
}
