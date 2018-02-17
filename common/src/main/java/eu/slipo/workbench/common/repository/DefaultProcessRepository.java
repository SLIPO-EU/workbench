package eu.slipo.workbench.common.repository;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.NotImplementedException;
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
import eu.slipo.workbench.common.model.process.ProcessExecutionStepRecord;
import eu.slipo.workbench.common.model.process.ProcessQuery;
import eu.slipo.workbench.common.model.process.ProcessRecord;

// Todo Examine (per-method) transaction propagation. 
// Focus on updateExecution which is subject to several race conditions.   

@Repository
@Transactional
public class DefaultProcessRepository implements ProcessRepository 
{
    @PersistenceContext(unitName = "default")
    EntityManager entityManager;

    @Override
    public QueryResultPage<ProcessRecord> find(
        ProcessQuery query, PageRequest pageReq, final boolean includeExecutions)
    {
        // Check query parameters
        if (pageReq == null) {
            pageReq = new PageRequest(0, 10);
        }

        String qlString = "";

        // Resolve filters
        
        List<String> filters = new ArrayList<>();
        if (query != null) {
            if (query.getCreatedBy() != null) 
                filters.add("(p.createdBy.id = :ownerId)");
            if (!StringUtils.isBlank(query.getName()))
                filters.add("(p.name like :name)");
            if (query.getTaskType() != null)
                filters.add("(p.taskType = :taskType)");
            if (query.getTemplate() != null)
                filters.add("(p.isTemplate = :template)");
        }

        // Count records
        qlString = "select count(p.id) from Process p ";
        if (!filters.isEmpty()) {
            qlString += " where " + StringUtils.join(filters, " and ");
        }

        Integer count;
        TypedQuery<Number> countQuery = entityManager.createQuery(qlString, Number.class);
        if (query != null) 
            setFindParameters(query, countQuery);
        count = countQuery.getSingleResult().intValue();

        // Load records
        qlString = "select p from Process p ";
        if (!filters.isEmpty()) {
            qlString += " where " + StringUtils.join(filters, " and ");
        }
        qlString += " order by p.name, p.updatedOn ";

        TypedQuery<ProcessEntity> selectQuery = entityManager.createQuery(qlString, ProcessEntity.class);
        if (query != null) 
            setFindParameters(query, selectQuery);

        selectQuery.setFirstResult(pageReq.getOffset());
        selectQuery.setMaxResults(pageReq.getPageSize());
        
        final boolean includeRevisions = true;
        
        List<ProcessRecord> records = selectQuery.getResultList().stream()
            .map(p -> p.toProcessRecord(includeRevisions, includeExecutions, false))
            .collect(Collectors.toList());
        return new QueryResultPage<ProcessRecord>(records, pageReq, count);
    }

    @Override
    public QueryResultPage<ProcessExecutionRecord> findExecutions(ProcessExecutionQuery query, PageRequest pageReq)
    {
        // Check query parameters
        if (pageReq == null) {
            pageReq = new PageRequest(0, 10);
        }

        String qlString = "";

        // Resolve filters
        
        List<String> filters = new ArrayList<>();
        if (query != null) {
            if (query.getId() != null)
                filters.add("(e.process.parent.id = :id)");
            if (query.getVersion() != null)
                filters.add("(e.process.version = :version)");
            if (query.getCreatedBy() != null)
                filters.add("(e.process.parent.createdBy.id = :ownerId)");
            if (!StringUtils.isBlank(query.getName()))
                filters.add("(e.process.name like :name)");
            if (query.getTaskType() != null)
                filters.add("(e.process.parent.taskType = :taskType)");
            if (query.getStatus() != null)
                filters.add("(e.status = :status)");
        }

        // Count records
        qlString = "select count(e.id) from ProcessExecution e ";
        if (!filters.isEmpty()) {
            qlString += " where " + StringUtils.join(filters, " and ");
        }

        Integer count;
        TypedQuery<Number> countQuery = entityManager.createQuery(qlString, Number.class);
        if (query != null) 
            setFindParameters(query, countQuery);
        count = countQuery.getSingleResult().intValue();

        // Load records
        qlString = "select e from ProcessExecution e ";
        if (!filters.isEmpty()) {
            qlString += " where " + StringUtils.join(filters, " and ");
        }
        qlString += " order by e.startedOn desc, e.process.parent.name ";

        TypedQuery<ProcessExecutionEntity> selectQuery =
            entityManager.createQuery(qlString, ProcessExecutionEntity.class);
        if (query != null)
            setFindParameters(query, selectQuery);

        selectQuery.setFirstResult(pageReq.getOffset());
        selectQuery.setMaxResults(pageReq.getPageSize());

        List<ProcessExecutionRecord> records = selectQuery.getResultList().stream()
            .map(ProcessExecutionEntity::toProcessExecutionRecord)
            .collect(Collectors.toList());
        return new QueryResultPage<ProcessExecutionRecord>(records, pageReq, count);
    }

    @Override
    public ProcessRecord findOne(long id, final boolean includeExecutions)
    {
        ProcessRevisionEntity entity = findLatestRevision(id);
        return entity == null? null : entity.toProcessRecord(includeExecutions, false);
    }

    @Override
    public ProcessRecord findOne(long id, long version, final boolean includeExecutions)
    {
        ProcessRevisionEntity r = findRevision(id, version);
        return r == null? null : r.toProcessRecord(includeExecutions, false);
    }

    @Override
    public ProcessRecord findOne(String name)
    {
        String queryString =
            "select p from ProcessRevision p where p.parent.name = :name " +
            "order by p.version desc";

        List<ProcessRevisionEntity> result = entityManager
            .createQuery(queryString, ProcessRevisionEntity.class)
            .setParameter("name", name)
            .setMaxResults(1)
            .getResultList();

        return (result.isEmpty() ? null : result.get(0).toProcessRecord(false, false));
    }

    @Override
    public ProcessExecutionRecord findExecution(long id, long version, long execution)
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

        // Fixme is it ok to modify definition here (is the source of our update)? 
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

    @Override
    public ProcessExecutionRecord createExecution(long id, long version, int uid)
    {
        final ZonedDateTime now = ZonedDateTime.now();
        
        ProcessRevisionEntity revisionEntity = findRevision(id, version);
        AccountEntity submittedBy = uid < 0? null : entityManager.find(AccountEntity.class, uid);
        
        ProcessExecutionEntity executionEntity = new ProcessExecutionEntity(revisionEntity);
        executionEntity.setSubmittedBy(submittedBy);
        executionEntity.setSubmittedOn(now);
        
        entityManager.persist(executionEntity);
        entityManager.flush();
        return executionEntity.toProcessExecutionRecord();
    }
    
    @Override
    public ProcessExecutionRecord updateExecution(long executionId, ProcessExecutionRecord record)
    {
        // Todo ProcessRepository.updateExecution
        throw new NotImplementedException("Todo");
    }

    @Override
    public ProcessExecutionRecord createExecutionStep(long executionId,
        ProcessExecutionStepRecord record)
    {
        // Todo ProcessRepository.createExecutionStep
        throw new NotImplementedException("Todo");
    }

    @Override
    public ProcessExecutionRecord updateExecutionStep(long executionId, int stepKey,
        ProcessExecutionStepRecord record)
    {
        // Todo ProcessRepository.updateExecutionStep
        throw new NotImplementedException("Todo");
    }
    
    private void setFindParameters(ProcessQuery processQuery, Query query)
    {
        if (processQuery.getCreatedBy() != null)
            query.setParameter("ownerId", processQuery.getCreatedBy());

        if (!StringUtils.isBlank(processQuery.getName()))
            query.setParameter("name", "%" + processQuery.getName() + "%");
        
        if (processQuery.getTaskType() != null)
            query.setParameter("taskType", processQuery.getTaskType());
        
        if (processQuery.getTemplate() != null)
            query.setParameter("template", processQuery.getTemplate());
    }

    private void setFindParameters(ProcessExecutionQuery executionQuery, Query query)
    {
        if (executionQuery.getCreatedBy() != null)
            query.setParameter("ownerId", executionQuery.getCreatedBy());

        if (executionQuery.getId() != null)
            query.setParameter("id", executionQuery.getId());
        
        if (executionQuery.getVersion() != null)
            query.setParameter("version", executionQuery.getVersion());
        
        if (!StringUtils.isBlank(executionQuery.getName()))
            query.setParameter("name", "%" + executionQuery.getName() + "%");
       
        if (executionQuery.getTaskType() != null)
            query.setParameter("taskType", executionQuery.getTaskType());
        
        if (executionQuery.getStatus() != null)
            query.setParameter("status", executionQuery.getStatus());
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

        return result.isEmpty()? null : result.get(0);
    }

    /**
     * Find the single {@link ProcessRevisionEntity} associated with a given pair of 
     * id and version.
     * 
     * @param id The process id
     * @param version The version
     * @return a revision entity or <tt>null</tt> if no matching entity exists
     */
    private ProcessRevisionEntity findRevision(long id, long version)
    {
        String queryString =
            "FROM ProcessRevision p WHERE p.parent.id = :id AND p.version = :version";

        TypedQuery<ProcessRevisionEntity> query = entityManager
            .createQuery(queryString, ProcessRevisionEntity.class)
            .setParameter("id", id)
            .setParameter("version", version);

        ProcessRevisionEntity r = null;
        try {
            r = query.getSingleResult();
        } catch (NoResultException ex) {
            r = null;
        }
        
        return r;
    }
}
