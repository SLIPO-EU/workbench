package eu.slipo.workbench.web.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.slipo.workbench.common.model.Error;
import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.common.model.resource.EnumDataSourceType;
import eu.slipo.workbench.common.model.resource.ResourceMetadataCreate;
import eu.slipo.workbench.common.model.resource.ResourceRecord;
import eu.slipo.workbench.common.repository.ResourceRepository;
import eu.slipo.workbench.web.model.resource.RegistrationRequest;
import eu.slipo.workbench.web.model.resource.ResourceErrorCode;
import eu.slipo.workbench.web.model.resource.ResourceExportRequest;
import eu.slipo.workbench.web.model.resource.ResourceRegistrationRequest;

@Service
public class DefaultResourceValidationService implements IResourceValidationService
{
    @Autowired
    private ResourceRepository resourceRepository;

    @Override
    public List<Error> validate(ResourceRegistrationRequest request, int userId)
    {
        List<Error> errors = new ArrayList<Error>();

        // Shared validation rules
        validateMetadata(request.getMetadata(), userId, errors);

        // File upload is not a valid data source
        if (request.getDataSource().getType() == EnumDataSourceType.UPLOAD) {
            errors.add(new Error(ResourceErrorCode.DATASOURCE_NOT_SUPPORTED, "Data source of type 'UPLOAD' is not supported"));
        }

        return errors;
    }

    @Override
    public List<Error> validate(RegistrationRequest request, int userId, Path inputPath)
    {
        List<Error> errors = new ArrayList<Error>();

        // Shared validation rules
        validateMetadata(request.getMetadata(), userId, errors);

        // Check if file exists
        if(!Files.exists(inputPath)) {
            errors.add(new Error(ResourceErrorCode.FILE_NOT_FOUND, "Input file does not exist"));
        }

        return errors;
    }

    @Override
    public List<Error> validate(ResourceExportRequest request, int userId)
    {
        List<Error> errors = new ArrayList<Error>();

        // Check resource
        if (request.getResource() == null) {
            errors.add(new Error(ResourceErrorCode.RESOURCE_IS_EMPTY, "The resource is not set"));
        } else {
            final ResourceRecord resource = resourceRepository.findOne(request.getResource().getId(), request.getResource().getVersion());
            if (resource == null) {
                errors.add(new Error(ResourceErrorCode.RESOURCE_NOT_FOUND, "The resource was not found"));
            }
        }
        // Check output format
        final EnumDataFormat format = request.getConfiguration().getOutputFormat();
        if (format == null) {
            errors.add(new Error(ResourceErrorCode.FORMAT_NOT_SUPPORTED, "Format is not set"));
        } else {
            switch (format) {
                case CSV:
                case SHAPEFILE:
                    break;
                default:
                    errors.add(new Error(ResourceErrorCode.FORMAT_NOT_SUPPORTED, "Format is not supported"));
                    break;
            }
        }

        return errors;
    }

    private void validateMetadata(ResourceMetadataCreate metadata, int userId, List<Error> errors)
    {
        if (resourceRepository.findOne(metadata.getName(), userId) != null) {
            errors.add(new Error(ResourceErrorCode.NAME_DUPLICATE, "Resource name already exists"));
        }
    }

}
