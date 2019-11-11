package eu.slipo.workbench.web.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.slipo.workbench.common.model.BasicErrorCode;
import eu.slipo.workbench.common.model.EnumRole;
import eu.slipo.workbench.common.model.ErrorCode;
import eu.slipo.workbench.common.model.RestResponse;
import eu.slipo.workbench.common.model.security.ApplicationKeyErrorCode;
import eu.slipo.workbench.common.model.security.ApplicationKeyException;
import eu.slipo.workbench.common.model.security.ApplicationKeyRecord;
import eu.slipo.workbench.web.model.Headers;
import eu.slipo.workbench.web.repository.ApplicationKeyRepository;
import eu.slipo.workbench.web.service.DefaultUserDetailsService;

/**
 * A servlet filter extending {@link GenericFilterBean} that handles API calls
 * authentication using application keys
 */
@Component
public class HeaderAuthenticationFilter extends GenericFilterBean {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ApplicationKeyRepository applicationKeyRepository;

    @Autowired
    @Qualifier("defaultUserDetailsService")
    private UserDetailsService userDetailsService;

    @Autowired
    ApplicationKeySessionRegistry applicationKeySessionRegistry;

    @Override
    public void doFilter(
        ServletRequest request, javax.servlet.ServletResponse response, FilterChain chain
    ) throws IOException, ServletException {
        boolean clearContext = false;
        boolean execute = true;

        // Get HTTP request/response
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Authenticate only API requests
        String path = httpRequest.getServletPath();
        if (path.startsWith("/api/")) {
            // Attempt to authenticate user
            String apiKey = httpRequest.getHeader(Headers.API_AUTHENTICATION_HEADER);
            String sessionToken = httpRequest.getHeader(Headers.API_SESSION_TOKEN);

            try {
                this.attemptAuthentication(apiKey, sessionToken);
            } catch (Exception ex) {
                this.handleUnauthorizedRequest(httpResponse, ex);
                execute = false;
            }
            // Reset context after the request is completed
            clearContext = true;
        }

        // Continue request
        if(execute) {
          chain.doFilter(request, response);
        }

        // Cleanup
        if (clearContext) {
            // Clear security context
            SecurityContextHolder.clearContext();
            // Invalidate session
            // TODO: Prevent session creation
            HttpSession session = httpRequest.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            // TODO: Remove session cookie
        }
    }

    private void attemptAuthentication(String apiKey, String sessionToken) {
        EnumRole roleToAssign = EnumRole.API;

        // If an application key exists, it will override the session token
        if(StringUtils.isBlank(apiKey)) {
            apiKey = this.applicationKeySessionRegistry.sessionTokenToKey(sessionToken);

            roleToAssign = EnumRole.API_SESSION;
        }

        // Check if a supported header exists
        if (StringUtils.isBlank(apiKey)) {
            throw new ApplicationKeyException(
                ApplicationKeyErrorCode.MISSING_KEY,
                String.format("Cannot resolve application key. Include either [%s] or [%s] header",
                              Headers.API_AUTHENTICATION_HEADER, Headers.API_SESSION_TOKEN)
            );
        }

        // Validate application key
        ApplicationKeyRecord applicationKey = this.applicationKeyRepository.findOne(apiKey);
        if (applicationKey == null) {
            throw new ApplicationKeyException(ApplicationKeyErrorCode.UNREGISTERED_KEY, "Application key is not registered");
        }
        if (applicationKey.isRevoked()) {
            throw new ApplicationKeyException(ApplicationKeyErrorCode.REVOKED_KEY, "Application key has been revoked");
        }
        // Get user details and authorities
        UserDetails userDetails = this.userDetailsService.loadUserByUsername(applicationKey.getMappedAccount().getUsername());
        // Set authorities
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + roleToAssign.name()));
        // Replace roles
        DefaultUserDetailsService.Details details = ((DefaultUserDetailsService.Details) userDetails);
        details.setRoles(new HashSet<>(Arrays.asList(roleToAssign)));
        // Create token
        ApplicationKeyAuthenticationToken token = new ApplicationKeyAuthenticationToken(details, "", authorities, applicationKey);
        // Update security context
        SecurityContextHolder.getContext().setAuthentication(token);
    }

    private void handleUnauthorizedRequest(HttpServletResponse response, Exception exception) {
        // Set status
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        // Set code/message
        ErrorCode code = BasicErrorCode.AUTHENTICATION_FAILED;
        String message = "Authentication has failed";
        if (exception instanceof ApplicationKeyException) {
            code = ((ApplicationKeyException) exception).getCode();
            message = exception.getMessage();
        }
        // Create response
        try {
            RestResponse<?> data = RestResponse.error(code, message);

            response.getOutputStream().print(objectMapper.writeValueAsString(data));
        } catch (Exception ex) {
            // Ignore
        }
    }

}
