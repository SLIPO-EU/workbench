package eu.slipo.workbench.web.service;

import java.nio.file.Path;
import java.util.List;

import eu.slipo.workbench.common.model.Error;
import eu.slipo.workbench.web.model.resource.RegistrationRequest;
import eu.slipo.workbench.web.model.resource.ResourceExportRequest;
import eu.slipo.workbench.web.model.resource.ResourceRegistrationRequest;

public interface IResourceValidationService {

    /**
     * Validates a resource registration request of type
     * {@link ResourceRegistrationRequest}
     *
     * @param request the request
     * @param userId the id of the user that performs the request
     *
     * @return an array of {@link Error} objects if validation has failed or an empty
     * array.
     */
    List<Error> validate(ResourceRegistrationRequest request, int userId);

    /**
     * Validates a resource registration request of type {@link RegistrationRequest}
     *
     * @param request the request
     * @param userId the id of the user that performs the request
     * @param inputPath the input file name
     *
     * @return an array of {@link Error} objects if validation has failed or an empty
     * array.
     */
    List<Error> validate(RegistrationRequest request, int userId, Path inputPath);

    /**
     * Validates a resource export request
     *
     * @param request the request
     * @param userId the id of the user that performs the request
     *
     * @return an array of {@link Error} objects if validation has failed or an empty
     * array.
     */
    List<Error> validate(ResourceExportRequest request, int userId);

}
