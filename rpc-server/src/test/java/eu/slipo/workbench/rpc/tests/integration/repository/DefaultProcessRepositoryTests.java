package eu.slipo.workbench.rpc.tests.integration.repository;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

import eu.slipo.workbench.common.domain.AccountEntity;
import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.common.model.poi.EnumOntology;
import eu.slipo.workbench.common.model.poi.EnumOperation;
import eu.slipo.workbench.common.model.poi.EnumResourceType;
import eu.slipo.workbench.common.model.poi.EnumTool;
import eu.slipo.workbench.common.model.process.EnumProcessExecutionStatus;
import eu.slipo.workbench.common.model.process.EnumStepFile;
import eu.slipo.workbench.common.model.process.ProcessDefinition;
import eu.slipo.workbench.common.model.process.ProcessDefinitionBuilder;
import eu.slipo.workbench.common.model.process.ProcessExecutionRecord;
import eu.slipo.workbench.common.model.process.ProcessExecutionStepFileRecord;
import eu.slipo.workbench.common.model.process.ProcessExecutionStepRecord;
import eu.slipo.workbench.common.model.process.ProcessRecord;
import eu.slipo.workbench.common.model.resource.DataSource;
import eu.slipo.workbench.common.model.resource.EnumDataSourceType;
import eu.slipo.workbench.common.model.resource.FileSystemDataSource;
import eu.slipo.workbench.common.model.resource.ResourceIdentifier;
import eu.slipo.workbench.common.model.resource.ResourceMetadataCreate;
import eu.slipo.workbench.common.model.resource.ResourceRecord;
import eu.slipo.workbench.common.model.tool.TriplegeoConfiguration;
import eu.slipo.workbench.common.repository.AccountRepository;
import eu.slipo.workbench.common.repository.DefaultProcessRepository;
import eu.slipo.workbench.common.repository.DefaultResourceRepository;
import eu.slipo.workbench.common.repository.ProcessRepository;
import eu.slipo.workbench.common.repository.ResourceRepository;

@RunWith(SpringRunner.class)
@ActiveProfiles({ "testing" })
@EnableAutoConfiguration
@EntityScan(basePackageClasses = { eu.slipo.workbench.common.domain._Marker.class })
@EnableJpaRepositories(basePackageClasses = { eu.slipo.workbench.common.repository._Marker.class })
@SpringBootTest(
    classes = { DefaultResourceRepository.class, DefaultProcessRepository.class },
    webEnvironment = WebEnvironment.NONE
)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DefaultProcessRepositoryTests
{
    private static final Random random = new Random();

    private static final GeometryFactory geomFactory = new GeometryFactory();

    private static final String INPUT_1_RESOURCE_NAME = "in-1";

    private static final String INPUT_1_RESOURCE_DESCRIPTION = "An input CSV file";

    private static final Geometry INPUT_1_RESOURCE_BBOX = geomFactory.createPolygon(
        new Coordinate[] {
            new Coordinate(0.0, 0.0), new Coordinate(-1.0, 0.0),
            new Coordinate(-1.0, -1.0), new Coordinate(0.0, -1.0), new Coordinate(0.0, 0.0)
        });

    private static final String OUTPUT_1_RESOURCE_NAME = "out-1";

    private static final String OUTPUT_1_RESOURCE_DESCRIPTION = "An output N-TRIPLES file";

    private static final Geometry OUTPUT_1_RESOURCE_BBOX = geomFactory.createPolygon(
        new Coordinate[] {
            new Coordinate(0.0, 0.0), new Coordinate(1.0, 0.0),
            new Coordinate(1.0, 1.0), new Coordinate(0.0, 1.0), new Coordinate(0.0, 0.0)
        });

    @TestConfiguration
    public static class Configuration
    {
        @Autowired
        private AccountRepository accountRepository;

        @Autowired
        private ResourceRepository resourceRepository;

        @Bean
        public ProcessDefinition sampleProcessDefinition1()
        {
            final TriplegeoConfiguration configuration = new TriplegeoConfiguration();
            configuration.setInputFormat(EnumDataFormat.CSV);
            configuration.setOutputFormat(EnumDataFormat.N_TRIPLES);
            configuration.setAttrX("lon");
            configuration.setAttrX("lat");
            configuration.setDelimiter(";");
            configuration.setFeatureName("points");
            configuration.setAttrKey("id");
            configuration.setAttrName("name");
            configuration.setAttrCategory("type");
            configuration.setTargetOntology(EnumOntology.GEOSPARQL);

            final ResourceMetadataCreate metadata =
                new ResourceMetadataCreate("sample-1", "A sample CSV file");
            final int resourceKey = 1;
            final DataSource source = new FileSystemDataSource("/tmp/1.csv");

            ProcessDefinition definition = ProcessDefinitionBuilder.create("register-1")
                .transform("triplegeo", b -> b
                    .group(1)
                    .outputKey(resourceKey)
                    .source(source)
                    .configuration(configuration))
                .register("register", resourceKey, metadata)
                .build();

            return definition;
        }

        @Bean
        public ProcessDefinition sampleProcessDefinition2()
        {
            final TriplegeoConfiguration configuration = new TriplegeoConfiguration();
            configuration.setInputFormat(EnumDataFormat.SHAPEFILE);
            configuration.setOutputFormat(EnumDataFormat.N_TRIPLES);
            configuration.setFeatureName("points");
            configuration.setAttrKey("id");
            configuration.setAttrName("name");
            configuration.setAttrCategory("type");
            configuration.setTargetOntology(EnumOntology.GEOSPARQL);

            final ResourceMetadataCreate metadata =
                new ResourceMetadataCreate("sample-2", "A sample SHP file");
            final int resourceKey = 1;
            final DataSource source = new FileSystemDataSource("/tmp/1.zip");

            ProcessDefinition definition = ProcessDefinitionBuilder.create("register-2")
                .transform("triplegeo", b -> b
                    .group(1)
                    .outputKey(resourceKey)
                    .source(source)
                    .configuration(configuration))
                .register("register", resourceKey, metadata)
                .build();

            return definition;
        }

        @PostConstruct
        public void loadData()
        {
            // Load sample user accounts

            AccountEntity a = new AccountEntity("baz", "baz@example.com");
            a.setBlocked(false);
            a.setActive(true);
            a.setRegistered(ZonedDateTime.now());
            accountRepository.save(a);

            // Load sample resources

            ResourceRecord inputResourceRecord1 = new ResourceRecord();
            inputResourceRecord1.setType(EnumResourceType.POI_DATA);
            inputResourceRecord1.setSourceType(EnumDataSourceType.FILESYSTEM);
            inputResourceRecord1.setInputFormat(EnumDataFormat.CSV);
            inputResourceRecord1.setFormat(EnumDataFormat.CSV);
            inputResourceRecord1.setFilePath("/tmp/1.csv");
            inputResourceRecord1.setFileSize(1024L);
            inputResourceRecord1.setMetadata(INPUT_1_RESOURCE_NAME, INPUT_1_RESOURCE_DESCRIPTION);
            inputResourceRecord1.setBoundingBox(INPUT_1_RESOURCE_BBOX);
            inputResourceRecord1.setNumberOfEntities(150);
            inputResourceRecord1 = resourceRepository.create(inputResourceRecord1, a.getId());
        }
    }

    /**
     * The repository under testing
     */
    @Autowired
    @Qualifier("defaultProcessRepository")
    private ProcessRepository processRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private ProcessDefinition sampleProcessDefinition1;

    @Autowired
    private ProcessDefinition sampleProcessDefinition2;

    @Autowired
    private ObjectMapper objectMapper;

    @Before
    public void setup() throws Exception {}

    @After
    public void teardown() throws Exception {}

    @Test
    public void test1_createAndUpdate() throws Exception
    {
        AccountEntity createdBy = accountRepository.findOneByUsername("baz");
        assertNotNull(createdBy);

        ProcessRecord record1 =
            processRepository.create(sampleProcessDefinition1, createdBy.getId());
        assertNotNull(record1);
        assertTrue(record1.getId() > 0 && record1.getVersion() > 0);

        final long id = record1.getId();
        assertEquals(1L, record1.getVersion());

        ProcessRecord record2 =
            processRepository.update(id, sampleProcessDefinition2, createdBy.getId());

        assertEquals(id, record2.getId());
        assertEquals(2L, record2.getVersion());

        List<ProcessRecord> revisions = record2.getRevisions().stream()
            .sorted(Comparator.comparingLong(ProcessRecord::getVersion))
            .collect(Collectors.toList());
        assertTrue(revisions.size() == 2);

        ProcessRecord rev1 = revisions.get(0);
        ProcessRecord rev2 = revisions.get(1);
        ProcessRecord rev1a = processRepository.findOne(id, 1L);
        ProcessRecord rev2a = processRepository.findOne(id, 2L);

        assertEquals(id, rev1.getId());
        assertEquals(1L, rev1.getVersion());
        assertEquals(id, rev1a.getId());
        assertEquals(1L, rev1a.getVersion());
        assertEquals(id, rev2.getId());
        assertEquals(2L, rev2.getVersion());
        assertEquals(id, rev2a.getId());
        assertEquals(2L, rev2a.getVersion());

        assertEquals(rev1.getCreatedOn(), rev2.getCreatedOn());
        assertEquals(rev1.getCreatedBy().getId(), rev2.getCreatedBy().getId());

        assertEquals(
            objectMapper.writeValueAsString(sampleProcessDefinition1),
            objectMapper.writeValueAsString(rev1.getDefinition()));
        assertEquals(
            objectMapper.writeValueAsString(sampleProcessDefinition1),
            objectMapper.writeValueAsString(rev1a.getDefinition()));
        assertEquals(
            objectMapper.writeValueAsString(sampleProcessDefinition2),
            objectMapper.writeValueAsString(rev2.getDefinition()));
        assertEquals(
            objectMapper.writeValueAsString(sampleProcessDefinition2),
            objectMapper.writeValueAsString(rev2a.getDefinition()));
    }

    @Test
    public void test2_createAndUpdateExecution() throws Exception
    {
        AccountEntity createdBy = accountRepository.findOneByUsername("baz");
        assertNotNull(createdBy);
        AccountEntity submittedBy = createdBy;

        ResourceRecord inputResourceRecord1 = resourceRepository.findOne(INPUT_1_RESOURCE_NAME);
        assertNotNull(inputResourceRecord1);
        ResourceIdentifier inputResourceIdentifier1 = new ResourceIdentifier(
            inputResourceRecord1.getId(), inputResourceRecord1.getVersion());

        ProcessRecord record1 =
            processRepository.create(sampleProcessDefinition1, createdBy.getId());
        assertNotNull(record1);

        final long id = record1.getId();
        final long version = record1.getVersion();

        //
        // Create an execution on this process revision
        //

        ProcessExecutionRecord executionRecord1 =
            processRepository.createExecution(id, version, submittedBy.getId());
        assertNotNull(executionRecord1);

        final long executionId = executionRecord1.getId();
        assertEquals(id, executionRecord1.getProcess().getId());
        assertEquals(version, executionRecord1.getProcess().getVersion());
        assertEquals(submittedBy.getId(), executionRecord1.getSubmittedBy().getId());
        assertNull(executionRecord1.getStartedOn());
        assertNull(executionRecord1.getCompletedOn());
        assertEquals(Collections.emptyList(), executionRecord1.getSteps());

        ProcessRecord record1a = processRepository.findOne(id, version, true);
        assertNotNull(record1a);
        assertEquals(1, record1a.getExecutions().size());

        ProcessExecutionRecord executionRecord1a = record1a.getExecutions().get(0);
        assertEquals(EnumProcessExecutionStatus.UNKNOWN, executionRecord1a.getStatus());
        assertEquals(executionRecord1.getId(), executionRecord1a.getId());
        assertEquals(executionRecord1.getProcess().toString(), executionRecord1a.getProcess().toString());

        //
        // Update basic execution metadata for status: UNKNOWN -> RUNNING
        //

        ZonedDateTime started = executionRecord1a.getSubmittedOn().plusMinutes(1);
        executionRecord1a.setStartedOn(started);
        executionRecord1a.setStatus(EnumProcessExecutionStatus.RUNNING);

        ProcessExecutionRecord executionRecord1b =
            processRepository.updateExecution(executionId, executionRecord1a);
        assertEquals(EnumProcessExecutionStatus.RUNNING, executionRecord1b.getStatus());
        assertEquals(started, executionRecord1b.getStartedOn());
        assertNull(executionRecord1b.getCompletedOn());

        ProcessExecutionRecord executionRecord1c = processRepository.findExecution(executionId);
        assertNotNull(executionRecord1c);
        assertEquals(EnumProcessExecutionStatus.RUNNING, executionRecord1c.getStatus());
        assertEquals(started, executionRecord1c.getStartedOn());
        assertEquals(Collections.emptyList(), executionRecord1c.getSteps());
        assertNull(executionRecord1c.getCompletedOn());

        //
        // Update execution by adding one step as RUNNING
        //

        final int step1Key = sampleProcessDefinition1.getSteps().stream()
            .filter(s -> s.operation() == EnumOperation.TRANSFORM)
            .mapToInt(s -> s.key())
            .findFirst()
            .getAsInt();

        final long step1JobExecutionId = 1999L;
        final long fileSize1p1 = 1000L, fileSize1p2 = 2000L;

        ProcessExecutionRecord executionRecord = null;
        ProcessExecutionStepRecord stepRecord1 = null;
        ProcessExecutionStepFileRecord fileRecord1p1 = null, fileRecord1p2 = null, fileRecord1p3 = null;

        fileRecord1p1 = new ProcessExecutionStepFileRecord(
            EnumStepFile.INPUT, "/tmp/1-1.csv", fileSize1p1, EnumDataFormat.CSV);
        fileRecord1p2 = new ProcessExecutionStepFileRecord(
            EnumStepFile.INPUT, "/tmp/1-2.csv", fileSize1p2, EnumDataFormat.CSV);

        stepRecord1 = new ProcessExecutionStepRecord(-1, step1Key, "triplegeo-1");
        stepRecord1.setOperation(EnumOperation.TRANSFORM);
        stepRecord1.setTool(EnumTool.TRIPLEGEO);
        stepRecord1.setJobExecutionId(step1JobExecutionId);
        stepRecord1.setStartedOn(started.plusMinutes(1));
        stepRecord1.setStatus(EnumProcessExecutionStatus.RUNNING);
        stepRecord1.addFiles(Arrays.asList(fileRecord1p1, fileRecord1p2));

        ProcessExecutionRecord executionRecord1d =
            processRepository.createExecutionStep(executionId, stepRecord1);
        assertNotNull(executionRecord1d);
        executionRecord1d = processRepository.findExecution(executionId);
        assertNotNull(executionRecord1d);
        List<ProcessExecutionStepRecord> executionRecord1dSteps = executionRecord1d.getSteps();
        assertTrue(executionRecord1dSteps.size() == 1);

        ProcessExecutionStepRecord stepRecord1d1 = executionRecord1dSteps.get(0);
        assertEquals(step1Key, stepRecord1d1.getKey());
        assertEquals("triplegeo-1", stepRecord1d1.getName());
        assertEquals(EnumProcessExecutionStatus.RUNNING, stepRecord1d1.getStatus());
        assertEquals(EnumOperation.TRANSFORM, stepRecord1d1.getOperation());
        assertEquals(EnumTool.TRIPLEGEO, stepRecord1d1.getTool());
        assertEquals(step1JobExecutionId, stepRecord1d1.getJobExecutionId());

        List<ProcessExecutionStepFileRecord> fileRecords1d = stepRecord1d1.getFiles();
        assertEquals(2, fileRecords1d.size());
        assertEquals("/tmp/1-1.csv", fileRecords1d.get(0).getFilePath());
        assertEquals("/tmp/1-2.csv", fileRecords1d.get(1).getFilePath());
        assertEquals(EnumStepFile.INPUT, fileRecords1d.get(0).getType());
        assertEquals(EnumStepFile.INPUT, fileRecords1d.get(1).getType());
        assertEquals(fileSize1p1, fileRecords1d.get(0).getFileSize().longValue());
        assertEquals(fileSize1p2, fileRecords1d.get(1).getFileSize().longValue());
        assertEquals(EnumDataFormat.CSV, fileRecords1d.get(0).getDataFormat());
        assertEquals(EnumDataFormat.CSV, fileRecords1d.get(1).getDataFormat());

        //
        // Update execution by updating 1st step:
        //    * mark as COMPLETED
        //    * update the size of an existing file and link to a resource
        //    * add an output file
        //

        final long fileSize1p3 = 3000L, fileSize1p2a = fileSize1p2 + 100L;

        stepRecord1 = new ProcessExecutionStepRecord(stepRecord1d1);
        stepRecord1.setStartedOn(stepRecord1d1.getStartedOn().plusMinutes(5)); // this is not updatable!
        stepRecord1.setCompletedOn(stepRecord1d1.getStartedOn().plusMinutes(5));
        stepRecord1.setStatus(EnumProcessExecutionStatus.COMPLETED);

        fileRecord1p2 = stepRecord1.getFiles().get(1);
        fileRecord1p2.setFileSize(fileSize1p2a);
        fileRecord1p2.setResource(inputResourceIdentifier1);
        fileRecord1p3 = new ProcessExecutionStepFileRecord(
            EnumStepFile.OUTPUT, "/tmp/out-1.nt", fileSize1p3, EnumDataFormat.N_TRIPLES);
        fileRecord1p3.setBoundingBox(OUTPUT_1_RESOURCE_BBOX);
        fileRecord1p3.setTableName(UUID.randomUUID());

        stepRecord1.setFile(1, fileRecord1p2);
        stepRecord1.addFile(fileRecord1p3);

        ProcessExecutionRecord executionRecord1e =
            processRepository.updateExecutionStep(executionId, step1Key, stepRecord1);
        assertNotNull(executionRecord1e);
        executionRecord1e = processRepository.findExecution(executionId);
        assertNotNull(executionRecord1e);
        List<ProcessExecutionStepRecord> executionRecord1eSteps = executionRecord1e.getSteps();
        assertTrue(executionRecord1eSteps.size() == 1);

        ProcessExecutionStepRecord stepRecord1e1 = executionRecord1eSteps.get(0);
        assertEquals(step1Key, stepRecord1e1.getKey());
        assertEquals("triplegeo-1", stepRecord1e1.getName());
        assertEquals(EnumProcessExecutionStatus.COMPLETED, stepRecord1e1.getStatus());
        assertEquals(EnumOperation.TRANSFORM, stepRecord1e1.getOperation());
        assertEquals(EnumTool.TRIPLEGEO, stepRecord1e1.getTool());
        assertEquals(step1JobExecutionId, stepRecord1e1.getJobExecutionId());
        assertEquals(stepRecord1d1.getStartedOn(), stepRecord1e1.getStartedOn()); // not updatable!
        assertEquals(stepRecord1.getCompletedOn(), stepRecord1e1.getCompletedOn());
        assertNull(stepRecord1e1.getErrorMessage());

        List<ProcessExecutionStepFileRecord> fileRecords1e = stepRecord1e1.getFiles();
        assertEquals(3, fileRecords1e.size());
        assertEquals("/tmp/1-1.csv", fileRecords1e.get(0).getFilePath());
        assertEquals("/tmp/1-2.csv", fileRecords1e.get(1).getFilePath());
        assertEquals("/tmp/out-1.nt", fileRecords1e.get(2).getFilePath());
        assertEquals(EnumStepFile.INPUT, fileRecords1e.get(0).getType());
        assertEquals(EnumStepFile.INPUT, fileRecords1e.get(1).getType());
        assertEquals(EnumStepFile.OUTPUT, fileRecords1e.get(2).getType());
        assertEquals(fileSize1p1, fileRecords1e.get(0).getFileSize().longValue());
        assertEquals(fileSize1p2a, fileRecords1e.get(1).getFileSize().longValue());
        assertEquals(fileSize1p3, fileRecords1e.get(2).getFileSize().longValue());
        assertEquals(EnumDataFormat.CSV, fileRecords1e.get(0).getDataFormat());
        assertEquals(EnumDataFormat.CSV, fileRecords1e.get(1).getDataFormat());
        assertEquals(EnumDataFormat.N_TRIPLES, fileRecords1e.get(2).getDataFormat());
        assertNull(fileRecords1e.get(0).getTableName());
        assertNull(fileRecords1e.get(1).getTableName());
        assertEquals(fileRecord1p3.getTableName(), fileRecords1e.get(2).getTableName());
        assertNull(fileRecords1e.get(0).getBoundingBox());
        assertNull(fileRecords1e.get(1).getBoundingBox());
        assertEquals(OUTPUT_1_RESOURCE_BBOX, fileRecords1e.get(2).getBoundingBox());

        //
        // Update execution as COMPLETED
        //

        executionRecord = new ProcessExecutionRecord(executionRecord1e, false);
        executionRecord.setStatus(EnumProcessExecutionStatus.COMPLETED);
        executionRecord.setCompletedOn(executionRecord.getStartedOn().plusMinutes(10));
        ProcessExecutionRecord executionRecord1f =
            processRepository.updateExecution(executionId, executionRecord);
        assertNotNull(executionRecord1f);
        executionRecord1f = processRepository.findExecution(executionId);
        assertNotNull(executionRecord1f);

        assertEquals(started, executionRecord1f.getStartedOn());
        assertEquals(executionRecord.getCompletedOn(), executionRecord1f.getCompletedOn());
        assertEquals(EnumProcessExecutionStatus.COMPLETED, executionRecord1f.getStatus());

        //
        // Register a new resource from the output of step #1
        //

        ResourceRecord outputResourceRecord1 = resourceRepository.createFromProcessExecution(
            executionId,
            step1Key,
            new ResourceMetadataCreate(OUTPUT_1_RESOURCE_NAME, OUTPUT_1_RESOURCE_DESCRIPTION));
        assertNotNull(outputResourceRecord1);
        assertTrue(outputResourceRecord1.getId() > 0);
        assertEquals(1L, outputResourceRecord1.getVersion());

        final ResourceIdentifier outputResourceIdentifier1 = new ResourceIdentifier(
            outputResourceRecord1.getId(), outputResourceRecord1.getVersion());

        outputResourceRecord1 = resourceRepository.findOne(outputResourceIdentifier1);
        assertNotNull(outputResourceRecord1);
        assertNotNull(outputResourceRecord1.getMetadata());
        assertEquals(executionId, outputResourceRecord1.getProcessExecutionId().longValue());
        assertEquals(EnumResourceType.POI_DATA, outputResourceRecord1.getType());
        assertEquals(EnumDataSourceType.FILESYSTEM, outputResourceRecord1.getSourceType());
        assertEquals(OUTPUT_1_RESOURCE_NAME, outputResourceRecord1.getName());
        assertEquals(OUTPUT_1_RESOURCE_DESCRIPTION, outputResourceRecord1.getDescription());
        assertEquals("/tmp/out-1.nt", outputResourceRecord1.getFilePath());
        assertEquals(EnumDataFormat.N_TRIPLES, outputResourceRecord1.getFormat());
        assertEquals(EnumDataFormat.CSV, outputResourceRecord1.getInputFormat());
        assertEquals(fileRecord1p3.getFileSize().longValue(), outputResourceRecord1.getFileSize().longValue());
        assertEquals(fileRecord1p3.getTableName(), outputResourceRecord1.getTableName());
        assertEquals(OUTPUT_1_RESOURCE_BBOX, outputResourceRecord1.getBoundingBox());
        assertNull(outputResourceRecord1.getNumberOfEntities());
    }

}
