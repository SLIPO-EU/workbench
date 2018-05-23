package eu.slipo.workbench.web.service.etl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Reader;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.sql.DataSource;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Service;

import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.common.model.poi.EnumTool;
import eu.slipo.workbench.common.model.process.EnumStepFile;
import eu.slipo.workbench.common.model.process.ProcessExecutionRecord;
import eu.slipo.workbench.common.model.process.ProcessExecutionStepFileRecord;
import eu.slipo.workbench.common.model.process.ProcessRecord;
import eu.slipo.workbench.common.model.process.Step;
import eu.slipo.workbench.common.model.resource.EnumDataSourceType;
import eu.slipo.workbench.common.model.resource.FileSystemDataSource;
import eu.slipo.workbench.common.model.tool.TriplegeoConfiguration;
import eu.slipo.workbench.common.repository.ProcessRepository;
import eu.slipo.workbench.common.service.UserFileNamingStrategy;
import eu.slipo.workbench.web.config.MapConfiguration;
import eu.slipo.workbench.web.controller.action.ImportController.ImportResult;
import eu.slipo.workbench.web.model.configuration.GeoServerConfiguration;

@Service
public class DefaultImportService implements ImportService, InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(DefaultImportService.class);

    private static final int DEFAULT_TIMEOUT = 60000;

    @Value("${vector-data.default.schema:spatial}")
    private String defaultSchema;

    @Value("${vector-data.default.geometry-column:the_geom}")
    private String defaultGeometryColumn;

    @Autowired
    MapConfiguration mapConfiguration;

    @Autowired
    @Qualifier("defaultWebFileNamingStrategry")
    private UserFileNamingStrategy fileNamingStrategy;

    @Autowired
    private ProcessRepository processRepository;

    @Autowired
    private DataSource dataSource;

    private JdbcTemplate jdbcTemplate;

    @Override
    public void afterPropertiesSet() throws Exception {
        jdbcTemplate = new JdbcTemplate(dataSource);

    }

    @Override
    public ImportResult publiseExecutionLayers(int userId, long executionId) {
        return this.publiseExecutionLayers(userId, executionId, this.defaultSchema, this.defaultGeometryColumn);
    }

    @Override
    public ImportResult publiseExecutionLayers(int userId, long executionId, String schema, String geometryColumn) {

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
                FileSystemDataSource dataSource = (FileSystemDataSource) s.sources().get(0);
                TriplegeoConfiguration config = (TriplegeoConfiguration) s.configuration();
                return Triple.<Step, FileSystemDataSource, TriplegeoConfiguration>of(s, dataSource, config);
             })
            .filter(p -> p.getRight().getInputFormat() == EnumDataFormat.CSV || p.getRight().getInputFormat() == EnumDataFormat.SHAPEFILE)
            .forEach(p-> {
                try {
                    DatabaseImportResult importResult = this.publishTripleGeoInput(
                        userId, executionId, schema, geometryColumn,
                        p.getLeft(), p.getMiddle(), p.getRight(), process.getName() + " : " + p.getLeft().name()
                    );

                    result.imports.add(importResult);
                } catch (Exception ex) {
                    result.imports.add(new DatabaseImportResult(ex));
                }
            });

        return result;
    }

    private DatabaseImportResult publishTripleGeoInput(
        int userId, long executionId, String schema, String geometryColumn,
        Step step, FileSystemDataSource dataSource, TriplegeoConfiguration config,
        String title
    ) throws Exception {
        final String tableName = UUID.randomUUID().toString();
        final Path path;

        switch (config.getInputFormat()) {
            case CSV:
                path = fileNamingStrategy.resolvePath(userId, dataSource.getPath());
                this.importCsvFile(schema, tableName, path.toString(), geometryColumn, config);
                break;

            case SHAPEFILE:
                path = fileNamingStrategy.resolvePath(userId, dataSource.getPath());
                this.importShapeFile(schema, tableName, path.toString(), geometryColumn, config, DEFAULT_TIMEOUT);
                break;

            default:
                throw new Exception(String.format("Input format [%s] is not supported", config.getInputFormat()));
        }

        this.registerGeoServerLayer(tableName, title);
        this.updateStepFile(executionId, step, schema, tableName, geometryColumn);

        return new DatabaseImportResult(defaultSchema, tableName, defaultGeometryColumn);
    }

    private void importShapeFile(String schema, String tableName, String fileName, String geomColumn,
            TriplegeoConfiguration config, int timeout) throws Exception {

        String outputFileName = null;
        File outputFile = null;

        try {
            // Create SQL statements
            String srid = "4326";
            if (!StringUtils.isEmpty(config.getSourceCRS())) {
                srid = config.getSourceCRS().split(":")[1];
            }

            outputFileName = FilenameUtils.removeExtension(fileName) + ".sql";
            outputFile = new File(outputFileName);

            String[] command = { "shp2pgsql", "-s", srid, "-t", "2D", "-g", config.getAttrGeometry(), "-W",
                    config.getEncoding(), fileName, schema + "." + tableName };

            CommandExecutor commandExecutor = new CommandExecutor(command, timeout, outputFileName);
            int result = commandExecutor.execute();
            String[] lines = commandExecutor.getOutput();
            String output = StringUtils.join(lines, System.getProperty("line.separator"));

            if (result != 0) {
                logger.error(output);
                throw new Exception("Failed to execute shp2pgsql. Output: " + output);
            }

            // Execute script with bath INSERT statements
            Resource resource = new FileSystemResource(outputFile);
            ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator(resource);
            databasePopulator.execute(dataSource);
        } finally {
            // Delete SQL file
            if ((outputFile != null) && (!FileUtils.deleteQuietly(outputFile))) {
                logger.warn("Temporary file {} was not deleted", outputFileName);
            }
        }
    }

    private void importCsvFile(
        String schema, String tableName, String fileName, String geomColumn, TriplegeoConfiguration config
    ) throws Exception {

        // Create table
        String creteTableSql = this.generateCreateTableScript(schema, tableName, fileName, config);
        jdbcTemplate.execute(creteTableSql);

        // Import data
        String copySql = String.format(
                "COPY \"%s\".\"%s\" FROM '%s' CSV HEADER DELIMITER '%s' QUOTE '%s' NULL '' ENCODING '%s';", schema,
                tableName, fileName, config.getDelimiter(), config.getQuote(), config.getEncoding());

        jdbcTemplate.execute(copySql);

        // Create, populate and index geometry column
        String createColumnSql = this.generateCreateGeometryColumnScript(schema, tableName, fileName, geomColumn,
                config);
        jdbcTemplate.execute(createColumnSql);
    }

    private String generateCreateTableScript(
        String schema, String tableName, String fileName, TriplegeoConfiguration config
    ) throws Exception {

        StringBuilder script = new StringBuilder();

        // Helper variables
        char delimiter = config.getDelimiter().charAt(0);
        char quote = config.getQuote().charAt(0);
        CSVFormat format = CSVFormat.DEFAULT
            .withIgnoreEmptyLines()
            .withFirstRecordAsHeader()
            .withDelimiter(delimiter)
            .withQuote(quote)
            .withTrim();

        // Scan the CSV file to extract columns names (the first line is assumed to
        // contain the column names) and to compute the maximum length for all string
        // values per field
        try (Reader reader = Files.newBufferedReader(Paths.get(fileName), Charset.forName(config.getEncoding()));
                CSVParser parser = new CSVParser(reader, format);) {

            String[] fields = parser.getHeaderMap().keySet().toArray(new String[] {});
            Map<String, Integer> width = new HashMap<String, Integer>();
            boolean keyExists = false;
            boolean xExists = false;
            boolean yExists = false;

            for (CSVRecord record : parser) {
                for (String field : fields) {
                    final String value = record.get(field);
                    if ((!width.containsKey(field)) || (width.get(field) < value.length())) {
                        width.put(field, value.length());
                    }
                }
            }

            // Build SQL script for creating the table
            script.append(String.format("CREATE TABLE \"%s\".\"%s\" (\n", schema, tableName));

            // Handle primary keys either as string values or integers
            if (width.containsKey(config.getAttrKey())) {
                script.append(String.format("%s varchar(%d) PRIMARY KEY,\n", config.getAttrKey(),
                        width.get(config.getAttrKey())));
            } else {
                script.append(String.format("%s bigint PRIMARY KEY,\n", config.getAttrKey()));
            }

            // Append all columns but key, longitude and latitude
            for (String field : fields) {
                if (config.getAttrKey().equals(field)) {
                    keyExists = true;
                    continue;
                }
                if (config.getAttrX().equals(field)) {
                    xExists = true;
                    continue;
                }
                if (config.getAttrY().equals(field)) {
                    yExists = true;
                    continue;
                }

                if (width.containsKey(field)) {
                    script.append(String.format("%s varchar(%d),\n", field, width.get(field)));
                } else {
                    script.append(String.format("%s double precision,\n", field));
                }
            }

            // Append longitude and latitude values
            script.append(String.format("%s double precision,\n", config.getAttrX()));
            script.append(String.format("%s double precision\n", config.getAttrY()));
            script.append(");\n");

            // Key, longitude and latitude attributes are required
            if (!keyExists) {
                throw new Exception(String.format("Key attribute [%s] was not found", config.getAttrKey()));
            }
            if (!xExists) {
                throw new Exception(String.format("X attribute [%s] was not found", config.getAttrX()));
            }
            if (!yExists) {
                throw new Exception(String.format("Y attribute [%s] was not found", config.getAttrY()));
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

    private String generateCreateGeometryColumnScript(
        String schema, String tableName, String fileName, String geomColumn, TriplegeoConfiguration config
    ) {

        StringBuilder script = new StringBuilder();

        // Add new column
        script.append(
            String.format("ALTER TABLE \"%s\".\"%s\" ADD COLUMN \"%s\" geometry(Point,4326);\n", schema, tableName, geomColumn)
        );
        // Populate column (optionally transform source data CRS to target CRS 4326)
        script.append(String.format("UPDATE \"%s\".\"%s\"\n", schema, tableName));
        if ((StringUtils.isBlank(config.getSourceCRS())) || (config.getSourceCRS().equalsIgnoreCase("EPSG:4326"))) {
            script.append(
                String.format("SET \"%s\" = ST_SetSRID(ST_Point(%s, %s),4326);\n", geomColumn, config.getAttrX(), config.getAttrY())
            );
        } else {
            script.append(
                String.format("SET \"%s\" = ST_Transform(ST_SetSRID(ST_Point(%s, %s),%s),4326);\n",
                              geomColumn, config.getAttrX(), config.getAttrY(), config.getSourceCRS().toUpperCase())
            );
        }
        // Build a spatial index for fast retrieval
        script.append(String.format("CREATE INDEX \"%2$s_%3$s_idx\" ON \"%1$s\".\"%2$s\" USING gist (%3$s);\n",
                                    schema, tableName, geomColumn));

        return script.toString();
    }

    private void registerGeoServerLayer(String tableName, String title) throws Exception {
        GeoServerConfiguration config = this.mapConfiguration.getGeoServer();
        if (!config.isEnabled()) {
            return;
        }

        HttpClient httpClient = HttpClients.createDefault();

        URI uri = new URIBuilder()
            .setScheme("http")
            .setHost(config.getHost())
            .setPort(config.getPort())
            .setPath(String.format("geoserver/rest/workspaces/%1$s/datastores/%2$s/featuretypes", config.getWorkspace(), config.getStore()))
            .build();

        String json = "{\"featureType\":{\"name\":\"%s\",\"title\":\"%s\",\"nativeCRS\":\"EPSG:4326\",\"recalculate\":\"nativebbox,latlonbbox\"}}";
        StringEntity entity = new StringEntity(String.format(json, tableName, title));

        String auth = config.getUsername() + ":" + config.getPassword();
        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("ISO-8859-1")));

        HttpUriRequest request = RequestBuilder.post(uri)
            .addHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE)
            .addHeader(HttpHeaders.AUTHORIZATION, "Basic " + new String(encodedAuth)).setEntity(entity).build();

        HttpResponse response = httpClient.execute(request);
        if (response.getStatusLine().getStatusCode() != 201) {
            throw new RuntimeException("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
        }
    }

    private void updateStepFile(long executionId, Step step, String schema, String tableName, String geometryColumn) {
        ProcessExecutionRecord record = processRepository.findExecution(executionId, true);

        Optional<ProcessExecutionStepFileRecord> file = record.getSteps().stream()
            .filter(s -> s.getKey() == step.key())
            .flatMap(s -> s.getFiles().stream()).filter(f -> f.getType() == EnumStepFile.OUTPUT)
            .findFirst();

        if (!file.isPresent()) {
            return;
        }

        String bboxSql = String.format(
                "update   process_execution_step_file " +
                "set      bbox = (select ST_SetSRID(ST_Extent(\"%3$s\"), 4326) from \"%1$s\".\"%2$s\"), " +
                "         table_name = '%2$s' " +
                "where    process_execution_step_file.id = %4$d;",
                schema, tableName, geometryColumn, file.get().getId());

        jdbcTemplate.execute(bboxSql);
    }
}
