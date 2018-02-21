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

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.vividsolutions.jts.geom.Geometry;

import eu.slipo.workbench.common.domain.AccountEntity;
import eu.slipo.workbench.common.domain.ProcessExecutionEntity;
import eu.slipo.workbench.common.domain.ResourceEntity;
import eu.slipo.workbench.common.domain.ResourceRevisionEntity;
import eu.slipo.workbench.common.model.QueryResultPage;
import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.common.model.poi.EnumResourceType;
import eu.slipo.workbench.common.model.resource.ResourceIdentifier;
import eu.slipo.workbench.common.model.resource.ResourceMetadataView;
import eu.slipo.workbench.common.model.resource.ResourceQuery;
import eu.slipo.workbench.common.model.resource.ResourceRecord;

@Repository
@Transactional
public class DefaultResourceRepository implements ResourceRepository 
{
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
            if (query.getCreatedBy() != null)
                filters.add("(r.createdBy.id = :ownerId)");

            if (!StringUtils.isBlank(query.getName()))
                filters.add("(r.name like :name)");
           
            if (!StringUtils.isBlank(query.getDescription()))
                filters.add("(r.description like :description)");
            
            if (query.getFormat() != EnumDataFormat.UNDEFINED)
                filters.add("(r.inputFormat like :format)");
            
            if (query.getType() != EnumResourceType.UNDEFINED) 
                filters.add("(r.type like :type)");
            
            if (query.getSize() != null) 
                filters.add("(g.size >= :size)");
            
            if (query.getBoundingBox() != null) 
                filters.add("(intersects(:geometry, g.geometry) = true)");
        }

        // Count records
        qlString = "select count(r.id) from Resource r ";
        if (!filters.isEmpty()) {
            qlString += " where " + StringUtils.join(filters, " and ");
        }

        TypedQuery<Number> countQuery = entityManager.createQuery(qlString, Number.class);
        if (query != null) 
            setFindParameters(query, countQuery);
        int count = countQuery.getSingleResult().intValue();

        // Load records
        qlString = "select r from Resource r ";
        if (!filters.isEmpty()) {
            qlString += " where " + StringUtils.join(filters, " and ");
        }
        qlString += " order by r.name, r.updatedOn ";

        TypedQuery<ResourceEntity> selectQuery = entityManager.createQuery(qlString, ResourceEntity.class);
        if (query != null) 
            setFindParameters(query, selectQuery);

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
    public ResourceRecord findOne(String name) 
    {
        String queryString = "select r from Resource r where r.name = :name";

        List<ResourceEntity> resources = entityManager
            .createQuery(queryString, ResourceEntity.class)
            .setParameter("name", name)
            .getResultList();

        return (resources.isEmpty() ? null : resources.get(0).toResourceRecord());
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
        
        AccountEntity createdBy = entityManager.getReference(AccountEntity.class, userId);
        
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
        
        if (record.getMetadata() != null)
            entity.setMetadata(record.getMetadata());
       
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
    public ResourceRecord update(long id, ResourceRecord record, int userId)
    {
        Assert.notNull(record, "Expected a non-null record");
        Assert.isTrue(record.getId() < 0, "Did not expect an explicit id");
        Assert.isTrue(record.getVersion() < 0, "Did not expect an explicit version");
        Assert.isTrue(id > 0, "Expected a valid id for a record to be updated");
        
        AccountEntity updatedBy = entityManager.getReference(AccountEntity.class, userId);
        
        ZonedDateTime now = ZonedDateTime.now();
        
        ResourceEntity entity = entityManager.find(ResourceEntity.class, id);
        Assert.notNull(entity, "The given id does not refer to a ResourceEntity");
        
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
        
        if (record.getMetadata() != null)
            entity.setMetadata(record.getMetadata());
       
        Long executionId = record.getProcessExecutionId();
        if (executionId != null) {
            entity.setProcessExecution(
                entityManager.find(ProcessExecutionEntity.class, executionId));
        }
        
        ResourceRevisionEntity revisionEntity = new ResourceRevisionEntity(entity);
        entity.addRevision(revisionEntity);
        
        // Save
        
        entityManager.flush();
        return entity.toResourceRecord();
    }
    
    private ResourceRevisionEntity findRevision(long id, long version)
    {
        String queryString =
            "FROM ResourceRevision r WHERE r.parent.id = :id AND r.version = :version";

        TypedQuery<ResourceRevisionEntity> query = entityManager
            .createQuery(queryString, ResourceRevisionEntity.class)
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
}
