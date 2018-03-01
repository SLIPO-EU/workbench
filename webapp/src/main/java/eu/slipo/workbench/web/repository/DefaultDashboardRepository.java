package eu.slipo.workbench.web.repository;

import java.time.ZonedDateTime;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.slipo.workbench.common.domain.ProcessExecutionEntity;
import eu.slipo.workbench.common.domain.ResourceEntity;
import eu.slipo.workbench.common.model.process.EnumProcessExecutionStatus;
import eu.slipo.workbench.web.domain.EventEntity;
import eu.slipo.workbench.web.model.Dashboard;
import eu.slipo.workbench.web.model.EnumEventLevel;

@Repository()
@Transactional(readOnly = true)
public class DefaultDashboardRepository implements DashboardRepository {

    @Value("${slipo.dashboard.day-interval:7}")
    private int dayInterval;

    @Value("${slipo.dashboard.max-result:50}")
    private int maxResult;

    @PersistenceContext(unitName = "default")
    EntityManager entityManager;

    @Override
    public Dashboard load() throws Exception {
        return this.load(this.dayInterval);
    }

    @Override
    public Dashboard load(int days) throws Exception {
        final Dashboard result = new Dashboard();

        // Resource data
        final String resourceCountQuery =
            "select count(r.id) from Resource r";

        final String resourceQuery =
            "select     r " +
            "from       Resource r " +
            "where      r.createdOn >= :date or r.updatedOn >= :date " +
            "order by   r.updatedOn desc, r.version desc, r.id desc";

        final int totalResources = entityManager
            .createQuery(resourceCountQuery, Number.class)
            .getSingleResult()
            .intValue();


        entityManager
            .createQuery(resourceQuery, ResourceEntity.class)
            .setParameter("date", ZonedDateTime.now().minusDays(this.dayInterval))
            .getResultList()
            .stream()
            .map(ResourceEntity::toResourceRecord)
            .collect(Collectors.toList())
            .forEach(result::addResource);


        final long updatedResources = result
            .getResources()
            .stream()
            .filter(r-> !r.getCreatedOn().equals(r.getUpdatedOn()))
            .count();
        final long newResources = result
            .getResources()
            .stream()
            .filter(r-> r.getCreatedOn().equals(r.getUpdatedOn()))
            .count();

        result.getStatistics().resources = new Dashboard.ResourceStatistics(totalResources, newResources, updatedResources);

        // Process data
        final String executionQuery =
            "select     e " +
            "from       ProcessExecution e " +
            "where      e.startedOn != null and e.startedOn >= :date " +
            "order by   e.startedOn desc, e.id desc";

        entityManager
            .createQuery(executionQuery, ProcessExecutionEntity.class)
            .setParameter("date", ZonedDateTime.now().minusDays(this.dayInterval))
            .getResultList()
            .stream()
            .map(e -> e.toProcessExecutionRecord(false))
            .collect(Collectors.toList())
            .forEach(result::addProcessExecution);


        final long completed = result
            .getProcesses()
            .stream()
            .filter(e-> e.getStatus() == EnumProcessExecutionStatus.COMPLETED)
            .count();
        final long running = result
            .getProcesses()
            .stream()
            .filter(e-> e.getStatus() == EnumProcessExecutionStatus.RUNNING)
            .count();
        final long failed = result
            .getProcesses()
            .stream()
            .filter(e-> e.getStatus() == EnumProcessExecutionStatus.FAILED)
            .count();

        result.getStatistics().processes = new Dashboard.ProcessStatistics(completed, running, failed);

        // Event data
        final String eventQuery =
                "select     e " +
                "from       Event e " +
                "where      e.generated >= :date " +
                "order by   e.generated desc, e.id desc";

        entityManager
                .createQuery(eventQuery, EventEntity.class)
                .setParameter("date", ZonedDateTime.now().minusDays(1))
                .setMaxResults(this.maxResult)
                .getResultList()
                .stream()
                .map(EventEntity::toEventRecord)
                .collect(Collectors.toList())
                .forEach(result::addEvent);


            final long error = result
                .getEvents()
                .stream()
                .filter(e-> e.getLevel() == EnumEventLevel.ERROR)
                .count();
            final long warning = result
                .getEvents()
                .stream()
                .filter(e-> e.getLevel() == EnumEventLevel.WARN)
                .count();
            final long info = result
                .getEvents()
                .stream()
                .filter(e-> e.getLevel() == EnumEventLevel.INFO)
                .count();

        result.getStatistics().events = new Dashboard.EventStatistics(error, warning, info);

        // System status

        // TODO: Get system information from rpc-server

        return result;
    }
}
