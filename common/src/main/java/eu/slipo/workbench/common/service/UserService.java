package eu.slipo.workbench.common.service;

import eu.slipo.workbench.common.domain.AccountEntity;
import eu.slipo.workbench.common.model.EnumRole;
import eu.slipo.workbench.common.model.user.Account;

public interface UserService
{
    /**
     * Find (unique or missing) account by id.
     * 
     * @param email
     */
    Account findOne(int uid);
    
    /**
     * Find (unique or missing) account by username.
     * 
     * @param email
     */
    Account findOneByUsername(String username);
    
    /**
     * Find (unique or missing) account by email.
     * 
     * @param email
     */
    Account findOneByEmail(String email);
    
    /**
     * Grant roles to an account
     * 
     * @param e
     * @param roles
     */
    void grant(Account account, Account grantedBy, EnumRole... roles);
    
    /**
     * Revoke roles from an account
     * 
     * @param e
     * @param roles
     */
    void revoke(Account account, EnumRole... roles);
    
    /**
     * Create a new entity from a DTO object.
     * 
     * @return a new DTO object mapped from the newly created entity
     */
    Account createWith(Account a, String digestedPassword);
}
