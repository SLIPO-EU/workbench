package eu.slipo.workbench.service;

import org.springframework.security.core.userdetails.UserDetailsService;

import eu.slipo.workbench.model.Account;

public interface UserService extends UserDetailsService
{
    Account findOneByUsername(String username);
    
    Account findOneByEmail(String email);
}
