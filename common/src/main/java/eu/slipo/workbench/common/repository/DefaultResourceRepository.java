package eu.slipo.workbench.common.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.vividsolutions.jts.geom.Geometry;

import eu.slipo.workbench.common.domain.ResourceEntity;
import eu.slipo.workbench.common.model.QueryResultPage;
import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.common.model.poi.EnumResourceType;
import eu.slipo.workbench.common.model.resource.ResourceQuery;
import eu.slipo.workbench.common.model.resource.ResourceRecord;

@Repository
@Transactional()
public class DefaultResourceRepository implements ResourceRepository {

    /**
     * Entity manager for persisting data.
     */
    @PersistenceContext(unitName = "default")
    EntityManager entityManager;

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

    @Override
    public QueryResultPage<ResourceRecord> find(ResourceQuery query, PageRequest pageReq) 
    {
        // Check query parameters
        if (pageReq == null)
            pageReq = new PageRequest(0, 10);

        // Load data
        String command = "";

        // Resolve filters
        List<String> filters = new ArrayList<>();

        filters.add("(r.createdBy.id = :ownerId)");

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

        // Count records
        command = "select count(r.id) from Resource r ";
        if (!filters.isEmpty()) {
            command += " where " + StringUtils.join(filters, " and ");
        }

        TypedQuery<Number> countQuery = entityManager.createQuery(command, Number.class);
        setFindParameters(query, countQuery);
        int count = countQuery.getSingleResult().intValue();

        // Load records
        command = "select r from Resource r ";
        if (!filters.isEmpty()) {
            command += " where " + StringUtils.join(filters, " and ");
        }
        command += " order by r.name, r.updatedOn ";

        TypedQuery<ResourceEntity> selectQuery = entityManager.createQuery(command, ResourceEntity.class);
        setFindParameters(query, selectQuery);

        selectQuery.setFirstResult(pageReq.getOffset());
        selectQuery.setMaxResults(pageReq.getPageSize());

        List<ResourceRecord> records = selectQuery.getResultList().stream()
            .map(ResourceEntity::toResourceRecord)
            .collect(Collectors.toList());
        return new QueryResultPage<>(records, pageReq, count);
    }

    @Override
    public ResourceRecord findOne(long id) 
    {
        String queryString = "select r from Resource r where r.id = :id";

        TypedQuery<ResourceEntity> query = entityManager.createQuery(queryString, ResourceEntity.class);
        query.setParameter("id", id);
        List<ResourceEntity> resources = query.getResultList();

        return (resources.isEmpty() ? null : resources.get(0).toResourceRecord());
    }

}
