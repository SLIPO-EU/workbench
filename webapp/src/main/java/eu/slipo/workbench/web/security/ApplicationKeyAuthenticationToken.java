package eu.slipo.workbench.web.security;

import java.util.Collection;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import eu.slipo.workbench.common.model.security.ApplicationKeyRecord;

public class ApplicationKeyAuthenticationToken extends UsernamePasswordAuthenticationToken {

    private static final long serialVersionUID = 1L;

    private final ApplicationKeyRecord applicationKey;

    public ApplicationKeyAuthenticationToken(
        Object principal, Object credentials, ApplicationKeyRecord applicationKey
    ) {
        super(principal, credentials);

        this.applicationKey = applicationKey;
    }

    public ApplicationKeyAuthenticationToken(
        Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities, ApplicationKeyRecord applicationKey
    ) {
        super(principal, credentials, authorities);

        this.applicationKey = applicationKey;
    }

    public ApplicationKeyRecord getApplicationKey() {
        return applicationKey;
    }

}
