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
import java.net.URISyntaxException;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import eu.slipo.workbench.common.domain.AccountEntity;
import eu.slipo.workbench.common.model.poi.EnumDataFormat;
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
import eu.slipo.workbench.common.model.tool.LimesConfiguration;
import eu.slipo.workbench.common.model.tool.TriplegeoConfiguration;
import eu.slipo.workbench.common.model.user.Account;
import eu.slipo.workbench.common.repository.AccountRepository;
import eu.slipo.workbench.common.repository.ProcessRepository;
import eu.slipo.workbench.common.repository.ResourceRepository;
import eu.slipo.workbench.common.service.UserFileNamingStrategy;
import eu.slipo.workbench.common.service.ProcessOperator;
import eu.slipo.workbench.common.service.util.PropertiesConverterService;
import eu.slipo.workbench.common.service.util.PropertiesConverterService.ConversionFailedException;
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

    private static class BaseFixture
    {
        final String name;

        /**
         * The application user directory (where input sources are staged)
         */
        final Path stagingDir;

        /**
         * The absolute path for the expected result of the transformation applied on given input
         */
        final Path expectedResultPath;

        BaseFixture(String name, Path stagingDir, Path expectedResultPath)
        {
            this.name = name;
            this.stagingDir = stagingDir;
            this.expectedResultPath = expectedResultPath;
        }

        public String getName()
        {
            return name;
        }

        public Path getExpectedResultPath()
        {
            return expectedResultPath;
        }

        DataSource convertInputAsDataSource(URI inputUri) throws MalformedURLException
        {
            final String scheme = inputUri.getScheme();
            if (scheme.equals("file")) {
                return new FileSystemDataSource(stagingDir.relativize(Paths.get(inputUri)));
            } else if (scheme.equals("http") || scheme.equals("https") || scheme.equals("ftp")) {
                return new UrlDataSource(inputUri.toURL());
            } else {
                 throw new IllegalStateException(
                     "Did not expect input with scheme of  [" + scheme + "]");
            }
        }

        Path convertInputToAbsolutePath(URI inputUri)
        {
            return inputUri.getScheme().equals("file")? Paths.get(inputUri) : null;
        }
    }

    /**
     * A test fixture that represents a basic to-RDF operation (using Triplegeo).
     */
    private static class TransformFixture extends BaseFixture
    {
        final URI input;

        final TriplegeoConfiguration configuration;

        TransformFixture(
            String name, URI input, Path stagingDir, Path expectedResultPath,
            TriplegeoConfiguration configuration)
        {
            super(name, stagingDir, expectedResultPath);
            this.input = input;
            this.configuration = configuration;
        }

        public TriplegeoConfiguration getConfiguration()
        {
            return configuration;
        }

        @Override
        public String toString()
        {
            return String.format("TransformFixture [name=%s, input=%s]", name, input);
        }

        public DataSource getInputAsDataSource() throws MalformedURLException
        {
            return convertInputAsDataSource(input);
        }

        public URL getInputAsUrl() throws MalformedURLException
        {
            return input.toURL();
        }
    }

    private static class InterlinkFixture extends BaseFixture
    {
        final Pair<URI, URI> inputPair;

        final Pair<TriplegeoConfiguration, TriplegeoConfiguration> transformConfigurationPair;

        final LimesConfiguration configuration;

        InterlinkFixture(
            String name,
            Path stagingDir,
            Pair<URI, URI> inputPair,
            Pair<TriplegeoConfiguration, TriplegeoConfiguration> transformConfigurationPair,
            Path expectedResultPath, LimesConfiguration configuration)
        {
            super(name, stagingDir, expectedResultPath);
            this.inputPair = inputPair;
            this.transformConfigurationPair = transformConfigurationPair;
            this.configuration = configuration;
        }

        public LimesConfiguration getConfiguration()
        {
            return configuration;
        }

        public Pair<TriplegeoConfiguration, TriplegeoConfiguration> getTransformConfiguration()
        {
            return transformConfigurationPair;
        }

        public Pair<DataSource, DataSource> getInputAsDataSource() throws MalformedURLException
        {
            return Pair.of(
                convertInputAsDataSource(inputPair.getFirst()),
                convertInputAsDataSource(inputPair.getSecond()));
        }

        public Pair<URL, URL> getInputAsUrl() throws MalformedURLException
        {
            return Pair.of(inputPair.getFirst().toURL(), inputPair.getSecond().toURL());
        }

        @Override
        public String toString()
        {
            return String.format("InterlinkFixture [name=%s, input=%s]", name, inputPair);
        }
    }

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
        private ObjectMapper jsonMapper;

        @Autowired
        private PropertiesConverterService propertiesConverter;

        @Autowired
        private UserFileNamingStrategy userFileNamingStrategy;

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
            final Path userDir = userFileNamingStrategy.getUserDir(userId, true);

            // Setup fixtures for transformation operations (triplegeo)

            setupTransformFixtures(userDir);

            // Setup fixtures for interlinking operations (triplegeo+limes)

            setupInterlinkFixtures(userDir);

            // Setup other fixtures ...
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
                "triplegeo/1", "triplegeo/2", "triplegeo/2a", "triplegeo/3"
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
                    TransformFixture fixture = createTransformFixture(
                        "file-" + inputNameToTempName.get(inputName) + "-a",
                        inputPath.toUri(),
                        userDir,
                        resultsDir.resolve(stripFilenameExtension(inputName) + ".nt"),
                        options,
                        mappingsTempPath.map(p -> userDir.relativize(p).toString()),
                        classificationTempPath.map(p -> userDir.relativize(p).toString()));
                    String key = String.format("file-%s-%d-a", dirName, index + 1);
                    transformFixtures.put(key, fixture);
                }

                // Add fixtures for input as a local file, specs given as file URIs

                for (int index = 0; index < inputNames.size(); ++index) {
                    String inputName = inputNames.get(index);
                    Path inputPath = inputNameToTempPath.get(inputName);
                    if (!mappingsPath.isPresent() || !classificationPath.isPresent())
                        continue;
                    TransformFixture fixture = createTransformFixture(
                        "file-" + inputNameToTempName.get(inputName) + "-b",
                        inputPath.toUri(),
                        userDir,
                        resultsDir.resolve(stripFilenameExtension(inputName) + ".nt"),
                        options,
                        mappingsPath.map(p -> p.toUri().toString()).get(),
                        classificationPath.map(p -> p.toUri().toString()).get());
                    String key = String.format("file-%s-%d-b", dirName, index + 1);
                    transformFixtures.put(key, fixture);
                }

                // Add fixtures for input as URLs, specs given as user-relative paths

                for (int index = 0; index < inputNames.size(); ++index) {
                    String inputName = inputNames.get(index);
                    String fixtureName = "url-" + inputNameToTempName.get(inputName) + "-a";
                    URL url = new URL(resourcesUrl, inputName);
                    TransformFixture fixture = createTransformFixture(
                        fixtureName,
                        new URL(url, "#" + fixtureName.substring(1 + inputName.length())),
                        resultsDir.resolve(stripFilenameExtension(inputName) + ".nt"),
                        options,
                        mappingsTempPath.map(p -> userDir.relativize(p).toString()),
                        classificationTempPath.map(p -> userDir.relativize(p).toString()));
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
                    TransformFixture fixture = createTransformFixture(
                        fixtureName,
                        new URL(url, "#" + fixtureName.substring(1 + inputName.length())),
                        resultsDir.resolve(stripFilenameExtension(inputName) + ".nt"),
                        options,
                        mappingsPath.map(p -> p.toUri().toString()).get(),
                        classificationPath.map(p -> p.toUri().toString()).get());
                    String key = String.format("url-%s-%d-b", dirName, index + 1);
                    transformFixtures.put(key, fixture);
                }
            }
        }

        private TransformFixture createTransformFixture(
            String name, URI input, Path stagingDir, Path expectedResultPath,
            Properties options, String mappingSpec, String classificationSpec)
            throws ConversionFailedException
        {
            Assert.notNull(!StringUtils.isEmpty(name), "A non-empty name is required");
            Assert.isTrue(expectedResultPath != null && expectedResultPath.isAbsolute()
                    && Files.isReadable(expectedResultPath),
                "The expected result should be given as an absolute file path");
            Assert.notNull(options, "A map of options is required");
            Assert.isTrue(!input.getScheme().equals("file") || (
                    stagingDir != null && stagingDir.isAbsolute()
                    && Files.isDirectory(stagingDir) && Files.isReadable(stagingDir)),
                "The stagingDir should be an absolute path for an existing readable directory");
            Assert.isTrue(!input.getScheme().equals("file") || Paths.get(input).startsWith(stagingDir),
                "If input is a local file, must be located under staging directory");

            TriplegeoConfiguration configuration =
                propertiesConverter.propertiesToValue(options, TriplegeoConfiguration.class);
            configuration.setMappingSpec(mappingSpec);
            configuration.setClassificationSpec(classificationSpec);

            return new TransformFixture(name, input, stagingDir, expectedResultPath, configuration);
        }

        private TransformFixture createTransformFixture(
            String name, URI input, Path stagingDir, Path expectedResultPath,
            Properties options, Optional<String> mappingSpec, Optional<String> classificationSpec)
            throws ConversionFailedException
        {
            return createTransformFixture(
                name, input, stagingDir, expectedResultPath, options, mappingSpec.orElse(null), classificationSpec.orElse(null));
        }

        private TransformFixture createTransformFixture(
            String name, URL input, Path expectedResultPath, Properties options,
            String mappingSpec, String classificationSpec)
            throws ConversionFailedException, URISyntaxException
        {
            return createTransformFixture(
                name, input.toURI(), null, expectedResultPath, options, mappingSpec, classificationSpec);
        }

        private TransformFixture createTransformFixture(
            String name, URL input, Path expectedResultPath, Properties options,
            Optional<String> mappingSpec, Optional<String> classificationSpec)
            throws ConversionFailedException, URISyntaxException
        {
            return createTransformFixture(
                name, input.toURI(), null, expectedResultPath, options, mappingSpec.orElse(null), classificationSpec.orElse(null));
        }

        @Bean
        public Map<String, TransformFixture> transformFixtures()
        {
            return Collections.unmodifiableMap(transformFixtures);
        }

        private void setupInterlinkFixtures(Path userDir) throws Exception
        {
            for (String dirPath: Arrays.asList("limes/1")) {
                final Resource dir = baseResource.createRelative(dirPath + "/");
                final String dirName = Paths.get(dirPath).getFileName().toString();

                final Path inputDir = Paths.get(dir.createRelative("input").getURI());
                final Path resultsDir = Paths.get(dir.createRelative("output").getURI());

                final URI mappingsUri = dir.createRelative("mappings.yml").getURI();
                final Path mappingsPath = Paths.get(mappingsUri);

                final URI classificationUri = dir.createRelative("classification.csv").getURI();
                final Path classificationPath = Paths.get(classificationUri);

                final Properties transformOptions1 =
                    readProperties(dir.createRelative("transform-a.properties"));
                final Properties transformOptions2 =
                    readProperties(dir.createRelative("transform-b.properties"));
                final Properties configuration =
                    readProperties(dir.createRelative("config.properties"));

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

                // Add fixture for input as a local file (specs given as user-relative paths)

                InterlinkFixture fixtureA = createInterlinkFixture(
                    "file-" + inputNameToTempName.get("a.csv") + "-a",
                    userDir,
                    Pair.of(
                        inputNameToTempPath.get("a.csv").toUri(),
                        inputNameToTempPath.get("b.csv").toUri()),
                    Pair.of(transformOptions1, transformOptions2),
                    userDir.relativize(mappingsTempPath).toString(),
                    userDir.relativize(classificationTempPath).toString(),
                    resultsDir.resolve("accepted.nt"),
                    configuration);
                String keyA = String.format("file-%s-a", dirName);
                interlinkFixtures.put(keyA, fixtureA);

                // Add fixture for input as a local file (specs given as file URIs)

                InterlinkFixture fixtureB = createInterlinkFixture(
                    "file-" + inputNameToTempName.get("a.csv") + "-b",
                    userDir,
                    Pair.of(
                        inputNameToTempPath.get("a.csv").toUri(),
                        inputNameToTempPath.get("b.csv").toUri()),
                    Pair.of(transformOptions1, transformOptions2),
                    mappingsUri.toString(),
                    classificationUri.toString(),
                    resultsDir.resolve("accepted.nt"),
                    configuration);
                String keyB = String.format("file-%s-b", dirName);
                interlinkFixtures.put(keyB, fixtureB);
            }
        }

        private InterlinkFixture createInterlinkFixture(
            String name,
            Path stagingDir,
            Pair<URI, URI> inputPair,
            Pair<Properties, Properties> transformOptionsPair,
            String mappingSpec,
            String classificationSpec,
            Path expectedResultPath,
            Properties configuration)
            throws ConversionFailedException
        {
            Assert.notNull(!StringUtils.isEmpty(name), "A non-empty name is required");
            Assert.isTrue(expectedResultPath != null && expectedResultPath.isAbsolute()
                    && Files.isReadable(expectedResultPath),
                "The expected result should be given as an absolute file path");
            Assert.notNull(configuration, "The triplegeo configuration is required");
            Assert.notNull(inputPair, "Expected an a pair of input URIs");
            Assert.notNull(transformOptionsPair,
                "Expected an a pair of transform options (for inputs to transform to RDF)");

            final URI input1 = inputPair.getFirst();
            Assert.notNull(input1, "Expected a non-null input URI");
            final URI input2 = inputPair.getSecond();
            Assert.notNull(input2, "Expected a non-null input URI");
            final Properties transformOptions1 = transformOptionsPair.getFirst();
            Assert.notNull(transformOptions1, "Expected non-null options");
            final Properties transformOptions2 = transformOptionsPair.getSecond();
            Assert.notNull(transformOptions2, "Expected non-null options");

            for (URI inputUri: Arrays.asList(input1, input2)) {
                Assert.isTrue(!inputUri.getScheme().equals("file") || (
                        stagingDir != null && stagingDir.isAbsolute()
                        && Files.isDirectory(stagingDir) && Files.isReadable(stagingDir)),
                    "The stagingDir should be an absolute path for an existing readable directory");
                Assert.isTrue(!inputUri.getScheme().equals("file") ||
                        Paths.get(inputUri).startsWith(stagingDir),
                    "If input is a local file, must be located under staging directory");
            }

            TriplegeoConfiguration transformConfiguration1 =
                propertiesConverter.propertiesToValue(transformOptions1, TriplegeoConfiguration.class);
            transformConfiguration1.setMappingSpec(mappingSpec);
            transformConfiguration1.setClassificationSpec(classificationSpec);

            TriplegeoConfiguration transformConfiguration2 =
                propertiesConverter.propertiesToValue(transformOptions2, TriplegeoConfiguration.class);
            transformConfiguration2.setMappingSpec(mappingSpec);
            transformConfiguration2.setClassificationSpec(classificationSpec);

            return new InterlinkFixture(
                name,
                stagingDir,
                inputPair,
                Pair.of(transformConfiguration1, transformConfiguration2),
                expectedResultPath,
                propertiesConverter.propertiesToValue(configuration, LimesConfiguration.class));
        }

        @Bean
        public Map<String, InterlinkFixture> interlinkFixtures()
        {
            return Collections.unmodifiableMap(interlinkFixtures);
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
    @Qualifier("tempDataDirectory")
    private Path stagingInputDir;

    @Autowired
    @Qualifier("catalogDataDirectory")
    private Path catalogDataDir;

    @Autowired
    private Map<String, TransformFixture> transformFixtures;

    @Autowired
    private Map<String, InterlinkFixture> interlinkFixtures;

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

        do {
            Thread.sleep(POLL_INTERVAL);
            logger.debug("Polling execution status for process #{}", id);
            executionRecord = processOperator.poll(id, version);
            assertEquals(executionId, executionRecord.getId());
            assertNotNull(executionRecord.getStartedOn());
        } while (!executionRecord.getStatus().isTerminated());

        Thread.sleep(3500L);

        final ProcessRecord processRecord1 = processRepository.findOne(id, version, true);
        assertNotNull(processRecord1);
        assertNotNull(processRecord1.getExecutedOn());
        assertNotNull(processRecord1.getExecutions());
        assertEquals(1, processRecord1.getExecutions().size());

        return processRepository.findExecution(executionId);
    }

    @FunctionalInterface
    interface TransformToDefinition
    {
        ProcessDefinition buildDefinition(
                String procName, TransformFixture fixture, String resourceName)
            throws Exception;
    }

    @FunctionalInterface
    interface InterlinkToDefinition
    {
        ProcessDefinition buildDefinition(
                String procName, InterlinkFixture fixture,
                String resourceName, String output1Name, String output2Name)
            throws Exception;
    }

    private ProcessDefinition buildDefinition(
            String procName, TransformFixture fixture, String resourceName)
        throws MalformedURLException
    {
        final int resourceKey = 1;
        final DataSource source = fixture.getInputAsDataSource();
        final TriplegeoConfiguration configuration = fixture.getConfiguration();

        return processDefinitionBuilderFactory.create(procName)
            .transform("Triplegeo 1", builder -> builder
                .source(source)
                .outputFormat(EnumDataFormat.N_TRIPLES)
                .outputKey(resourceKey)
                .configuration(configuration))
            .register("Register 1", resourceKey,
                new ResourceMetadataCreate(resourceName, "A sample input file"))
            .build();
    }

    private ProcessDefinition buildDefinitionWithImportSteps(
            String procName, TransformFixture fixture, String resourceName)
        throws MalformedURLException
    {
        final int resourceKey = 1, key1 = 101;
        final URL sourceUrl = fixture.getInputAsUrl();
        final TriplegeoConfiguration configuration = fixture.getConfiguration();
        final EnumDataFormat inputFormat1 = configuration.getInputFormat();

        return processDefinitionBuilderFactory.create(procName)
            .resource(resourceName, key1, sourceUrl, inputFormat1)
            .transform("Triplegeo 1", builder -> builder
                .input(key1)
                .outputFormat(EnumDataFormat.N_TRIPLES)
                .outputKey(resourceKey)
                .configuration(configuration))
            .register("Register 1", resourceKey,
                new ResourceMetadataCreate(resourceName, "A sample input file"))
            .build();
    }

    private void transformAndRegister(String procName, TransformFixture fixture, Account creator)
        throws Exception
    {
        transformAndRegister(procName, fixture, creator, this::buildDefinition);
    }

    private void transformAndRegister(
            String procName, TransformFixture fixture, Account creator,
            TransformToDefinition transformToDefinition)
        throws Exception
    {
        logger.debug("tranformAndRegister: procName={} fixture={}", procName, fixture);

        final int creatorId = creator.getId();

        // Define the process

        final String resourceName = procName + "." + fixture.getName();
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
            .filter(f -> f.getType() == EnumStepFile.OUTPUT).findFirst()
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
        AssertFile.assertFileEquals(fixture.getExpectedResultPath().toFile(), resourcePath.toFile());
    }

    private ProcessDefinition buildDefinition(
            String procName, InterlinkFixture fixture,
            String resourceName, String output1Name, String output2Name)
        throws Exception
    {
        final int resourceKey = 1, outputKey1 = 2, outputKey2 = 3;
        final Pair<DataSource, DataSource> sourcePair = fixture.getInputAsDataSource();

        return processDefinitionBuilderFactory.create(procName)
            .transform("Transform 1", builder -> builder
                .source(sourcePair.getFirst())
                .outputFormat(EnumDataFormat.N_TRIPLES)
                .outputKey(outputKey1)
                .configuration(fixture.getTransformConfiguration().getFirst()))
            .transform("Transform 2", builder -> builder
                .source(sourcePair.getSecond())
                .outputFormat(EnumDataFormat.N_TRIPLES)
                .outputKey(outputKey2)
                .configuration(fixture.getTransformConfiguration().getSecond()))
            .interlink("Link 1-2", builder -> builder
                .link(outputKey1, outputKey2)
                .outputFormat(EnumDataFormat.N_TRIPLES)
                .outputKey(resourceKey)
                .configuration(fixture.getConfiguration()))
            .register("Register 1", outputKey1,
                new ResourceMetadataCreate(output1Name, "The first input file"))
            .register("Register 2", outputKey2,
                new ResourceMetadataCreate(output2Name, "The second input file"))
            .register("Register links", resourceKey,
                new ResourceMetadataCreate(resourceName, "The links on pair of inputs"))
            .build();
    }

    private ProcessDefinition buildDefinitionWithImportSteps(
            String procName, InterlinkFixture fixture,
            String resourceName, String output1Name, String output2Name)
        throws Exception
    {
        // Todo buildDefinitionWithImportSteps
        return null;
    }

    private void transformAndLinkAndRegister(String procName, InterlinkFixture fixture, Account creator)
        throws Exception
    {
        transformAndLinkAndRegister(procName, fixture, creator, this::buildDefinition);
    }

    private void transformAndLinkAndRegister(
            String procName, InterlinkFixture fixture, Account creator,
            InterlinkToDefinition interlinkToDefinition)
        throws Exception
    {
        logger.debug("linkAndRegister: procName={} fixture={}", procName, fixture);

        final int creatorId = creator.getId();
        final String fixtureName = fixture.getName();

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

        for (String name: Arrays.asList("Transform 1", "Transform 2", "Link 1-2")) {
            ProcessExecutionStepRecord stepRecord = executionRecord.getStepByName(name);
            assertNotNull(stepRecord);
            assertEquals(EnumProcessExecutionStatus.COMPLETED, stepRecord.getStatus());
            ProcessExecutionStepFileRecord outfileRecord = stepRecord.getFiles().stream()
                .filter(f -> f.getType() == EnumStepFile.OUTPUT)
                .findFirst().orElse(null);
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
            .getStepByName("Link 1-2")
            .getFiles().stream()
            .filter(f -> f.getType() == EnumStepFile.OUTPUT)
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
        AssertFile.assertFileEquals(fixture.getExpectedResultPath().toFile(), resourcePath.toFile());
    }

    //
    // Tests
    //

    @Test(timeout = 40 * 1000L)
    public void test1_transformAndRegister1a() throws Exception
    {
        Account user = accountRepository.findOneByUsername(USER_NAME).toDto();
        transformAndRegister("register-file-1-1-a", transformFixtures.get("file-1-1-a"), user);
    }

    @Test(timeout = 40 * 1000L)
    public void test1_transformAndRegister1a_withImportSteps() throws Exception
    {
        Account user = accountRepository.findOneByUsername(USER_NAME).toDto();
        transformAndRegister("register-file-1-1-a-imported", transformFixtures.get("file-1-1-a"), user,
            this::buildDefinitionWithImportSteps);
    }

    @Test(timeout = 40 * 1000L)
    public void test1_transformAndRegister1b() throws Exception
    {
        Account user = accountRepository.findOneByUsername(USER_NAME).toDto();
        transformAndRegister("register-file-1-1-b", transformFixtures.get("file-1-1-b"), user);
    }

    @Test(timeout = 40 * 1000L)
    public void test1_transformAndRegister1b_withImportSteps() throws Exception
    {
        Account user = accountRepository.findOneByUsername(USER_NAME).toDto();
        transformAndRegister("register-file-1-1-b-imported", transformFixtures.get("file-1-1-b"), user,
            this::buildDefinitionWithImportSteps);
    }

    @Test(timeout = 40 * 1000L)
    public void test1_transformAndRegister2a() throws Exception
    {
        Account user = accountRepository.findOneByUsername(USER_NAME).toDto();
        transformAndRegister("register-file-1-2-a", transformFixtures.get("file-1-2-a"), user);
    }

    @Test(timeout = 40 * 1000L)
    public void test1_transformAndRegister2b() throws Exception
    {
        Account user = accountRepository.findOneByUsername(USER_NAME).toDto();
        transformAndRegister("register-file-1-2-b", transformFixtures.get("file-1-2-b"), user);
    }

    @Test(timeout = 40 * 1000L)
    public void test1_transformAndRegister3a() throws Exception
    {
        Account user = accountRepository.findOneByUsername(USER_NAME).toDto();
        transformAndRegister("register-file-1-3-a", transformFixtures.get("file-1-3-a"), user);
    }

    @Test(timeout = 40 * 1000L)
    public void test1_transformAndRegister3b() throws Exception
    {
        Account user = accountRepository.findOneByUsername(USER_NAME).toDto();
        transformAndRegister("register-file-1-3-b", transformFixtures.get("file-1-3-b"), user);
    }

    @Test(timeout = 40 * 1000L)
    public void test2_transformAndRegister1a() throws Exception
    {
        Account user = accountRepository.findOneByUsername(USER_NAME).toDto();
        transformAndRegister("register-file-2-1-a", transformFixtures.get("file-2-1-a"), user);
    }

    @Test(timeout = 40 * 1000L)
    public void test2_transformAndRegister1b() throws Exception
    {
        Account user = accountRepository.findOneByUsername(USER_NAME).toDto();
        transformAndRegister("register-file-2-1-b", transformFixtures.get("file-2-1-b"), user);
    }

    @Test(timeout = 40 * 1000L)
    public void test2a_transformAndRegister1a() throws Exception
    {
        Account user = accountRepository.findOneByUsername(USER_NAME).toDto();
        transformAndRegister("register-file-2a-1-a", transformFixtures.get("file-2a-1-a"), user);
    }

    @Test(timeout = 40 * 1000L)
    public void test3_transformAndRegister1a() throws Exception
    {
        Account user = accountRepository.findOneByUsername(USER_NAME).toDto();
        transformAndRegister("register-file-3-1-a", transformFixtures.get("file-3-1-a"), user);
    }

    @Test(timeout = 40 * 1000L)
    public void test3_transformAndRegister1b() throws Exception
    {
        Account user = accountRepository.findOneByUsername(USER_NAME).toDto();
        transformAndRegister("register-file-3-1-b", transformFixtures.get("file-3-1-b"), user);
    }

    @Test(timeout = 40 * 1000L)
    public void test1_downloadAndTransformAndRegister1a() throws Exception
    {
        Account user = accountRepository.findOneByUsername(USER_NAME).toDto();
        transformAndRegister("register-url-1-1-a", transformFixtures.get("url-1-1-a"), user);
    }

    @Test // Fixme (timeout = 40 * 1000L)
    public void test1_downloadAndTransformAndRegister1a_withImportSteps() throws Exception
    {
        Account user = accountRepository.findOneByUsername(USER_NAME).toDto();
        transformAndRegister("register-url-1-1-a-imported", transformFixtures.get("url-1-1-a"), user,
            this::buildDefinitionWithImportSteps);
    }

    @Test(timeout = 40 * 1000L)
    public void test1_downloadAndTransformAndRegister1b() throws Exception
    {
        Account user = accountRepository.findOneByUsername(USER_NAME).toDto();
        transformAndRegister("register-url-1-1-b", transformFixtures.get("url-1-1-b"), user);
    }

    @Test(timeout = 40 * 1000L)
    public void test1_downloadAndTransformAndRegister1b_withImportSteps() throws Exception
    {
        Account user = accountRepository.findOneByUsername(USER_NAME).toDto();
        transformAndRegister("register-url-1-1-b-imported", transformFixtures.get("url-1-1-b"), user,
            this::buildDefinitionWithImportSteps);
    }

    @Test(timeout = 40 * 1000L)
    public void test1_downloadAndTransformAndRegister2a() throws Exception
    {
        Account user = accountRepository.findOneByUsername(USER_NAME).toDto();
        transformAndRegister("register-url-1-2-a", transformFixtures.get("url-1-2-a"), user);
    }

    @Test(timeout = 40 * 1000L)
    public void test1_downloadAndTransformAndRegister2b() throws Exception
    {
        Account user = accountRepository.findOneByUsername(USER_NAME).toDto();
        transformAndRegister("register-url-1-2-b", transformFixtures.get("url-1-2-b"), user);
    }

    @Test(timeout = 40 * 1000L)
    public void test1_downloadAndTransformAndRegister3a() throws Exception
    {
        Account user = accountRepository.findOneByUsername(USER_NAME).toDto();
        transformAndRegister("register-url-1-3-a", transformFixtures.get("url-1-3-a"), user);
    }

    @Test(timeout = 40 * 1000L)
    public void test1_downloadAndTransformAndRegister3b() throws Exception
    {
        Account user = accountRepository.findOneByUsername(USER_NAME).toDto();
        transformAndRegister("register-url-1-3-b", transformFixtures.get("url-1-3-b"), user);
    }

    @Test(timeout = 40 * 1000L)
    public void test2a_downloadAndTransformAndRegister1b() throws Exception
    {
        Account user = accountRepository.findOneByUsername(USER_NAME).toDto();
        transformAndRegister("register-url-2a-1-b", transformFixtures.get("url-2a-1-a"), user);
    }

    @Test(timeout = 40 * 1000L)
    public void test1_transformAndLinkAndRegister1a() throws Exception
    {
        Account user = accountRepository.findOneByUsername(USER_NAME).toDto();
        transformAndLinkAndRegister("register-links-1-a", interlinkFixtures.get("file-1-a"), user);
    }

    @Test(timeout = 40 * 1000L)
    public void test1_transformAndLinkAndRegister1a_withImportSteps() throws Exception
    {
        Account user = accountRepository.findOneByUsername(USER_NAME).toDto();
        transformAndLinkAndRegister("register-links-1-a-imported", interlinkFixtures.get("file-1-a"), user,
            this::buildDefinitionWithImportSteps);
    }

    @Test(timeout = 40 * 1000L)
    public void test1_transformAndLinkAndRegister1b() throws Exception
    {
        Account user = accountRepository.findOneByUsername(USER_NAME).toDto();
        transformAndLinkAndRegister("register-links-1-b", interlinkFixtures.get("file-1-b"), user);
    }

    @Test(timeout = 150 * 1000L)
    public void test1p_transformAndRegisterP6() throws Exception
    {
        final Account user = accountRepository.findOneByUsername(USER_NAME).toDto();

        Future<?> f1 = taskExecutor.submit(
            new TransformRunnable("register-1-1-a-p6", transformFixtures.get("file-1-1-a"), user));

        Future<?> f2 = taskExecutor.submit(
            new TransformRunnable("register-1-2-a-p6", transformFixtures.get("file-1-2-a"), user));

        Future<?> f3 = taskExecutor.submit(
            new TransformRunnable("register-1-3-a-p6", transformFixtures.get("file-1-3-a"), user));

        Future<?> f4 = taskExecutor.submit(
            new TransformRunnable("register-1-4-a-p6", transformFixtures.get("url-1-1-a"), user));

        Future<?> f5 = taskExecutor.submit(
            new TransformRunnable("register-1-5-a-p6", transformFixtures.get("url-1-2-a"), user));

        Future<?> f6 = taskExecutor.submit(
            new TransformRunnable("register-1-6-a-p6", transformFixtures.get("url-1-3-a"), user));

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
}
