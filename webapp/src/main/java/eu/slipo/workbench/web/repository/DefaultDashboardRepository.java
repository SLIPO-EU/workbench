package eu.slipo.workbench.web.repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.slipo.workbench.common.domain.ProcessExecutionEntity;
import eu.slipo.workbench.common.domain.ResourceEntity;
import eu.slipo.workbench.common.model.process.EnumProcessExecutionStatus;
import eu.slipo.workbench.common.service.ProcessOperator;
import eu.slipo.workbench.web.domain.EventEntity;
import eu.slipo.workbench.web.model.Dashboard;
import eu.slipo.workbench.web.model.EventCounter;

@Repository()
@Transactional(readOnly = true)
public class DefaultDashboardRepository implements DashboardRepository {

    @Value("${slipo.dashboard.day-interval:7}")
    private int dayInterval;

    @Value("${slipo.dashboard.max-result:50}")
    private int maxResult;

    @PersistenceContext(unitName = "default")
    EntityManager entityManager;

    @Autowired
    private ProcessOperator processOperator;

    @Override
    public Dashboard load(Integer userId) throws Exception {
        return this.load(userId, this.dayInterval);
    }

    @Override
    public Dashboard load(Integer userId, int days) throws Exception {
        final Dashboard result = new Dashboard();
        final boolean isAdmin = userId == null;

        // Resource data
        final String resourceCountQuery =
            "select count(r.id) from Resource r where (r.createdBy.id = :userId or :userId is null)";

        final String resourceQuery =
            "select     r " +
            "from       Resource r " +
            "where      (r.createdOn >= :date or r.updatedOn >= :date) and " +
            "           (r.createdBy.id = :userId or :userId is null) " +
            "order by   r.updatedOn desc, r.version desc, r.id desc";

        final int totalResources = entityManager
            .createQuery(resourceCountQuery, Number.class)
            .setParameter("userId", userId)
            .getSingleResult()
            .intValue();


        entityManager
            .createQuery(resourceQuery, ResourceEntity.class)
            .setParameter("date", ZonedDateTime.now().minusDays(this.dayInterval))
            .setParameter("userId", userId)
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
        // TODO: Handle executions initiated by the system
        final String executionQuery =
            "select     e " +
            "from       ProcessExecution e " +
            "where      (e.startedOn != null and e.startedOn >= :date) and " +
            "           (e.submittedBy.id = :userId or :userId is null) " +
            "order by   e.startedOn desc, e.id desc";

        entityManager
            .createQuery(executionQuery, ProcessExecutionEntity.class)
            .setParameter("date", ZonedDateTime.now().minusDays(this.dayInterval))
            .setParameter("userId", userId)
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
        if(isAdmin) {
            final String eventQuery =
                    "select     e " +
                    "from       Event e " +
                    "order by   e.generated desc, e.id desc";

            entityManager
                .createQuery(eventQuery, EventEntity.class)
                .setMaxResults(this.maxResult)
                .getResultList()
                .stream()
                .map(EventEntity::toEventRecord)
                .collect(Collectors.toList())
                .forEach(result::addEvent);

            final String countEventQuery =
                    "select     new eu.slipo.workbench.web.model.EventCounter(e.level, count(e)) " +
                    "from       Event e " +
                    "where      e.generated >= :date " +
                    "group by   e.level";

            long error = 0, warning = 0, info = 0;

            List<EventCounter> counters = entityManager
                .createQuery(countEventQuery, EventCounter.class)
                .setParameter("date", ZonedDateTime.now().minusDays(1))
                .getResultList();

            for (EventCounter c : counters) {
                switch (c.getLevel()) {
                    case ERROR:
                        error = c.getValue();
                        break;
                    case WARN:
                        warning = c.getValue();
                        break;
                    case INFO:
                        info = c.getValue();
                        break;
                    default:
                        break;
                }
            }

            result.getStatistics().events = new Dashboard.EventStatistics(error, warning, info);
        } else {
            result.getStatistics().events = new Dashboard.EventStatistics(0, 0, 0);
        }

        // System status
        if(isAdmin) {
            try {
                this.processOperator.list().size();
                // TODO: Compute cluster resources
                result.getStatistics().system = new Dashboard.SystemStatistics(null, null, null, null, null, null);
            } catch(Exception ex) {
                result.getStatistics().system = new Dashboard.SystemStatistics(false);
            }
        }

        return result;
    }

}
