package eu.slipo.workbench.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

import javax.servlet.http.HttpSession;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonProperty;

import eu.slipo.workbench.common.model.Error;
import eu.slipo.workbench.common.model.RestResponse;

@RestController
public class LoginController
{
    private static Logger logger = LoggerFactory.getLogger(LoginController.class);
    
    private static class Token
    {
        private final CsrfToken token;
        
        public Token(CsrfToken token)
        {
            this.token = token;
        }
        
        @JsonProperty("csrfToken")
        public String getToken()
        {
            return token.getToken();
        }
    }
    
    @RequestMapping(
        value = "/login", method = RequestMethod.GET, produces = "application/json")
    public RestResponse<Void> login(
        HttpSession session, @RequestParam(required = false) String error)
    {
        if (error != null) {
            AuthenticationException ex = (AuthenticationException) 
                session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
            Error e = new Error(ex.getClass().getName(), ex.getMessage());
            return RestResponse.error(e); 
        } 
        return RestResponse.result(null);
    }
    
    @RequestMapping(
        value = "/logged-in", method = RequestMethod.GET, produces = "application/json")
    public RestResponse<Token> loggedIn(HttpSession session, CsrfToken token)
    {
        return RestResponse.result(new Token(token));
    }
    
    @RequestMapping(
        value = "/logged-out", method = RequestMethod.GET, produces = "application/json")
    public RestResponse<Token> loggedOut(HttpSession session, CsrfToken token)
    {
        return RestResponse.result(new Token(token));
    }
    
}
