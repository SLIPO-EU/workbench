package eu.slipo.workbench.rpc.tests.integration.service;

import java.io.File;
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
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.util.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.util.StringUtils.stripFilenameExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.Futures;

import eu.slipo.workbench.common.domain.AccountEntity;
import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.common.model.process.ProcessDefinition;
import eu.slipo.workbench.common.model.process.ProcessDefinitionBuilder;
import eu.slipo.workbench.common.model.process.ProcessExecutionRecord;
import eu.slipo.workbench.common.model.process.ProcessExecutionStartException;
import eu.slipo.workbench.common.model.process.ProcessNotFoundException;
import eu.slipo.workbench.common.model.process.ProcessRecord;
import eu.slipo.workbench.common.model.resource.DataSource;
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
    private static Logger logger = LoggerFactory.getLogger(DefaultProcessOperatorTests.class);

    /**
     * A test fixture that represents a basic transform-to-rdf operation (using
     * triplegeo as the transformation tool).
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

            AccountEntity a = new AccountEntity("baz", "baz@example.com");
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
                    .filter(p -> p.getNameCount() == 1)
                    .map(p -> p.getFileName().toString())
                    .collect(Collectors.toList());

                final BidiMap<String,Path> inputNameToTempPath = new DualHashBidiMap<>();

                for (String inputName: inputNames) {
                    Path p = Files.createTempFile(stagingInputDir, null, ".csv");
                    Files.copy(inputDir.resolve(inputName), p, StandardCopyOption.REPLACE_EXISTING);
                    inputNameToTempPath.put(inputName, p);
                }

                this.transformFixtures = inputNames.stream()
                    .map(p -> new TransformFixture(
                        stripFilenameExtension(p),
                        stagingInputDir,
                        stagingInputDir.relativize(inputNameToTempPath.get(p)),
                        resultsDir.resolve(stripFilenameExtension(p) + ".nt"),
                        configuration))
                    .collect(Collectors.toList());
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
        throws ProcessNotFoundException, ProcessExecutionStartException
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

        ProcessRecord record = processRepository.create(definition, creatorId);
        assertNotNull(record);

        // Start process

        ProcessExecutionRecord executionRecord =
            processOperator.start(record.getId(), record.getVersion(), creatorId);
        assertNotNull(executionRecord);

    }

    @Test
    public void test1_transformAndRegister1() throws Exception
    {
        AccountEntity user = accountRepository.findOneByUsername("baz");
        assertNotNull(user);

        tranformAndRegister(transformFixtures.get(0), user.toDto());
    }

}