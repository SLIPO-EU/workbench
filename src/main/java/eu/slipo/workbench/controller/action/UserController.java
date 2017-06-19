package eu.slipo.workbench.controller.action;

import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonProperty;

import eu.slipo.workbench.model.Error;
import eu.slipo.workbench.model.RestResponse;

@RestController
public class UserController
{
    private static Logger logger = LoggerFactory.getLogger(UserController.class);
    
    private static class UserProfile
    {
        @JsonProperty
        private final String username;
        
        @JsonProperty
        private final String email;
        
        @JsonProperty
        private final String givenName;
        
        @JsonProperty
        private final String familyName;
        
        @JsonProperty
        private final Locale locale;

        public UserProfile(
            String username, String email, String givenName, String familyName, Locale locale)
        {
            this.username = username;
            this.email = email;
            this.givenName = givenName;
            this.familyName = familyName;
            this.locale = locale;
        }

        @Override
        public String toString()
        {
            return String.format(
                "UserProfile [username=%s, email=%s, givenName=%s, familyName=%s, locale=%s]",
                username, email, givenName, familyName, locale);
        }
    }
    
    @RequestMapping(
        value = "/action/user/profile", method = RequestMethod.GET, produces = "application/json")
    public RestResponse<UserProfile> getProfile(Authentication authentication)
    {
        String username = authentication.getName();

        // Todo Fill actual profile
        UserProfile profile = new UserProfile(
            username, "someone@example.com", "Foo", "Baz",
            Locale.forLanguageTag("el"));
        
        return RestResponse.result(profile);
    }
}
