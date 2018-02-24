package eu.slipo.workbench.rpc.tests.integration.jobs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.test.AssertFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.slipo.workbench.rpc.Application;
import eu.slipo.workbench.rpc.jobs.TriplegeoJobConfiguration;

@RunWith(SpringRunner.class)
@ActiveProfiles({ "testing" })
@SpringBootTest(classes = { Application.class }, webEnvironment = WebEnvironment.NONE)
public class TriplegeoJobTests
{
    private static Logger logger = LoggerFactory.getLogger(TriplegeoJobTests.class);

    private static class Fixture
    {
        final Path inputDir;

        final Path resultsDir;

        final JobParameters parameters;

        Fixture(Path inputDir, Path expectedResultsDir, Map<String,Object> parametersMap)
            throws IOException
        {
            Assert.notNull(inputDir, "Expected a non-null input directory");
            Assert.isTrue(Files.isDirectory(inputDir) && Files.isReadable(inputDir),
                "Expected a readable input directory");
            this.inputDir = inputDir;

            Assert.notNull(expectedResultsDir, "Expected a non-null input directory");
            Assert.isTrue(
                Files.isDirectory(expectedResultsDir) && Files.isReadable(expectedResultsDir),
                "Expected a readable directory of expected results");
            this.resultsDir = expectedResultsDir;

            JobParametersBuilder parametersBuilder = new JobParametersBuilder();
            parametersBuilder.addString("_id", Long.toHexString(System.currentTimeMillis()));

            parametersMap.forEach((name, value) -> {
                if (value instanceof Date)
                    parametersBuilder.addDate(name, (Date) value);
                else if (value instanceof Double)
                    parametersBuilder.addDouble(name, (Double) value);
                else if (value instanceof Number)
                    parametersBuilder.addLong(name, ((Number) value).longValue());
                else
                    parametersBuilder.addString(name, value.toString());
            });
            this.parameters = parametersBuilder.toJobParameters();
        }

        Fixture(URL inputUrl, URL resultsUrl,  Map<String,Object> parametersMap)
            throws IOException
        {
            this(Paths.get(inputUrl.getPath()), Paths.get(resultsUrl.getPath()), parametersMap);
        }
    }

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private JobOperator jobOperator;

    @Autowired
    private JobExplorer jobExplorer;

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    @Qualifier("triplegeo.flow")
    private Flow triplegeoFlow;

    @Autowired
    private Path jobDataDirectory;

    private static final String CONFIG_NAME = TriplegeoJobConfiguration.CONFIG_KEY;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        Class<?> cls = TriplegeoJobTests.class;

        ObjectMapper jsonMapper = new ObjectMapper();
        TypeReference<Map<String, Object>> parametersMapType = new TypeReference<Map<String, Object>>() {};

        // Add fixtures from src/test/resources

        final String rootPath = "/testcases/triplegeo";

        for (String path: Arrays.asList("csv/1")) {
            URL inputDir = cls.getResource(Paths.get(rootPath, path, "input").toString());
            URL resultsDir = cls.getResource(Paths.get(rootPath, path, "output").toString());

            File f = new File(cls.getResource(
                Paths.get(rootPath, path, "parameters.json").toString()).getPath());
            Map<String, Object> parametersMap = jsonMapper.readValue(f, parametersMapType);

            fixtures.add(new Fixture(inputDir, resultsDir, parametersMap));
        }

    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception
    {
    }

    private static List<Fixture> fixtures = new ArrayList<>();

    @Before
    public void setUp() throws Exception
    {
    }

    @After
    public void tearDown() throws Exception
    {
    }

    @Test(timeout = 10 * 1000L)
    public void test1() throws Exception
    {
        testFixture(fixtures.get(0));
    }

    private void testFixture(final Fixture fixture) throws Exception
    {
        // Build parameters

        Stream<Path> inputPaths = Files.list(fixture.inputDir)
            .sorted()
            .map(Path::toAbsolutePath);
        JobParameters parameters = new JobParametersBuilder(fixture.parameters)
            .addString(
                "input",
                inputPaths.map(Path::toString).collect(Collectors.joining(File.pathSeparator)))
            .toJobParameters();

        // Setup listeners

        final CountDownLatch done = new CountDownLatch(1);

        final AtomicReference<Path> workDirReference = new AtomicReference<>();
        final AtomicReference<Path> outputDirReference = new AtomicReference<>();
        final AtomicReference<Path> configFileReference = new AtomicReference<>();

        JobExecutionListener listener = new JobExecutionListenerSupport()
        {
            @Override
            public void afterJob(JobExecution jobExecution)
            {
                ExecutionContext executionContext = jobExecution.getExecutionContext();
                if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
                    if (executionContext.containsKey("workDir"))
                        workDirReference.set(
                            Paths.get(executionContext.getString("workDir")));

                    if (executionContext.containsKey("outputDir"))
                        outputDirReference.set(
                            Paths.get(executionContext.getString("outputDir")));

                    if (executionContext.containsKey("configByName")) {
                        Map<?,?> configByName = (Map<?,?>) executionContext.get("configByName");
                        Path configPath = configByName == null?
                            null : Paths.get(configByName.get(CONFIG_NAME).toString());
                        if (configPath != null)
                            configFileReference.set(configPath);
                    }
                }
                // Done
                done.countDown();
            }
        };

        // Build job from flow

        Job job = jobBuilderFactory.get("triplegeo")
            .start(triplegeoFlow)
                .end()
            .listener(listener)
            .build();

        // Launch job and wait

        jobLauncher.run(job, parameters);
        done.await();

        // Check results

        final Path workDir = workDirReference.get();
        assertTrue(workDir != null && workDir.isAbsolute()
            && Files.isDirectory(workDir)
            && workDir.startsWith(jobDataDirectory));

        logger.debug("The job has completed succesfully: workDir={}", workDir);

        final Path configFile = configFileReference.get();
        assertTrue(configFile != null && !configFile.isAbsolute());
        final Path configPath = workDir.resolve(configFile);
        assertTrue(configPath != null && Files.isRegularFile(configPath));
        final Path outputDir = outputDirReference.get();
        assertTrue(outputDir != null && Files.isDirectory(outputDir)
            && outputDir.startsWith(jobDataDirectory));

        final List<Path> expectedResults =
            Files.list(fixture.resultsDir).collect(Collectors.toList());
        final List<Path> actualResults =
            Files.list(outputDir).collect(Collectors.toList());
        assertEquals(expectedResults.size(), actualResults.size());

        for (Path expectedResult: expectedResults) {
            Path name = expectedResult.getFileName();
            Path result = outputDir.resolve(name);
            logger.debug("Comparing actual {} vs expected {}", result, expectedResult);
            AssertFile.assertFileEquals(expectedResult.toFile(), result.toFile());
        }
    }
}
