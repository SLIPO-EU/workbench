package eu.slipo.workbench.web.repository;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.slipo.workbench.common.domain.AccountEntity;
import eu.slipo.workbench.common.model.ApplicationException;
import eu.slipo.workbench.web.domain.ProcessEntity;
import eu.slipo.workbench.web.domain.ProcessExecutionEntity;
import eu.slipo.workbench.web.domain.ProcessRevisionEntity;
import eu.slipo.workbench.web.model.QueryPagingOptions;
import eu.slipo.workbench.web.model.QueryResult;
import eu.slipo.workbench.web.model.process.EnumProcessTask;
import eu.slipo.workbench.web.model.process.ProcessDefinitionUpdate;
import eu.slipo.workbench.web.model.process.ProcessErrorCode;
import eu.slipo.workbench.web.model.process.ProcessExecutionQuery;
import eu.slipo.workbench.web.model.process.ProcessExecutionRecord;
import eu.slipo.workbench.web.model.process.ProcessQuery;
import eu.slipo.workbench.web.model.process.ProcessRecord;
import eu.slipo.workbench.web.service.IAuthenticationFacade;

@Repository
public class JpaProcessRepository implements IProcessRepository {

    /**
     * Entity manager for persisting data.
     */
    @PersistenceContext(unitName = "default")
    EntityManager entityManager;

    @Autowired
    IAuthenticationFacade authenticationFacade;

    private void setFindParameters(ProcessQuery processQuery, Query query) {
        Integer ownerId = authenticationFacade.getCurrentUserId();

        query.setParameter("ownerId", ownerId);

        if (!StringUtils.isBlank(processQuery.getName())) {
            query.setParameter("name", "%" + processQuery.getName() + "%");
        }
        if (processQuery.getTask() != null) {
            query.setParameter("task", processQuery.getTask());
        }
        if (processQuery.getTemplate() != null) {
            query.setParameter("template", processQuery.getTemplate());
        }
    }

    private void setFindParameters(ProcessExecutionQuery executionQuery, Query query) {
        Integer ownerId = authenticationFacade.getCurrentUserId();

        query.setParameter("ownerId", ownerId);

        if (!StringUtils.isBlank(executionQuery.getName())) {
            query.setParameter("name", "%" + executionQuery.getName() + "%");
        }
        if (executionQuery.getTask() != null) {
            query.setParameter("task", executionQuery.getTask());
        }
        if (executionQuery.getStatus() != null) {
            query.setParameter("status", executionQuery.getStatus());
        }
    }

    @Override
    public QueryResult<ProcessRecord> find(ProcessQuery query) {
        // Check query parameters
        QueryPagingOptions pagingOptions = query.getPagingOptions();
        if (pagingOptions == null) {
            pagingOptions = new QueryPagingOptions();
            pagingOptions.pageIndex = 0;
            pagingOptions.pageSize = 10;

        } else if (pagingOptions.pageIndex < 0) {
            pagingOptions.pageIndex = 0;
        } else if (pagingOptions.pageSize <= 0) {
            pagingOptions.pageSize = 10;
        }

        // Load data
        String command = "";

        // Resolve filters
        List<String> filters = new ArrayList<>();

        filters.add("(p.createdBy.id = :ownerId)");

        if (!StringUtils.isBlank(query.getName())) {
            filters.add("(p.name like :name)");
        }
        if (query.getTask() != null) {
            filters.add("(p.task = :task)");
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

        selectQuery.setFirstResult(pagingOptions.pageIndex * pagingOptions.pageSize);
        selectQuery.setMaxResults(pagingOptions.pageSize);

        QueryResult<ProcessRecord> result = new QueryResult<ProcessRecord>(pagingOptions, count);
        for (ProcessEntity process : selectQuery.getResultList()) {
            result.addItem(process.toProcessRecord(false, false));
        }

        return result;
    }

    @Override
    public QueryResult<ProcessExecutionRecord> find(ProcessExecutionQuery query) {
        // Check query parameters
        QueryPagingOptions pagingOptions = query.getPagingOptions();
        if (pagingOptions == null) {
            pagingOptions = new QueryPagingOptions();
            pagingOptions.pageIndex = 0;
            pagingOptions.pageSize = 10;

        } else if (pagingOptions.pageIndex < 0) {
            pagingOptions.pageIndex = 0;
        } else if (pagingOptions.pageSize <= 0) {
            pagingOptions.pageSize = 10;
        }

        // Load data
        String command = "";

        // Resolve filters
        List<String> filters = new ArrayList<>();

        filters.add("(e.process.parent.createdBy.id = :ownerId)");

        if (!StringUtils.isBlank(query.getName())) {
            filters.add("(e.process.name like :name)");
        }
        if (query.getTask() != null) {
            filters.add("(e.process.parent.task = :task)");
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

        TypedQuery<ProcessExecutionEntity> selectQuery = entityManager.createQuery(command, ProcessExecutionEntity.class);
        setFindParameters(query, selectQuery);

        selectQuery.setFirstResult(pagingOptions.pageIndex * pagingOptions.pageSize);
        selectQuery.setMaxResults(pagingOptions.pageSize);

        QueryResult<ProcessExecutionRecord> result = new QueryResult<ProcessExecutionRecord>(pagingOptions, count);
        for (ProcessExecutionEntity execution : selectQuery.getResultList()) {
            result.addItem(execution.toProcessExecutionRecord(false));
        }

        return result;
    }

    @Override
    public ProcessRecord findOne(long id) {
        ProcessRevisionEntity entity = this.findById(id);

        return (entity == null ? null : entity.toProcessRecord(true, false));
    }

    @Override
    public ProcessRecord findOne(long id, long version) {
        String queryString = "select p from ProcessRevision p where p.process.id = :id and p.version = :version";

        List<ProcessRevisionEntity> result = entityManager
                .createQuery(queryString, ProcessRevisionEntity.class)
                .setParameter("id", id)
                .setParameter("version", version)
                .getResultList();

        return (result.isEmpty() ? null : result.get(0).toProcessRecord(true, false));
    }

    @Override
    public ProcessExecutionRecord findOne(long id, long version, long execution) {
        String queryString = "select e from ProcessExecution e where e.process.parent.id = :id and e.process.parent.version = :version and e.id = :execution";

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
    @Transactional()
    public void create(ProcessDefinitionUpdate definition) {
        ProcessEntity process = new ProcessEntity();

        // Process
        process.setVersion(1);
        process.setName(definition.getName());
        process.setDescription(definition.getDescription());
        process.setTask(EnumProcessTask.DATA_INTEGRATION);
        process.setTemplate(false);

        process.setCreatedBy(this.entityManager.getReference(AccountEntity.class, authenticationFacade.getCurrentUserId()));
        process.setUpdatedBy(process.getCreatedBy());
        process.setCreatedOn(ZonedDateTime.now());
        process.setUpdatedOn(process.getCreatedOn());

        process.setDefinition(definition);

        // Process revision
        ProcessRevisionEntity processRevision = new ProcessRevisionEntity();

        processRevision.setVersion(1);
        processRevision.setName(definition.getName());
        processRevision.setDescription(definition.getDescription());

        processRevision.setUpdatedBy(process.getCreatedBy());
        processRevision.setUpdatedOn(process.getCreatedOn());

        processRevision.setDefinition(definition);

        // References
        processRevision.setParent(process);
        process.getVersions().add(processRevision);

        this.entityManager.persist(process);
        this.entityManager.flush();
    }

    @Override
    @Transactional()
    public void update(ProcessDefinitionUpdate definition) {
        ProcessRevisionEntity revision = this.findById(definition.getId());

        if(revision == null) {
            throw ApplicationException.fromPattern(ProcessErrorCode.NOT_FOUND);
        }

        // Process
        ProcessEntity process = revision.getParent();
        process.setVersion(process.getVersion() + 1);
        process.setName(definition.getName());
        process.setDescription(definition.getDescription());

        process.setUpdatedBy(this.entityManager.getReference(AccountEntity.class, authenticationFacade.getCurrentUserId()));
        process.setUpdatedOn(ZonedDateTime.now());

        process.setDefinition(definition);

        // Process revision
        ProcessRevisionEntity processRevision = new ProcessRevisionEntity();

        processRevision.setVersion(process.getVersion());
        processRevision.setName(process.getName());
        processRevision.setDescription(process.getDescription());

        processRevision.setUpdatedBy(process.getUpdatedBy());
        processRevision.setUpdatedOn(process.getUpdatedOn());

        processRevision.setDefinition(definition);

        // References
        processRevision.setParent(process);
        process.getVersions().add(processRevision);

        this.entityManager.flush();
    }

    private ProcessRevisionEntity findById(long id) {
        String queryString = "select p from ProcessRevision p where p.process.id = :id order by p.version desc";

        List<ProcessRevisionEntity> result = entityManager
                .createQuery(queryString, ProcessRevisionEntity.class)
                .setParameter("id", id)
                .setMaxResults(1)
                .getResultList();

        return (result.isEmpty() ? null : result.get(0));
    }
}
