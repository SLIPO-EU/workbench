package eu.slipo.workbench.common.repository;

import static org.apache.commons.collections4.map.DefaultedMap.defaultedMap;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.vividsolutions.jts.geom.Geometry;

import eu.slipo.workbench.common.domain.AccountEntity;
import eu.slipo.workbench.common.domain.ProcessExecutionEntity;
import eu.slipo.workbench.common.domain.ProcessExecutionStepEntity;
import eu.slipo.workbench.common.domain.ProcessExecutionStepFileEntity;
import eu.slipo.workbench.common.domain.ProcessRevisionEntity;
import eu.slipo.workbench.common.domain.ResourceEntity;
import eu.slipo.workbench.common.domain.ResourceRevisionEntity;
import eu.slipo.workbench.common.model.QueryResultPage;
import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.common.model.poi.EnumOperation;
import eu.slipo.workbench.common.model.poi.EnumResourceType;
import eu.slipo.workbench.common.model.process.EnumStepFile;
import eu.slipo.workbench.common.model.process.ProcessDefinition;
import eu.slipo.workbench.common.model.resource.DataSource;
import eu.slipo.workbench.common.model.resource.EnumDataSourceType;
import eu.slipo.workbench.common.model.resource.ResourceIdentifier;
import eu.slipo.workbench.common.model.resource.ResourceMetadataCreate;
import eu.slipo.workbench.common.model.resource.ResourceQuery;
import eu.slipo.workbench.common.model.resource.ResourceRecord;

@Repository
@Transactional
public class DefaultResourceRepository implements ResourceRepository
{
    private static Logger logger = LoggerFactory.getLogger(DefaultResourceRepository.class);

    @PersistenceContext(unitName = "default")
    EntityManager entityManager;

    @Transactional(readOnly = true)
    @Override
    public QueryResultPage<ResourceRecord> find(ResourceQuery query, PageRequest pageReq)
    {
        // Check query parameters
        if (pageReq == null) {
            pageReq = new PageRequest(0, 10);
        }

        // Load data
        String qlString = "";

        // Resolve filters
        List<String> filters = new ArrayList<>();

        if (query != null) {
            if (query.getCreatedBy() != null) {
                filters.add("(r.createdBy.id = :ownerId)");
            }

            if (!StringUtils.isBlank(query.getName())) {
                filters.add("(r.name like :name)");
            }

            if (!StringUtils.isBlank(query.getDescription())) {
                filters.add("(r.description like :description)");
            }

            if (query.getFormat() != EnumDataFormat.UNDEFINED) {
                filters.add("(r.inputFormat like :format)");
            }

            if (query.getType() != EnumResourceType.UNDEFINED) {
                filters.add("(r.type like :type)");
            }

            if (query.getSize() != null) {
                filters.add("(g.size >= :size)");
            }

            if (query.getBoundingBox() != null) {
                filters.add("(intersects(:geometry, g.geometry) = true)");
            }
        }

        // Count records
        qlString = "select count(r.id) from Resource r ";
        if (!filters.isEmpty()) {
            qlString += " where " + StringUtils.join(filters, " and ");
        }

        TypedQuery<Number> countQuery = entityManager.createQuery(qlString, Number.class);
        if (query != null) {
            setFindParameters(query, countQuery);
        }
        int count = countQuery.getSingleResult().intValue();

        // Load records
        qlString = "select r from Resource r ";
        if (!filters.isEmpty()) {
            qlString += " where " + StringUtils.join(filters, " and ");
        }
        qlString += " order by r.updatedOn, r.name ";

        TypedQuery<ResourceEntity> selectQuery = entityManager.createQuery(qlString, ResourceEntity.class);
        if (query != null) {
            setFindParameters(query, selectQuery);
        }

        selectQuery.setFirstResult(pageReq.getOffset());
        selectQuery.setMaxResults(pageReq.getPageSize());

        List<ResourceRecord> records = selectQuery.getResultList().stream()
            .map(ResourceEntity::toResourceRecord)
            .collect(Collectors.toList());
        return new QueryResultPage<>(records, pageReq, count);
    }

    @Transactional(readOnly = true)
    @Override
    public ResourceRecord findOne(long id)
    {
        ResourceEntity r = entityManager.find(ResourceEntity.class, id);
        return r == null? null : r.toResourceRecord(false);
    }

    @Transactional(readOnly = true)
    @Override
    public ResourceRecord findOne(long id, long version)
    {
        ResourceRevisionEntity r = findRevision(id, version);
        return r == null? null : r.toResourceRecord();
    }

    @Transactional(readOnly = true)
    @Override
    public ResourceRecord findOne(String resourceName, int userId)
    {
        Assert.isTrue(!StringUtils.isEmpty(resourceName), "Expected a non-empty resource name");

        AccountEntity createdBy = entityManager.find(AccountEntity.class, userId);
        Assert.notNull(createdBy, "The userId does not correspond to a user entity");

        String qlString = "FROM Resource r WHERE r.name = :name AND r.createdBy.id = :userId";
        TypedQuery<ResourceEntity> query = entityManager.createQuery(qlString, ResourceEntity.class)
            .setParameter("name", resourceName)
            .setParameter("userId", userId);

        ResourceEntity entity = null;
        try {
            entity = query.getSingleResult();
        } catch (NoResultException ex) {
            entity = null;
        }

        return entity == null? null : entity.toResourceRecord();
    }

    @Transactional(readOnly = true)
    @Override
    public ResourceRecord findOne(ResourceIdentifier resourceIdentifier)
    {
        return ResourceRepository.super.findOne(resourceIdentifier);
    }

    @Override
    public ResourceRecord create(ResourceRecord record, int userId)
    {
        Assert.notNull(record, "Expected a non-null record");
        Assert.isTrue(record.getId() < 0, "Did not expect an explicit id");
        Assert.isTrue(record.getVersion() < 0, "Did not expect an explicit version");

        AccountEntity createdBy = entityManager.find(AccountEntity.class, userId);

        ZonedDateTime now = ZonedDateTime.now();

        // Create new entity

        ResourceEntity entity = new ResourceEntity();

        entity.setVersion(1L);
        entity.setCreatedBy(createdBy);
        entity.setCreatedOn(now);
        entity.setUpdatedBy(createdBy);
        entity.setUpdatedOn(now);

        entity.setType(record.getType());
        entity.setSourceType(record.getSourceType());
        entity.setFilePath(record.getFilePath());
        entity.setFileSize(record.getFileSize());
        entity.setInputFormat(record.getInputFormat());
        entity.setFormat(record.getFormat());
        entity.setTableName(record.getTableName());
        entity.setBoundingBox(record.getBoundingBox());
        entity.setProcessExecution(null);

        if (record.getMetadata() != null) {
            entity.setMetadata(record.getMetadata());
        }

        Long executionId = record.getProcessExecutionId();
        if (executionId != null) {
            entity.setProcessExecution(
                entityManager.find(ProcessExecutionEntity.class, executionId));
        }

        ResourceRevisionEntity revisionEntity = new ResourceRevisionEntity(entity);
        entity.addRevision(revisionEntity);

        // Save

        entityManager.persist(entity);
        entityManager.flush();
        return entity.toResourceRecord();
    }

    @Override
    public ResourceRecord createFromProcessExecution(
        long executionId, int stepKey, ResourceMetadataCreate metadata)
    {
        Assert.notNull(metadata, "Expected non-null metadata");
        Assert.isTrue(!StringUtils.isBlank(metadata.getName()), "A non-blank name is required");

        // Resolve process-related entities

        ProcessExecutionStepEntity stepEntity = findProcessExecutionStep(executionId, stepKey);
        Assert.notNull(stepEntity,
            "The pair of (executionId, stepKey) does not refer to a processing step");
        ProcessExecutionEntity executionEntity = stepEntity.getExecution();

        EnumOperation operation = stepEntity.getOperation();
        Assert.state(operation != null && operation != EnumOperation.UNDEFINED,
            "Expected a valid operation type for this processing step");

        Map<EnumStepFile, List<ProcessExecutionStepFileEntity>> fileEntitiesByType =
            stepEntity.getFiles().stream()
                .filter(ProcessExecutionStepFileEntity::isVerified)
                .collect(Collectors.groupingBy(ProcessExecutionStepFileEntity::getType));
        fileEntitiesByType = defaultedMap(fileEntitiesByType, Collections.emptyList());
        Assert.state(fileEntitiesByType.get(EnumStepFile.OUTPUT).size() == 1,
            "A processing step is expected to produce a single output file!");

        ProcessExecutionStepFileEntity fileEntity =
            fileEntitiesByType.get(EnumStepFile.OUTPUT).get(0);

        // Build the new resource entity

        ZonedDateTime now = ZonedDateTime.now();
        AccountEntity createdBy = executionEntity.getSubmittedBy();

        ResourceEntity entity = new ResourceEntity();

        entity.setVersion(1L);
        entity.setCreatedBy(createdBy);
        entity.setCreatedOn(now);
        entity.setUpdatedBy(createdBy);
        entity.setUpdatedOn(now);

        entity.setType(operation == EnumOperation.INTERLINK?
            EnumResourceType.POI_LINKED_DATA : EnumResourceType.POI_DATA);
        entity.setSourceType(determineSourceType(fileEntity));
        entity.setFilePath(fileEntity.getPath());
        entity.setFileSize(fileEntity.getSize());
        entity.setFormat(fileEntity.getDataFormat());
        entity.setInputFormat(fileEntitiesByType.get(EnumStepFile.INPUT).get(0).getDataFormat());
        entity.setTableName(fileEntity.getTableName());
        entity.setBoundingBox(fileEntity.getBoundingBox());
        entity.setProcessExecution(executionEntity);

        entity.setName(metadata.getName());
        entity.setDescription(metadata.getDescription());

        ResourceRevisionEntity revisionEntity = new ResourceRevisionEntity(entity);
        entity.addRevision(revisionEntity);

        // Persist entity

        entityManager.persist(entity);
        entityManager.flush();

        // Update resource link inside targeted file entity

        fileEntity.setResource(revisionEntity);

        // Save

        entityManager.flush();
        return entity.toResourceRecord();
    }

    @Override
    public void setProcessExecution(long id, long version, long executionId, Integer stepKey)
    {
        final ResourceRevisionEntity resourceRevisionEntity = findRevision(id, version);
        Assert.notNull(resourceRevisionEntity,
            "The pair of (id, version) does not refer to a ResourceRevision entity");

        final ProcessExecutionEntity executionEntity =
            entityManager.find(ProcessExecutionEntity.class, executionId);
        Assert.notNull(executionEntity,
            "The execution id does not refer to a ProcessExecution entity");
        final ZonedDateTime submittedOn = executionEntity.getSubmittedOn();

        // Associate targeted revision entity with given execution

        String qlUpdateRevision =
            "UPDATE ResourceRevision r SET r.processExecution = :execution " +
            "WHERE r.parent.id = :id AND r.version = :version AND r.processExecution is NULL";

        int countUpdated = entityManager.createQuery(qlUpdateRevision)
            .setParameter("execution", executionEntity)
            .setParameter("id", id)
            .setParameter("version", version)
            .executeUpdate();

        if (countUpdated == 0) {
            logger.warn("Did not update process execution for resource %d@%d: " +
                "resource is already linked to a process execution",
                id, version);
        }

        // If stepKey is given, must also update the step file entity

        if (stepKey != null) {
            ProcessExecutionStepEntity stepEntity = executionEntity.getStepByKey(stepKey);
            if (stepEntity == null) {
                // Find step entity inside a former execution (which handled this step key)
                ProcessRevisionEntity processRevisionEntity = executionEntity.getProcess();
                stepEntity = processRevisionEntity.getExecutions().stream()
                    .filter(x -> x.getSubmittedOn().isBefore(submittedOn))
                    .sorted(ProcessExecutionEntity.ORDER_BY_SUBMITTED.reversed())
                    .filter(x -> x.getStepByKey(stepKey) != null)
                    .findFirst()
                    .map(x -> x.getStepByKey(stepKey))
                    .orElse(null);
                Assert.state(stepEntity != null, String.format(
                    "No step entity for step #%d inside execution #%d (or former executions of same process)",
                    stepKey, executionId));
            }
            ProcessExecutionStepFileEntity fileEntity = stepEntity.getFiles().stream()
                .filter(f -> f.getType() == EnumStepFile.OUTPUT && f.isPrimary())
                .findFirst()
                .orElse(null);
            if (fileEntity != null) {
                fileEntity.setResource(resourceRevisionEntity);
            } else {
                Assert.state(false, String.format(
                    "No output file entity for step #%d inside execution #%d", stepKey, executionId));
            }
        }

        // If targeted revision is the latest, parent entity must also be updated

        String qlUpdateParent =
            "UPDATE Resource r SET r.processExecution = :execution " +
            "WHERE r.id = :id AND r.version = :version";

        entityManager.createQuery(qlUpdateParent)
            .setParameter("execution", executionEntity)
            .setParameter("id", id)
            .setParameter("version", version)
            .executeUpdate();
    }

    @Override
    public void setProcessExecution(long id, long version, long executionId)
    {
        ResourceRepository.super.setProcessExecution(id, version, executionId);
    }

    @Override
    public ResourceRecord update(long id, ResourceRecord record, int userId)
    {
        Assert.notNull(record, "Expected a non-null record");
        Assert.isTrue(record.getId() < 0, "Did not expect an explicit id");
        Assert.isTrue(record.getVersion() < 0, "Did not expect an explicit version");
        Assert.isTrue(id > 0, "Expected a valid id for a record to be updated");

        AccountEntity updatedBy = entityManager.getReference(AccountEntity.class, userId);

        ZonedDateTime now = ZonedDateTime.now();

        ResourceEntity entity = entityManager.find(ResourceEntity.class, id);
        Assert.notNull(entity, "The given id does not refer to a Resource entity");

        // Update entity and create new revision

        long version = entity.getVersion() + 1;

        entity.setVersion(version);
        entity.setUpdatedBy(updatedBy);
        entity.setUpdatedOn(now);

        entity.setSourceType(record.getSourceType());
        entity.setFilePath(record.getFilePath());
        entity.setFileSize(record.getFileSize());
        entity.setInputFormat(record.getInputFormat());
        entity.setFormat(record.getFormat());
        entity.setTableName(record.getTableName());
        entity.setBoundingBox(record.getBoundingBox());

        if (record.getMetadata() != null) {
            entity.setMetadata(record.getMetadata());
        }

        Long executionId = record.getProcessExecutionId();
        if (executionId != null) {
            entity.setProcessExecution(
                entityManager.find(ProcessExecutionEntity.class, executionId));
        } else {
            entity.setProcessExecution(null);
        }

        ResourceRevisionEntity revisionEntity = new ResourceRevisionEntity(entity);
        entity.addRevision(revisionEntity);

        // Save

        entityManager.flush();
        return entity.toResourceRecord();
    }

    private ResourceRevisionEntity findRevision(long id, long version)
    {
        String qlString =
            "FROM ResourceRevision r WHERE r.parent.id = :id AND r.version = :version";

        TypedQuery<ResourceRevisionEntity> query =
            entityManager.createQuery(qlString, ResourceRevisionEntity.class)
                .setParameter("id", id)
                .setParameter("version", version);

        ResourceRevisionEntity r = null;
        try {
            r = query.getSingleResult();
        } catch (NoResultException ex) {
            r = null;
        }
        return r;
    }

    private void setFindParameters(ResourceQuery resourceQuery, Query query)
    {
        Geometry geometry = resourceQuery.getBoundingBox();
        if ((geometry != null) && (geometry.getSRID() == 0)) {
            geometry.setSRID(4326);
        }

        if (!StringUtils.isBlank(resourceQuery.getName())) {
            query.setParameter("name", "%" + resourceQuery.getName() + "%");
        }
        if (!StringUtils.isBlank(resourceQuery.getDescription())) {
            query.setParameter("description", "%" + resourceQuery.getDescription() + "%");
        }
        if (resourceQuery.getFormat() != EnumDataFormat.UNDEFINED) {
            query.setParameter("format", resourceQuery.getFormat());
        }
        if (resourceQuery.getType() != EnumResourceType.UNDEFINED) {
            query.setParameter("type", resourceQuery.getType());
        }
        if (resourceQuery.getSize() != null) {
            query.setParameter("size", resourceQuery.getSize());
        }
        if (geometry != null) {
            query.setParameter("geometry", geometry);
        }

        Integer userId = resourceQuery.getCreatedBy();
        query.setParameter("ownerId", userId == null? -1 : userId.intValue());
    }

    /**
     * Find the entity representing a processing step inside an execution
     * @param executionId
     * @param stepKey
     * @return
     */
    private ProcessExecutionStepEntity findProcessExecutionStep(long executionId, int stepKey)
    {
        TypedQuery<ProcessExecutionStepEntity> q = entityManager
            .createQuery(
                "FROM ProcessExecutionStep s WHERE s.execution.id = :xid AND s.key = :key",
                ProcessExecutionStepEntity.class)
            .setParameter("xid", executionId)
            .setParameter("key", stepKey);

        ProcessExecutionStepEntity r = null;
        try {
            r = q.getSingleResult();
        } catch (NoResultException ex) {
            r = null;
        }

        return r;
    }

    /**
     * Determine the source-type (i.e {@link EnumDataSourceType}) of a given file entity
     * produced as an output of a processing step.
     *
     * <p>If the processing step carries a TRANSFORM operation (the only case where an external
     * data source can be imported), then the source-type is determined by "ascending" to the actual
     * definition of the process that fired the execution (producing our output).
     *
     * <p>If any other type of operation takes place, then the source-type is
     * {@link EnumDataSourceType#FILESYSTEM}.
     *
     * @param fileEntity
     * @return
     */
    private EnumDataSourceType determineSourceType(ProcessExecutionStepFileEntity fileEntity)
    {
        Assert.state(fileEntity != null && fileEntity.getType() == EnumStepFile.OUTPUT,
            "Expected a non-null file entity produced as output from a processing step");

        final ProcessExecutionStepEntity stepEntity = fileEntity.getStep();
        final int stepKey = stepEntity.getKey();
        final EnumOperation operation = stepEntity.getOperation();
        Assert.state(operation != null
                && operation != EnumOperation.UNDEFINED && operation != EnumOperation.REGISTER,
            "Encountered an invalid operation type (for the kind of processing step)");
        if (operation != EnumOperation.TRANSFORM) {
            return EnumDataSourceType.FILESYSTEM;
        }

        // If here, the operation is EnumOperation.TRANSFORM and we must fetch the actual step
        // definition in order to determine the source-type

        final ProcessExecutionEntity executionEntity = stepEntity.getExecution();
        final ProcessDefinition definition = executionEntity.getProcess().getDefinition();

        List<DataSource> sources = definition.steps().stream()
            .filter(s -> s.key() == stepKey)
            .findFirst()
            .map(s -> s.sources())
            .get();

        if (sources.isEmpty()) {
            return EnumDataSourceType.FILESYSTEM;
        } else {
            return sources.get(0).getType();
        }
    }
}
