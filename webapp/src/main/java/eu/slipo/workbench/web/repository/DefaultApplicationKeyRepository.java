package eu.slipo.workbench.web.repository;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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
import eu.slipo.workbench.common.domain.ApplicationKeyEntity;
import eu.slipo.workbench.common.model.QueryResultPage;
import eu.slipo.workbench.common.model.security.ApplicationKeyRecord;
import eu.slipo.workbench.web.model.admin.ApplicationKeyQuery;

@Repository()
@Transactional()
public class DefaultApplicationKeyRepository implements ApplicationKeyRepository {

    @PersistenceContext(unitName = "default")
    EntityManager entityManager;

    @Override
    public ApplicationKeyRecord create(
        int userId, String applicationName, Integer mappedUserId, Integer maxDailyRequestLimit, Integer maxConcurrentRequestLimit
    ) {
        AccountEntity createdBy = entityManager.getReference(AccountEntity.class, userId);
        AccountEntity mappedTo = entityManager.getReference(AccountEntity.class, mappedUserId);

        ApplicationKeyEntity key = new ApplicationKeyEntity();
        key.setName(applicationName);
        key.setCreatedBy(createdBy);
        key.setMappedAccount(mappedTo);
        key.setMaxConcurrentRequestLimit(
            maxConcurrentRequestLimit == null ? ApplicationKeyRepository.DEFAULT_MAX_CONCURRENT_REQUEST_LIMIT : maxConcurrentRequestLimit
        );
        key.setMaxDailyRequestLimit(
            maxDailyRequestLimit == null ? ApplicationKeyRepository.DEFAULT_MAX_DAILY_REQUEST_LIMIT : maxDailyRequestLimit
        );
        key.setKey(this.createApplicationKey(userId, mappedUserId));

        this.entityManager.persist(key);
        this.entityManager.flush();

        return key.toRecord();
    }

    private String createApplicationKey(int userId, int mappedUserId) {
        final String key = String.format("%d:%d:%s", userId, mappedUserId, UUID.randomUUID().toString());
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.encode(key);
    }

    @Override
    public QueryResultPage<ApplicationKeyRecord> query(ApplicationKeyQuery query, PageRequest pageRequest) {
        // Check query parameters
        if (pageRequest == null) {
            pageRequest = new PageRequest(0, 10);
        }

        String qlString = "";

        // Resolve filters
        List<String> filters = new ArrayList<>();
        if (query != null) {
            if (!StringUtils.isEmpty(query.getApplicationName())) {
                filters.add("(k.name like :name)");
            }
            if (!StringUtils.isEmpty(query.getUserName())) {
                filters.add("(k.mappedAccount.username like :userName)");
            }
            if (query.getRevoked() != null) {
                if (query.getRevoked().booleanValue()) {
                    filters.add("(k.revokedOn is not null)");
                } else {
                    filters.add("(k.revokedOn is null)");
                }
            }
        }

        // Count records
        qlString = "select count(k.id) from ApplicationKey k ";
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
        qlString = "select k from ApplicationKey k ";
        if (!filters.isEmpty()) {
            qlString += " where " + StringUtils.join(filters, " and ");
        }
        qlString += " order by k.createdOn, k.id ";

        TypedQuery<ApplicationKeyEntity> selectQuery = entityManager.createQuery(qlString, ApplicationKeyEntity.class);
        if (query != null) {
            setFindParameters(query, selectQuery);
        }

        selectQuery.setFirstResult(pageRequest.getOffset());
        selectQuery.setMaxResults(pageRequest.getPageSize());

        List<ApplicationKeyRecord> records = selectQuery.getResultList().stream()
            .map(ApplicationKeyEntity::toRecord)
            .collect(Collectors.toList());

        return new QueryResultPage<ApplicationKeyRecord>(records, pageRequest, count);
    }

    @Override
    public ApplicationKeyRecord findOne(String key) {
        final String query = "From ApplicationKey k where k.key = :key";

        return entityManager.createQuery(query, ApplicationKeyEntity.class)
            .setParameter("key", key)
            .setMaxResults(1)
            .getResultList()
            .stream()
            .map(ApplicationKeyEntity::toRecord)
            .findFirst()
            .orElse(null);
    }

    @Override
    public void revoke(int userId, long id) {
        AccountEntity revokedBy = entityManager.getReference(AccountEntity.class, userId);

        final String query = "From ApplicationKey k where k.id = :id";

        ApplicationKeyEntity entity = entityManager.createQuery(query, ApplicationKeyEntity.class)
            .setParameter("id", id)
            .setMaxResults(1)
            .getSingleResult();

        entity.setRevokedBy(revokedBy);
        entity.setRevokedOn(ZonedDateTime.now());
    }

    private void setFindParameters(ApplicationKeyQuery applicationKeyQuery, Query query) {
        if (!StringUtils.isEmpty(applicationKeyQuery.getApplicationName())) {
            query.setParameter("name", "%" + applicationKeyQuery.getApplicationName() + "%");
        }
        if (!StringUtils.isEmpty(applicationKeyQuery.getUserName())) {
            query.setParameter("userName", "%" + applicationKeyQuery.getUserName() + "%");
        }
    }

}
