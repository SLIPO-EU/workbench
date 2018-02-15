package eu.slipo.workbench.web.service;

import java.util.List;

import eu.slipo.workbench.common.model.Error;
import eu.slipo.workbench.web.model.resource.RegistrationRequest;
import eu.slipo.workbench.web.model.resource.ResourceRegistrationRequest;

public interface IResourceValidationService {

    /**
     * Validates a resource registration request of type
     * {@link ResourceRegistrationRequest}
     *
     * @param request the request
     * @return an array of {@link Error} objects if validation has failed or an empty
     * array.
     */
    List<Error> validate(ResourceRegistrationRequest request);

    /**
     * Validates a resource registration request of type {@link RegistrationRequest}
     *
     * @param request
     * @param request the request
     * @param inputFileName the input file name
     * @return an array of {@link Error} objects if validation has failed or an empty
     * array.
     */
    List<Error> validate(RegistrationRequest request, String inputFileName);

}
