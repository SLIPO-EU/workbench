package eu.slipo.workbench.web.controller;

import javax.servlet.http.HttpSession;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonProperty;

import eu.slipo.workbench.common.model.BasicErrorCode;
import eu.slipo.workbench.common.model.Error;
import eu.slipo.workbench.common.model.RestResponse;

@RestController
@RequestMapping(produces = "application/json")
public class LoginController
{

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

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public RestResponse<Void> login(HttpSession session, @RequestParam(required = false) String error)
    {
        if (error != null) {
            AuthenticationException ex = (AuthenticationException)
                session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
            Error e = new Error(BasicErrorCode.AUTHENTICATION_FAILED, ex.getMessage());
            return RestResponse.error(e);
        }
        return RestResponse.result(null);
    }

    @RequestMapping(value = "/logged-in", method = RequestMethod.GET)
    public RestResponse<Token> loggedIn(HttpSession session, CsrfToken token)
    {
        return RestResponse.result(new Token(token));
    }

    @RequestMapping(value = "/logged-out", method = RequestMethod.GET)
    public RestResponse<Token> loggedOut(HttpSession session, CsrfToken token)
    {
        return RestResponse.result(new Token(token));
    }

}
