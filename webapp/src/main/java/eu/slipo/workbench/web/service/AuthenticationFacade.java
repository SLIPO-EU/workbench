package eu.slipo.workbench.web.service;

import java.util.Locale;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

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

}