package eu.slipo.workbench.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.slipo.workbench.domain.AccountEntity;
import eu.slipo.workbench.domain.AccountRoleEntity;
import eu.slipo.workbench.model.Account;
import eu.slipo.workbench.model.EnumRole;

@Repository
@Transactional
public class DefaultAccountRepository implements AccountRepository
{
    @PersistenceContext
    private EntityManager entityManager;
        
    @Override
    public AccountEntity save(AccountEntity entity)
    {  
        if (entity.getId() < 0) {
            entityManager.persist(entity);
        } else {
            // note: merge is not really needed  if a is managed
            entity = entityManager.merge(entity);
        }
        return entity;
    }

    @Override
    public void grant(AccountEntity entity, AccountEntity grantedBy, EnumRole... roles)
    {
        for (EnumRole role: roles)
            entity.grant(role, grantedBy);
    }
    
    @Override
    public void revoke(AccountEntity entity, EnumRole... roles)
    {
        for (EnumRole role: roles)
            entity.revoke(role);
    }
    
    @Override
    public AccountEntity createWith(Account account, String digestedPassword)
    {
        Assert.isTrue(account.getId() < 0, 
            "Expected a negative id for a new entity");
        Assert.notNull(account.getUsername(), 
            "Expected a non-null username");
        Assert.notNull(account.getEmail(), 
            "Expected a non-null email");
        
        // Copy account data
        
        AccountEntity entity = 
            new AccountEntity(account.getUsername(), account.getEmail());

        entity.setLang(account.getLang());
        entity.setActive(account.isActive());
        entity.setBlocked(account.isBlocked());
        
        entity.setName(account.getGivenName(), account.getFamilyName());
       
        entity.setPassword(digestedPassword);
        
        // Grant roles
        
        grant(entity, null, account.getRoles().toArray(new EnumRole[0]));
        
        // Persist
        
        entityManager.persist(entity);
        
        return entity;
    }
    
    @Override
    public AccountEntity findOne(int uid)
    {
        return entityManager.find(AccountEntity.class, uid);
    }

    @Override
    public AccountEntity findOneByUsername(String username)
    {
        TypedQuery<AccountEntity> q = entityManager.createQuery(
            "SELECT a FROM Account a WHERE a.username = :username",
            AccountEntity.class);
        q.setParameter("username", username);
        
        AccountEntity res = null;
        try {
            res = q.getSingleResult();
        } catch (NoResultException ex) {
            res = null;
        }
        
        return res;
    }

    @Override
    public AccountEntity findOneByEmail(String email)
    {
        TypedQuery<AccountEntity> q = entityManager.createQuery(
            "SELECT a FROM Account a WHERE a.email = :email",
            AccountEntity.class);
        q.setParameter("email", email);
        
        AccountEntity res = null;
        try {
            res = q.getSingleResult();
        } catch (NoResultException ex) {
            res = null;
        }
        
        return res;
    }

    @Override
    public List<AccountEntity> findAll()
    {
        TypedQuery<AccountEntity> q = entityManager.createQuery(
            "SELECT a FROM Account a", AccountEntity.class);
        return q.getResultList();
    }
}
