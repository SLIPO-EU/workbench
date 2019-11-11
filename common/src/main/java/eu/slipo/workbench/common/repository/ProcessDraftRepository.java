package eu.slipo.workbench.common.repository;

import org.springframework.transaction.annotation.Transactional;

import eu.slipo.workbench.common.model.process.ProcessDraftRecord;

@Transactional
public interface ProcessDraftRepository {

    default ProcessDraftRecord findOne(int ownerId) {
        return this.findOne(ownerId, 0);
    }

    /**
     * Get the process draft for the specific owner and process id
     *
     * @param ownerId The process owner id.
     * @param id The process id. If a new process was created, the id is equal to 0.
     * @return An instance of {@link ProcessDraftRecord} if a draft is found; Otherwise
     * <code>null</code> is returned.
     */
    ProcessDraftRecord findOne(int ownerId, long id);

    default ProcessDraftRecord save(int ownerId, String definition, boolean isTemplate) {
        return this.save(ownerId, definition, 0, isTemplate);
    }

    /**
     * Stores a process draft for the specific owner and process id. If a draft already
     * exists, it is overwritten.
     *
     * @param ownerId The process owner id.
     * @param definition The serialized process definition.
     * @param id The process id. If a new process was created, the id is equal to 0.
     * @param isTemplate True if the process is a template.
     * @return An instance of {@link ProcessDraftRecord}.
     */
    ProcessDraftRecord save(int ownerId, String definition, long id, boolean isTemplate);

    default void remove(int ownerId) {
        this.remove(ownerId, 0);
    }

    /**
     * Remove draft for the specific owner and process id
     *
     * @param ownerId The process owner id.
     * @param id The process id.
     */
    void remove(int ownerId, long id);
}
