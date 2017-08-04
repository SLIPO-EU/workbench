package eu.slipo.workbench.web.logging.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.MDC;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import eu.slipo.workbench.web.logging.MappedDiagnosticContextKeys;

public class MappedDiagnosticContextFilter extends OncePerRequestFilter
{
    @Override
    protected void doFilterInternal(
        HttpServletRequest request, HttpServletResponse response, FilterChain chain) 
        throws ServletException, IOException
    {  
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authn = securityContext.getAuthentication();
        
        // Set current user (if any)
        MDC.put(MappedDiagnosticContextKeys.USERNAME, authn == null? "-" : authn.getName());

        // Determine and set remote address
        String remoteAddress = request.getHeader("X-FORWARDED-FOR");
        if (remoteAddress == null)
            remoteAddress = request.getRemoteAddr();
        MDC.put(MappedDiagnosticContextKeys.CLIENT_ADDRESS, remoteAddress);

        // Forward in chain
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }

}
