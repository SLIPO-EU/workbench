package eu.slipo.workbench.web.repository;

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

import com.vividsolutions.jts.geom.Geometry;

import eu.slipo.workbench.web.domain.ResourceEntity;
import eu.slipo.workbench.web.model.EnumDataFormat;
import eu.slipo.workbench.web.model.EnumResourceType;
import eu.slipo.workbench.web.model.QueryPagingOptions;
import eu.slipo.workbench.web.model.QueryResult;
import eu.slipo.workbench.web.model.resource.ResourceQuery;
import eu.slipo.workbench.web.model.resource.ResourceRecord;
import eu.slipo.workbench.web.service.IAuthenticationFacade;

@Repository
@Transactional()
public class JpaResourceRepository implements IResourceRepository {

    /**
     * Entity manager for persisting data.
     */
    @PersistenceContext(unitName = "default")
    EntityManager entityManager;

    @Autowired
    IAuthenticationFacade authenticationFacade;

    private void setFindParameters(ResourceQuery resourceQuery, Query query) {
        Integer ownerId = authenticationFacade.getCurrentUserId();

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
        query.setParameter("ownerId", ownerId);
    }

    @Override
    public QueryResult<ResourceRecord> find(ResourceQuery query) {
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

        Integer totalResources;
        TypedQuery<Number> countQuery = entityManager.createQuery(command, Number.class);
        setFindParameters(query, countQuery);
        totalResources = countQuery.getSingleResult().intValue();

        // Load records
        command = "select r from Resource r ";
        if (!filters.isEmpty()) {
            command += " where " + StringUtils.join(filters, " and ");
        }
        command += " order by r.name, r.updatedOn ";

        TypedQuery<ResourceEntity> selectQuery = entityManager.createQuery(command, ResourceEntity.class);
        setFindParameters(query, selectQuery);

        selectQuery.setFirstResult(pagingOptions.pageIndex * pagingOptions.pageSize);
        selectQuery.setMaxResults(pagingOptions.pageSize);

        QueryResult<ResourceRecord> result = new QueryResult<ResourceRecord>(pagingOptions, totalResources);
        for (ResourceEntity resource : selectQuery.getResultList()) {
            result.addItem(resource.toResourceRecord());
        }

        return result;
    }

    @Override
    public ResourceRecord findOne(long id) {
        String queryString = "select r from Resource r where r.id = :id";

        TypedQuery<ResourceEntity> query = entityManager.createQuery(queryString, ResourceEntity.class);
        query.setParameter("id", id);
        List<ResourceEntity> resources = query.getResultList();

        return (resources.isEmpty() ? null : resources.get(0).toResourceRecord());
    }

}
