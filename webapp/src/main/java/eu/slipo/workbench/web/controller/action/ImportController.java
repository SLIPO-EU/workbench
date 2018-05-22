package eu.slipo.workbench.web.controller.action;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.slipo.workbench.common.model.BasicErrorCode;
import eu.slipo.workbench.common.model.RestResponse;
import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.common.model.poi.EnumTool;
import eu.slipo.workbench.common.model.process.ProcessExecutionRecord;
import eu.slipo.workbench.common.model.process.ProcessRecord;
import eu.slipo.workbench.common.model.process.Step;
import eu.slipo.workbench.common.model.resource.EnumDataSourceType;
import eu.slipo.workbench.common.model.resource.FileSystemDataSource;
import eu.slipo.workbench.common.model.tool.TriplegeoConfiguration;
import eu.slipo.workbench.common.repository.ProcessRepository;
import eu.slipo.workbench.web.service.etl.DatabaseImportResult;
import eu.slipo.workbench.web.service.etl.ImportService;

@RestController
@Secured({ "ROLE_ADMIN" })
@RequestMapping(produces = "application/json")
public class ImportController extends BaseController {

    @Autowired
    ProcessRepository processRepository;

    @Autowired
    ImportService importService;

    @RequestMapping(value = "/action/import/{executionId}", method = RequestMethod.GET)
    public RestResponse<?> importExecutionData(@PathVariable() Long executionId) {

        try {
            final ProcessExecutionRecord execution = processRepository.findExecution(executionId);
            final ProcessRecord process = processRepository.findOne(execution.getProcess().getId(), execution.getProcess().getVersion());

            final ImportResult result = new ImportResult();
            result.process = process;
            result.execution = execution;

            process.getDefinition()
                .steps()
                .stream()
                .filter(s -> s.tool() == EnumTool.TRIPLEGEO &&
                             s.sources().size() == 1 &&
                             s.sources().get(0).getType() == EnumDataSourceType.FILESYSTEM)
                .map(s-> {
                    TriplegeoConfiguration config = (TriplegeoConfiguration) s.configuration();
                    config.setInputFromString(((FileSystemDataSource) s.sources().get(0)).getPath());
                    return Pair.<Step, TriplegeoConfiguration>of(s, config);
                 })
                .filter(p -> p.getRight().getInputFormat() == EnumDataFormat.CSV || p.getRight().getInputFormat() == EnumDataFormat.SHAPEFILE)
                .forEach(p-> {
                    DatabaseImportResult dir = importService.exportWfsLayer(
                        currentUserId(), execution.getId(), p.getLeft(), process.getName() + " : " + p.getLeft().name()
                    );
                    result.imports.add(dir);
                });

            return RestResponse.result(result);
        } catch (Exception ex) {
            return RestResponse.error(BasicErrorCode.UNKNOWN, "An unknown error has occurred");
        }
    }

    public static class ImportResult {

        public ProcessRecord process;

        public ProcessExecutionRecord execution;

        public List<DatabaseImportResult> imports = new ArrayList<DatabaseImportResult>();

    }

}
