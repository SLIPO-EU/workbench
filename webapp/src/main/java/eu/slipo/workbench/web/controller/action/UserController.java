package eu.slipo.workbench.web.controller.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.slipo.workbench.common.model.Account;
import eu.slipo.workbench.common.model.RestResponse;
import eu.slipo.workbench.common.service.UserService;

/**
 * Actions for querying and updating user data
 */
@RestController
public class UserController {

    @Autowired
    UserService userService;

    /**
     * Get profile data for the authenticated user
     *
     * @param authentication the authenticated principal
     * @return user profile data
     */
    @Secured({ "ROLE_USER", "ROLE_ADMIN" })
    @RequestMapping(value = "/action/user/profile", method = RequestMethod.GET, produces = "application/json")
    public RestResponse<Account> getProfile(Authentication authentication) {
        String username = authentication.getName();

        Account account = userService.findOneByUsername(username);
        Assert.state(account != null, "Expected to find a user with authenticated username!");

        return RestResponse.result(account);
    }

}
