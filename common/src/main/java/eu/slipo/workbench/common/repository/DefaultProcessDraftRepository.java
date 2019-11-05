package eu.slipo.workbench.common.repository;

import java.time.ZonedDateTime;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import eu.slipo.workbench.common.domain.AccountEntity;
import eu.slipo.workbench.common.domain.ProcessDraftEntity;
import eu.slipo.workbench.common.model.process.ProcessDraftRecord;

@Repository
public class DefaultProcessDraftRepository implements ProcessDraftRepository {

    @PersistenceContext(unitName = "default")
    private EntityManager entityManager;

    @Override
    public ProcessDraftRecord findOne(int ownerId, long id) {
        ProcessDraftEntity entity = this.find(ownerId, id);

        return (entity == null ? null : entity.toRecord());
    }

    @Override
    public ProcessDraftRecord save(int ownerId, String definition, long id, boolean isTemplate) {
        boolean persist = false;

        AccountEntity owner = entityManager.find(AccountEntity.class, ownerId);
        Assert.notNull(owner, "The owner does not correspond to a user entity");

        ProcessDraftEntity draft = this.find(ownerId, id);
        if (draft == null) {
            persist = true;

            draft = new ProcessDraftEntity();
            draft.setId(id);
            draft.setOwner(owner);
        }

        draft.setUpdatedOn(ZonedDateTime.now());
        draft.setDefinition(definition);

        // Save
        if(persist) {
            entityManager.persist(draft);
        }
        entityManager.flush();

        return draft.toRecord();
    }

    @Override
    public void remove(int ownerId, long id) {
        ProcessDraftEntity draft = this.find(ownerId, id);
        if (draft != null) {
            this.entityManager.remove(draft);
        }
    }

    private ProcessDraftEntity find(int ownerId, long id) {
        String queryString = "select d from ProcessDraft d where d.id = :id and d.owner.id = :ownerId";

        return entityManager
            .createQuery(queryString, ProcessDraftEntity.class)
            .setParameter("id", id)
            .setParameter("ownerId", ownerId)
            .setMaxResults(1)
            .getResultList()
            .stream()
            .findFirst()
            .orElse(null);
    }

}
