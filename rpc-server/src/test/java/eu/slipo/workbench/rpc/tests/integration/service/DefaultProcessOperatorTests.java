package eu.slipo.workbench.rpc.tests.integration.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.util.StringUtils.stripFilenameExtension;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
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
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.test.AssertFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.util.concurrent.ListenableFuture;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import eu.slipo.workbench.common.domain.AccountEntity;
import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.common.model.process.EnumProcessExecutionStatus;
import eu.slipo.workbench.common.model.process.EnumStepFile;
import eu.slipo.workbench.common.model.process.ProcessDefinition;
import eu.slipo.workbench.common.model.process.ProcessDefinitionBuilder;
import eu.slipo.workbench.common.model.process.ProcessExecutionRecord;
import eu.slipo.workbench.common.model.process.ProcessExecutionStartException;
import eu.slipo.workbench.common.model.process.ProcessExecutionStepRecord;
import eu.slipo.workbench.common.model.process.ProcessNotFoundException;
import eu.slipo.workbench.common.model.process.ProcessRecord;
import eu.slipo.workbench.common.model.resource.DataSource;
import eu.slipo.workbench.common.model.resource.ExternalUrlDataSource;
import eu.slipo.workbench.common.model.resource.FileSystemDataSource;
import eu.slipo.workbench.common.model.resource.ResourceIdentifier;
import eu.slipo.workbench.common.model.resource.ResourceMetadataCreate;
import eu.slipo.workbench.common.model.resource.ResourceRecord;
import eu.slipo.workbench.common.model.tool.ToolConfiguration;
import eu.slipo.workbench.common.model.tool.TriplegeoConfiguration;
import eu.slipo.workbench.common.model.user.Account;
import eu.slipo.workbench.common.model.user.AccountInfo;
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

        final URI input;

        final Path stagingDir;

        /**
         * The absolute path for the expected result of the transformation applied on given input
         */
        final Path expectedResultPath;

        /**
         * The configuration for a triplegeo transformation
         */
        final TriplegeoConfiguration configuration;

        public TransformFixture(
            String name, URI input, Path stagingDir, Path expectedResultPath, TriplegeoConfiguration configuration)
        {
            Assert.notNull(!StringUtils.isEmpty(name), "A non-empty name is required");
            Assert.isTrue(expectedResultPath != null && expectedResultPath.isAbsolute()
                    && Files.isReadable(expectedResultPath),
                "An absolute file path is required for the expected result");
            Assert.notNull(configuration, "The triplegeo configuration is required");
            Assert.notNull(stagingDir, "The path for the staging directory is required");
            Assert.isTrue(stagingDir.isAbsolute()
                    && Files.isDirectory(stagingDir) && Files.isReadable(stagingDir),
                "The stagingDir should be an absolute path for an existing readable directory");
            Assert.isTrue(!input.getScheme().equals("file") || Paths.get(input).startsWith(stagingDir),
                "If input is a local file, must be located under staging directory");

            this.input = input;
            this.name = name;
            this.stagingDir = stagingDir;
            this.expectedResultPath = expectedResultPath;
            this.configuration = configuration;
        }

        public TransformFixture(
            Path inputPath, Path stagingDir, Path expectedResultPath, TriplegeoConfiguration configuration)
        {
            this(
                stripFilenameExtension(inputPath.getFileName().toString()),
                inputPath.toUri(),
                stagingDir,
                expectedResultPath,
                configuration);
        }

        public TriplegeoConfiguration getConfiguration()
        {
            return configuration;
        }

        public Path getExpectedResultPath()
        {
            return expectedResultPath;
        }

        public Path getInputAsAbsolutePath()
        {
            return input.getScheme().equals("file")? Paths.get(input) : null;
        }

        public DataSource getInputAsDataSource() throws MalformedURLException
        {
            final String scheme = input.getScheme();
            if (scheme.equals("file")) {
                return new FileSystemDataSource(stagingDir.relativize(Paths.get(input)));
            } else if (scheme.equals("http") || scheme.equals("ftp")) {
                return new ExternalUrlDataSource(input.toURL());
            } else {
                 throw new IllegalStateException(
                     "Did not expect an input with a scheme of  [" + scheme + "]");
            }
        }

        public String getName()
        {
            return name;
        }

        @Override
        public String toString()
        {
            return String.format("TransformFixture [name=%s, input=%s]", name, input);
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

                final BiMap<String, Path> inputNameToTempPath = HashBiMap.create();

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
                        inputNameToTempPath.get(p),
                        stagingInputDir,
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
                Path p = f.getInputAsAbsolutePath();
                if (p != null)
                    Files.delete(p);
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

    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    private class TransformRunnable implements Runnable
    {
        private final String procName;

        private final TransformFixture fixture;

        private final Account creator;

        public TransformRunnable(String procName, TransformFixture fixture, Account creator)
        {
            this.procName = procName;
            this.fixture = fixture;
            this.creator = creator;
        }

        @Override
        public void run()
        {
            logger.info("Starting tranformAndRegister for fixture {}", fixture);
            try {
                DefaultProcessOperatorTests.this.tranformAndRegister(procName, fixture, creator);
            } catch (RuntimeException e) {
                e.printStackTrace();
                throw e;
            } catch (Exception e) {
                e.printStackTrace();
                throw new IllegalStateException("runnable has failed", e);
            }
        }
    }

    private void tranformAndRegister(String procName, TransformFixture fixture, Account creator)
        throws Exception
    {
        final int creatorId = creator.getId();
        final int resourceKey = 1;

        final ResourceMetadataCreate metadata =
            new ResourceMetadataCreate(fixture.getName(), "A sample input file");

        // Define the process, create a new entity

        DataSource source = fixture.getInputAsDataSource();

        ProcessDefinition definition = ProcessDefinitionBuilder.create(procName)
            .transform("triplegeo-1", builder -> builder
                .source(source)
                .outputFormat(EnumDataFormat.N_TRIPLES)
                .outputKey(resourceKey)
                .configuration(fixture.getConfiguration()))
            .register("register-1", resourceKey, metadata)
            .build();

        final ProcessRecord processRecord = processRepository.create(definition, creatorId);
        assertNotNull(processRecord);
        final long id = processRecord.getId(), version = processRecord.getVersion();

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

        Thread.sleep(2500L);

        final ProcessRecord processRecord1 = processRepository.findOne(id, version, true);
        assertNotNull(processRecord1);
        assertNotNull(processRecord1.getExecutedOn());
        assertNotNull(processRecord1.getExecutions());
        assertEquals(1, processRecord1.getExecutions().size());

        // Test that output of transformation step is registered as a resource

        assertEquals(EnumProcessExecutionStatus.COMPLETED, executionRecord.getStatus());
        assertNotNull(executionRecord.getCompletedOn());

        List<ProcessExecutionStepRecord> stepRecords = executionRecord.getSteps();
        assertNotNull(stepRecords);
        assertEquals(2, stepRecords.size());

        ProcessExecutionStepRecord step1Record = executionRecord.getStep("triplegeo-1");
        assertNotNull(step1Record);
        assertEquals(EnumProcessExecutionStatus.COMPLETED, step1Record.getStatus());
        ResourceIdentifier resourceIdentifier = step1Record.getFiles().stream()
            .filter(f -> f.getType() == EnumStepFile.OUTPUT)
            .map(f -> f.getResource())
            .findFirst().orElse(null);
        assertNotNull(resourceIdentifier);
        ResourceRecord resourceRecord = resourceRepository.findOne(resourceIdentifier);
        assertNotNull(resourceRecord);
        assertEquals(Long.valueOf(executionId), resourceRecord.getProcessExecutionId());

        ProcessExecutionStepRecord step2Record = executionRecord.getStep("register-1");
        assertNotNull(step2Record);
        assertEquals(EnumProcessExecutionStatus.COMPLETED, step2Record.getStatus());

        // Todo Find and check resource by (name,user)

        // Check output against expected result

        Path resourcePath = catalogDataDir.resolve(resourceRecord.getFilePath());
        assertTrue(Files.isRegularFile(resourcePath) && Files.isReadable(resourcePath));
        AssertFile.assertFileEquals(
            fixture.getExpectedResultPath().toFile(), resourcePath.toFile());
    }

    @Test(timeout = 35 * 1000L)
    public void test1_transformAndRegister1a() throws Exception
    {
        AccountEntity user = accountRepository.findOneByUsername(USER_NAME);
        tranformAndRegister("register-1-1-a", transformFixtures.get(0), user.toDto());
    }

    @Test(timeout = 35 * 1000L)
    public void test1_transformAndRegister2a() throws Exception
    {
        AccountEntity user = accountRepository.findOneByUsername(USER_NAME);
        tranformAndRegister("register-1-2-a", transformFixtures.get(1), user.toDto());
    }

    @Test(timeout = 35 * 1000L)
    public void test1_transformAndRegister3a() throws Exception
    {
        AccountEntity user = accountRepository.findOneByUsername(USER_NAME);
        tranformAndRegister("register-1-3-a", transformFixtures.get(2), user.toDto());
    }

    @Test(timeout = 35 * 1000L)
    public void test1_transformAndRegister1b() throws Exception
    {
        AccountEntity user = accountRepository.findOneByUsername(USER_NAME);
        tranformAndRegister("register-1-1-b", transformFixtures.get(0), user.toDto());
    }

    @Test // Fixme (timeout = 35 * 1000L)
    public void test2_transformAndRegisterP3() throws Exception
    {
        final AccountEntity userEntity = accountRepository.findOneByUsername(USER_NAME);
        final Account user = userEntity.toDto();

        final CountDownLatch done = new CountDownLatch(1);

        Future<?> f1 = taskExecutor.submit(
            new TransformRunnable("register-2-1", transformFixtures.get(0), user));

        Future<?> f2 = taskExecutor.submit(
            new TransformRunnable("register-2-2", transformFixtures.get(1), user));

        Future<?> f3 = taskExecutor.submit(
            new TransformRunnable("register-2-3", transformFixtures.get(2), user));

        for (Future<?> f: Arrays.asList(f1, f2, f3)) {
            try {
                f.get();
            } catch (ExecutionException e) {
                // Unwrap the exception and rethrow
                Throwable cause = e.getCause();
                if (cause instanceof Error)
                    throw ((Error) cause);
                else
                    throw ((Exception) cause);
            }

        }
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
