package eu.slipo.workbench.common.repository;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.slipo.workbench.common.domain.AccountEntity;
import eu.slipo.workbench.common.domain.ProcessEntity;
import eu.slipo.workbench.common.domain.ProcessExecutionEntity;
import eu.slipo.workbench.common.domain.ProcessExecutionStepEntity;
import eu.slipo.workbench.common.domain.ProcessExecutionStepFileEntity;
import eu.slipo.workbench.common.domain.ProcessRevisionEntity;
import eu.slipo.workbench.common.domain.ResourceEntity;
import eu.slipo.workbench.common.domain.ResourceRevisionEntity;
import eu.slipo.workbench.common.model.ApplicationException;
import eu.slipo.workbench.common.model.QueryResultPage;
import eu.slipo.workbench.common.model.process.EnumProcessTaskType;
import eu.slipo.workbench.common.model.process.ProcessDefinition;
import eu.slipo.workbench.common.model.process.ProcessErrorCode;
import eu.slipo.workbench.common.model.process.ProcessExecutionQuery;
import eu.slipo.workbench.common.model.process.ProcessExecutionRecord;
import eu.slipo.workbench.common.model.process.ProcessExecutionStepFileRecord;
import eu.slipo.workbench.common.model.process.ProcessExecutionStepRecord;
import eu.slipo.workbench.common.model.process.ProcessIdentifier;
import eu.slipo.workbench.common.model.process.ProcessQuery;
import eu.slipo.workbench.common.model.process.ProcessRecord;
import eu.slipo.workbench.common.model.resource.ResourceIdentifier;
import eu.slipo.workbench.common.model.resource.ResourceRecord;

@Repository
@Transactional
public class DefaultProcessRepository implements ProcessRepository 
{
    @PersistenceContext(unitName = "default")
    EntityManager entityManager;

    @Autowired
    ResourceRepository resourceRepository;
    
    @Transactional(readOnly = true)
    @Override
    public QueryResultPage<ProcessRecord> find(ProcessQuery query, PageRequest pageReq)
    {
        return ProcessRepository.super.find(query, pageReq);
    }
    
    @Transactional(readOnly = true)
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
    
    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
    @Override
    public ProcessRecord findOne(long id)
    {
        return ProcessRepository.super.findOne(id);
    }
    
    @Transactional(readOnly = true)
    @Override
    public ProcessRecord findOne(long id, final boolean includeExecutions)
    {
        ProcessRevisionEntity entity = findLatestRevision(id);
        return entity == null? null : entity.toProcessRecord(includeExecutions, false);
    }
    
    @Transactional(readOnly = true)
    @Override
    public ProcessRecord findOne(long id, long version, final boolean includeExecutions)
    {
        ProcessRevisionEntity r = findRevision(id, version);
        return r == null? null : r.toProcessRecord(includeExecutions, false);
    }
    
    @Transactional(readOnly = true)
    @Override
    public ProcessRecord findOne(long id, long version)
    {
        return ProcessRepository.super.findOne(id, version);
    }
    
    @Transactional(readOnly = true)
    @Override
    public ProcessRecord findOne(ProcessIdentifier processIdentifier, boolean includeExecutions)
    {
        return ProcessRepository.super.findOne(processIdentifier, includeExecutions);
    }
    
    @Transactional(readOnly = true)
    @Override
    public ProcessRecord findOne(ProcessIdentifier processIdentifier)
    {
        return ProcessRepository.super.findOne(processIdentifier);
    }
    
    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
    @Override
    public ProcessExecutionRecord findExecution(long executionId)
    {
        ProcessExecutionEntity e = entityManager.find(ProcessExecutionEntity.class, executionId);
        return e == null? null : e.toProcessExecutionRecord(true);
    }
    
    @Transactional(readOnly = true)
    @Override
    public List<ProcessExecutionRecord> findExecutions(long id, long version)
    {
        ProcessRecord r = findOne(id, version, true);
        return r == null? Collections.emptyList() : r.getExecutions();
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
        ProcessExecutionEntity executionEntity = 
            entityManager.find(ProcessExecutionEntity.class, executionId);
        Assert.notNull(executionEntity, "The execution ID does not refer to an execution entity");
        
        if (record.getStartedOn() != null)
            executionEntity.setStartedOn(record.getStartedOn());
        
        if (record.getCompletedOn() != null)
            executionEntity.setCompletedOn(record.getCompletedOn());
        
        executionEntity.setStatus(record.getStatus());
        executionEntity.setErrorMessage(record.getErrorMessage());
        
        // Save
        
        entityManager.flush();
        return executionEntity.toProcessExecutionRecord(true);
    }

    @Override
    public ProcessExecutionRecord createExecutionStep(long executionId, ProcessExecutionStepRecord record)
    {
        Assert.notNull(record, "A non-empty record is required");
        
        final ProcessExecutionEntity executionEntity = 
            entityManager.find(ProcessExecutionEntity.class, executionId);
        Assert.notNull(executionEntity, "The execution id does not match an execution entity");
        
        // Set step metadata
        
        ProcessExecutionStepEntity executionStepEntity = new ProcessExecutionStepEntity(
            executionEntity, record.getKey(), record.getName(), record.getJobExecutionId());
        executionStepEntity.setStatus(record.getStatus());
        executionStepEntity.setOperation(record.getOperation());
        executionStepEntity.setTool(record.getTool());
        executionStepEntity.setStartedOn(record.getStartedOn());
        
        // Add file entities associated with this step
        
        for (ProcessExecutionStepFileRecord fileRecord: record.getFiles()) {
            Assert.state(fileRecord.getId() < 0, 
                "Did not expect an id for a record of a new file entity");
            ProcessExecutionStepFileEntity fileEntity = new ProcessExecutionStepFileEntity(
                executionStepEntity, 
                fileRecord.getType(), fileRecord.getFilePath(), fileRecord.getFileSize());
            ResourceIdentifier resourceIdentifier = fileRecord.getResource();
            if (resourceIdentifier != null)
                fileEntity.setResource(findResourceEntity(resourceIdentifier, true));
            executionStepEntity.addFile(fileEntity);
        }
        
        executionEntity.addStep(executionStepEntity);
        
        // Save
        
        entityManager.flush();
        return executionEntity.toProcessExecutionRecord(true);
    }

    @Override
    public ProcessExecutionRecord updateExecutionStep(
        long executionId, int stepKey, ProcessExecutionStepRecord record)
    {
        Assert.notNull(record, "A non-empty record is required");
        
        final ProcessExecutionEntity executionEntity = 
            entityManager.find(ProcessExecutionEntity.class, executionId);
        Assert.notNull(executionEntity, "The execution id does not match an execution entity");
        
        final ProcessExecutionStepEntity executionStepEntity = executionEntity.getStepByKey(stepKey);
        Assert.notNull(executionStepEntity, 
            "The step key does not correspond to a step inside the execution entity");
        
        final List<ProcessExecutionStepFileRecord> fileRecords = record.getFiles();
        final List<Long> fids = fileRecords.stream().map(r -> r.getId())
            .collect(Collectors.toList());
        
        // Update top-level step metadata
        
        executionStepEntity.setStatus(record.getStatus());
        executionStepEntity.setCompletedOn(record.getCompletedOn());
        executionStepEntity.setErrorMessage(record.getErrorMessage());
        
        // Examine and add/update contained file records
        // Due to the nature of a processing step, a file record can never be removed; it can
        // only be added or updated (on specific updatable fields).
        
        // Update existing file records
        
        for (ProcessExecutionStepFileEntity fileEntity: executionStepEntity.getFiles()) {
            // Every existing file entity must correspond to a given record
            final long fid = fileEntity.getId();
            final ProcessExecutionStepFileRecord fileRecord = 
                IterableUtils.find(fileRecords, r -> r.getId() == fid);
            Assert.state(fileRecord != null && IterableUtils.frequency(fids, fid) == 1, 
                "Expected a single file record to match to a given id!");
            // Update metadata from current file record
            fileEntity.setSize(fileRecord.getFileSize());
            ResourceIdentifier resourceIdentifier = fileRecord.getResource();
            if (resourceIdentifier != null) 
                fileEntity.setResource(findResourceEntity(resourceIdentifier, true));
            else
                fileEntity.setResource(null);
        }
        
        // Add file records (if any)
        // A file record is considered as a new one if carrying an invalid (negative) id
        
        for (ProcessExecutionStepFileRecord fileRecord: 
                IterableUtils.filteredIterable(fileRecords, f -> f.getId() < 0)) 
        {
            ProcessExecutionStepFileEntity fileEntity = new ProcessExecutionStepFileEntity(
                executionStepEntity, 
                fileRecord.getType(), fileRecord.getFilePath(), fileRecord.getFileSize());
            ResourceIdentifier resourceIdentifier = fileRecord.getResource();
            if (resourceIdentifier != null)
                fileEntity.setResource(findResourceEntity(resourceIdentifier, true));
            executionStepEntity.addFile(fileEntity);
        }
        
        // Save
        
        entityManager.flush();
        return executionEntity.toProcessExecutionRecord(true);
    }
  
    @Override
    public ProcessExecutionRecord updateExecutionStepAddingFile(
        long executionId, int stepKey, ProcessExecutionStepFileRecord fileRecord)
    {
        Assert.notNull(fileRecord, "A non-empty record is required");
        Assert.state(fileRecord.getId() < 0, 
            "Did not expect an id for a record of a new file entity");
        
        final ProcessExecutionEntity executionEntity = 
            entityManager.find(ProcessExecutionEntity.class, executionId);
        Assert.notNull(executionEntity, "The execution id does not match an execution entity");
        
        final ProcessExecutionStepEntity executionStepEntity = executionEntity.getStepByKey(stepKey);
        Assert.notNull(executionStepEntity, 
            "The step key does not correspond to a step inside the execution entity");
        
        // Add file record
        
        ProcessExecutionStepFileEntity fileEntity = new ProcessExecutionStepFileEntity(
            executionStepEntity, 
            fileRecord.getType(), fileRecord.getFilePath(), fileRecord.getFileSize());
        ResourceIdentifier resourceIdentifier = fileRecord.getResource();
        if (resourceIdentifier != null)
            fileEntity.setResource(findResourceEntity(resourceIdentifier, true));
        executionStepEntity.addFile(fileEntity);
        
        // Save
        
        entityManager.flush();
        return executionEntity.toProcessExecutionRecord(true);
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
            "from ProcessRevision p where p.parent.id = :id order by p.version desc";

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
    
    private ResourceRevisionEntity findResourceEntity(long resourceId, long resourceVersion)
    {
        ResourceRecord resourceRecord = resourceRepository.findOne(resourceId, resourceVersion);
        return resourceRecord == null?
            null : entityManager.find(ResourceRevisionEntity.class, resourceRecord.getId());
    }
    
    private ResourceRevisionEntity findResourceEntity(
        ResourceIdentifier resourceIdentifier, boolean failIfNotExists)
    {
        ResourceRevisionEntity e = 
            findResourceEntity(resourceIdentifier.getId(), resourceIdentifier.getVersion());
        if (e == null && failIfNotExists) {
            throw new NoSuchElementException(
                "The resource identifier does not correspond to a resource entity");
        }
        return e;
    }
}
