package eu.slipo.workbench.web.repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.slipo.workbench.common.domain.AccountEntity;
import eu.slipo.workbench.common.domain.ProcessExecutionEntity;
import eu.slipo.workbench.common.domain.ProcessExecutionMapExportEntity;
import eu.slipo.workbench.common.model.etl.EnumMapExportStatus;
import eu.slipo.workbench.common.model.etl.MapExportTask;
import eu.slipo.workbench.common.model.process.EnumProcessExecutionStatus;

@Repository
@Transactional
public class DefaultMapExportTaskRepository implements MapExportTaskRepository {

    @PersistenceContext(unitName = "default")
    private EntityManager entityManager;

    @Override
    public void schedule(int userId, long executionId) {
        String queryString = "FROM ProcessExecution e WHERE e.id = :id";

        TypedQuery<ProcessExecutionEntity> query = entityManager
            .createQuery(queryString, ProcessExecutionEntity.class)
            .setParameter("id", executionId);

        ProcessExecutionEntity execution = query.getSingleResult();
        ProcessExecutionMapExportEntity export = execution.getMap();

        if ((export == null) || (export.getStatus() == EnumMapExportStatus.FAILED)) {
            // Remove existing export entity if any exists
            if(export != null) {
                execution.setMap(null);
                this.entityManager.remove(export);
            }
            AccountEntity createdBy = entityManager.getReference(AccountEntity.class, userId);

            export = new ProcessExecutionMapExportEntity();
            export.setCreatedBy(createdBy);
            export.setWorkflow(execution);

            this.entityManager.persist(export);
        }
    }

    @Override
    public void setTransformExecution(long taskId, long executionId) {
        String taskQueryString = "FROM ProcessExecutionMapExport e WHERE e.id = :id";
        String executionQueryString = "FROM ProcessExecution e WHERE e.id = :id";

        TypedQuery<ProcessExecutionMapExportEntity> taskQuery = entityManager
            .createQuery(taskQueryString, ProcessExecutionMapExportEntity.class);

        TypedQuery<ProcessExecutionEntity> executionQuery = entityManager
            .createQuery(executionQueryString, ProcessExecutionEntity.class);

        ProcessExecutionMapExportEntity task = taskQuery.setParameter("id", taskId).getSingleResult();
        ProcessExecutionEntity execution = executionQuery.setParameter("id", executionId).getSingleResult();

        task.setTransform(execution);
    }

    @Override
    public void setStatus(long taskId, EnumMapExportStatus status) {
        String queryString = "FROM ProcessExecutionMapExport e WHERE e.id = :id";

        ProcessExecutionMapExportEntity task = entityManager
            .createQuery(queryString, ProcessExecutionMapExportEntity.class)
            .setParameter("id", taskId)
            .getSingleResult();

        task.setStatus(status);
        switch (status) {
            case COMPLETED:
            case FAILED:
                task.setCompletedOn(ZonedDateTime.now());
                break;
            case RUNNING:
                task.setStartedOn(ZonedDateTime.now());
                break;
            default:
                // Do nothing
        }
    }

    @Override
    public void remove(long taskId) {
        String queryString = "FROM ProcessExecutionMapExport e WHERE e.id = :id";

        ProcessExecutionMapExportEntity task = entityManager
            .createQuery(queryString, ProcessExecutionMapExportEntity.class)
            .setParameter("id", taskId)
            .getSingleResult();

        this.entityManager.remove(task);
    }

    @Override
    public List<MapExportTask> getPendingTasks() {
        String queryString =
            "FROM ProcessExecutionMapExport e WHERE e.status <> :completed1 and e.status <> :failed1 and e.workflow.status = :completed2";

        TypedQuery<ProcessExecutionMapExportEntity> query = entityManager
            .createQuery(queryString, ProcessExecutionMapExportEntity.class)
            .setParameter("completed1", EnumMapExportStatus.COMPLETED)
            .setParameter("failed1", EnumMapExportStatus.FAILED)
            .setParameter("completed2", EnumProcessExecutionStatus.COMPLETED);

        return query.getResultList().stream()
            .map(r -> r.toRecord())
            .collect(Collectors.toList());
    }

    @Override
    public void resetRunningTasks() {
        String queryString = "FROM ProcessExecutionMapExport e WHERE e.status = :running";

        TypedQuery<ProcessExecutionMapExportEntity> query = entityManager
            .createQuery(queryString, ProcessExecutionMapExportEntity.class)
            .setParameter("running", EnumMapExportStatus.RUNNING);

        query.getResultList().stream().forEach(t -> {
            t.setStatus(EnumMapExportStatus.PENDING);
        });
    }

}
