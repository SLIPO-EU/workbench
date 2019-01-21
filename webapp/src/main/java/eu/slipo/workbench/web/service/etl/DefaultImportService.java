package eu.slipo.workbench.web.service.etl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.sql.DataSource;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.CaseFormat;

import eu.slipo.workbench.common.model.etl.EnumMapExportStatus;
import eu.slipo.workbench.common.model.etl.MapExportTask;
import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.common.model.poi.EnumTool;
import eu.slipo.workbench.common.model.process.CatalogResource;
import eu.slipo.workbench.common.model.process.EnumInputType;
import eu.slipo.workbench.common.model.process.EnumProcessExecutionStatus;
import eu.slipo.workbench.common.model.process.EnumProcessTaskType;
import eu.slipo.workbench.common.model.process.EnumStepFile;
import eu.slipo.workbench.common.model.process.ProcessDefinitionBuilder;
import eu.slipo.workbench.common.model.process.ProcessDefinitionBuilderFactory;
import eu.slipo.workbench.common.model.process.ProcessExecutionRecord;
import eu.slipo.workbench.common.model.process.ProcessExecutionStepFileRecord;
import eu.slipo.workbench.common.model.process.ProcessExecutionStepRecord;
import eu.slipo.workbench.common.model.process.ProcessRecord;
import eu.slipo.workbench.common.model.process.Step.Input;
import eu.slipo.workbench.common.model.resource.ResourceRecord;
import eu.slipo.workbench.common.model.tool.ReverseTriplegeoConfiguration;
import eu.slipo.workbench.common.model.tool.output.EnumDeerOutputPart;
import eu.slipo.workbench.common.model.tool.output.EnumFagiOutputPart;
import eu.slipo.workbench.common.model.tool.output.EnumReverseTriplegeoOutputPart;
import eu.slipo.workbench.common.model.tool.output.EnumTriplegeoOutputPart;
import eu.slipo.workbench.common.repository.ProcessRepository;
import eu.slipo.workbench.common.repository.ResourceRepository;
import eu.slipo.workbench.web.repository.MapExportTaskRepository;
import eu.slipo.workbench.web.service.DefaultWebFileNamingStrategry;
import eu.slipo.workbench.web.service.ProcessService;

@Service
public class DefaultImportService implements ImportService, InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(DefaultImportService.class);

    private static final int TRANSFORM_EXECUTION_TIMEOUT = 60 * 60 * 1000;

    private static final int POLL_INTERVAL = 10000;

    private static final String FAGI_LINKS_TABLE_SUFFIX = "links";

    private static final String FAGI_ACTIONS_TABLE_SUFFIX = "actions";

    private static final String DEFAULT_REVERSE_TRIPLEGEO_PROFILE = "SLIPO_default";

    @Value("${vector-data.default.schema:spatial}")
    private String defaultGeometrySchema;

    @Value("${vector-data.default.id-column:id}")
    private String defaultIdColumn;

    @Value("${vector-data.default.geometry-column:the_geom}")
    private String defaultGeometryColumn;

    @Value("${vector-data.default.geometry-simple-column:the_geom_simple}")
    private String defaultGeometrySimpleColumn;

    @Value("${fagi-data.default.schema:fagi}")
    private String defaultFagiSchema;

    @Value("${map-export.use-copy:false}")
    private boolean useCopy;

    @Value("${map-export.quote:\"}")
    private String defaultQuote;

    @Value("${map-export.delimiter:;}")
    private String defaultDelimiter;

    @Value("${map-export.batch-size:100}")
    private int batchSize;

    @Autowired
    @Qualifier("defaultWebFileNamingStrategry")
    protected DefaultWebFileNamingStrategry fileNamingStrategy;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private ProcessRepository processRepository;

    @Autowired
    private MapExportTaskRepository taskRepository;

    @Autowired
    private ProcessService processService;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProcessDefinitionBuilderFactory processDefinitionBuilderFactory;

    private JdbcTemplate jdbcTemplate;

    private MapExportTask activeTask = null;

    @Override
    public void afterPropertiesSet() throws Exception {
        jdbcTemplate = new JdbcTemplate(dataSource);

        this.taskRepository.resetRunningTasks();
    }

    @Override
    public void schedule(int userId, long executionId) throws Exception {
        this.taskRepository.schedule(userId, executionId);
    }

    /**
     * Checks for pending map generation tasks and initializes the execution of the least
     * recent one.
     */
    @Scheduled(fixedRate = 30000L, initialDelay = 5000L)
    public void checkScheduledTasks() {
        try {
            synchronized (this) {
                // Allow only a single task to run
                if (activeTask != null) {
                    return;
                }
                // Start the least recent pending task
                List<MapExportTask> tasks = this.taskRepository.getPendingTasks();
                if (!tasks.isEmpty()) {
                    activeTask = tasks.get(0);
                    this.executeTask(activeTask);
                }
            }
        } catch (Exception ex) {
            logger.error("Failed to process pending map generation tasks", ex);
        }
    }

    private void executeTask(MapExportTask task) {
        try {
            this.taskRepository.setStatus(task.getId(), EnumMapExportStatus.RUNNING);

            // Find workflow process and execution records
            final ProcessExecutionRecord execution = processRepository.findExecution(task.getWorkflow().getId());
            final ProcessRecord process = processRepository.findOne(execution.getProcess().getId(), execution.getProcess().getVersion());

            // Check if workflow execution is completed
            switch (execution.getStatus()) {
                case COMPLETED:
                    // Continue
                    break;
                case FAILED:
                    // Mark export task as failed too
                    this.taskRepository.setStatus(task.getId(), EnumMapExportStatus.FAILED);
                    this.activeTask = null;
                    return;
                case STOPPED:
                    // User must initialize a map export task manually if a process
                    // execution has been stopped
                    this.taskRepository.remove(task.getId());
                    this.activeTask = null;
                    return;
                default:
                    this.activeTask = null;
                    return;
            }

            // Check transform execution
            ProcessExecutionRecord transform = this.getTransformExecution(task, process, execution);

            // Wait for the transform process execution to complete
            transform = this.waitForExecution(task, transform);

            // Check transform execution status
            switch (transform.getStatus()) {
                case COMPLETED:
                    // Do nothing
                    break;
                case RUNNING:
                    // Task will be resumed by the scheduler
                    logger.error("Failed to execute map export task for execution #{}. RDF transform process has timeout", execution.getId());
                    activeTask = null;
                    break;
                case STOPPED:
                    // Task will be resumed by the scheduler
                    logger.error("Failed to execute map export task for execution #{}. RDF transform process was stopped", execution.getId());
                    activeTask = null;
                    break;
                default:
                    // User must start a new export task
                    logger.error("Failed to execute map export task for execution #{}. RDF transform process has failed", execution.getId());
                    this.taskRepository.setStatus(task.getId(), EnumMapExportStatus.FAILED);
                    activeTask = null;
                    break;
            }

            // Import CSV files to PostgreSQL and update tables
            this.importTransformedFiles(process, execution, transform);

            // Import fusion logs
            List<ProcessExecutionStepRecord> fagiSteps = execution
                .getSteps()
                .stream()
                .filter(s -> s.getTool() == EnumTool.FAGI)
                .collect(Collectors.toList());

            for(ProcessExecutionStepRecord step : fagiSteps) {
                ProcessExecutionStepFileRecord file = step.getOutputFile(EnumStepFile.LOG, EnumFagiOutputPart.LOG.key());
                final Path path = fileNamingStrategy.resolveExecutionPath(file.getFilePath());

                this.importFusionLog(task.getCreatedBy().getId(), execution.getId(), step, defaultFagiSchema, path.toString());
            };

            // Update task status
            this.taskRepository.setStatus(task.getId(), EnumMapExportStatus.COMPLETED);
        } catch (Exception ex) {
            this.taskRepository.setStatus(task.getId(), EnumMapExportStatus.FAILED);
            logger.error("Unknown error has occured during processing map export task", ex);
        } finally {
            // Reset task
            this.activeTask = null;
        }
    }

    private ProcessExecutionRecord getTransformExecution(
        MapExportTask task, ProcessRecord workflowProcess, ProcessExecutionRecord workflowExecution
    ) throws Exception {

        try {
            // Get transform process execution
            ProcessExecutionRecord transform = task.getTransform();

            if (transform == null) {
                // Execution is either not yet created or the database is not updated
                String name = this.getTransformProcessName(workflowExecution);

                // Search if a process was created but database was not updated
                ProcessRecord transformProcess = this.processRepository.findOne(name, task.getCreatedBy().getId());

                if (transformProcess != null) {
                    // Transform process was created but database record failed to update
                    transform = this.processRepository.getExecutionCompactView(transformProcess.getId(), transformProcess.getVersion());
                    if (transform == null) {
                        // Transform process was created but execution never started
                        transform = processService.start(
                            transformProcess.getId(), transformProcess.getVersion(), EnumProcessTaskType.EXPORT_MAP, task.getCreatedBy().getId()
                        );
                    }
                    // Update task record
                    this.taskRepository.setTransformExecution(task.getId(), transform.getId());
                }
            }

            // If either transform process or execution is not found, create a new process and
            // execution
            return (transform == null ? this.createTransformExecution(task, workflowProcess, workflowExecution) : transform);
        } catch (Exception ex) {
            logger.error("Failed to initialize transform process execution for map export task #{}", task.getId());
            throw ex;
        }
    }

    private String getTransformProcessName(ProcessExecutionRecord workflowExecution) {
        return String.format("Map-Export-%d", workflowExecution.getId());
    }

    private ProcessExecutionRecord createTransformExecution(
        MapExportTask task, ProcessRecord workflowProcess, ProcessExecutionRecord workflowExecution
    ) throws Exception {

        // Find all TripeGeo tasks
        List<Pair<ProcessExecutionStepRecord, ProcessExecutionStepFileRecord>> transformedFiles = workflowExecution.getSteps()
            .stream()
            .filter(s -> s.getTool() == EnumTool.TRIPLEGEO)
            .map(s-> {
                ProcessExecutionStepFileRecord file = s.getFiles().stream()
                   .filter(f-> f.getType() == EnumStepFile.OUTPUT &&
                               f.getOutputPartKey().equals(EnumTriplegeoOutputPart.TRANSFORMED.key()))
                   .findFirst().orElse(null);
                if (file != null) {
                    return Pair.<ProcessExecutionStepRecord, ProcessExecutionStepFileRecord>of(s, file);
                }
                return null;
            })
            .filter(p -> p != null)
            .collect(Collectors.toList());

        // Find all catalog resources
        List<CatalogResource> resources = workflowProcess.getDefinition().resources()
            .stream()
            .filter(r -> r.getInputType() == EnumInputType.CATALOG)
            .map(r -> (CatalogResource) r)
            .collect(Collectors.toList());

        // Find all input codes (exclude registration and transformation tasks)
        List<Input> allInput = workflowProcess.getDefinition().steps()
            .stream()
            .filter(s -> s.tool() != EnumTool.REGISTER &&
                         s.tool() != EnumTool.TRIPLEGEO &&
                         s.tool() != EnumTool.REVERSE_TRIPLEGEO)
            .flatMap(s -> s.input().stream())
            .collect(Collectors.toList());

        // Find process output steps (all output steps whose result is not used as input). Ignore LIMES results
        List<Pair<ProcessExecutionStepRecord, ProcessExecutionStepFileRecord>> outputFiles = workflowProcess.getDefinition().steps()
            .stream()
            .filter(s -> {
                Input input = allInput.stream()
                    .filter(i -> i.inputKey().equals(s.outputKey()))
                    .findFirst()
                    .orElse(null);

                return (s.tool() != EnumTool.TRIPLEGEO && s.tool() != EnumTool.LIMES && input == null);
            })
            .map(s-> {
                final String outputPartKey;
                switch (s.tool()) {
                    case FAGI:
                        outputPartKey = EnumFagiOutputPart.FUSED.key();
                        break;
                    case DEER:
                        outputPartKey = EnumDeerOutputPart.ENRICHED.key();
                        break;
                    default:
                        outputPartKey = "";
                        break;
                }

                ProcessExecutionStepRecord executedStep = workflowExecution.getStep(s.key());

                ProcessExecutionStepFileRecord file = executedStep.getFiles().stream()
                   .filter(f-> f.getType() == EnumStepFile.OUTPUT &&
                               f.getOutputPartKey().equals(outputPartKey))
                   .findFirst().orElse(null);

                if (file != null) {
                    return Pair.<ProcessExecutionStepRecord, ProcessExecutionStepFileRecord>of(executedStep, file);
                }
                return null;
            })
            .filter(p -> p != null)
            .collect(Collectors.toList());

        // Build export process
        final int groupId = 1;
        final String procName = this.getTransformProcessName(workflowExecution);
        int resourceKey = 0;

        ProcessDefinitionBuilder builder = processDefinitionBuilderFactory.create(procName).description("Map data export");

        // Create default export configuration
        ReverseTriplegeoConfiguration defaultReverseConfiguration = this.buildReverseTripleGeoConfiguration();

        // Add resources
        for (CatalogResource r : resources) {
            // Check if resource revision instance has already been exported
            ResourceRecord resource = this.resourceRepository.findOne(r.getId(), r.getVersion());
            if (resource.getTableName() != null) {
                continue;
            }

            final String key = Integer.toString(++resourceKey);

            builder.resource(r.getName(), key, r.getResource())
                .export(String.format("resource-%d-%d", r.getId(), r.getVersion()), stepBuilder -> stepBuilder
                    .group(groupId)
                    .input(key)
                    .configuration(defaultReverseConfiguration)
                );
        }

        // Add TripleGeo output
        for (Pair<ProcessExecutionStepRecord, ProcessExecutionStepFileRecord> f : transformedFiles) {
            final String key = Integer.toString(++resourceKey);
            final Path path = fileNamingStrategy.resolveExecutionPath(f.getRight().getFilePath());
            builder.resource(f.getLeft().getName(), key, path, EnumDataFormat.N_TRIPLES)
                .export(String.format("triplegeo-%d-%d", f.getLeft().getKey(), f.getRight().getId()), stepBuilder -> stepBuilder
                    .group(groupId)
                    .input(key)
                    .configuration(defaultReverseConfiguration)
                );
        };

        // Add process output
        for (Pair<ProcessExecutionStepRecord, ProcessExecutionStepFileRecord> f : outputFiles) {
            final String key = Integer.toString(++resourceKey);
            final Path path = fileNamingStrategy.resolveExecutionPath(f.getRight().getFilePath());
            builder.resource(f.getLeft().getName(), key, path, EnumDataFormat.N_TRIPLES)
            .export(String.format("output-%d-%d", f.getLeft().getKey(), f.getRight().getId()), stepBuilder -> stepBuilder
                .group(groupId)
                .input(key)
                .configuration(defaultReverseConfiguration)
            );
        };

        // Save process
        ProcessRecord transformProcess = processService.create(builder.build(), EnumProcessTaskType.EXPORT_MAP, task.getCreatedBy().getId());

        // Start execution
        ProcessExecutionRecord transformExecution = processService.start(
            transformProcess.getId(), transformProcess.getVersion(), EnumProcessTaskType.EXPORT_MAP, task.getCreatedBy().getId()
        );

        // Update task record
        this.taskRepository.setTransformExecution(task.getId(), transformExecution.getId());

        return transformExecution;
    }

    private ReverseTriplegeoConfiguration buildReverseTripleGeoConfiguration() {
        ReverseTriplegeoConfiguration config = new ReverseTriplegeoConfiguration();

        config.setProfile(DEFAULT_REVERSE_TRIPLEGEO_PROFILE);
        config.setOutputFormat(EnumDataFormat.CSV);
        config.setQuote(this.defaultQuote);
        config.setDelimiter(this.defaultDelimiter);
        config.setEncoding("UTF-8");

        return config;
    }

    private ProcessExecutionRecord waitForExecution(MapExportTask task, ProcessExecutionRecord execution) throws Exception {
        long counter = 0;
        ProcessExecutionRecord current = execution;

        try {
            switch (current.getStatus()) {
                case COMPLETED:
                    return current;
                case FAILED:
                    this.taskRepository.setStatus(task.getId(), EnumMapExportStatus.FAILED);
                    return current;
                case STOPPED:
                    current = processService.start(
                        execution.getProcess().getId(),
                        execution.getProcess().getVersion(),
                        EnumProcessTaskType.EXPORT_MAP,
                        task.getCreatedBy().getId());
                    break;
                default:
                    // Do nothing
                    break;
            }

            while (true) {
                if (current.getStatus() == EnumProcessExecutionStatus.RUNNING) {
                    try {
                        Thread.sleep(POLL_INTERVAL);
                    } catch (InterruptedException e) {
                        // Ignore exception
                    }
                    counter += POLL_INTERVAL;

                    current = this.processRepository.getExecutionCompactView(
                        current.getProcess().getId(),
                        current.getProcess().getVersion()
                    );

                    if (counter > TRANSFORM_EXECUTION_TIMEOUT) {
                        break;
                    }
                } else {
                    break;
                }
            }
        } catch (Exception ex) {
            logger.error(String.format("Transform process execution #%d has failed.", execution.getId()), ex);
            throw ex;
        }

        return current;
    }

    private void importTransformedFiles(
        ProcessRecord workflowProcess, ProcessExecutionRecord workflowExecution, ProcessExecutionRecord transformExecution
    ) throws Exception {
        List<ExportFile> exportedFiles = transformExecution.getSteps().stream()
            .filter(s -> s.getTool() == EnumTool.REVERSE_TRIPLEGEO)
            .map(s-> {
                ProcessExecutionStepFileRecord file = s.getOutputFile(EnumStepFile.OUTPUT, EnumReverseTriplegeoOutputPart.TRANSFORMED.key());

                Path path = null;
                try {
                    path = fileNamingStrategy.resolveExecutionPath(file.getFilePath());
                } catch (URISyntaxException e) {
                    logger.error("Failed to get path for step {}", s.getName());
                    return null;
                }

                String[] tokens = s.getName().split("-");
                switch(tokens[0]) {
                    case "resource":
                        return new ResourceExportFile(
                            Long.parseLong(tokens[1]),
                            Long.parseLong(tokens[2]),
                            path
                        );
                    case "triplegeo":
                        return new StepExportFile(
                            Integer.parseInt(tokens[1]),
                            Long.parseLong(tokens[2]),
                            path
                        );
                    case "output":
                        return new StepExportFile(
                            Integer.parseInt(tokens[1]),
                            Long.parseLong(tokens[2]),
                            path
                        );
                    default:
                        return null;
                }
            })
            .filter(f -> f != null)
            .collect(Collectors.toList());

        // Import files and update tables
        for (ExportFile e : exportedFiles) {
            if (e instanceof ResourceExportFile) {
                ResourceExportFile r = (ResourceExportFile) e;
                this.updateResource(workflowProcess, workflowExecution, r.getId(), r.getVersion(), r.getPath());
            }
            if (e instanceof StepExportFile) {
                StepExportFile s = (StepExportFile) e;
                this.updateStep(workflowProcess, workflowExecution, s.getStepKey(), s.getFileId(), s.getPath());
            }
        }
    }

    private void updateResource(
        ProcessRecord process, ProcessExecutionRecord execution, long id, long version, Path path
    ) throws Exception {

        File csv = null;
        try {
            // Check if resource revision instance has already been exported
            ResourceRecord resource = this.resourceRepository.findOne(id, version);
            if (resource.getTableName() != null) {
                return;
            }

            // Get CSV file
            csv = getCsvFileFromZip(path);
            String tableName = UUID.randomUUID().toString();

            // Import CSV data to table
            this.importCsvFile(this.defaultGeometrySchema, tableName, csv.toString(), this.defaultGeometryColumn, this.useCopy);

            // Update resource record
            String updateSql = String.format(
                    "update   resource_revision " +
                    "set      bbox = (select ST_SetSRID(ST_Extent(\"%3$s\"), 4326) from \"%1$s\".\"%2$s\"), " +
                    "         table_name = '%2$s' " +
                    "where    resource_revision.parent = %4$d and resource_revision.version =  %5$d;",
                    defaultGeometrySchema, tableName, defaultGeometryColumn, id, version);

            jdbcTemplate.execute(updateSql);
        } catch(Exception ex) {
            logger.error(String.format("Failed to import data for resource %d-%d (id-version)", id, version), ex);
            throw ex;
        } finally {
            if (csv != null) {
                FileUtils.deleteQuietly(csv.getParentFile());
            }
        }
    }

    private void updateStep(
        ProcessRecord process, ProcessExecutionRecord execution, int stepKey, long fileId, Path path
    ) throws Exception {

        File csv = null;
        try {
            // Get CSV file
            csv = getCsvFileFromZip(path);
            String tableName = UUID.randomUUID().toString();

            // Import CSV data to table
            this.importCsvFile(defaultGeometrySchema, tableName, csv.toString(), defaultGeometryColumn, this.useCopy);

            // Get step file
            ProcessExecutionStepFileRecord file = execution.getSteps()
                .stream()
                .filter(s -> s.getKey() == stepKey)
                .flatMap(s -> s.getFiles().stream())
                .filter(f -> f.getId() == fileId)
                .findFirst()
                .orElse(null);

            if (file == null) {
                return;
            }

            // Update file record
            String updateSql = String.format(
                "update   process_execution_step_file " +
                "set      bbox = (select ST_SetSRID(ST_Extent(\"%3$s\"), 4326) from \"%1$s\".\"%2$s\"), " +
                "         table_name = '%2$s' " +
                "where    process_execution_step_file.id = %4$d;",
                defaultGeometrySchema, tableName, defaultGeometryColumn, fileId);

            jdbcTemplate.execute(updateSql);
        } catch(Exception ex) {
            logger.error(String.format("Failed to import data for step file %d-%d-%d (execution-step key-file id)", execution.getId(), stepKey, fileId), ex);
            throw ex;
        } finally {
            if (csv != null) {
                FileUtils.deleteQuietly(csv.getParentFile());
            }
        }
    }

    private File getCsvFileFromZip(Path zip) throws Exception {
        File destinationDir = null;
        File csv = null;

        destinationDir = new File(createTempDir().toString());
        destinationDir.mkdirs();

        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(new FileInputStream(zip.toString()));
        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
            // Get CSV file only
            if (zipEntry.getName().endsWith(".csv")) {
                csv = this.getFile(destinationDir, zipEntry);
                FileOutputStream fos = new FileOutputStream(csv);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                break;
            }
            zipEntry = zis.getNextEntry();
        }
        zis.closeEntry();
        zis.close();

        return csv;
    }

    private Path createTempDir() {
        return Paths.get("/tmp", UUID.randomUUID().toString());
    }

    public File getFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    private void importCsvFile(
        String schema, String tableName, String fileName, String geomColumn, boolean useCopy
    ) throws Exception {

        // Create table
        String creteTableSql = this.createSpatialTableScript(schema, tableName, fileName);
        jdbcTemplate.execute(creteTableSql);

        // Import data
        if(useCopy) {
            String copySql = String.format(
                    "COPY \"%s\".\"%s\" FROM '%s' CSV HEADER DELIMITER '%s' QUOTE '%s' NULL '' ENCODING '%s';",
                    schema, tableName, fileName, this.defaultDelimiter, this.defaultQuote, "UTF-8");

            jdbcTemplate.execute(copySql);
        } else {
            this.insertRowsFromFile(schema, tableName, fileName);
        }

        // Add simplified geometry column
        this.addSimplifiedGeometryColumn(schema, tableName);

        // Add indexes
        this.addGeometryIndexes(schema, tableName);
    }

    private void insertRowsFromFile(
        String schema, String tableName, String fileName
    ) throws Exception {
        final String insertSql = this.createInsertRowScript(schema, tableName, fileName);
        final List<Object> params = new ArrayList<Object>();

        int size = 0;

        // Helper variables
        CSVFormat format = CSVFormat.DEFAULT
            .withIgnoreEmptyLines()
            .withFirstRecordAsHeader()
            .withDelimiter(this.defaultDelimiter.charAt(0))
            .withQuote(this.defaultQuote.charAt(0))
            .withTrim();

        try (
            Reader reader = Files.newBufferedReader(Paths.get(fileName), Charset.forName("UTF-8"));
            CSVParser parser = new CSVParser(reader, format);
        ) {
            for (final CSVRecord record : parser) {
                size++;

                record.forEach(value -> {
                    params.add(value);
                });

                if (size == this.batchSize) {
                    jdbcTemplate.update(StringUtils.repeat(insertSql, size), params.toArray(new Object[0]));
                    size = 0;
                    params.clear();
                }
            }
            // Insert remaining rows
            if (size != 0) {
                jdbcTemplate.update(StringUtils.repeat(insertSql, size), params.toArray(new Object[0]));
            }
        } catch (Exception ex) {
            logger.error(String.format("Failed to insert rows from file [%s]", fileName), ex);
            throw ex;
        }
    }

    private void addSimplifiedGeometryColumn(String schema, String tableName) {
        String createColumnSql = String.format(
            "ALTER TABLE \"%s\".\"%s\" ADD COLUMN %s geometry",
            schema, tableName, defaultGeometrySimpleColumn);

        jdbcTemplate.execute(createColumnSql);

        String updateColumnSql = String.format(
            "update \"%s\".\"%s\" set %s = ST_ConvexHull(%s);",
            schema, tableName, defaultGeometrySimpleColumn, defaultGeometryColumn);

        jdbcTemplate.execute(updateColumnSql);
    }

    private void addGeometryIndexes(String schema, String tableName) {
        String createIndexGeom = String.format(
            "CREATE INDEX \"%2$s_%3$s\" ON \"%1$s\".\"%2$s\" USING GIST (%3$s);",
            schema, tableName, defaultGeometryColumn);

        jdbcTemplate.execute(createIndexGeom);

        String createIndexGeomSimplified = String.format(
            "CREATE INDEX \"%2$s_%3$s\" ON \"%1$s\".\"%2$s\" USING GIST (%3$s);",
            schema, tableName, defaultGeometrySimpleColumn);

        jdbcTemplate.execute(createIndexGeomSimplified);
    }

    private String createSpatialTableScript(
        String schema, String tableName, String fileName
    ) throws Exception {

        StringBuilder script = new StringBuilder();

        // Helper variables
        CSVFormat format = CSVFormat.DEFAULT
            .withIgnoreEmptyLines()
            .withFirstRecordAsHeader()
            .withDelimiter(';')
            .withQuote('"')
            .withTrim();

        // Scan the CSV file to extract columns names (the first line is assumed to
        // contain the column names)
        try (
            Reader reader = Files.newBufferedReader(Paths.get(fileName), Charset.forName("UTF-8"));
            CSVParser parser = new CSVParser(reader, format);
        ) {

            String[] fields = parser.getHeaderMap().keySet().toArray(new String[] {});
            boolean keyExists = false;
            boolean geometryExists = false;


            // Build SQL script for creating the table
            script.append(String.format("CREATE TABLE \"%s\".\"%s\" (\n", schema, tableName));

            // Append all columns
            int index = 0;
            for (String field : fields) {
                if (field.equals(defaultIdColumn)) {
                    keyExists = true;
                    // Handle primary keys either as string values or integers
                    script.append(String.format("%s varchar PRIMARY KEY", defaultIdColumn));
                } else if (field.equals(defaultGeometryColumn)) {
                    script.append(String.format("%s geometry", defaultGeometryColumn));
                    geometryExists= true;
                } else {
                    script.append(String.format("%s varchar", field));
                }
                script.append(++index == fields.length ? "\n" : ",\n");
            }

            script.append(");\n");

            // Key, longitude and latitude attributes are required
            if (!keyExists) {
                throw new Exception(String.format("Key attribute [%s] was not found", defaultIdColumn));
            }
            if (!geometryExists) {
                throw new Exception(String.format("Geometry attribute [%s] was not found", defaultGeometryColumn));
            }
        } catch (FileNotFoundException fileEx) {
            logger.error(String.format("File [%s] was not found", fileName), fileEx);
            throw fileEx;
        } catch (Exception ex) {
            logger.error(String.format("Failed to parse file [%s]", fileName), ex);
            throw ex;
        }

        return script.toString();
    }

    private String createInsertRowScript(String schema, String tableName, String fileName) {
        List<String> columns = this.getColumns(tableName);
        List<String> values = new ArrayList<String>();

        for (String c : columns) {
            if (c.equals(defaultGeometryColumn)) {
                values.add("ST_GeomFromEWKT(?)");
            } else {
                values.add("?");
            }
        }

        return String.format("insert into \"%1$s\".\"%2$s\" (%3$s) values (%4$s);",
            schema,
            tableName,
            String.join(",", columns),
            String.join(",", values));
    }

    private List<String> getColumns(String tableName) {
        String columnQuery = "select column_name from information_schema.columns where table_name = ?";

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(columnQuery, new Object[] { tableName });
        List<String> columns = rows.stream()
            .map(r -> (String) r.get("column_name"))
            .collect(Collectors.toList());

        return columns;
    }

    private void importFusionLog(
        int userId, long executionId, ProcessExecutionStepRecord step, String schema, String fileName
    ) throws Exception {
        final String tableNamePrefix = UUID.randomUUID().toString();

        // Create table
        String creteTableSql = this.createFusionLogTableScript(schema, tableNamePrefix);
        jdbcTemplate.execute(creteTableSql);

        this.importFagiFusionLog(fileName, schema, tableNamePrefix);
        this.updateFagiStepFile(executionId, step, schema, tableNamePrefix);
    }

    private String createFusionLogTableScript(String schema, String tableNamePrefix) throws Exception {
        String sql = String.format(
            "CREATE TABLE \"%1$s\".\"%2$s\" (\n" +
            "   id bigint,\n" +
            "   left_uri varchar,\n" +
            "   right_uri varchar,\n" +
            "   default_fusion_action varchar,\n" +
            "   validation_action varchar,\n" +
            "   confidence_score varchar,\n" +
            "   CONSTRAINT \"pk_%2$s\" PRIMARY KEY (id));\n" +
            "CREATE TABLE \"%1$s\".\"%3$s\" (\n" +
            "   id bigint,\n" +
            "   parent bigint,\n" +
            "   attribute varchar,\n" +
            "   fusion_action varchar,\n" +
            "   left_value varchar,\n" +
            "   right_value varchar,\n" +
            "   fused_value varchar,\n" +
            "   CONSTRAINT \"pk_%3$s\" PRIMARY KEY (id),\n" +
            "   CONSTRAINT \"fk_%3$s\" FOREIGN KEY (parent)\n" +
            "       REFERENCES \"%1$s\".\"%2$s\" (id) MATCH SIMPLE\n" +
            "       ON UPDATE CASCADE ON DELETE CASCADE);\n",
            schema, tableNamePrefix + "_" + FAGI_LINKS_TABLE_SUFFIX, tableNamePrefix + "_" + FAGI_ACTIONS_TABLE_SUFFIX);

        return sql;
    }

    private void importFagiFusionLog(String fileName, String schema, String tableNamePrefix) throws IOException {
        long logId = 0;
        long actionId = 0;

        try (
            FileInputStream fis = new FileInputStream(fileName);
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            BufferedReader br = new BufferedReader(isr);
        ) {

            while (br.ready()) {
                String line = br.readLine();
                FusionLog log = objectMapper.readValue(line, FusionLog.class);

                final String logSql = String.format(
                    "insert into \"%1$s\".\"%2$s\" values (?, ? ,?, ?, ? ,?);",
                    schema, tableNamePrefix + "_" + FAGI_LINKS_TABLE_SUFFIX);

                final Object[] logParams = {
                        ++logId,
                        log.leftURI, log.rightURI,
                        log.defaultFusionAction, log.validationAction, log.confidenceScore
                    };

                jdbcTemplate.update(logSql, logParams);

                for(FusionAction action : log.actions) {
                    final String actionSql = String.format(
                        "insert into \"%1$s\".\"%2$s\" values (?, ?, ?, ?, ?, ?, ?);",
                        schema, tableNamePrefix + "_" + FAGI_ACTIONS_TABLE_SUFFIX);

                    final Object[] actionParams = {
                        ++actionId, logId,
                        action.attribute, CaseFormat.LOWER_HYPHEN.to(CaseFormat.UPPER_UNDERSCORE, action.fusionAction),
                        action.valueA, action.valueB, action.fusedValue
                    };

                    jdbcTemplate.update(actionSql, actionParams);
                }
            }
        }
    }

    private void updateFagiStepFile(long executionId, ProcessExecutionStepRecord step, String schema, String tableName) {
        ProcessExecutionStepFileRecord file = step.getFiles().stream()
            .filter(f -> f.getType() == EnumStepFile.OUTPUT && f.getOutputPartKey().equals(EnumFagiOutputPart.FUSED.key()))
            .findFirst()
            .orElse(null);

        if (file == null) {
            logger.warn("FAGI output file for execution {} and step {} was not found.", executionId, step.getKey());
            return;
        }

        String updateSql =
            "update   process_execution_step_file " +
            "set      table_name = ? " +
            "where    process_execution_step_file.id = ?;";

        jdbcTemplate.update(updateSql, new Object[] { UUID.fromString(tableName), file.getId() });
    }

}
