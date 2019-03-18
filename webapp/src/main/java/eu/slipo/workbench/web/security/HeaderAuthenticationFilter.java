package eu.slipo.workbench.web.security;

import java.io.IOException;
import java.util.ArrayList;
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
import eu.slipo.workbench.common.model.ErrorCode;
import eu.slipo.workbench.common.model.RestResponse;
import eu.slipo.workbench.common.model.security.ApplicationKeyErrorCode;
import eu.slipo.workbench.common.model.security.ApplicationKeyException;
import eu.slipo.workbench.common.model.security.ApplicationKeyRecord;
import eu.slipo.workbench.web.repository.ApplicationKeyRepository;

/**
 * A servlet filter extending {@link GenericFilterBean} that handles API calls
 * authentication using application keys
 */
@Component
public class HeaderAuthenticationFilter extends GenericFilterBean {

    private static final String AUTHENTICATION_HEADER = "X-API-Key";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ApplicationKeyRepository applicationKeyRepository;

    @Autowired
    @Qualifier("defaultUserDetailsService")
    private UserDetailsService userDetailsService;

    @Override
    public void doFilter(
        ServletRequest request, javax.servlet.ServletResponse response, FilterChain chain
    ) throws IOException, ServletException {
        boolean clearContext = false;

        // Get HTTP request/response
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Authenticate only API requests
        String path = httpRequest.getServletPath();
        if (path.startsWith("/api/")) {
            // Attempt to authenticate user
            String key = httpRequest.getHeader(AUTHENTICATION_HEADER);
            try {
                this.attemptAuthentication(key);
            } catch (Exception ex) {
                this.handleUnauthorizedRequest(httpResponse, ex);
            }
            // Reset context after the request is completed
            clearContext = true;
        }

        // Continue request
        chain.doFilter(request, response);

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
        }
    }

    private void attemptAuthentication(String key) {
        // Check if header exists
        if (StringUtils.isBlank(key)) {
            throw new ApplicationKeyException(
                ApplicationKeyErrorCode.MISSING_KEY,
                String.format("Authentication header [%s] is missing", AUTHENTICATION_HEADER)
            );
        }
        // Get application key
        ApplicationKeyRecord applicationKey = this.applicationKeyRepository.findOne(key);
        if (applicationKey == null) {
            throw new ApplicationKeyException(ApplicationKeyErrorCode.UNREGISTERED_KEY, "Application key is not registered");
        }
        if (applicationKey.isRevoked()) {
            throw new ApplicationKeyException(ApplicationKeyErrorCode.REVOKED_KEY, "Application key has been revoked");
        }
        // Get user details and authorities
        UserDetails details = this.userDetailsService.loadUserByUsername(applicationKey.getMappedAccount().getUsername());
        // Set authorities
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_API"));
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
