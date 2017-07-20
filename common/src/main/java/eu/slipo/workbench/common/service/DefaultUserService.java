package eu.slipo.workbench.common.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.slipo.workbench.common.domain.AccountEntity;
import eu.slipo.workbench.common.model.Account;
import eu.slipo.workbench.common.model.EnumRole;
import eu.slipo.workbench.common.repository.AccountRepository;

@Service
public class DefaultUserService implements UserService
{
    @Autowired
    private AccountRepository accountRepository;
   
    @Override
    public Account findOneByUsername(String username)
    {
        AccountEntity accountEntity = accountRepository.findOneByUsername(username);
        return accountEntity == null? null : accountEntity.toDto();
    }

    @Override
    public Account findOneByEmail(String email)
    {
        AccountEntity accountEntity = accountRepository.findOneByEmail(email);
        return accountEntity == null? null : accountEntity.toDto();
    }
    
    @Override
    public Account findOne(int uid)
    {
        AccountEntity accountEntity = accountRepository.findOne(uid);
        return accountEntity == null? null : accountEntity.toDto();
    }
    
    @Override
    @Transactional
    public Account createWith(Account account, String digestedPassword)
    {
        Assert.isTrue(account.getId() == null, "Not expecting an id for a new entity");
        
        // Copy account data
        
        AccountEntity entity = 
            new AccountEntity(account.getUsername(), account.getEmail());

        entity.setLang(account.getLang());
        entity.setActive(account.isActive());
        entity.setBlocked(account.isBlocked());       
        entity.setName(account.getGivenName(), account.getFamilyName());
       
        entity.setPassword(digestedPassword);
        
        // Grant roles
        
        for (EnumRole role: account.getRoles())
            entity.grant(role, null);
        
        // Save
        
        entity = accountRepository.saveAndFlush(entity);
        
        return entity.toDto();
    }

    @Override
    @Transactional
    public void grant(Account account, Account grantedby, EnumRole... roles)
    {
        Assert.notNull(account, "Expected a non-null account");
        Assert.notEmpty(roles, "Expected at least 1 role");
        
        AccountEntity accountEntity = 
            accountRepository.findOne(example(account));
        if (accountEntity == null)
            return; // no such account
        
        AccountEntity grantedbyEntity = grantedby != null? 
            accountRepository.findOne(example(grantedby)) : null;
            
        for (EnumRole role: roles)
            accountEntity.grant(role, grantedbyEntity);
        
        accountRepository.saveAndFlush(accountEntity);
    }

    @Override
    @Transactional
    public void revoke(Account account, EnumRole... roles)
    {
        Assert.notNull(account, "Expected a non-null account");
        Assert.notEmpty(roles, "Expected at least 1 role");
        
        AccountEntity accountEntity = 
            accountRepository.findOne(example(account));
        if (accountEntity == null)
            return; // no such account
        
        for (EnumRole role: roles)
            accountEntity.revoke(role);
        
        accountRepository.saveAndFlush(accountEntity);
    }
    
    private Example<AccountEntity> example(Account account)
    {
        Integer uid = account.getId();
        String username = account.getUsername();
        
        AccountEntity probe = null;
        if (uid != null) {
            probe = new AccountEntity(uid);
        } else if (username != null) {
            probe = new AccountEntity(username, null);
        }
        return probe != null? Example.of(probe) : null;
    }
    
}
