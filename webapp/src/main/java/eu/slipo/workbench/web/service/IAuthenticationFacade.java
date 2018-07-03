package eu.slipo.workbench.web.service;

import java.util.Locale;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import eu.slipo.workbench.common.model.EnumRole;

public interface IAuthenticationFacade {

    /**
     * Get the current {@link Authentication} object from the
     * {@link SecurityContextHolder}
     * @return
     */
    Authentication getAuthentication();

    /**
     * Returns {@code true} if the user is authenticated.
     * @return
     */
    boolean isAuthenticated();

    /**
     * Returns {@code true} if the user is an administrator
     * @return
     */
    boolean isAdmin();

    /**
     * Returns {@code true} if the user is authenticated and has the given {@code role}
     * @return
     */
    boolean hasRole(EnumRole role);

    /**
     * Returns {@code true} if the user has any of the roles in the {@code roles} array.
     * @param roles the roles to check
     * @return
     */
    boolean hasAnyRole(EnumRole... roles);

    /**
     * Get the unique id of the authenticated user
     *
     * @return the user unique id or {@code null} if the user is not authenticated
     */
    Integer getCurrentUserId();

    /**
     * Get the user name
     *
     * @return the user name or {@code null} if the user is not authenticated
     */
    String getCurrentUserName();

    /**
     * Get the user locale
     *
     * @return the user locale or {@code null} if the user is not authenticated
     */
    Locale getCurrentUserLocale();

}