package eu.slipo.workbench.web.controller.action;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.slipo.workbench.common.model.BasicErrorCode;
import eu.slipo.workbench.common.model.RestResponse;
import eu.slipo.workbench.common.model.process.ProcessExecutionRecord;
import eu.slipo.workbench.common.model.process.ProcessRecord;
import eu.slipo.workbench.web.service.etl.DatabaseImportResult;
import eu.slipo.workbench.web.service.etl.ImportService;

@RestController
@Secured({ "ROLE_USER", "ROLE_AUTHOR", "ROLE_ADMIN" })
@RequestMapping(produces = "application/json")
public class ImportController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ImportController.class);

    @Autowired
    ImportService importService;

    @RequestMapping(value = "/action/import/{executionId}", method = RequestMethod.GET)
    public RestResponse<?> importExecutionData(@PathVariable() Long executionId) {

        try {
            final ImportResult result = importService.publishExecutionLayers(currentUserId(), executionId);

            return RestResponse.result(result);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            return RestResponse.error(BasicErrorCode.UNKNOWN, "An unknown error has occurred");
        }
    }

    public static class ImportResult {

        public ProcessRecord process;

        public ProcessExecutionRecord execution;

        public List<DatabaseImportResult> imports = new ArrayList<DatabaseImportResult>();

    }

}
