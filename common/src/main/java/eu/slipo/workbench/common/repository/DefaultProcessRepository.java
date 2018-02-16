package eu.slipo.workbench.common.repository;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.slipo.workbench.common.domain.AccountEntity;
import eu.slipo.workbench.common.domain.ProcessEntity;
import eu.slipo.workbench.common.domain.ProcessExecutionEntity;
import eu.slipo.workbench.common.domain.ProcessRevisionEntity;
import eu.slipo.workbench.common.model.ApplicationException;
import eu.slipo.workbench.common.model.QueryResultPage;
import eu.slipo.workbench.common.model.process.EnumProcessTaskType;
import eu.slipo.workbench.common.model.process.ProcessDefinition;
import eu.slipo.workbench.common.model.process.ProcessErrorCode;
import eu.slipo.workbench.common.model.process.ProcessExecutionQuery;
import eu.slipo.workbench.common.model.process.ProcessExecutionRecord;
import eu.slipo.workbench.common.model.process.ProcessQuery;
import eu.slipo.workbench.common.model.process.ProcessRecord;

@Repository
@Transactional
public class DefaultProcessRepository implements ProcessRepository {

    /**
     * Entity manager for persisting data.
     */
    @PersistenceContext(unitName = "default")
    EntityManager entityManager;

    private void setFindParameters(ProcessQuery processQuery, Query query)
    {
        Integer creatorId = processQuery.getCreatedBy();
        query.setParameter("ownerId", creatorId == null? -1 : creatorId.intValue());

        if (!StringUtils.isBlank(processQuery.getName())) {
            query.setParameter("name", "%" + processQuery.getName() + "%");
        }
        if (processQuery.getTaskType() != null) {
            query.setParameter("taskType", processQuery.getTaskType());
        }
        if (processQuery.getTemplate() != null) {
            query.setParameter("template", processQuery.getTemplate());
        }
    }

    private void setFindParameters(ProcessExecutionQuery executionQuery, Query query)
    {
        Integer creatorId = executionQuery.getCreatedBy();
        query.setParameter("ownerId", creatorId == null? -1 : creatorId.intValue());

        if (!StringUtils.isBlank(executionQuery.getName())) {
            query.setParameter("name", "%" + executionQuery.getName() + "%");
        }
        if (executionQuery.getTaskType() != null) {
            query.setParameter("taskType", executionQuery.getTaskType());
        }
        if (executionQuery.getStatus() != null) {
            query.setParameter("status", executionQuery.getStatus());
        }
    }

    @Override
    public QueryResultPage<ProcessRecord> find(ProcessQuery query, PageRequest pageReq)
    {
        // Check query parameters
        if (pageReq == null) {
            pageReq = new PageRequest(0, 10);
        }

        // Load data
        String command = "";

        // Resolve filters
        List<String> filters = new ArrayList<>();

        filters.add("(p.createdBy.id = :ownerId)");

        if (!StringUtils.isBlank(query.getName())) {
            filters.add("(p.name like :name)");
        }
        if (query.getTaskType() != null) {
            filters.add("(p.taskType = :taskType)");
        }
        if (query.getTemplate() != null) {
            filters.add("(p.isTemplate = :template)");
        }

        // Count records
        command = "select count(p.id) from Process p ";
        if (!filters.isEmpty()) {
            command += " where " + StringUtils.join(filters, " and ");
        }

        Integer count;
        TypedQuery<Number> countQuery = entityManager.createQuery(command, Number.class);
        setFindParameters(query, countQuery);
        count = countQuery.getSingleResult().intValue();

        // Load records
        command = "select p from Process p ";
        if (!filters.isEmpty()) {
            command += " where " + StringUtils.join(filters, " and ");
        }
        command += " order by p.name, p.updatedOn ";

        TypedQuery<ProcessEntity> selectQuery = entityManager.createQuery(command, ProcessEntity.class);
        setFindParameters(query, selectQuery);

        selectQuery.setFirstResult(pageReq.getOffset());
        selectQuery.setMaxResults(pageReq.getPageSize());

        List<ProcessRecord> records = selectQuery.getResultList().stream()
            .map(ProcessEntity::toProcessRecord)
            .collect(Collectors.toList());
        return new QueryResultPage<ProcessRecord>(records, pageReq, count);
    }

    @Override
    public QueryResultPage<ProcessExecutionRecord> find(ProcessExecutionQuery query, PageRequest pageReq)
    {
        // Check query parameters
        if (pageReq == null) {
            pageReq = new PageRequest(0, 10);
        }

        // Load data
        String command = "";

        // Resolve filters
        List<String> filters = new ArrayList<>();

        filters.add("(e.process.parent.createdBy.id = :ownerId)");

        if (!StringUtils.isBlank(query.getName())) {
            filters.add("(e.process.name like :name)");
        }
        if (query.getTaskType() != null) {
            filters.add("(e.process.parent.taskType = :taskType)");
        }
        if (query.getStatus() != null) {
            filters.add("(e.status = :status)");
        }

        // Count records
        command = "select count(e.id) from ProcessExecution e ";
        if (!filters.isEmpty()) {
            command += " where " + StringUtils.join(filters, " and ");
        }

        Integer count;
        TypedQuery<Number> countQuery = entityManager.createQuery(command, Number.class);
        setFindParameters(query, countQuery);
        count = countQuery.getSingleResult().intValue();

        // Load records
        command = "select e from ProcessExecution e ";
        if (!filters.isEmpty()) {
            command += " where " + StringUtils.join(filters, " and ");
        }
        command += " order by e.startedOn desc, e.process.parent.name ";

        TypedQuery<ProcessExecutionEntity> selectQuery =
            entityManager.createQuery(command, ProcessExecutionEntity.class);
        setFindParameters(query, selectQuery);

        selectQuery.setFirstResult(pageReq.getOffset());
        selectQuery.setMaxResults(pageReq.getPageSize());

        List<ProcessExecutionRecord> records = selectQuery.getResultList().stream()
            .map(ProcessExecutionEntity::toProcessExecutionRecord)
            .collect(Collectors.toList());
        return new QueryResultPage<ProcessExecutionRecord>(records, pageReq, count);
    }

    @Override
    public ProcessRecord findOne(long id)
    {
        ProcessRevisionEntity entity = this.findLatestRevision(id);
        return (entity == null ? null : entity.toProcessRecord(false, false));
    }

    @Override
    public ProcessRecord findOne(long id, long version)
    {
        String queryString =
            "select p from ProcessRevision p where p.parent.id = :id and p.version = :version";

        List<ProcessRevisionEntity> result = entityManager
            .createQuery(queryString, ProcessRevisionEntity.class)
            .setParameter("id", id)
            .setParameter("version", version)
            .getResultList();

        return (result.isEmpty() ? null : result.get(0).toProcessRecord(true, false));
    }

    @Override
    public ProcessRecord findOne(String name)
    {
        String queryString =
            "select p from ProcessRevision p where p.parent.name = :name order by p.version desc";

        List<ProcessRevisionEntity> result = entityManager
            .createQuery(queryString, ProcessRevisionEntity.class)
            .setParameter("name", name)
            .setMaxResults(1)
            .getResultList();

        return (result.isEmpty() ? null : result.get(0).toProcessRecord(false, false));
    }

    @Override
    public ProcessExecutionRecord findOne(long id, long version, long execution)
    {
        String queryString =
            "select e from ProcessExecution e " +
            "where e.process.parent.id = :id and e.process.parent.version = :version and e.id = :execution";

        List<ProcessExecutionEntity> result = entityManager
            .createQuery(queryString, ProcessExecutionEntity.class)
            .setParameter("id", id)
            .setParameter("version", version)
            .setParameter("execution", execution)
            .setMaxResults(1)
            .getResultList();

        return (result.isEmpty() ? null : result.get(0).toProcessExecutionRecord(true));
    }

    @Override
    public ProcessRecord create(ProcessDefinition definition, int userId)
    {
        AccountEntity createdBy = entityManager.getReference(AccountEntity.class, userId);
        ZonedDateTime now = ZonedDateTime.now();

        // Create new process entity

        ProcessEntity entity = new ProcessEntity(definition);
        entity.setTaskType(EnumProcessTaskType.DATA_INTEGRATION);
        entity.setTemplate(false);
        entity.setCreatedBy(createdBy);
        entity.setUpdatedBy(createdBy);
        entity.setCreatedOn(now);
        entity.setUpdatedOn(now);


        // Create an associated process revision, update reference

        ProcessRevisionEntity revisionEntity = new ProcessRevisionEntity(entity);
        entity.addRevision(revisionEntity);

        // Save

        entityManager.persist(entity);
        entityManager.flush();
        return entity.toProcessRecord();
    }

    @Override
    public ProcessRecord update(long id, ProcessDefinition definition, int userId)
    {
        AccountEntity updatedBy = entityManager.getReference(AccountEntity.class, userId);

        ProcessRevisionEntity revision = findLatestRevision(id);
        if(revision == null) {
            throw ApplicationException.fromPattern(ProcessErrorCode.NOT_FOUND);
        }

        // Update process entity

        ProcessEntity entity = revision.getParent();

        definition.setName(entity.getName());

        entity.setVersion(entity.getVersion() + 1);
        entity.setDescription(definition.getDescription());
        entity.setUpdatedBy(updatedBy);
        entity.setUpdatedOn(ZonedDateTime.now());
        entity.setDefinition(definition);

        // Create new process revision, update references

        ProcessRevisionEntity revisionEntity = new ProcessRevisionEntity(entity);
        entity.addRevision(revisionEntity);

        // Save

        entityManager.flush();
        return entity.toProcessRecord();
    }

    /**
     * Find the latest {@link ProcessRevisionEntity} associated with a given process id.
     *
     * @param id The process id
     * @return
     */
    private ProcessRevisionEntity findLatestRevision(long id)
    {
        String queryString =
            "select p from ProcessRevision p where p.parent.id = :id " +
            "order by p.version desc";

        List<ProcessRevisionEntity> result = entityManager
                .createQuery(queryString, ProcessRevisionEntity.class)
                .setParameter("id", id)
                .setMaxResults(1)
                .getResultList();

        return (result.isEmpty() ? null : result.get(0));
    }
}
