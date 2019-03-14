package eu.slipo.workbench.web.service;

import java.util.Locale;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import eu.slipo.workbench.common.model.EnumRole;
import eu.slipo.workbench.common.model.security.ApplicationKeyRecord;
import eu.slipo.workbench.web.security.ApplicationKeyAuthenticationToken;
import eu.slipo.workbench.web.service.DefaultUserDetailsService.Details;

@Component
public class AuthenticationFacade implements IAuthenticationFacade {

    @Override
    public Authentication getAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return authentication;
    }

    @Override
    public boolean isAuthenticated() {
        Authentication authentication = this.getAuthentication();

        return (authentication != null && authentication.isAuthenticated());
    }

    @Override
    public boolean isAdmin() {
        return this.hasRole(EnumRole.ADMIN);
    }

    @Override
    public boolean hasRole(EnumRole role) {
        Authentication authentication = this.getAuthentication();
        if (authentication == null) {
            return false;
        }
        return ((Details) authentication.getPrincipal()).hasRole(role);
    }

    @Override
    public boolean hasAnyRole(EnumRole... roles) {
        if (roles == null) {
            return false;
        }
        for (EnumRole role : roles) {
            if (this.hasRole(role)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Integer getCurrentUserId() {
        Authentication authentication = this.getAuthentication();
        if (authentication == null) {
            return null;
        }
        return ((Details) authentication.getPrincipal()).getId();
    }

    @Override
    public String getCurrentUserName() {
        Authentication authentication = this.getAuthentication();
        if (authentication == null) {
            return null;
        }
        return ((Details) authentication.getPrincipal()).getUsername();
    }

    @Override
    public Locale getCurrentUserLocale() {
        Authentication authentication = this.getAuthentication();
        if (authentication == null) {
            return null;
        }
        String lang = ((Details) authentication.getPrincipal()).getLang();

        return Locale.forLanguageTag(lang);
    }

    @Override
    public ApplicationKeyRecord getApplicationKey() {
        Authentication authentication = this.getAuthentication();
        if (authentication == null) {
            return null;
        }
        if (authentication instanceof ApplicationKeyAuthenticationToken) {
            return ((ApplicationKeyAuthenticationToken) authentication).getApplicationKey();
        }

        return null;
    }

}