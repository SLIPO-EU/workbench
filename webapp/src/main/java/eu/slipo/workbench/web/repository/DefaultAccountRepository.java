package eu.slipo.workbench.web.repository;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.slipo.workbench.common.domain.AccountEntity;
import eu.slipo.workbench.common.model.EnumRole;
import eu.slipo.workbench.common.model.QueryResultPage;
import eu.slipo.workbench.common.model.user.Account;
import eu.slipo.workbench.web.model.admin.AccountQuery;

@Repository()
@Transactional()
public class DefaultAccountRepository implements AccountRepository {

    @PersistenceContext(unitName = "default")
    private EntityManager entityManager;

    @Override
    public int count() {
       final String q = "select count(a.id) from Account a ";

       TypedQuery<Number> countQuery = entityManager.createQuery(q, Number.class);

       return countQuery.getSingleResult().intValue();
    }

    @Override
    public QueryResultPage<Account> query(AccountQuery query, PageRequest pageReq) {
        // Check query parameters
        if (pageReq == null) {
            pageReq = new PageRequest(0, 10);
        }

        String qlString = "";

        // Resolve filters

        List<String> filters = new ArrayList<>();
        if (query != null) {
            if (!StringUtils.isEmpty(query.getUserName())) {
                filters.add("(a.username like :userName)");
            }
        }

        // Count records
        qlString = "select count(a.id) from Account a ";
        if (!filters.isEmpty()) {
            qlString += " where " + StringUtils.join(filters, " and ");
        }

        Integer count;
        TypedQuery<Number> countQuery = entityManager.createQuery(qlString, Number.class);
        if (query != null) {
            setFindParameters(query, countQuery);
        }
        count = countQuery.getSingleResult().intValue();

        // Load records
        qlString = "select a from Account a ";
        if (!filters.isEmpty()) {
            qlString += " where " + StringUtils.join(filters, " and ");
        }
        qlString += " order by a.username, a.id ";

        TypedQuery<AccountEntity> selectQuery = entityManager.createQuery(qlString, AccountEntity.class);
        if (query != null) {
            setFindParameters(query, selectQuery);
        }

        selectQuery.setFirstResult(pageReq.getOffset());
        selectQuery.setMaxResults(pageReq.getPageSize());

        List<Account> records = selectQuery.getResultList().stream()
            .map(e -> e.toDto())
            .collect(Collectors.toList());

        return new QueryResultPage<Account>(records, pageReq, count);
    }

    @Override
    public Account findOne(int id) {
        AccountEntity account = this.findEntity(id);

        return (account == null ? null : account.toDto());
    }

    @Override
    public Account findOne(String userName) {
        String qlString = "select a from Account a where a.username = :userName ";

        List<AccountEntity> accounts = entityManager
            .createQuery(qlString, AccountEntity.class)
            .setParameter("userName", userName)
            .setMaxResults(1)
            .getResultList();

        return (accounts.isEmpty() ? null : accounts.get(0).toDto());
    }

    @Override
    public Account create(Integer createdBy, String userName, String password, String givenName, String familyName, Set<EnumRole> roles) {
        PasswordEncoder encoder = new BCryptPasswordEncoder();

        AccountEntity entity = new AccountEntity(userName, userName);
        AccountEntity grantedBy = createdBy == null ? null : this.findEntity(createdBy);

        entity.setActive(true);
        entity.setBlocked(false);
        entity.setLang("en");
        entity.setName(givenName, familyName);
        entity.setRegistered(ZonedDateTime.now());
        entity.setPassword(encoder.encode(password));

        roles.stream().forEach(r-> {
            entity.grant(r, grantedBy);
        });

        entityManager.persist(entity);
        entityManager.flush();

        return entity.toDto();
    }

    @Override
    public void update(int updatedBy, Account account) {
        AccountEntity entity = this.findEntity(account.getId());
        AccountEntity grantedBy = this.findEntity(updatedBy);

        entity.setName(account.getGivenName(), account.getFamilyName());

        // Revoke roles
        for (Iterator<EnumRole> i = entity.getRoles().iterator(); i.hasNext();) {
            EnumRole role = i.next();
            if(!account.getRoles().contains(role)) {
                entity.revoke(role);
            }
        }

        // Grant roles
        account.getRoles().stream().forEach(r-> {
            if(!entity.hasRole(r)) {
                entity.grant(r, grantedBy);
            }
        });
    }

    private AccountEntity findEntity(int id) {
        String qlString = "select a from Account a where a.id = :id ";

        List<AccountEntity> accounts = entityManager
            .createQuery(qlString, AccountEntity.class)
            .setParameter("id", id)
            .setMaxResults(1)
            .getResultList();

        return (accounts.isEmpty() ? null : accounts.get(0));
    }

    private void setFindParameters(AccountQuery eventQuery, Query query) {
        if (!StringUtils.isEmpty(eventQuery.getUserName())) {
            query.setParameter("userName", "%" + eventQuery.getUserName() + "%");
        }
    }

}
