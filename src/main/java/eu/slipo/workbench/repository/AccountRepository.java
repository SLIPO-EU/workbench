package eu.slipo.workbench.repository;

import java.util.List;

import eu.slipo.workbench.domain.AccountEntity;
import eu.slipo.workbench.model.Account;
import eu.slipo.workbench.model.EnumRole;

public interface AccountRepository
{
    /**
     * Save entity (either create or update).
     * 
     * @param e
     * @return a managed entity
     */
    AccountEntity save(AccountEntity e);
    
    /**
     * Create a new entity from a DTO object.
     * 
     * @return a new managed entity
     */
    AccountEntity createWith(Account a, String digestedPassword);
    
    /**
     * Grant roles to an account
     * 
     * @param e
     * @param roles
     */
    void grant(AccountEntity e, AccountEntity grantedBy, EnumRole... roles);
    
    /**
     * Revoke roles from an account
     * 
     * @param e
     * @param roles
     */
    void revoke(AccountEntity e, EnumRole... roles);
    
    /**
     * Find account by primary key.
     * 
     * @param uid
     * @return a managed entity or null
     */
    AccountEntity findOne(int uid);
    
    /**
     * Find (unique or non-existent) account by username.
     * 
     * @param username
     * @return a managed entity or null
     */
    AccountEntity findOneByUsername(String username);
    
    /**
     * Find (unique or non-existent) account by email.
     * 
     * @param email
     * @return a managed entity or null
     */
    AccountEntity findOneByEmail(String email);
    
    /**
     * List all accounts.
     * 
     * @return
     */
    List<AccountEntity> findAll();
}
