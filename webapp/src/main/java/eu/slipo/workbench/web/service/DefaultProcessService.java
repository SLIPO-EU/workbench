package eu.slipo.workbench.web.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.slugify.Slugify;

import eu.slipo.workbench.common.model.ApplicationException;
import eu.slipo.workbench.common.model.BasicErrorCode;
import eu.slipo.workbench.common.model.EnumRole;
import eu.slipo.workbench.common.model.ErrorCode;
import eu.slipo.workbench.common.model.FileSystemErrorCode;
import eu.slipo.workbench.common.model.QueryResultPage;
import eu.slipo.workbench.common.model.poi.EnumOperation;
import eu.slipo.workbench.common.model.process.ApiCallQuery;
import eu.slipo.workbench.common.model.process.CatalogResource;
import eu.slipo.workbench.common.model.process.EnumInputType;
import eu.slipo.workbench.common.model.process.EnumProcessExecutionStatus;
import eu.slipo.workbench.common.model.process.EnumProcessTaskType;
import eu.slipo.workbench.common.model.process.EnumStepFile;
import eu.slipo.workbench.common.model.process.InvalidProcessDefinitionException;
import eu.slipo.workbench.common.model.process.ProcessDefinition;
import eu.slipo.workbench.common.model.process.ProcessErrorCode;
import eu.slipo.workbench.common.model.process.ProcessExecutionApiRecord;
import eu.slipo.workbench.common.model.process.ProcessExecutionNotFoundException;
import eu.slipo.workbench.common.model.process.ProcessExecutionQuery;
import eu.slipo.workbench.common.model.process.ProcessExecutionRecord;
import eu.slipo.workbench.common.model.process.ProcessExecutionStartException;
import eu.slipo.workbench.common.model.process.ProcessExecutionStepFileRecord;
import eu.slipo.workbench.common.model.process.ProcessExecutionStepRecord;
import eu.slipo.workbench.common.model.process.ProcessExecutionStopException;
import eu.slipo.workbench.common.model.process.ProcessIdentifier;
import eu.slipo.workbench.common.model.process.ProcessNotFoundException;
import eu.slipo.workbench.common.model.process.ProcessQuery;
import eu.slipo.workbench.common.model.process.ProcessRecord;
import eu.slipo.workbench.common.model.process.Step;
import eu.slipo.workbench.common.model.resource.DataSource;
import eu.slipo.workbench.common.model.resource.EnumDataSourceType;
import eu.slipo.workbench.common.model.resource.FileSystemDataSource;
import eu.slipo.workbench.common.model.resource.ResourceRecord;
import eu.slipo.workbench.common.model.security.ApplicationKeyRecord;
import eu.slipo.workbench.common.model.tool.TriplegeoConfiguration;
import eu.slipo.workbench.common.model.tool.TriplegeoFieldMapping;
import eu.slipo.workbench.common.repository.ProcessRepository;
import eu.slipo.workbench.common.repository.ResourceRepository;
import eu.slipo.workbench.common.service.ProcessOperator;
import eu.slipo.workbench.web.model.process.ProcessExecutionRecordView;
import eu.slipo.workbench.web.model.triplegeo.Predicates;
import eu.slipo.workbench.web.service.etl.ImportService;

@Service
public class DefaultProcessService implements ProcessService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultProcessService.class);

    protected static final Slugify slugify = new Slugify();

    @Value("${slipo-toolkit.triplegeo.ml-mappings-folder:triplegeo-ml-mappings}")
    private String tripleGeoMappingFolder;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IAuthenticationFacade authenticationFacade;

    @Autowired
    private IProcessValidationService processValidationService;

    @Autowired
    @Qualifier("defaultWebFileNamingStrategry")
    protected DefaultWebFileNamingStrategry fileNamingStrategy;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private ProcessRepository processRepository;

    @Autowired
    private ProcessOperator processOperator;

    @Autowired
    private ImportService importService;

    private Integer currentUserId() {
        return authenticationFacade.getCurrentUserId();
    }

    private Locale currentUserLocale() {
        return authenticationFacade.getCurrentUserLocale();
    }

    private boolean isAdmin() {
        return this.authenticationFacade.isAdmin();
    }

    private ApplicationException wrapAndFormatException(Exception ex, ErrorCode errorCode, String message) {
        return ApplicationException.fromMessage(ex, errorCode, message).withFormattedMessage(messageSource, currentUserLocale());
    }

    private ApplicationException accessDenied() {
        return ApplicationException.fromPattern(BasicErrorCode.AUTHORIZATION_FAILED).withFormattedMessage(messageSource, currentUserLocale());
    }

    @Override
    public QueryResultPage<ProcessRecord> find(ProcessQuery query, PageRequest pageRequest) {
        query.setTemplate(false);
        query.setCreatedBy(isAdmin() ? null : currentUserId());
        if (!this.authenticationFacade.isAdmin()) {
            query.setTaskType(EnumProcessTaskType.DATA_INTEGRATION);
        }

        final QueryResultPage<ProcessRecord> result = processRepository.query(query, pageRequest);
        updateProcessRecords(result.getItems());

        return result;
    }

    @Override
    public QueryResultPage<ProcessRecord> findTemplates(ProcessQuery query, PageRequest pageRequest) {
        query.setTaskType(EnumProcessTaskType.DATA_INTEGRATION);
        query.setTemplate(true);
        query.setCreatedBy(isAdmin() ? null : currentUserId());

        return processRepository.query(query, pageRequest);
    }

    @Override
    public QueryResultPage<ProcessExecutionRecord> find(ProcessExecutionQuery query, PageRequest pageRequest) {
        query.setCreatedBy(isAdmin() ? null : currentUserId());

        final QueryResultPage<ProcessExecutionRecord> result = processRepository.queryExecutions(query, pageRequest);
        updateProcessExecutionRecords(result.getItems());

        return result;
    }

    @Override
    public QueryResultPage<ProcessExecutionApiRecord> find(ApiCallQuery query, PageRequest pageRequest) {
        final QueryResultPage<ProcessExecutionApiRecord> result = processRepository.queryExecutions(query, pageRequest);
        updateProcessExecutionApiCallRecords(result.getItems());

        return result;
    }

    private void refreshCatalogResources(ProcessRecord record) {
        record
            .getDefinition()
            .resources()
            .stream()
            .filter(r->r.getInputType() == EnumInputType.CATALOG)
            .map(r-> (CatalogResource) r)
            .forEach(r1-> {
                ResourceRecord r2 = resourceRepository.findOne(r1.getId(), r1.getVersion());
                if (r2 != null) {
                    r1.setBoundingBox(r2.getBoundingBox());
                    r1.setTableName(r2.getTableName());
                }
            });
    }

    @Override
    public ProcessExecutionRecordView getProcessExecution(long id, long version) throws ProcessExecutionNotFoundException{

        ProcessRecord processRecord = processRepository.findOne(id, version, false);

        checkProcessExecutionAccess(processRecord);

        ProcessExecutionRecord executionRecord = processRepository.getExecutionCompactView(id, version);

        // For catalog resources update bounding box and table name values
        refreshCatalogResources(processRecord);

        return new ProcessExecutionRecordView(processRecord, executionRecord);
    }

    @Override
    public ProcessExecutionRecordView getProcessExecution(long id, long version, long executionId)
        throws ProcessExecutionNotFoundException{

        ProcessRecord processRecord = processRepository.findOne(id, version, false);
        ProcessExecutionRecord executionRecord = processRepository.getExecutionCompactView(id, version);
        if (processRecord == null ||
            executionRecord == null ||
            executionRecord.getProcess().getId() != id ||
            executionRecord.getProcess().getVersion() != version) {
            throw ProcessExecutionNotFoundException.forExecution(executionId);
        }
        checkProcessExecutionAccess(processRecord);

        // For catalog resources update bounding box and table name values
        refreshCatalogResources(processRecord);

        return new ProcessExecutionRecordView(processRecord, executionRecord);
    }

    @Override
    public ProcessRecord create(ProcessDefinition definition, EnumProcessTaskType taskType) throws InvalidProcessDefinitionException {
        return create(definition, taskType, false, currentUserId());
    }

    @Override
    public ProcessRecord create(ProcessDefinition definition, EnumProcessTaskType taskType, int userId) throws InvalidProcessDefinitionException {
        return create(definition, taskType, false, userId);
    }

    @Override
    public ProcessRecord create(ProcessDefinition definition, boolean isTemplate) throws InvalidProcessDefinitionException {
        return create(definition, EnumProcessTaskType.DATA_INTEGRATION, isTemplate, currentUserId());
    }

    private ProcessRecord create(
        ProcessDefinition definition, EnumProcessTaskType taskType, boolean isTemplate, int userId
    ) throws InvalidProcessDefinitionException, ApplicationException {
        // Ignore EXPORT_MAP tasks generated by the system
        if ((taskType != EnumProcessTaskType.EXPORT_MAP) &&
            (!this.authenticationFacade.hasAnyRole(EnumRole.ADMIN, EnumRole.AUTHOR))) {
            throw this.accessDenied();
        }
        try {
            processValidationService.validate(null, definition, isTemplate, userId);

            // Set default values for auto configuration
            this.setAutoConfigurationDefaults(null, definition);

            return  processRepository.create(definition, userId, taskType, isTemplate);
        } catch(InvalidProcessDefinitionException ex) {
            throw ex;
        } catch (ApplicationException ex) {
            throw ex.withFormattedMessage(messageSource, currentUserLocale() == null ? Locale.ENGLISH : currentUserLocale());
        } catch (Exception ex) {
            throw wrapAndFormatException(ex, ProcessErrorCode.UNKNOWN, "Failed to create process");
        }
    }

    @Override
    public ProcessRecord update(
        long id, ProcessDefinition definition, boolean isTemplate
    ) throws InvalidProcessDefinitionException, ApplicationException {
        if (!this.authenticationFacade.hasAnyRole(EnumRole.ADMIN, EnumRole.AUTHOR)) {
            throw this.accessDenied();
        }
        try {
            processValidationService.validate(id, definition, isTemplate);

            ProcessRecord record = processRepository.findOne(id);
            checkProcessAccess(record);

            // Set default values for auto configuration
            this.setAutoConfigurationDefaults(record, definition);

            return processRepository.update(id, definition, currentUserId());
        } catch (InvalidProcessDefinitionException ex) {
            throw ex;
        } catch (ApplicationException ex) {
            throw ex.withFormattedMessage(messageSource, currentUserLocale());
        } catch (Exception ex) {
            throw wrapAndFormatException(ex, ProcessErrorCode.UNKNOWN, "Failed to update process");
        }
    }

    @Override
    public ProcessRecord findOne(long id) {
        ProcessRecord record = processRepository.findOne(id);
        checkProcessAccess(record);
        return record;
    }

    @Override
    public ProcessRecord findOne(long id, long version) {
        ProcessRecord record = processRepository.findOne(id, version);
        checkProcessAccess(record);
        return record;
    }

    @Override
    public List<ProcessExecutionRecord> findExecutions(long id, long version) {
        ProcessRecord record = processRepository.findOne(id, version, true);
        checkProcessAccess(record);
        return record == null ? Collections.emptyList() : record.getExecutions();
    }

    @Override
    public ProcessExecutionRecord start(
        long id, long version, EnumProcessTaskType task
    ) throws ProcessNotFoundException, ProcessExecutionStartException, IOException {
        return this.start(id, version, task, this.currentUserId());
    }

    @Override
    public ProcessExecutionRecord start(
        long id, long version, EnumProcessTaskType task, Integer userId
    ) throws ProcessNotFoundException, ProcessExecutionStartException, IOException {

        final ProcessRecord processRecord = this.processRepository.findOne(id, version);

        if (processRecord == null) {
            throw new ProcessNotFoundException(id, version);
        }

        // Resolve authorization based on requested task type
        if (!this.authenticationFacade.isAdmin()) {
            // Check record owner
            if (!userId.equals(processRecord.getCreatedBy().getId())) {
                throw this.accessDenied();
            }
            switch (task) {
                case API:
                    // Already authenticated using a valid application key
                    break;
                case EXPORT_MAP:
                    // Started by the system
                    break;
                case REGISTRATION:
                case EXPORT:
                    // Registration/Export tasks can only be initiated by authors
                    if (!this.authenticationFacade.hasRole(EnumRole.AUTHOR)) {
                        throw this.accessDenied();
                    }
                    break;
                case DATA_INTEGRATION:
                    // Data integration tasks can be initiated by any user
                    break;
                default:
                    // When no specific task type is given, allow only data integration
                    // tasks
                    if (processRecord.getTaskType() != EnumProcessTaskType.DATA_INTEGRATION) {
                        throw this.accessDenied();
                    }
                    break;
            }
        }

        final ProcessExecutionRecord record = this.processOperator.poll(id, version);
        if ((record == null) ||
            (record.getStatus() == EnumProcessExecutionStatus.FAILED) ||
            (record.getStatus() == EnumProcessExecutionStatus.STOPPED)) {
            return this.processOperator.start(id, version, userId);
        }
        if ((record == null) || (record.getStatus() == EnumProcessExecutionStatus.COMPLETED)) {
            throw ApplicationException.fromMessage("Process has already been executed");
        }
        throw ApplicationException.fromMessage("Process failed to start");
    }

    @Override
    public void stop(long id, long version) throws ProcessNotFoundException, ProcessExecutionStopException {
        final ProcessRecord processRecord = this.processRepository.findOne(id, version);

        if (processRecord == null) {
            throw new ProcessNotFoundException(id, version);
        }

        checkProcessAccess(processRecord);

        final ProcessExecutionRecord record = this.processOperator.poll(id, version);
        if ((record != null) && (record.getStatus() == EnumProcessExecutionStatus.RUNNING)) {
            this.processOperator.stop(id, version);
        } else {
            throw ApplicationException.fromMessage("Process is not running");
        }
    }

    @Override
    public void exportMap(
        long id, long version, long executionId
    ) throws ProcessNotFoundException, ProcessExecutionNotFoundException, Exception {
        final ProcessRecord processRecord = this.processRepository.findOne(id, version);

        if (processRecord == null) {
            throw new ProcessNotFoundException(id, version);
        }

        checkProcessAccess(processRecord);

        if ((processRecord.getTaskType() != EnumProcessTaskType.DATA_INTEGRATION) &&
            (processRecord.getTaskType() != EnumProcessTaskType.REGISTRATION)) {
            throw this.accessDenied();
        }

        final ProcessExecutionRecord executionRecord = this.processRepository.findExecution(executionId);
        if ((executionRecord == null) ||
            (executionRecord.getProcess().getId() != id) ||
            (executionRecord.getProcess().getVersion() != version)) {
            throw ProcessExecutionNotFoundException.forExecution(executionId);
        } else {
            this.exportMap(executionRecord);
        }
    }

    @Override
    public void exportMap(ProcessExecutionRecord execution) throws Exception {
        this.importService.schedule(this.currentUserId(), execution.getId());
    }

    @Override
    public File getProcessExecutionFile(long id, long version, long executionId, long fileId)
        throws ProcessNotFoundException, ProcessExecutionNotFoundException {

        ProcessRecord processRecord = processRepository.findOne(id, version, false);
        ProcessExecutionRecord executionRecord = processRepository.findExecution(executionId);
        if (processRecord == null ||
            executionRecord == null ||
            executionRecord.getProcess().getId() != id ||
            executionRecord.getProcess().getVersion() != version) {

            throw new ProcessNotFoundException(id, version);
        }

        checkProcessExecutionAccess(processRecord);

        final Optional<ProcessExecutionStepFileRecord> result = executionRecord
            .getSteps()
            .stream()
            .flatMap(s -> s.getFiles().stream())
            .filter(f -> f.getId() == fileId)
            .findFirst();

        if (!result.isPresent()) {
            throw ProcessExecutionNotFoundException.forExecution(executionId);
        }

        final String filename = result.get().getFilePath();
        Path path;
        try {
            path = fileNamingStrategy.resolveExecutionPath(filename);
        } catch (URISyntaxException e) {
            return null;
        }

        return path.toFile();
    }

    @Override
    public Object getProcessExecutionKpiData(long id, long version, long executionId, long fileId)
        throws ApplicationException, ProcessExecutionNotFoundException {

        ProcessRecord processRecord = processRepository.findOne(id, version, false);
        ProcessExecutionRecord executionRecord = processRepository.findExecution(executionId);
        if (processRecord == null ||
            executionRecord == null ||
            executionRecord.getProcess().getId() != id ||
            executionRecord.getProcess().getVersion() != version) {
            throw ProcessExecutionNotFoundException.forExecution(executionId);
        }

        checkProcessExecutionAccess(processRecord);

        final Optional<Pair<ProcessExecutionStepRecord, ProcessExecutionStepFileRecord>> result = executionRecord
            .getSteps()
            .stream()
            .flatMap(s -> {
                return s.getFiles()
                    .stream()
                    .map(f-> Pair.<ProcessExecutionStepRecord, ProcessExecutionStepFileRecord>of(s, f));
            })
            .filter(f -> f.getRight().getId() == fileId)
            .findFirst();

        if (!result.isPresent()) {
            throw ApplicationException.fromMessage(BasicErrorCode.NO_RESULT, "File was not found");
        }
        final ProcessExecutionStepRecord stepRecord = result.get().getLeft();
        final ProcessExecutionStepFileRecord fileRecord = result.get().getRight();
        if (fileRecord.getType() != EnumStepFile.KPI) {
            throw ApplicationException.fromMessage(BasicErrorCode.NOT_SUPPORTED, "File type is not supported");
        }

        final String filename = fileRecord.getFilePath();
        Path path;
        try {
            path = fileNamingStrategy.resolveExecutionPath(filename);
        } catch (URISyntaxException e) {
            throw ApplicationException.fromMessage(BasicErrorCode.NO_RESULT, "File was not found");
        }
        final File file = path.toFile();

        if (!file.exists()) {
            throw ApplicationException.fromMessage(FileSystemErrorCode.PATH_NOT_FOUND, "File was not found");
        }

        try {
            switch (stepRecord.getTool()) {
                case TRIPLEGEO:
                case FAGI:
                    JsonNode node = objectMapper.readTree(file);
                    return node;
                default:
                    Resource resource = new FileSystemResource(file);
                    Properties props = PropertiesLoaderUtils.loadProperties(resource);
                    return props;
            }
        } catch (JsonParseException ex) {
            String message = "Failed to parse JSON file";
            logger.error(message,ex);
            return ApplicationException.fromMessage(BasicErrorCode.UNKNOWN, message);
        } catch (IOException ex) {
            String message = "Failed to access file";
            logger.error(message,ex);
            return ApplicationException.fromMessage(BasicErrorCode.IO_ERROR, message);
        }
    }

    // TODO: Store process status in database redundant field to avoid querying RPC-server
    // on every request

    private void updateProcessRecords(List<ProcessRecord> records) {
        try {
            final List<ProcessIdentifier> running = this.processOperator.list();

            // Update most recent versions
            records.stream()
            .forEach(p -> {
                final Optional<ProcessIdentifier> identifier = running.stream()
                    .filter(e -> e.getId() == p.getId() && e.getVersion() == p.getVersion())
                    .findFirst();

                p.setRunning(identifier.isPresent());
            });

            // Update all versions for every process
            records.stream()
                .flatMap(p -> p.getRevisions().stream())
                .forEach(p -> {
                    final Optional<ProcessIdentifier> identifier = running.stream()
                        .filter(e -> e.getId() == p.getId() && e.getVersion() == p.getVersion())
                        .findFirst();

                    p.setRunning(identifier.isPresent());
                });
        } catch(Exception ex) {
            // Ignore
        }
    }

    private void updateProcessExecutionRecords(List<ProcessExecutionRecord> records) {
        try {
            final List<ProcessIdentifier> running = this.processOperator.list();

            records.stream()
            .forEach(e -> {
                final Optional<ProcessIdentifier> identifier = running.stream()
                    .filter(r -> r.getId() == e.getProcess().getId() && r.getVersion() == e.getProcess().getVersion())
                    .findFirst();

                e.setRunning(identifier.isPresent());
            });
        } catch(Exception ex) {
            // Ignore
        }
    }

    private void updateProcessExecutionApiCallRecords(List<ProcessExecutionApiRecord> records) {
        try {
            final List<ProcessIdentifier> running = this.processOperator.list();

            records.stream()
            .forEach(e -> {
                final Optional<ProcessIdentifier> identifier = running.stream()
                    .filter(r -> r.getId() == e.getProcessId() && r.getVersion() == e.getProcessVersion())
                    .findFirst();

                e.setRunning(identifier.isPresent());
            });
        } catch(Exception ex) {
            // Ignore
        }
    }

    private void checkProcessAccess(ProcessRecord record) {
        if ((!this.authenticationFacade.isAdmin()) && (!this.currentUserId().equals(record.getCreatedBy().getId()))) {
            throw this.accessDenied();
        }
        if ((!this.authenticationFacade.isAdmin()) && (record.getTaskType() != EnumProcessTaskType.DATA_INTEGRATION)) {
            throw this.accessDenied();
        }
    }

    private void checkProcessExecutionAccess(ProcessRecord record) {
        if ((!this.authenticationFacade.isAdmin()) && (!this.currentUserId().equals(record.getCreatedBy().getId()))) {
            throw this.accessDenied();
        }
    }

    private void setAutoConfigurationDefaults(ProcessRecord record, ProcessDefinition definition) throws IOException {
        long version = record == null ? 1 : record.getVersion() + 1;

        for(Step step: definition.steps()) {
            switch (step.tool()) {
                case TRIPLEGEO:
                    final TriplegeoConfiguration config = (TriplegeoConfiguration) step.getConfigurationUnsafe();

                    if (config.getLevel() == TriplegeoConfiguration.EnumLevel.AUTO) {
                        // Create unique name for files and feature source
                        final String name = slugify.slugify(
                            String.format(
                                "%s-%s-%d", definition.name(), step.name(), version
                            )
                        );

                        // Set value for feature source
                        config.setFeatureSource(name);

                        // Create custom mapping file
                        config.setMappingSpec(tripleGeoMappingsToFile(name, config.getUserMappings()));

                        // Set delimiter and quote
                        final DataSource source = step.sources().isEmpty() ? null : step.sources().get(0);
                        if (source != null) {
                            final FileSystemDataSource fileSource =
                                source.getType() == EnumDataSourceType.FILESYSTEM ? (FileSystemDataSource) source : null;

                            if (fileSource != null) {
                                Path path = this.fileNamingStrategy.resolvePath(this.currentUserId(), fileSource.getPath());

                                config.setDelimiter(CsvUtils.detectDelimiter(path));
                                config.setQuote(CsvUtils.detectQuote(path, config.getDelimiter().charAt(0)));
                            }
                        }

                        // Create empty classification file
                        config.setClassificationSpec(createTripleGeoClassificationFile(name));

                        // Set id, longitude and latitude attributes
                        TriplegeoFieldMapping id = config.getUserMappings().stream()
                            .filter(m -> m.getPredicate().equals(Predicates.ID.toUpperCase()))
                            .findFirst()
                            .orElse(null);
                        if (id != null) {
                            config.setAttrKey(id.getField());
                        }

                        TriplegeoFieldMapping lon = config.getUserMappings().stream()
                            .filter(m -> m.getPredicate().equals(Predicates.LONGITUDE.toUpperCase()))
                            .findFirst()
                            .orElse(null);
                        if (lon != null) {
                            config.setAttrX(lon.getField());
                        }

                        TriplegeoFieldMapping lat = config.getUserMappings().stream()
                            .filter(m -> m.getPredicate().equals(Predicates.LATITUDE.toUpperCase()))
                            .findFirst()
                            .orElse(null);
                        if (lat != null) {
                            config.setAttrY(lat.getField());
                        }
                    }
                    break;
                default:
                    // Do nothing
            }
        };
    }

    @Override
    public String tripleGeoMappingsAsText(
        List<TriplegeoFieldMapping> mappings
    )throws IOException {
        Map<String, Object> predicates = this.createTripleGeoMappings(mappings);

        try (
            StringWriter writer = new StringWriter();
        ) {
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            options.setPrettyFlow(true);

            Yaml yaml = new Yaml(options);
            yaml.dump(predicates, writer);

            return writer.toString();
        }
    }

    @Override
    public void log(ApplicationKeyRecord applicationKey, ProcessExecutionRecord execution, EnumOperation operation) {
        this.processRepository.log(applicationKey.getId(), execution.getId(), operation);
    }

    private String tripleGeoMappingsToFile(
        String name, List<TriplegeoFieldMapping> mappings
    ) throws IOException {
        Map<String, Object> predicates = this.createTripleGeoMappings(mappings);

        // Create folder for auto-generated mapping files
        final Path targetDir = this.createTripleGeoFolder();
        final Path targetFile = Paths.get(targetDir.toString(), name + ".yml");

        try (
            FileWriter writer = new FileWriter(targetFile.toString());
        ) {
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            options.setPrettyFlow(true);

            Yaml yaml = new Yaml(options);
            yaml.dump(predicates, writer);

            return Paths.get(tripleGeoMappingFolder, name + ".yml").toString();
        }
    }

    private Map<String, Object> createTripleGeoMappings(List<TriplegeoFieldMapping> mappings) {
        Map<String, Object> predicates = new LinkedHashMap<String, Object>();
        Map<String, String> items = null;

        // Get id, longitude and latitude mappings
        TriplegeoFieldMapping id = mappings.stream()
            .filter(m -> m.getPredicate().equals(Predicates.ID.toUpperCase()))
            .findFirst()
            .orElse(null);

        TriplegeoFieldMapping lon = mappings.stream()
            .filter(m -> m.getPredicate().equals(Predicates.LONGITUDE.toUpperCase()))
            .findFirst()
            .orElse(null);

        TriplegeoFieldMapping lat = mappings.stream()
            .filter(m -> m.getPredicate().equals(Predicates.LATITUDE.toUpperCase()))
            .findFirst()
            .orElse(null);

        // Add default mappings

        // URI
        if (id != null) {
            items = new LinkedHashMap<String, String>();
            items.put("entity", "uri");
            items.put("generateWith", String.format("getUUID(DATA_SOURCE,%s)", id.getField()));
            predicates.put("URI", items);

            items = new LinkedHashMap<String, String>();
            items.put("partOf", "sourceInfo");
            items.put("entity", "source");
            items.put("predicate", Predicates.ID);
            predicates.put(id.getField(), items);
        }

        // Data Source
        items = new LinkedHashMap<String, String>();
        items.put("partOf", "sourceInfo");
        items.put("entity", "source");
        items.put("predicate", "slipo:sourceRef");
        items.put("generateWith", "getDataSource");
        predicates.put("DATA_SOURCE", items);

        // Longitude
        if (lon != null) {
            items = new LinkedHashMap<String, String>();
            items.put("entity", "uri");
            items.put("predicate", lon.getPredicate().toLowerCase());
            items.put("datatype", "float");
            predicates.put(lon.getField(), items);
        }

        // Latitude
        if (lat != null) {
            items = new LinkedHashMap<String, String>();
            items.put("entity", "uri");
            items.put("predicate", lat.getPredicate().toLowerCase());
            items.put("datatype", "float");
            predicates.put(lat.getField(), items);
        }

        // Add other mappings
        for (TriplegeoFieldMapping m : mappings) {
            items = new LinkedHashMap<String, String>();

            switch (m.getPredicate().toLowerCase()) {
                case Predicates.ACCURACY:
                    items.put("instanceOf", "accuracy");
                    switch (m.getType()) {
                        case Predicates.Types.ACCURACY_POSITIONAL:
                            items.put("entity", "position_accuracy");
                            break;
                        case Predicates.Types.ACCURACY_GEOCODING:
                            items.put("entity", "geocode_accuracy");
                            break;
                    }
                    items.put("predicate", m.getPredicate().toLowerCase());
                    items.put("datatype", "integer");
                    break;

                case Predicates.ADDRESS_STREET:
                case Predicates.ADDRESS_NUMBER:
                case Predicates.ADDRESS_POSTAL_CODE:
                case Predicates.ADDRESS_REGION:
                case Predicates.ADDRESS_LOCALITY:
                case Predicates.ADDRESS_COUNTRY:
                    items.put("partOf", "address");
                    items.put("entity", "address");
                    items.put("predicate", m.getPredicate().toLowerCase());
                    break;

                case Predicates.DESCRIPTION:
                    items.put("entity", "description");
                    items.put("predicate", m.getPredicate().toLowerCase());
                    break;

                case Predicates.EMAIL:
                    items.put("instanceOf", "contact");
                    items.put("entity", "email");
                    items.put("predicate", m.getPredicate().toLowerCase());
                    items.put("type", "email");
                    break;

                case Predicates.FAX:
                    items.put("instanceOf", "contact");
                    items.put("entity", "fax");
                    items.put("predicate", m.getPredicate().toLowerCase());
                    items.put("type", "fax");
                    break;

                case Predicates.HOMEPAGE:
                    items.put("entity", "homepage");
                    items.put("predicate", m.getPredicate().toLowerCase());
                    items.put("datatype","uri");
                    break;

                case Predicates.LAST_UPDATED:
                    items.put("entity", "lastUpdated");
                    items.put("predicate", m.getPredicate().toLowerCase());
                    items.put("datatype", "datetime");
                    break;

                case Predicates.NAME:
                    items.put("instanceOf", "name");
                    switch (m.getType()) {
                        case Predicates.Types.NAME_BRAND:
                            items.put("entity", "brandname");
                            break;
                        case Predicates.Types.NAME_COMPANY:
                            items.put("entity", "companyname");
                            break;
                        case Predicates.Types.NAME_TRANSLIT:
                            items.put("entity", "translit_name");
                            items.put("generateWith","getTransliteration(name)");
                            break;
                        default:
                            items.put("entity", "name");
                            break;
                    }
                    items.put("predicate", m.getPredicate().toLowerCase());
                    break;

                case Predicates.OPENING_HOURS:
                    items.put("partOf", "timeSlot");
                    items.put("entity", "openingHours");
                    items.put("predicate", m.getPredicate().toLowerCase());
                    break;

                case Predicates.OTHER_LINK:
                    items.put("entity", "wikipedia");
                    items.put("predicate", m.getPredicate().toLowerCase());
                    items.put("datatype","uri");
                    break;

                case Predicates.PHONE:
                    items.put("instanceOf", "contact");
                    items.put("entity", "phone");
                    items.put("predicate", m.getPredicate().toLowerCase());
                    items.put("type", "phone");
                    break;

                case Predicates.URL:
                    items.put("partOf", "media");
                    items.put("entity", "media");
                    items.put("predicate", m.getPredicate().toLowerCase());
                    items.put("datatype","uri");
                    break;
            }

            if (!items.isEmpty()) {
                if (!StringUtils.isBlank(m.getType())) {
                    items.put("type", m.getType());
                }
                if (!StringUtils.isBlank(m.getLanguage())) {
                    items.put("language", m.getLanguage());
                }
                predicates.put(m.getField(), items);
            }
        }

        return predicates;
    }

    private String createTripleGeoClassificationFile(String name) throws IOException {
        // Create folder for auto-generated mapping files
        final Path targetDir = this.createTripleGeoFolder();
        final Path targetFile = Paths.get(targetDir.toString(), name + ".csv");

        String content = "\"category_id\",\"category\",\"subcategory_id\",\"subcategory\"\n";

        try (
            BufferedWriter writer = new BufferedWriter(new FileWriter(targetFile.toString()));
        ) {
            writer.write(content);
        }

        return Paths.get(tripleGeoMappingFolder, name + ".csv").toString();
    }

    private Path createTripleGeoFolder() throws IOException {
        final Path userDir = fileNamingStrategy.getUserDir(currentUserId(), true);
        final Path targetDir = Paths.get(userDir.toString(), tripleGeoMappingFolder);

        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir);
        }

        return targetDir;
    }

}
