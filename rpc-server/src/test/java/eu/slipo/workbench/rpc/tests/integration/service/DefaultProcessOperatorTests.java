package eu.slipo.workbench.rpc.tests.integration.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.util.StringUtils.stripFilenameExtension;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import eu.slipo.workbench.common.domain.AccountEntity;
import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.common.model.process.EnumProcessExecutionStatus;
import eu.slipo.workbench.common.model.process.ProcessDefinition;
import eu.slipo.workbench.common.model.process.ProcessDefinitionBuilder;
import eu.slipo.workbench.common.model.process.ProcessExecutionRecord;
import eu.slipo.workbench.common.model.process.ProcessExecutionStartException;
import eu.slipo.workbench.common.model.process.ProcessExecutionStepRecord;
import eu.slipo.workbench.common.model.process.ProcessNotFoundException;
import eu.slipo.workbench.common.model.process.ProcessRecord;
import eu.slipo.workbench.common.model.resource.ExternalUrlDataSource;
import eu.slipo.workbench.common.model.resource.FileSystemDataSource;
import eu.slipo.workbench.common.model.resource.ResourceMetadataCreate;
import eu.slipo.workbench.common.model.tool.ToolConfiguration;
import eu.slipo.workbench.common.model.tool.TriplegeoConfiguration;
import eu.slipo.workbench.common.model.user.Account;
import eu.slipo.workbench.common.repository.AccountRepository;
import eu.slipo.workbench.common.repository.ProcessRepository;
import eu.slipo.workbench.common.repository.ResourceRepository;
import eu.slipo.workbench.common.service.ProcessOperator;
import eu.slipo.workbench.common.service.util.PropertiesConverterService;
import eu.slipo.workbench.common.service.util.PropertiesConverterService.ConversionFailedException;
import eu.slipo.workbench.rpc.Application;

@RunWith(SpringRunner.class)
@ActiveProfiles({ "testing" })
@EnableAutoConfiguration
@SpringBootTest(classes = { Application.class }, webEnvironment = WebEnvironment.NONE)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DefaultProcessOperatorTests
{
    private static final Logger logger = LoggerFactory.getLogger(DefaultProcessOperatorTests.class);

    private static final Random random = new Random(System.currentTimeMillis());

    /**
     * The polling interval (in milliseconds) to check the status of a process execution
     */
    private static final long POLL_INTERVAL = 1500L;

    private static final String USER_NAME = "user-" + Long.toUnsignedString(random.nextLong(), 36);

    private static final String USER_EMAIL = USER_NAME + "@example.com";

    /**
     * A test fixture that represents a basic to-RDF operation (using Triplegeo).
     */
    private static class TransformFixture
    {
        final String name;

        /**
         * The root directory for input data (as an absolute path).
         */
        final Path inputDir;

        /**
         * The input name
         */
        final String inputName;

        /**
         * The absolute path for the expected result of the transformation applied on given input
         */
        final Path expectedResultPath;

        /**
         * The configuration for a triplegeo transformation
         */
        final TriplegeoConfiguration configuration;

        public TransformFixture(
            String name, Path inputDir, String inputName, Path expectedResultPath, TriplegeoConfiguration configuration)
        {
            Assert.isTrue(inputDir != null && inputDir.isAbsolute()
                    && Files.isDirectory(inputDir),
                "An existing input directory is required (as an absolute path)");
            Assert.isTrue(!StringUtils.isEmpty(inputName), "A input name is required");
            Assert.isTrue(expectedResultPath != null && expectedResultPath.isAbsolute()
                    && Files.isReadable(expectedResultPath),
                "An absolute file path is required for the expected result");
            Assert.notNull(configuration, "The triplegeo configuration is required");
            Assert.notNull(!StringUtils.isEmpty(name), "A non-empty name is required");

            this.name = name;
            this.inputDir = inputDir;
            this.inputName = inputName;
            this.expectedResultPath = expectedResultPath;
            this.configuration = configuration;
        }

        public TransformFixture(
            String name, Path inputDir, Path inputPath, Path expectedResultPath, TriplegeoConfiguration configuration)
        {
            this(name, inputDir, inputPath.getFileName().toString(), expectedResultPath, configuration);
        }

        public TriplegeoConfiguration getConfiguration()
        {
            return configuration;
        }

        public Path getExpectedResultPath()
        {
            return expectedResultPath;
        }

        public Path getInputDir()
        {
            return inputDir;
        }

        public String getInputName()
        {
            return inputName;
        }

        public Path getInputAsAbsolutePath()
        {
            return inputDir.resolve(inputName);
        }

        public FileSystemDataSource getInputAsDataSource()
        {
            return new FileSystemDataSource(inputName);
        }

        public String getName()
        {
            return name;
        }

        @Override
        public String toString()
        {
            return String.format(
                "TransformFixture [" +
                    "name=%s, inputDir=%s, inputName=%s, expectedResultPath=%s, configuration=%s]",
                name, inputDir, inputName, expectedResultPath, configuration);
        }
    }

    @TestConfiguration
    public static class Setup
    {
        @Autowired
        private AccountRepository accountRepository;

        @Autowired
        private ResourceRepository resourceRepository;

        @Autowired
        private ObjectMapper jsonMapper;

        @Autowired
        private PropertiesConverterService propertiesConverterService;

        @Autowired
        @Qualifier("tempDataDirectory")
        private Path stagingInputDir;

        private List<TransformFixture> transformFixtures = new ArrayList<>();

        private <T extends ToolConfiguration> T fromParameters(Map<String,Object> map, Class<T> valueType)
        {
            T t = null;
            try {
                t = propertiesConverterService.propertiesToValue(map, valueType);
            } catch (ConversionFailedException e) {
                throw new IllegalStateException("cannot convert properties", e);
            }
            return t;
        }

        @PostConstruct
        public void loadUserData()
        {
            // Load sample user accounts

            AccountEntity a = new AccountEntity(USER_NAME, USER_EMAIL);
            a.setBlocked(false);
            a.setActive(true);
            a.setRegistered(ZonedDateTime.now());
            accountRepository.save(a);
        }

        @PostConstruct
        public void setupFixtures() throws Exception
        {
            //
            // Setup fixtures for transformation operations (triplegeo)
            //

            final String rootPath = "/testcases/triplegeo";
            for (String path: Arrays.asList("csv/1")) {
                final URL inputUrl = DefaultProcessOperatorTests.class
                    .getResource(Paths.get(rootPath, path, "input").toString());
                final URL resultsUrl = DefaultProcessOperatorTests.class
                    .getResource(Paths.get(rootPath, path, "output").toString());

                final Path inputDir = Paths.get(inputUrl.getPath());
                final Path resultsDir = Paths.get(resultsUrl.getPath());

                // Read configuration parameters

                File f = new File(DefaultProcessOperatorTests.class
                        .getResource(Paths.get(rootPath, path, "parameters.json").toString()).getPath());
                @SuppressWarnings("unchecked")
                final Map<String, Object> parametersMap = jsonMapper.readValue(f, Map.class);

                final TriplegeoConfiguration configuration =
                    fromParameters(parametersMap, TriplegeoConfiguration.class);

                // Copy inputs to application temp directory

                final List<String> inputNames = Files.list(inputDir)
                    .map(p -> inputDir.relativize(p))
                    .map(p -> p.getFileName().toString())
                    .collect(Collectors.toList());

                final BiMap<String,Path> inputNameToTempPath = HashBiMap.create();

                for (String inputName: inputNames) {
                    Path p = Files.createTempFile(
                        stagingInputDir,
                        StringUtils.stripFilenameExtension(inputName) + "-",
                        ".csv");
                    Files.copy(inputDir.resolve(inputName), p, StandardCopyOption.REPLACE_EXISTING);
                    inputNameToTempPath.put(inputName, p);
                }

                inputNames.stream()
                    .map(p -> new TransformFixture(
                        stripFilenameExtension(p),
                        stagingInputDir,
                        stagingInputDir.relativize(inputNameToTempPath.get(p)),
                        resultsDir.resolve(stripFilenameExtension(p) + ".nt"),
                        configuration))
                    .forEach(transformFixtures::add);
            }

            //
            // Setup other fixtures ...
            //

        }

        @PreDestroy
        public void teardownFixtures() throws Exception
        {
            // Delete staging input files

            for (TransformFixture f: transformFixtures) {
                Files.delete(f.getInputAsAbsolutePath());
            }
        }

        @Bean
        public List<TransformFixture> transformFixtures()
        {
            return Collections.unmodifiableList(transformFixtures);
        }
    }

    @Autowired
    @Qualifier("defaultProcessOperator")
    private ProcessOperator processOperator;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ProcessRepository processRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    @Qualifier("tempDataDirectory")
    private Path stagingInputDir;

    @Autowired
    @Qualifier("catalogDataDirectory")
    private Path catalogDataDir;

    @Autowired
    private List<TransformFixture> transformFixtures;

    private void tranformAndRegister(final TransformFixture fixture, Account creator)
        throws ProcessNotFoundException, ProcessExecutionStartException, IOException, InterruptedException
    {
        final int creatorId = creator.getId();

        final String name = "register-" + stripFilenameExtension(fixture.getInputName());
        final int resourceKey = 1;

        final ResourceMetadataCreate metadata =
            new ResourceMetadataCreate(fixture.getName(), "A sample input file");

        // Define the process, create a new entity

        ProcessDefinition definition = ProcessDefinitionBuilder.create(name)
            .transform("triplegeo-1", builder -> builder
                .source(fixture.getInputAsDataSource())
                .outputFormat(EnumDataFormat.N_TRIPLES)
                .outputKey(resourceKey)
                .configuration(fixture.getConfiguration()))
            .register("register-1", resourceKey, metadata)
            .build();

        final ProcessRecord record = processRepository.create(definition, creatorId);
        assertNotNull(record);
        final long id = record.getId(), version = record.getVersion();

        // Start process

        ProcessExecutionRecord executionRecord = processOperator.start(id, version);
        assertNotNull(executionRecord);
        final long executionId = executionRecord.getId();

        assertNotNull(executionRecord.getStatus());
        assertNotNull(executionRecord.getSubmittedOn());
        assertNotNull(executionRecord.getSubmittedBy());

        // Poll execution for completion

        do {
            Thread.sleep(POLL_INTERVAL);
            executionRecord = processOperator.poll(id, version);
            assertEquals(executionId, executionRecord.getId());
            assertNotNull(executionRecord.getStartedOn());
        } while (!executionRecord.getStatus().isTerminated());

        Thread.sleep(2000L);

        final ProcessRecord record1 = processRepository.findOne(id, version, true);
        assertNotNull(record1);
        assertNotNull(record1.getExecutedOn());
        assertNotNull(record1.getExecutions());
        assertEquals(1, record1.getExecutions().size());

        // Test

        assertEquals(EnumProcessExecutionStatus.COMPLETED, executionRecord.getStatus());
        assertNotNull(executionRecord.getCompletedOn());

        List<ProcessExecutionStepRecord> stepRecords = executionRecord.getSteps();
        assertNotNull(stepRecords);
        assertEquals(2, stepRecords.size());

        // Todo check result is registered by this process execution
    }

    @Test(timeout = 30 * 1000L)
    public void test1_transformAndRegister1() throws Exception
    {
        AccountEntity user = accountRepository.findOneByUsername(USER_NAME);
        assertNotNull(user);

        tranformAndRegister(transformFixtures.get(0), user.toDto());
    }

    ///////////////////////////////////////////////////////////////////////////////
    //                Fixme scratch                                              //
    ///////////////////////////////////////////////////////////////////////////////

    //@Test
    public void test99_transformAndRegister1() throws Exception
    {
        AccountEntity user = accountRepository.findOneByUsername(USER_NAME);
        assertNotNull(user);

        final String name = "register-" + Long.toHexString(System.currentTimeMillis());
        final int resourceKey = 1;

        final ResourceMetadataCreate metadata =
            new ResourceMetadataCreate("sample", "A sample input file");

        ExternalUrlDataSource source = new ExternalUrlDataSource("http://localhost/~malex/share/1.csv");

        TriplegeoConfiguration configuration = new TriplegeoConfiguration();
        configuration.setInputFormat(EnumDataFormat.CSV);
        configuration.setOutputFormat(EnumDataFormat.N_TRIPLES);
        configuration.setTmpDir(Paths.get("/tmp/triplegeo/1"));
        configuration.setAttrX("lon");
        configuration.setAttrX("lat");
        configuration.setFeatureName("points");
        configuration.setAttrKey("id");
        configuration.setAttrName("name");
        configuration.setAttrCategory("type");

        ProcessDefinition definition = ProcessDefinitionBuilder.create(name)
            .transform("triplegeo-1", builder -> builder
                .source(source)
                .outputFormat(EnumDataFormat.N_TRIPLES)
                .outputKey(resourceKey)
                .configuration(configuration))
            .register("register-1", resourceKey, metadata)
            .build();

        ProcessRecord record = processRepository.create(definition, user.getId());
        assertNotNull(record);

        // Start process

        ProcessExecutionRecord executionRecord =
            processOperator.start(record.getId(), record.getVersion());
        assertNotNull(executionRecord);

    }
}
