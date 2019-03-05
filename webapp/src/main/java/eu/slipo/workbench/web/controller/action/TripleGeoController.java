package eu.slipo.workbench.web.controller.action;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import eu.slipo.triplegeo.ml.mappings.FieldMatcher;
import eu.slipo.triplegeo.ml.mappings.Mappings;
import eu.slipo.workbench.common.model.BasicErrorCode;
import eu.slipo.workbench.common.model.Error;
import eu.slipo.workbench.common.model.RestResponse;
import eu.slipo.workbench.common.model.tool.TriplegeoRankedFieldPredicateMapping;
import eu.slipo.workbench.web.model.triplegeo.MappingFileRequest;
import eu.slipo.workbench.web.service.CsvUtils;
import eu.slipo.workbench.web.service.ProcessService;

/**
 * Provides helper actions for TripleGeo
 */
@RestController
@Secured({ "ROLE_USER", "ROLE_AUTHOR", "ROLE_ADMIN" })
@RequestMapping(produces = "application/json")
public class TripleGeoController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(TripleGeoController.class);

    @Value("classpath:triplegeo/triplegeo-ml-mappings.model")
    Resource resourceFile;

    @Autowired
    private ProcessService processService;

    @GetMapping(value = "/action/triplegeo/mappings", params = { "path" })
    public RestResponse<?> getMappings(@RequestParam("path") String relativePath) {

        try {
            // Resolve file
            Path path = this.fileNamingStrategy.resolvePath(this.currentUserId(), relativePath);
            if (!Files.exists(path)) {
                return RestResponse.error(BasicErrorCode.RESOURCE_NOT_FOUND, "File was not found");
            }
            // Get model
            if (!resourceFile.exists()) {
                return RestResponse.error(BasicErrorCode.RESOURCE_NOT_FOUND, "Model does not exists");
            }
            // Generate mappings
            FieldMatcher fm = FieldMatcher.create(resourceFile.getFile());

            fm.setDelimiter(CsvUtils.detectDelimiter(path).charAt(0));
            fm.setQuote(CsvUtils.detectQuote(path, fm.getDelimiter()));

            Mappings mappings = fm.giveMatchings(path.toString());

            List<TriplegeoRankedFieldPredicateMapping> predicates = new ArrayList<TriplegeoRankedFieldPredicateMapping>();
            for (Mappings.Field field : mappings.getFields()) {
                for (String key : field.getPredicates().keySet()) {
                    predicates.add(
                        new TriplegeoRankedFieldPredicateMapping(field.getName(), key, field.getPredicates().get(key))
                    );
                }
            }

            return RestResponse.result(predicates);
        } catch (Exception ex) {
            return this.exceptionToResponse(ex);
        }
    }

    @PostMapping(value = "/action/triplegeo/mappings")
    public RestResponse<?> getMappings(@RequestBody MappingFileRequest request) {

        try {
            final String result = this.processService.tripleGeoMappingsAsText(request.getMappings());

            return RestResponse.result(result);
        } catch (Exception ex) {
            return this.exceptionToResponse(ex);
        }
    }

    private RestResponse<?> exceptionToResponse(Exception ex) {
        logger.error(ex.getMessage(), ex);
        return exceptionToResponse(ex, Error.EnumLevel.ERROR);
    }

}
