package eu.slipo.workbench.rpc.tests.integration.jobs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
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
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import eu.slipo.workbench.rpc.Application;
import eu.slipo.workbench.rpc.jobs.TriplegeoJobConfiguration;

@RunWith(SpringRunner.class)
@ActiveProfiles({ "testing" })
@SpringBootTest(classes = { Application.class }, webEnvironment = WebEnvironment.NONE)
public class TriplegeoJobTests extends AbstractJobTests
{
    private static Logger logger = LoggerFactory.getLogger(TriplegeoJobTests.class);

    private static final String CONFIG_KEY = TriplegeoJobConfiguration.CONFIG_KEY;

    private static final String JOB_NAME = "triplegeo";

    private static final List<Fixture> fixtures = new ArrayList<>();

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        // Add fixtures from src/test/resources

        for (String path: Arrays.asList("csv-1")) {
            Path inputDir = getResource(JOB_NAME, path, "input");
            Path resultsDir = getResource(JOB_NAME, path, "output");
            Path parametersFile = getResource(JOB_NAME, path, "config.properties");
            Properties parametersMap = new Properties();
            try (BufferedReader in = Files.newBufferedReader(parametersFile)) {
                parametersMap.load(in);
            }
            fixtures.add(new Fixture(inputDir, resultsDir, parametersMap));
        }
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {}

    @Autowired
    @Qualifier("triplegeo.flow")
    private Flow triplegeoFlow;

    @Before
    public void setUp() throws Exception {}

    @After
    public void tearDown() throws Exception {}

    @Override
    protected String configKey()
    {
        return CONFIG_KEY;
    }

    @Override
    protected Flow jobFlow()
    {
        return triplegeoFlow;
    }

    @Override
    protected String jobName()
    {
        return JOB_NAME;
    }

    @Override
    protected void info(String msg, Object... args)
    {
        logger.info(msg, args);
    }

    @Override
    protected void warn(String msg, Object... args)
    {
        logger.warn(msg, args);
    }

    protected Map<String, String> extractInputParameters(Fixture f)
    {
        String inputPathsAsString = null;

        try (Stream<Path> inputPaths = Files.list(f.inputDir)) {
            inputPathsAsString = inputPaths
                .sorted()
                .map(p -> p.toAbsolutePath().toString())
                .collect(Collectors.joining(File.pathSeparator));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        return Collections.singletonMap("input", inputPathsAsString);
    }

    //
    // Tests
    //

    @Test(timeout = 10 * 1000L)
    public void test1() throws Exception
    {
        testWithFixture(fixtures.get(0), this::extractInputParameters);
    }
}
