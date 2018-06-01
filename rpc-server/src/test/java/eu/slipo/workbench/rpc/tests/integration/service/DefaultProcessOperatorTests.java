package eu.slipo.workbench.rpc.tests.integration.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.util.StringUtils.stripFilenameExtension;
import static org.springframework.util.StringUtils.getFilenameExtension;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

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
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.util.Pair;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.MoreCollectors;

import eu.slipo.workbench.common.domain.AccountEntity;
import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.common.model.poi.EnumResourceType;
import eu.slipo.workbench.common.model.process.EnumProcessExecutionStatus;
import eu.slipo.workbench.common.model.process.EnumStepFile;
import eu.slipo.workbench.common.model.process.ProcessDefinition;
import eu.slipo.workbench.common.model.process.ProcessDefinitionBuilderFactory;
import eu.slipo.workbench.common.model.process.ProcessExecutionRecord;
import eu.slipo.workbench.common.model.process.ProcessExecutionStepFileRecord;
import eu.slipo.workbench.common.model.process.ProcessExecutionStepRecord;
import eu.slipo.workbench.common.model.process.ProcessIdentifier;
import eu.slipo.workbench.common.model.process.ProcessRecord;
import eu.slipo.workbench.common.model.resource.DataSource;
import eu.slipo.workbench.common.model.resource.FileSystemDataSource;
import eu.slipo.workbench.common.model.resource.ResourceIdentifier;
import eu.slipo.workbench.common.model.resource.ResourceMetadataCreate;
import eu.slipo.workbench.common.model.resource.ResourceRecord;
import eu.slipo.workbench.common.model.resource.UrlDataSource;
import eu.slipo.workbench.common.model.tool.AnyTool;
import eu.slipo.workbench.common.model.tool.FagiConfiguration;
import eu.slipo.workbench.common.model.tool.LimesConfiguration;
import eu.slipo.workbench.common.model.tool.TriplegeoConfiguration;
import eu.slipo.workbench.common.model.tool.output.EnumFagiOutputPart;
import eu.slipo.workbench.common.model.tool.output.EnumLimesOutputPart;
import eu.slipo.workbench.common.model.tool.output.EnumTriplegeoOutputPart;
import eu.slipo.workbench.common.model.tool.output.OutputPart;
import eu.slipo.workbench.common.model.user.Account;
import eu.slipo.workbench.common.repository.AccountRepository;
import eu.slipo.workbench.common.repository.ProcessRepository;
import eu.slipo.workbench.common.repository.ResourceRepository;
import eu.slipo.workbench.common.service.UserFileNamingStrategy;
import eu.slipo.workbench.common.service.ProcessOperator;
import eu.slipo.workbench.common.service.util.PropertiesConverterService;
import eu.slipo.workbench.rpc.Application;

@RunWith(SpringRunner.class)
@ActiveProfiles({ "testing" })
@EnableAutoConfiguration
@SpringBootTest(
    classes = { Application.class },
    properties = {
        "slipo.rpc-server.workflows.salt-for-identifier=${random.long}"
    },
    webEnvironment = WebEnvironment.NONE)
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

    @TestConfiguration
    public static class Setup
    {
        /**
         * The project URL is only needed for testing with external sources (URLDataSource)
         */
        @Value("${slipo.tests.download-url:https://raw.githubusercontent.com/SLIPO-EU/workbench/master/}")
        private URL projectDownloadUrl;

        @Autowired
        private AccountRepository accountRepository;

        @Autowired
        private PropertiesConverterService propertiesConverter;

        @Autowired
        private UserFileNamingStrategy defaultUserFileNamingStrategy;

        private URL baseUrl;

        private Resource baseResource;

        @Autowired
        private void setBaseUrl(
            @Value("${slipo.tests.project-download-url:https://raw.githubusercontent.com/SLIPO-EU/workbench/master/}") URL url)
            throws MalformedURLException
        {
            this.baseUrl = new URL(url, "rpc-server/src/test/resources/testcases/");
        }

        @Autowired
        private void setBaseResource(ResourceLoader resourceLoader)
        {
            this.baseResource = resourceLoader.getResource("classpath:testcases/");
        }

        private Map<String, TransformFixture> transformFixtures = new HashMap<>();

        private Map<String, InterlinkFixture> interlinkFixtures = new HashMap<>();

        private Map<String, FuseFixture> fuseFixtures = new HashMap<>();

        private TransformFixture.Builder newTransformFixtureBuilder()
        {
            return TransformFixture.newBuilder(propertiesConverter);
        }

        private InterlinkFixture.Builder newInterlinkFixtureBuilder()
        {
            return InterlinkFixture.newBuilder(propertiesConverter);
        }

        private FuseFixture.Builder newFuseFixtureBuilder()
        {
            return FuseFixture.newBuilder(propertiesConverter);
        }

        @PostConstruct
        public void initialize() throws Exception
        {
            // Load sample user account

            AccountEntity accountEntity = new AccountEntity(USER_NAME, USER_EMAIL);
            accountEntity.setBlocked(false);
            accountEntity.setActive(true);
            accountEntity.setRegistered(ZonedDateTime.now());
            accountEntity = accountRepository.save(accountEntity);

            // Setup a user's home directory

            final int userId = accountEntity.getId();
            final Path userDir = defaultUserFileNamingStrategy.getUserDir(userId, true);

            // Setup fixtures for transformation operations (triplegeo)

            setupTransformFixtures(userDir);

            // Setup fixtures for interlinking operations (triplegeo+limes)

            setupInterlinkFixtures(userDir);

            // Setup fixtures for fusion operations (limes+fagi)

            setupFuseFixtures(userDir);

            // Setup other fixtures ...
        }

        @Bean
        public Map<String, TransformFixture> transformFixtures()
        {
            return Collections.unmodifiableMap(transformFixtures);
        }

        @Bean
        public Map<String, InterlinkFixture> interlinkFixtures()
        {
            return Collections.unmodifiableMap(interlinkFixtures);
        }

        @Bean
        public Map<String, FuseFixture> fuseFixtures()
        {
            return Collections.unmodifiableMap(fuseFixtures);
        }

        /**
         * Copy source into target directory as a temporary file.
         * @return the path to created file
         */
        private Path copyTempIntoDirectory(Path source, String prefix, String suffix, Path targetDir)
        {
            Path path = null;
            try {
                path = Files.createTempFile(targetDir, prefix, suffix);
                Files.copy(source, path, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
            return path;
        }

        private Properties readProperties(Resource r) throws IOException
        {
            final Properties props = new Properties();
            try (InputStream in = r.getInputStream()) {
                props.load(in);
            }
            return props;
        }

        private void setupTransformFixtures(Path userDir) throws Exception
        {
            List<String> dirPaths = Arrays.asList(
                "triplegeo/1", "triplegeo/2", "triplegeo/2a", "triplegeo/3",
                "triplegeo/4", "triplegeo/5"
            );

            for (String dirPath: dirPaths) {
                final Resource dir = baseResource.createRelative(dirPath + "/");
                final String dirName = Paths.get(dirPath).getFileName().toString();

                final URL resourcesUrl = new URL(baseUrl, Paths.get(dirPath, "input").toString() + "/");

                final Path inputDir = Paths.get(dir.createRelative("input").getURI());
                final Path resultsDir = Paths.get(dir.createRelative("output").getURI());

                final List<String> inputNames = Files.list(inputDir)
                    .filter(Files::isRegularFile)
                    .map(p -> p.getFileName().toString())
                    .collect(Collectors.toList());
                final String sampleInputName = inputNames.get(0);
                final String inputExtension = getFilenameExtension(sampleInputName);

                final Properties options = readProperties(dir.createRelative("options.conf"));

                final Resource mappingsResource = dir.createRelative("mappings.yml");
                final Optional<Path> mappingsPath = mappingsResource.exists()?
                    Optional.of(Paths.get(mappingsResource.getURI())) : Optional.empty();

                final Resource classificationResource = dir.createRelative("classification.csv");
                final Optional<Path> classificationPath = classificationResource.exists()?
                    Optional.of(Paths.get(classificationResource.getURI())) : Optional.empty();

                // Copy mapping/classification files into user's data directory

                Optional<Path> mappingsTempPath = mappingsPath
                    .map(p -> copyTempIntoDirectory(p, "mappings-", ".yml", userDir));
                Optional<Path> classificationTempPath = classificationPath
                    .map(p -> copyTempIntoDirectory(p, "classification-", ".csv", userDir));

                // Copy inputs into user's data directory

                final BiMap<String, String> inputNameToTempName = HashBiMap.create();
                final BiMap<String, Path> inputNameToTempPath = HashBiMap.create();
                for (String inputName: inputNames) {
                    Path path = copyTempIntoDirectory(
                        inputDir.resolve(inputName), null, "." + inputExtension, userDir);
                    inputNameToTempPath.put(inputName, path);
                    inputNameToTempName.put(inputName,
                        stripFilenameExtension(userDir.relativize(path).toString()));
                }

                // Add fixtures for input as a local file, specs given as user-relative paths

                for (int index = 0; index < inputNames.size(); ++index) {
                    String inputName = inputNames.get(index);
                    Path inputPath = inputNameToTempPath.get(inputName);
                    TransformFixture fixture = newTransformFixtureBuilder()
                        .name("file-" + inputNameToTempName.get(inputName) + "-a")
                        .stagingDir(userDir)
                        .inputUri(inputPath.toUri())
                        .expectedResult(resultsDir.resolve(stripFilenameExtension(inputName) + ".nt"))
                        .configuration(
                            options,
                            mappingsTempPath.map(p -> userDir.relativize(p).toString()),
                            classificationTempPath.map(p -> userDir.relativize(p).toString()))
                        .build();
                    String key = String.format("file-%s-%d-a", dirName, index + 1);
                    transformFixtures.put(key, fixture);
                }

                // Add fixtures for input as a local file, specs given as file URIs

                for (int index = 0; index < inputNames.size(); ++index) {
                    String inputName = inputNames.get(index);
                    Path inputPath = inputNameToTempPath.get(inputName);
                    if (!mappingsPath.isPresent() || !classificationPath.isPresent())
                        continue;
                    TransformFixture fixture = newTransformFixtureBuilder()
                        .name("file-" + inputNameToTempName.get(inputName) + "-b")
                        .stagingDir(userDir)
                        .inputUri(inputPath.toUri())
                        .expectedResult(resultsDir.resolve(stripFilenameExtension(inputName) + ".nt"))
                        .configuration(
                            options,
                            mappingsPath.map(p -> p.toUri().toString()),
                            classificationPath.map(p -> p.toUri().toString()))
                        .build();
                    String key = String.format("file-%s-%d-b", dirName, index + 1);
                    transformFixtures.put(key, fixture);
                }

                // Add fixtures for input as URLs, specs given as user-relative paths

                for (int index = 0; index < inputNames.size(); ++index) {
                    String inputName = inputNames.get(index);
                    String fixtureName = "url-" + inputNameToTempName.get(inputName) + "-a";
                    URL url = new URL(resourcesUrl, inputName);
                    TransformFixture fixture = newTransformFixtureBuilder()
                        .name(fixtureName)
                        .inputUri(new URL(url, "#" + fixtureName.substring(1 + inputName.length())))
                        .expectedResult(resultsDir.resolve(stripFilenameExtension(inputName) + ".nt"))
                        .configuration(
                            options,
                            mappingsTempPath.map(p -> userDir.relativize(p).toString()),
                            classificationTempPath.map(p -> userDir.relativize(p).toString()))
                        .build();
                        String key = String.format("url-%s-%d-a", dirName, index + 1);
                    transformFixtures.put(key, fixture);
                }

                // Add fixtures for input as URLs, specs given as file URIs

                for (int index = 0; index < inputNames.size(); ++index) {
                    String inputName = inputNames.get(index);
                    String fixtureName = "url-" + inputNameToTempName.get(inputName) + "-b";
                    URL url = new URL(resourcesUrl, inputName);
                    if (!mappingsPath.isPresent() || !classificationPath.isPresent())
                        continue;
                    TransformFixture fixture = newTransformFixtureBuilder()
                        .name(fixtureName)
                        .inputUri(new URL(url, "#" + fixtureName.substring(1 + inputName.length())))
                        .expectedResult(resultsDir.resolve(stripFilenameExtension(inputName) + ".nt"))
                        .configuration(
                            options,
                            mappingsPath.map(p -> p.toUri().toString()),
                            classificationPath.map(p -> p.toUri().toString()))
                        .build();
                    String key = String.format("url-%s-%d-b", dirName, index + 1);
                    transformFixtures.put(key, fixture);
                }
            }
        }

        private void setupInterlinkFixtures(Path userDir) throws Exception
        {
            for (String dirPath: Arrays.asList("limes/1")) {
                final Resource dir = baseResource.createRelative(dirPath + "/");
                final String dirName = Paths.get(dirPath).getFileName().toString();

                final Path inputDir = Paths.get(dir.createRelative("input").getURI());
                final Path resultsDir = Paths.get(dir.createRelative("output").getURI());
                final Path expectedResult = resultsDir.resolve("accepted.nt");

                final URI mappingsUri = dir.createRelative("mappings.yml").getURI();
                final Path mappingsPath = Paths.get(mappingsUri);

                final URI classificationUri = dir.createRelative("classification.csv").getURI();
                final Path classificationPath = Paths.get(classificationUri);

                final Properties transformOptions1 =
                    readProperties(dir.createRelative("transform-a.properties"));
                final Properties transformOptions2 =
                    readProperties(dir.createRelative("transform-b.properties"));
                final Properties interlinkOptions =
                    readProperties(dir.createRelative("config.properties"));

                final Pair<Path, Path> expectedTransformedPair = Pair.of(
                    inputDir.resolve("a-merged.nt"), inputDir.resolve("b-merged.nt"));

                // Copy mapping/classification files into user's data directory

                Path mappingsTempPath =
                    copyTempIntoDirectory(mappingsPath, "mappings-", ".yml", userDir);
                Path classificationTempPath =
                    copyTempIntoDirectory(classificationPath, "classification-", ".csv", userDir);

                // Copy inputs into user's data directory

                final BiMap<String, String> inputNameToTempName = HashBiMap.create();
                final BiMap<String, Path> inputNameToTempPath = HashBiMap.create();
                for (String inputName: Arrays.asList("a.csv", "b.csv")) {
                    Path path = copyTempIntoDirectory(inputDir.resolve(inputName), null, ".csv", userDir);
                    inputNameToTempPath.put(inputName, path);
                    inputNameToTempName.put(inputName,
                        stripFilenameExtension(userDir.relativize(path).toString()));
                }

                // Add fixture for input as a local file, specs given as user-relative paths

                InterlinkFixture fixtureA = newInterlinkFixtureBuilder()
                    .name("file-" + inputNameToTempName.get("a.csv") + "-a")
                    .stagingDir(userDir)
                    .inputPair(Pair.of(
                        inputNameToTempPath.get("a.csv").toUri(),
                        inputNameToTempPath.get("b.csv").toUri()))
                    .configurationForTransformation(
                        transformOptions1,
                        transformOptions2,
                        userDir.relativize(mappingsTempPath).toString(),
                        userDir.relativize(classificationTempPath).toString())
                    .expectedTransformedResults(expectedTransformedPair)
                    .expectedResult(expectedResult)
                    .configuration(interlinkOptions)
                    .build();
                String keyA = String.format("file-%s-a", dirName);
                interlinkFixtures.put(keyA, fixtureA);

                // Add fixture for input as a local file, specs given as file URIs

                InterlinkFixture fixtureB = newInterlinkFixtureBuilder()
                    .name("file-" + inputNameToTempName.get("a.csv") + "-b")
                    .stagingDir(userDir)
                    .inputPair(Pair.of(
                        inputNameToTempPath.get("a.csv").toUri(),
                        inputNameToTempPath.get("b.csv").toUri()))
                    .configurationForTransformation(
                        transformOptions1,
                        transformOptions2,
                        mappingsUri.toString(),
                        classificationUri.toString())
                    .expectedTransformedResults(expectedTransformedPair)
                    .expectedResult(expectedResult)
                    .configuration(interlinkOptions)
                    .build();
                String keyB = String.format("file-%s-b", dirName);
                interlinkFixtures.put(keyB, fixtureB);
            }
        }

        private void setupFuseFixtures(Path userDir) throws Exception
        {
            for (String dirPath: Arrays.asList("fagi/1")) {
                final Resource dir = baseResource.createRelative(dirPath + "/");
                final String dirName = Paths.get(dirPath).getFileName().toString();

                final Path inputDir = Paths.get(dir.createRelative("input").getURI());
                final Path resultsDir = Paths.get(dir.createRelative("output").getURI());
                final Path expectedResult = resultsDir.resolve("fused.nt");
                final Path expectedLinkResult = inputDir.resolve("links.nt");

                final URI rulesUri = dir.createRelative("rules.xml").getURI();
                final Path rulesPath = Paths.get(rulesUri);

                final Properties linkOptions = readProperties(dir.createRelative("link.properties"));
                final Properties fuseOptions = readProperties(dir.createRelative("spec.properties"));

                // Copy rules.xml into user's data directory

                Path rulesTempPath = copyTempIntoDirectory(rulesPath, "rules-", ".xml", userDir);

                // Copy inputs into user's data directory

                final BiMap<String, String> inputNameToTempName = HashBiMap.create();
                final BiMap<String, Path> inputNameToTempPath = HashBiMap.create();
                for (String inputName: Arrays.asList("a.nt", "b.nt")) {
                    Path path = copyTempIntoDirectory(inputDir.resolve(inputName), null, ".nt", userDir);
                    inputNameToTempPath.put(inputName, path);
                    inputNameToTempName.put(inputName,
                        stripFilenameExtension(userDir.relativize(path).toString()));
                }

                FuseFixture fixture = null;
                String fixtureKey = null;

                // Add fixture for input as a local file, rules given as user-relative paths

                fixture = newFuseFixtureBuilder()
                    .name("file-" + inputNameToTempName.get("a.nt") + "-a")
                    .stagingDir(userDir)
                    .inputPair(Pair.of(
                        inputNameToTempPath.get("a.nt").toUri(),
                        inputNameToTempPath.get("b.nt").toUri()))
                    .configurationForLinking(linkOptions)
                    .expectedLinkResult(expectedLinkResult)
                    .expectedResult(expectedResult)
                    .configuration(fuseOptions, userDir.relativize(rulesTempPath).toString())
                    .build();
                    fixtureKey = String.format("file-%s-a", dirName);
                fuseFixtures.put(fixtureKey, fixture);

                // Add fixture for input as a local file, rules given as file URIs

                fixture = newFuseFixtureBuilder()
                    .name("file-" + inputNameToTempName.get("a.nt") + "-b")
                    .stagingDir(userDir)
                    .inputPair(Pair.of(
                        inputNameToTempPath.get("a.nt").toUri(),
                        inputNameToTempPath.get("b.nt").toUri()))
                    .configurationForLinking(linkOptions)
                    .expectedLinkResult(expectedLinkResult)
                    .expectedResult(expectedResult)
                    .configuration(fuseOptions, rulesUri.toString())
                    .build();
                fixtureKey = String.format("file-%s-b", dirName);
                fuseFixtures.put(fixtureKey, fixture);
            }
        }
    }

    @Autowired
    private ProcessDefinitionBuilderFactory processDefinitionBuilderFactory;

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
    @Qualifier("catalogDataDirectory")
    private Path catalogDataDir;

    @Autowired
    private Map<String, TransformFixture> transformFixtures;

    @Autowired
    private Map<String, InterlinkFixture> interlinkFixtures;

    @Autowired
    private Map<String, FuseFixture> fuseFixtures;

    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    private Account user;

    @PostConstruct
    private void initialize()
    {
        this.user = accountRepository.findOneByUsername(USER_NAME).toDto();
    }

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
                DefaultProcessOperatorTests.this.transformAndRegister(procName, fixture, creator);
            } catch (RuntimeException e) {
                e.printStackTrace();
                throw e;
            } catch (Exception e) {
                e.printStackTrace();
                throw new IllegalStateException("runnable has failed", e);
            }
        }
    }

    private ProcessExecutionRecord executeDefinition(ProcessDefinition definition, Account creator)
        throws Exception
    {
        final int creatorId = creator.getId();

        final ProcessRecord processRecord = processRepository.create(definition, creatorId, false);
        assertNotNull(processRecord);
        final long id = processRecord.getId(), version = processRecord.getVersion();
        final ProcessIdentifier processIdentifier = ProcessIdentifier.of(id, version);

        // Start process

        ProcessExecutionRecord executionRecord = processOperator.start(id, version);
        assertNotNull(executionRecord);
        final long executionId = executionRecord.getId();

        assertNotNull(executionRecord.getStatus());
        assertNotNull(executionRecord.getSubmittedOn());
        assertNotNull(executionRecord.getSubmittedBy());

        assertTrue(processOperator.list(true).contains(processIdentifier));

        // Poll execution for completion

        boolean terminated = false;
        do {
            Thread.sleep(POLL_INTERVAL);
            logger.debug("Polling execution status for process #{}", id);
            executionRecord = processOperator.poll(id, version);
            assertEquals(executionId, executionRecord.getId());
            assertNotNull(executionRecord.getStartedOn());
            terminated = executionRecord.getStatus().isTerminated() &&
                Iterables.all(executionRecord.getSteps(), s -> s.getStatus().isTerminated());
        } while (!terminated);

        Thread.sleep(250L);

        final ProcessRecord processRecord1 = processRepository.findOne(id, version, true);
        assertNotNull(processRecord1);
        assertNotNull(processRecord1.getExecutedOn());
        assertNotNull(processRecord1.getExecutions());
        assertEquals(1, processRecord1.getExecutions().size());

        return processRepository.findExecution(executionId);
    }

    private ProcessDefinition buildDefinition(
            String procName, TransformFixture fixture, String resourceName)
        throws MalformedURLException
    {
        final int resourceKey = 1;
        final DataSource source = fixture.inputAsDataSource();
        final ResourceMetadataCreate resourceMetadata =
            new ResourceMetadataCreate(resourceName, "A sample input file");

        return processDefinitionBuilderFactory.create(procName)
            .transform("Triplegeo 1", builder -> builder
                .source(source)
                .outputFormat(EnumDataFormat.N_TRIPLES)
                .outputKey(resourceKey)
                .configuration(fixture.configuration()))
            .register("Register 1", resourceKey, resourceMetadata)
            .build();
    }

    private ProcessDefinition buildDefinitionWithImportSteps(
            String procName, TransformFixture fixture, String resourceName)
        throws MalformedURLException
    {
        final int resourceKey = 1, key1 = 101;
        final URL sourceUrl = fixture.inputUri().toURL();
        final TriplegeoConfiguration configuration = fixture.configuration();
        final EnumDataFormat inputFormat1 = configuration.getInputFormat();
        final ResourceMetadataCreate resourceMetadata =
            new ResourceMetadataCreate(resourceName, "A sample input file");

        return processDefinitionBuilderFactory.create(procName)
            .resource(resourceName, key1, sourceUrl, inputFormat1)
            .transform("Triplegeo 1", builder -> builder
                .input(key1)
                .outputFormat(EnumDataFormat.N_TRIPLES)
                .outputKey(resourceKey)
                .configuration(configuration))
            .register("Register 1", resourceKey, resourceMetadata)
            .build();
    }

    private ProcessDefinition buildDefinitionUsingNamedParts(
        String procName, TransformFixture fixture, String resourceName)
        throws MalformedURLException
    {
        final int resourceKey = 1;
        final DataSource source = fixture.inputAsDataSource();
        final ResourceMetadataCreate resourceMetadata =
            new ResourceMetadataCreate(resourceName, "A sample input file");

        return processDefinitionBuilderFactory.create(procName)
            .transform("Triplegeo 1", builder -> builder
                .source(source)
                .outputFormat(EnumDataFormat.N_TRIPLES)
                .outputKey(resourceKey)
                .configuration(fixture.configuration()))
            .register("Register 1", resourceKey, "transformed", resourceMetadata)
            .build();
    }

    private ResourceRecord transformAndRegister(String procName, TransformFixture fixture, Account creator)
        throws Exception
    {
        return transformAndRegister(procName, fixture, creator, this::buildDefinition);
    }

    private ResourceRecord transformAndRegister(
            String procName, TransformFixture fixture, Account creator, TransformToDefinition transformToDefinition)
        throws Exception
    {
        logger.debug("tranformAndRegister: procName={} fixture={}", procName, fixture);

        final int creatorId = creator.getId();

        // Define the process

        final String resourceName = procName + "." + fixture.name();
        final ProcessDefinition definition =
            transformToDefinition.buildDefinition(procName, fixture, resourceName);

        final ProcessExecutionRecord executionRecord = executeDefinition(definition, creator);
        final long executionId = executionRecord.getId();

        // Test that output of transformation step is registered as a resource

        assertEquals(EnumProcessExecutionStatus.COMPLETED, executionRecord.getStatus());
        assertNotNull(executionRecord.getCompletedOn());

        List<ProcessExecutionStepRecord> stepRecords = executionRecord.getSteps();
        assertNotNull(stepRecords);
        assertEquals(2, stepRecords.size());

        ProcessExecutionStepRecord step1Record = executionRecord.getStepByName("Triplegeo 1");
        assertNotNull(step1Record);
        assertEquals(EnumProcessExecutionStatus.COMPLETED, step1Record.getStatus());
        ProcessExecutionStepFileRecord outfile1Record = step1Record.getFiles().stream()
            .filter(f -> EnumTriplegeoOutputPart.TRANSFORMED.key().equals(f.getOutputPartKey()))
            .collect(MoreCollectors.toOptional())
            .orElse(null);
        assertNotNull(outfile1Record);
        assertNotNull(outfile1Record.getFileSize());

        ResourceIdentifier resourceIdentifier = outfile1Record.getResource();
        assertNotNull(resourceIdentifier);
        ResourceRecord resourceRecord = resourceRepository.findOne(resourceIdentifier);
        assertNotNull(resourceRecord);
        assertEquals(Long.valueOf(executionId), resourceRecord.getProcessExecutionId());

        ProcessExecutionStepRecord step2Record = executionRecord.getStepByName("Register 1");
        assertNotNull(step2Record);
        assertEquals(EnumProcessExecutionStatus.COMPLETED, step2Record.getStatus());

        // Find and check resource by (name, user)

        ResourceRecord resourceRecord1 = resourceRepository.findOne(resourceName, creatorId);
        assertNotNull(resourceRecord1);
        assertEquals(resourceRecord.getId(), resourceRecord1.getId());
        assertEquals(resourceRecord.getVersion(), resourceRecord1.getVersion());

        // Check output against expected result

        Path resourcePath = catalogDataDir.resolve(resourceRecord.getFilePath());
        assertTrue(Files.isRegularFile(resourcePath) && Files.isReadable(resourcePath));
        AssertFile.assertFileEquals(fixture.expectedResult().toFile(), resourcePath.toFile());

        return resourceRecord1;
    }

    private ProcessDefinition buildDefinition(
            String procName, InterlinkFixture fixture,
            String resourceName, String output1Name, String output2Name)
        throws Exception
    {
        final int resourceKey = 1, outputKey1 = 2, outputKey2 = 3;
        final Pair<DataSource, DataSource> sourcePair = fixture.inputAsDataSource();
        final ResourceMetadataCreate outputMetadata1 =
            new ResourceMetadataCreate(output1Name, "The first (transformed) input file");
        final ResourceMetadataCreate outputMetadata2 =
            new ResourceMetadataCreate(output2Name, "The second (transformed) input file");
        final ResourceMetadataCreate resourceMetadata =
            new ResourceMetadataCreate(resourceName, "The links on pair of inputs");

        return processDefinitionBuilderFactory.create(procName)
            .transform("Transform 1", builder -> builder
                .source(sourcePair.getFirst())
                .outputFormat(EnumDataFormat.N_TRIPLES)
                .outputKey(outputKey1)
                .configuration(fixture.configurationForTransformation().getFirst()))
            .transform("Transform 2", builder -> builder
                .source(sourcePair.getSecond())
                .outputFormat(EnumDataFormat.N_TRIPLES)
                .outputKey(outputKey2)
                .configuration(fixture.configurationForTransformation().getSecond()))
            .interlink("Link 1 with 2", builder -> builder
                .left(outputKey1)
                .right(outputKey2)
                .outputFormat(EnumDataFormat.N_TRIPLES)
                .outputKey(resourceKey)
                .configuration(fixture.configuration()))
            .register("Register 1", outputKey1, outputMetadata1)
            .register("Register 2", outputKey2, outputMetadata2)
            .register("Register links", resourceKey, resourceMetadata)
            .build();
    }

    private ProcessDefinition buildDefinitionWithImportSteps(
            String procName, InterlinkFixture fixture,
            String resourceName, String output1Name, String output2Name)
        throws Exception
    {
        final int resourceKey = 1, outputKey1 = 2, outputKey2 = 3, inputKey1 = 101, inputKey2 = 102;
        final Pair<URI, URI> sourcePair = fixture.inputPair();
        final Pair<TriplegeoConfiguration, TriplegeoConfiguration> transformConfiguration =
            fixture.configurationForTransformation();
        final URL url1 = sourcePair.getFirst().toURL(), url2 = sourcePair.getSecond().toURL();
        final TriplegeoConfiguration transformConfiguration1 = transformConfiguration.getFirst();
        final TriplegeoConfiguration transformConfiguration2 = transformConfiguration.getSecond();
        final ResourceMetadataCreate outputMetadata1 =
            new ResourceMetadataCreate(output1Name, "The first (transformed) input file");
        final ResourceMetadataCreate outputMetadata2 =
            new ResourceMetadataCreate(output2Name, "The second (transformed) input file");
        final ResourceMetadataCreate resourceMetadata =
            new ResourceMetadataCreate(resourceName, "The links on pair of inputs");

        return processDefinitionBuilderFactory.create(procName)
            .resource("input 1", inputKey1, url1, transformConfiguration1.getInputFormat())
            .resource("input 2", inputKey2, url2, transformConfiguration2.getInputFormat())
            .transform("Transform 1", builder -> builder
                .input(inputKey1)
                .outputFormat(EnumDataFormat.N_TRIPLES)
                .outputKey(outputKey1)
                .configuration(transformConfiguration1))
            .transform("Transform 2", builder -> builder
                .input(inputKey2)
                .outputFormat(EnumDataFormat.N_TRIPLES)
                .outputKey(outputKey2)
                .configuration(transformConfiguration2))
            .interlink("Link 1 with 2", builder -> builder
                .left(outputKey1)
                .right(outputKey2)
                .outputFormat(EnumDataFormat.N_TRIPLES)
                .outputKey(resourceKey)
                .configuration(fixture.configuration()))
            .register("Register 1", outputKey1, outputMetadata1)
            .register("Register 2", outputKey2, outputMetadata2)
            .register("Register links", resourceKey, resourceMetadata)
            .build();
    }

    private ProcessDefinition buildDefinitionUsingNamedParts(
        String procName, InterlinkFixture fixture,
        String resourceName, String output1Name, String output2Name)
        throws Exception
    {
        final int resourceKey = 1, outputKey1 = 2, outputKey2 = 3;
        final Pair<DataSource, DataSource> sourcePair = fixture.inputAsDataSource();
        final ResourceMetadataCreate outputMetadata1 =
            new ResourceMetadataCreate(output1Name, "The first (transformed) input file");
        final ResourceMetadataCreate outputMetadata2 =
            new ResourceMetadataCreate(output2Name, "The second (transformed) input file");
        final ResourceMetadataCreate resourceMetadata =
            new ResourceMetadataCreate(resourceName, "The links on pair of inputs");

        return processDefinitionBuilderFactory.create(procName)
            .transform("Transform 1", builder -> builder
                .source(sourcePair.getFirst())
                .outputFormat(EnumDataFormat.N_TRIPLES)
                .outputKey(outputKey1)
                .configuration(fixture.configurationForTransformation().getFirst()))
            .transform("Transform 2", builder -> builder
                .source(sourcePair.getSecond())
                .outputFormat(EnumDataFormat.N_TRIPLES)
                .outputKey(outputKey2)
                .configuration(fixture.configurationForTransformation().getSecond()))
            .interlink("Link 1 with 2", builder -> builder
                .left(outputKey1, "transformed")
                .right(outputKey2, "transformed")
                .outputFormat(EnumDataFormat.N_TRIPLES)
                .outputKey(resourceKey)
                .configuration(fixture.configuration()))
            .register("Register 1", outputKey1, "transformed", outputMetadata1)
            .register("Register 2", outputKey2, "transformed", outputMetadata2)
            .register("Register links", resourceKey, "accepted", resourceMetadata)
            .build();
    }

    private ResourceRecord transformAndLinkAndRegister(String procName, InterlinkFixture fixture, Account creator)
        throws Exception
    {
        return transformAndLinkAndRegister(procName, fixture, creator, this::buildDefinition);
    }

    private ResourceRecord transformAndLinkAndRegister(
            String procName, InterlinkFixture fixture, Account creator,
            InterlinkToDefinition interlinkToDefinition)
        throws Exception
    {
        logger.debug("linkAndRegister: procName={} fixture={}", procName, fixture);

        final int creatorId = creator.getId();
        final String fixtureName = fixture.name();

        // Define the process

        final String resourceName = procName + "." + fixtureName + ".links";
        final String output1Name = procName + "." + fixtureName + ".input-1";
        final String output2Name = procName + "." + fixtureName + ".input-2";

        final ProcessDefinition definition =
            interlinkToDefinition.buildDefinition(procName, fixture, resourceName, output1Name, output2Name);

        final ProcessExecutionRecord executionRecord = executeDefinition(definition, creator);
        final long executionId = executionRecord.getId();

        // Check overall/step statuses and step files

        assertEquals(EnumProcessExecutionStatus.COMPLETED, executionRecord.getStatus());
        assertNotNull(executionRecord.getCompletedOn());

        List<ProcessExecutionStepRecord> stepRecords = executionRecord.getSteps();
        assertNotNull(stepRecords);
        assertEquals(6, stepRecords.size());

        for (String name: Arrays.asList("Transform 1", "Transform 2", "Link 1 with 2")) {
            OutputPart<? extends AnyTool> outputPart = definition.stepByName(name).defaultOutputPart();
            ProcessExecutionStepRecord stepRecord = executionRecord.getStepByName(name);
            assertNotNull(stepRecord);
            assertEquals(EnumProcessExecutionStatus.COMPLETED, stepRecord.getStatus());
            ProcessExecutionStepFileRecord outfileRecord = stepRecord.getFiles().stream()
                .filter(f -> outputPart.key().equals(f.getOutputPartKey()))
                .collect(MoreCollectors.toOptional())
                .orElse(null);
            assertNotNull(outfileRecord);
            ResourceIdentifier outfileResourceIdentifier = outfileRecord.getResource();
            assertNotNull(outfileResourceIdentifier);
            ResourceRecord outfileResourceRecord = resourceRepository.findOne(outfileResourceIdentifier);
            assertNotNull(outfileResourceRecord);
            assertEquals(Long.valueOf(executionId), outfileResourceRecord.getProcessExecutionId());
        }

        for (String name: Arrays.asList("Register 1", "Register 2", "Register links")) {
            ProcessExecutionStepRecord stepRecord = executionRecord.getStepByName(name);
            assertNotNull(stepRecord);
            assertEquals(EnumProcessExecutionStatus.COMPLETED, stepRecord.getStatus());
        }

        ProcessExecutionStepFileRecord resourceStepFileRecord = executionRecord
            .getStepByName("Link 1 with 2").getFiles()
            .stream()
            .filter(f -> EnumLimesOutputPart.ACCEPTED.key().equals(f.getOutputPartKey()))
            .findFirst().get();
        ResourceIdentifier resourceIdentifier = resourceStepFileRecord.getResource();
        assertNotNull(resourceIdentifier);
        ResourceRecord resourceRecord = resourceRepository.findOne(resourceIdentifier);

        // Find and check resource by (name, user)

        ResourceRecord resourceRecord1 = resourceRepository.findOne(resourceName, creatorId);
        assertNotNull(resourceRecord1);
        assertEquals(resourceRecord.getId(), resourceRecord1.getId());
        assertEquals(resourceRecord.getVersion(), resourceRecord1.getVersion());

        // Check output against expected result

        Path resourcePath = catalogDataDir.resolve(resourceRecord.getFilePath());
        assertTrue(Files.isRegularFile(resourcePath) && Files.isReadable(resourcePath));
        AssertFile.assertFileEquals(fixture.expectedResult().toFile(), resourcePath.toFile());

        return resourceRecord1;
    }

    private ProcessDefinition buildDefinition(
            String procName, FuseFixture fixture, String resourceName)
        throws Exception
    {
        final int inputKey1 = 1, inputKey2 = 2, linksKey = 3, resourceKey = 4;
        final Pair<URI, URI> inputPair = fixture.inputPair();
        final ResourceMetadataCreate linksMetadata =
            new ResourceMetadataCreate(resourceName + "-links", "The links from a pair of inputs");
        final ResourceMetadataCreate resourceMetadata =
            new ResourceMetadataCreate(resourceName, "The fusion of a pair of inputs");

        return processDefinitionBuilderFactory.create(procName)
            .resource("input 1",
                inputKey1, inputPair.getFirst().toURL(), EnumDataFormat.N_TRIPLES)
            .resource("input 2",
                inputKey2, inputPair.getSecond().toURL(), EnumDataFormat.N_TRIPLES)
            .interlink("Link 1 with 2", builder -> builder
                .group(1)
                .left(inputKey1)
                .right(inputKey2)
                .configuration(fixture.configurationForLinking())
                .outputFormat(EnumDataFormat.N_TRIPLES)
                .outputKey(linksKey))
            .fuse("Fuse 1 with 2", builder -> builder
                .group(2)
                .left(inputKey1)
                .right(inputKey2)
                .link(linksKey)
                .configuration(fixture.configuration())
                .outputFormat(EnumDataFormat.N_TRIPLES)
                .outputKey(resourceKey))
            .register("Register links", linksKey, linksMetadata)
            .register("Register fused", resourceKey, resourceMetadata)
            .build();
    }

    private ProcessDefinition buildDefinitionUsingNamedParts(
        String procName, FuseFixture fixture, String resourceName)
        throws Exception
    {
        final int inputKey1 = 1, inputKey2 = 2, linksKey = 3, resourceKey = 4;
        final Pair<URI, URI> inputPair = fixture.inputPair();
        final ResourceMetadataCreate linksMetadata =
            new ResourceMetadataCreate(resourceName + "-links", "The links from a pair of inputs");
        final ResourceMetadataCreate resourceMetadata =
            new ResourceMetadataCreate(resourceName, "The fusion of a pair of inputs");

        return processDefinitionBuilderFactory.create(procName)
            .resource("input 1",
                inputKey1, inputPair.getFirst().toURL(), EnumDataFormat.N_TRIPLES)
            .resource("input 2",
                inputKey2, inputPair.getSecond().toURL(), EnumDataFormat.N_TRIPLES)
            .interlink("Link 1 with 2", builder -> builder
                .group(1)
                .left(inputKey1)
                .right(inputKey2)
                .configuration(fixture.configurationForLinking())
                .outputFormat(EnumDataFormat.N_TRIPLES)
                .outputKey(linksKey))
            .fuse("Fuse 1 with 2", builder -> builder
                .group(2)
                .left(inputKey1)
                .right(inputKey2)
                .link(linksKey, "accepted")
                .configuration(fixture.configuration())
                .outputFormat(EnumDataFormat.N_TRIPLES)
                .outputKey(resourceKey))
            .register("Register links", linksKey, "accepted", linksMetadata)
            .register("Register fused", resourceKey, "fused", resourceMetadata)
            .build();
    }

    private ResourceRecord linkAndFuseAndRegister(
            String procName, FuseFixture fixture, Account creator, FuseToDefinition fuseToDefinition)
        throws Exception
    {
        logger.debug("linkAndFuseAndRegister: procName={} fixture={}", procName, fixture);

        final int creatorId = creator.getId();
        final String fixtureName = fixture.name();

        // Define the process

        final String resourceName = procName + "." + fixtureName;

        final ProcessDefinition definition =
            fuseToDefinition.buildDefinition(procName, fixture, resourceName);

        final ProcessExecutionRecord executionRecord = executeDefinition(definition, creator);
        final long executionId = executionRecord.getId();

        // Check overall/step statuses and step files

        assertEquals(EnumProcessExecutionStatus.COMPLETED, executionRecord.getStatus());
        assertNotNull(executionRecord.getCompletedOn());

        List<ProcessExecutionStepRecord> stepRecords = executionRecord.getSteps();
        assertNotNull(stepRecords);
        assertEquals(4, stepRecords.size());

        for (String name: Arrays.asList("Link 1 with 2", "Fuse 1 with 2")) {
            OutputPart<? extends AnyTool> outputPart = definition.stepByName(name).defaultOutputPart();
            ProcessExecutionStepRecord stepRecord = executionRecord.getStepByName(name);
            assertNotNull(stepRecord);
            assertEquals(EnumProcessExecutionStatus.COMPLETED, stepRecord.getStatus());
            ProcessExecutionStepFileRecord outfileRecord = stepRecord.getFiles().stream()
                .filter(f -> outputPart.key().equals(f.getOutputPartKey()))
                .collect(MoreCollectors.toOptional())
                .orElse(null);
            assertNotNull(outfileRecord);
            ResourceIdentifier outfileResourceIdentifier = outfileRecord.getResource();
            assertNotNull(outfileResourceIdentifier);
            ResourceRecord outfileResourceRecord = resourceRepository.findOne(outfileResourceIdentifier);
            assertNotNull(outfileResourceRecord);
            assertEquals(Long.valueOf(executionId), outfileResourceRecord.getProcessExecutionId());
        }

        for (String name: Arrays.asList("Register links", "Register fused")) {
            ProcessExecutionStepRecord stepRecord = executionRecord.getStepByName(name);
            assertNotNull(stepRecord);
            assertEquals(EnumProcessExecutionStatus.COMPLETED, stepRecord.getStatus());
        }

        // Check output against expected result

        ProcessExecutionStepFileRecord resourceStepFileRecord = executionRecord.getStepByName("Fuse 1 with 2")
            .getFiles().stream()
            .filter(f -> EnumFagiOutputPart.FUSED.key().equals(f.getOutputPartKey()))
            .collect(MoreCollectors.onlyElement());
        ResourceIdentifier resourceIdentifier = resourceStepFileRecord.getResource();
        assertNotNull(resourceIdentifier);
        ResourceRecord resourceRecord = resourceRepository.findOne(resourceIdentifier);

        // Find and check resource by (name, user)

        ResourceRecord resourceRecord1 = resourceRepository.findOne(resourceName, creatorId);
        assertNotNull(resourceRecord1);
        assertEquals(resourceRecord.getId(), resourceRecord1.getId());
        assertEquals(resourceRecord.getVersion(), resourceRecord1.getVersion());

        // Check output against expected result

        Path resourcePath = catalogDataDir.resolve(resourceRecord.getFilePath());
        assertTrue(Files.isRegularFile(resourcePath) && Files.isReadable(resourcePath));
        AssertFile.assertFileEquals(fixture.expectedResult().toFile(), resourcePath.toFile());

        return resourceRecord1;
    }

    private ResourceRecord linkAndFuseAndRegister(String procName, FuseFixture fixture, Account creator)
        throws Exception
    {
        return linkAndFuseAndRegister(procName, fixture, creator, this::buildDefinition);
    }

    private ResourceRecord transformAndRegisterThenLink(String procName, InterlinkFixture fixture, Account creator)
        throws Exception
    {
        final int creatorId = creator.getId();
        final String fixtureName = fixture.name();

        final Pair<TransformFixture, TransformFixture> transformFixtures = fixture.getTransformFixtures();

        final ResourceRecord resourceRecord1 =
            transformAndRegister(procName + ".tr-1", transformFixtures.getFirst(), creator);
        final ResourceIdentifier resourceIdentifier1 =
            ResourceIdentifier.of(resourceRecord1.getId(), resourceRecord1.getVersion());
        final int inputKey1 = 1;

        final ResourceRecord resourceRecord2 =
            transformAndRegister(procName + ".tr-2", transformFixtures.getSecond(), creator);
        final ResourceIdentifier resourceIdentifier2 =
            ResourceIdentifier.of(resourceRecord2.getId(), resourceRecord2.getVersion());
        final int inputKey2 = 2;

        // Build a process that uses on the 2 previously registered resources

        final String resourceName = procName + "." + fixtureName;

        final int linksKey = 3;

        ProcessDefinition definition = processDefinitionBuilderFactory.create(procName)
            .resource("tr-1", inputKey1, resourceIdentifier1, EnumResourceType.POI_DATA)
            .resource("tr-2", inputKey2, resourceIdentifier2, EnumResourceType.POI_DATA)
            .interlink("Link 1 with 2", builder -> builder
                .left(inputKey1)
                .right(inputKey2)
                .outputFormat(EnumDataFormat.N_TRIPLES)
                .outputKey(linksKey)
                .configuration(fixture.configuration()))
            .register("Register links", linksKey,
                new ResourceMetadataCreate(resourceName, "The links from a pair of inputs"))
            .build();

        final ProcessExecutionRecord executionRecord = executeDefinition(definition, creator);
        final long executionId = executionRecord.getId();

        // Check overall/step statuses and step files

        assertEquals(EnumProcessExecutionStatus.COMPLETED, executionRecord.getStatus());
        assertNotNull(executionRecord.getCompletedOn());

        List<ProcessExecutionStepRecord> stepRecords = executionRecord.getSteps();
        assertNotNull(stepRecords);
        assertEquals(2, stepRecords.size());

        ProcessExecutionStepRecord outputStepRecord = executionRecord.getStepByName("Link 1 with 2");
        assertNotNull(outputStepRecord);
        assertEquals(EnumProcessExecutionStatus.COMPLETED, outputStepRecord.getStatus());
        ProcessExecutionStepFileRecord outfileRecord = outputStepRecord.getFiles().stream()
            .filter(f -> EnumLimesOutputPart.ACCEPTED.key().equals(f.getOutputPartKey()))
            .collect(MoreCollectors.toOptional())
            .orElse(null);
        assertNotNull(outfileRecord);
        ResourceIdentifier outfileResourceIdentifier = outfileRecord.getResource();
        assertNotNull(outfileResourceIdentifier);
        ResourceRecord outfileResourceRecord = resourceRepository.findOne(outfileResourceIdentifier);
        assertNotNull(outfileResourceRecord);
        assertEquals(Long.valueOf(executionId), outfileResourceRecord.getProcessExecutionId());

        ProcessExecutionStepRecord registerStepRecord = executionRecord.getStepByName("Register links");
        assertNotNull(registerStepRecord);
        assertEquals(EnumProcessExecutionStatus.COMPLETED, registerStepRecord.getStatus());

        // Find and check resource by (name, user)

        ResourceRecord resourceRecord = resourceRepository.findOne(resourceName, creatorId);
        assertNotNull(resourceRecord);
        assertEquals(outfileResourceRecord.getId(), resourceRecord.getId());
        assertEquals(outfileResourceRecord.getVersion(), resourceRecord.getVersion());

        // Check output against expected result

        Path resourcePath = catalogDataDir.resolve(resourceRecord.getFilePath());
        assertTrue(Files.isRegularFile(resourcePath) && Files.isReadable(resourcePath));
        AssertFile.assertFileEquals(fixture.expectedResult().toFile(), resourcePath.toFile());

        return resourceRecord;
    }

    //
    // Tests
    //

    @Test(timeout = 40 * 1000L)
    public void test1T_transformAndRegister1a() throws Exception
    {
        transformAndRegister("file-1-1-a", transformFixtures.get("file-1-1-a"), user);
    }

    @Test(timeout = 40 * 1000L)
    public void test1T_transformAndRegister1a_withImportSteps() throws Exception
    {
        transformAndRegister("file-1-1-a-imported", transformFixtures.get("file-1-1-a"), user,
            this::buildDefinitionWithImportSteps);
    }

    @Test(timeout = 40 * 1000L)
    public void test1T_transformAndRegister1a_namedPart() throws Exception
    {
        transformAndRegister("file-1-1-a-namedPart", transformFixtures.get("file-1-1-a"), user,
            this::buildDefinitionUsingNamedParts);
    }

    @Test(timeout = 40 * 1000L)
    public void test1T_transformAndRegister1b() throws Exception
    {
        transformAndRegister("file-1-1-b", transformFixtures.get("file-1-1-b"), user);
    }

    @Test(timeout = 40 * 1000L)
    public void test1T_transformAndRegister1b_withImportSteps() throws Exception
    {
        transformAndRegister("file-1-1-b-imported", transformFixtures.get("file-1-1-b"), user,
            this::buildDefinitionWithImportSteps);
    }

    @Test(timeout = 40 * 1000L)
    public void test1T_transformAndRegister1b_namedPart() throws Exception
    {
        transformAndRegister("file-1-1-b-namedPart", transformFixtures.get("file-1-1-b"), user,
            this::buildDefinitionUsingNamedParts);
    }

    @Test(timeout = 40 * 1000L)
    public void test1T_transformAndRegister2a() throws Exception
    {
        transformAndRegister("file-1-2-a", transformFixtures.get("file-1-2-a"), user);
    }

    @Test(timeout = 40 * 1000L)
    public void test1T_transformAndRegister2b() throws Exception
    {
        transformAndRegister("file-1-2-b", transformFixtures.get("file-1-2-b"), user);
    }

    @Test(timeout = 40 * 1000L)
    public void test1T_transformAndRegister3a() throws Exception
    {
        transformAndRegister("file-1-3-a", transformFixtures.get("file-1-3-a"), user);
    }

    @Test(timeout = 40 * 1000L)
    public void test1T_transformAndRegister3b() throws Exception
    {
        transformAndRegister("file-1-3-b", transformFixtures.get("file-1-3-b"), user);
    }

    @Test(timeout = 40 * 1000L)
    public void test2T_transformAndRegister1a() throws Exception
    {
        transformAndRegister("file-2-1-a", transformFixtures.get("file-2-1-a"), user);
    }

    @Test(timeout = 40 * 1000L)
    public void test2T_transformAndRegister1b() throws Exception
    {
        transformAndRegister("file-2-1-b", transformFixtures.get("file-2-1-b"), user);
    }

    @Test(timeout = 40 * 1000L)
    public void test2aT_transformAndRegister1a() throws Exception
    {
        transformAndRegister("file-2a-1-a", transformFixtures.get("file-2a-1-a"), user);
    }

    @Test(timeout = 40 * 1000L)
    public void test3T_transformAndRegister1a() throws Exception
    {
        transformAndRegister("file-3-1-a", transformFixtures.get("file-3-1-a"), user);
    }

    @Test(timeout = 40 * 1000L)
    public void test3T_transformAndRegister1b() throws Exception
    {
        transformAndRegister("file-3-1-b", transformFixtures.get("file-3-1-b"), user);
    }

    @Test(timeout = 40 * 1000L)
    public void test4T_transformAndRegister1a() throws Exception
    {
        transformAndRegister("file-4-1-a", transformFixtures.get("file-4-1-a"), user);
    }

    @Test(timeout = 40 * 1000L)
    public void test4T_transformAndRegister1b() throws Exception
    {
        transformAndRegister("file-4-1-b", transformFixtures.get("file-4-1-b"), user);
    }

    @Test(timeout = 40 * 1000L)
    public void test5T_transformAndRegister1a() throws Exception
    {
        transformAndRegister("file-5-1-a", transformFixtures.get("file-5-1-a"), user);
    }

    @Test(timeout = 40 * 1000L)
    public void test5T_transformAndRegister1b() throws Exception
    {
        transformAndRegister("file-5-1-b", transformFixtures.get("file-5-1-b"), user);
    }

    @Test(timeout = 40 * 1000L)
    public void test1T_downloadAndTransformAndRegister1a() throws Exception
    {
        transformAndRegister("url-1-1-a", transformFixtures.get("url-1-1-a"), user);
    }

    @Test(timeout = 40 * 1000L)
    public void test1T_downloadAndTransformAndRegister1a_withImportSteps() throws Exception
    {
        transformAndRegister("url-1-1-a-imported", transformFixtures.get("url-1-1-a"), user,
            this::buildDefinitionWithImportSteps);
    }

    @Test(timeout = 40 * 1000L)
    public void test1T_downloadAndTransformAndRegister1a_namedPart() throws Exception
    {
        transformAndRegister("url-1-1-a-namedPart", transformFixtures.get("url-1-1-a"), user,
            this::buildDefinitionUsingNamedParts);
    }

    @Test(timeout = 40 * 1000L)
    public void test1T_downloadAndTransformAndRegister1b() throws Exception
    {
        transformAndRegister("url-1-1-b", transformFixtures.get("url-1-1-b"), user);
    }

    @Test(timeout = 40 * 1000L)
    public void test1T_downloadAndTransformAndRegister1b_withImportSteps() throws Exception
    {
        Account user = accountRepository.findOneByUsername(USER_NAME).toDto();
        transformAndRegister("url-1-1-b-imported", transformFixtures.get("url-1-1-b"), user,
            this::buildDefinitionWithImportSteps);
    }

    @Test(timeout = 40 * 1000L)
    public void test1T_downloadAndTransformAndRegister2a() throws Exception
    {
        Account user = accountRepository.findOneByUsername(USER_NAME).toDto();
        transformAndRegister("url-1-2-a", transformFixtures.get("url-1-2-a"), user);
    }

    @Test(timeout = 40 * 1000L)
    public void test1T_downloadAndTransformAndRegister2b() throws Exception
    {
        transformAndRegister("url-1-2-b", transformFixtures.get("url-1-2-b"), user);
    }

    @Test(timeout = 40 * 1000L)
    public void test1T_downloadAndTransformAndRegister3a() throws Exception
    {
        transformAndRegister("url-1-3-a", transformFixtures.get("url-1-3-a"), user);
    }

    @Test(timeout = 40 * 1000L)
    public void test1T_downloadAndTransformAndRegister3b() throws Exception
    {
        transformAndRegister("url-1-3-b", transformFixtures.get("url-1-3-b"), user);
    }

    @Test(timeout = 40 * 1000L)
    public void test2aT_downloadAndTransformAndRegister1a() throws Exception
    {
        transformAndRegister("url-2a-1-a", transformFixtures.get("url-2a-1-a"), user);
    }

    @Test(timeout = 40 * 1000L)
    public void test4T_downloadAndTransformAndRegister1a() throws Exception
    {
        transformAndRegister("url-4-1-a", transformFixtures.get("url-4-1-a"), user);
    }

    @Test(timeout = 40 * 1000L)
    public void test5T_downloadAndTransformAndRegister1a() throws Exception
    {
        transformAndRegister("url-5-1-a", transformFixtures.get("url-5-1-a"), user);
    }

    @Test(timeout = 40 * 1000L)
    public void test1L_transformAndLinkAndRegister1a() throws Exception
    {
        transformAndLinkAndRegister("links-1-a", interlinkFixtures.get("file-1-a"), user);
    }

    @Test(timeout = 40 * 1000L)
    public void test1L_transformAndLinkAndRegister1a_withImportSteps() throws Exception
    {
        transformAndLinkAndRegister("links-1-a-imported", interlinkFixtures.get("file-1-a"), user,
            this::buildDefinitionWithImportSteps);
    }

    @Test(timeout = 40 * 1000L)
    public void test1L_transformAndLinkAndRegister1a_namedPart() throws Exception
    {
        transformAndLinkAndRegister("links-1-a-namedPart", interlinkFixtures.get("file-1-a"), user,
            this::buildDefinitionUsingNamedParts);
    }

    @Test(timeout = 40 * 1000L)
    public void test1L_transformAndLinkAndRegister1b() throws Exception
    {
        transformAndLinkAndRegister("links-1-b", interlinkFixtures.get("file-1-b"), user);
    }

    @Test(timeout = 40 * 1000L)
    public void test1F_linkAndFuseAndRegister1a() throws Exception
    {
        linkAndFuseAndRegister("fused-1-a", fuseFixtures.get("file-1-a"), user);
    }

    @Test(timeout = 40 * 1000L)
    public void test1F_linkAndFuseAndRegister1a_namedPart() throws Exception
    {
        linkAndFuseAndRegister("fused-1-a-namedPart", fuseFixtures.get("file-1-a"), user,
            this::buildDefinitionUsingNamedParts);
    }

    @Test(timeout = 40 * 1000L)
    public void test1F_linkAndFuseAndRegister1b() throws Exception
    {
        linkAndFuseAndRegister("fused-1-b", fuseFixtures.get("file-1-b"), user);
    }

    @Test(timeout = 150 * 1000L)
    public void test1Tp_transformAndRegisterP6() throws Exception
    {
        Future<?> f1 = taskExecutor.submit(
            new TransformRunnable("file-1-1-a-p6", transformFixtures.get("file-1-1-a"), user));

        Future<?> f2 = taskExecutor.submit(
            new TransformRunnable("file-1-2-a-p6", transformFixtures.get("file-1-2-a"), user));

        Future<?> f3 = taskExecutor.submit(
            new TransformRunnable("file-1-3-a-p6", transformFixtures.get("file-1-3-a"), user));

        Future<?> f4 = taskExecutor.submit(
            new TransformRunnable("file-1-4-a-p6", transformFixtures.get("url-1-1-a"), user));

        Future<?> f5 = taskExecutor.submit(
            new TransformRunnable("file-1-5-a-p6", transformFixtures.get("url-1-2-a"), user));

        Future<?> f6 = taskExecutor.submit(
            new TransformRunnable("file-1-6-a-p6", transformFixtures.get("url-1-3-a"), user));

        // Wait for all tasks to complete

        List<Future<?>> futures = Arrays.asList(f1, f2, f3, f4, f5, f6);

        logger.info("Submitted {} tasks. Waiting for all to complete", futures.size());
        for (Future<?> f: futures) {
            try {
                f.get();
            } catch (ExecutionException e) {
                // Unwrap the exception and rethrow
                Throwable cause = e.getCause();
                if (cause instanceof Error) {
                    throw ((Error) cause);
                } else {
                    throw ((Exception) cause);
                }
            }
        }
    }

    @Test(timeout = 40 * 1000L)
    public void test1L_transformAndRegisterThenLink() throws Exception
    {
        transformAndRegisterThenLink("links-1-a", interlinkFixtures.get("file-1-a"), user);
    }
}
