package eu.slipo.workbench.rpc.tests.integration.jobs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

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

import com.google.common.collect.ImmutableMap;

import eu.slipo.workbench.common.model.tool.LimesConfiguration;
import eu.slipo.workbench.rpc.Application;
import eu.slipo.workbench.rpc.jobs.LimesJobConfiguration;

@RunWith(SpringRunner.class)
@ActiveProfiles({ "testing" })
@SpringBootTest(classes = { Application.class }, webEnvironment = WebEnvironment.NONE)
public class LimesJobTests extends AbstractJobTests
{
    private static Logger logger = LoggerFactory.getLogger(LimesJobTests.class);

    private static final String CONFIG_KEY = LimesJobConfiguration.CONFIG_KEY;

    private static final String JOB_NAME = "limes";

    private static List<Fixture> fixtures = new ArrayList<>();

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

    @Autowired
    @Qualifier("limes.flow")
    private Flow limesFlow;

    @Override
    protected String configKey()
    {
        return CONFIG_KEY;
    }

    @Override
    protected Flow jobFlow()
    {
        return limesFlow;
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
        return ImmutableMap.of(
            "input.source", f.inputDir.resolve("a.nt").toString(),
            "input.target", f.inputDir.resolve("b.nt").toString());
    }

    protected Map<String, String> extractInputParametersAsSingletonMap(Fixture f)
    {
        String[] inputFiles = new String[] {
            f.inputDir.resolve("a.nt").toString(), // source
            f.inputDir.resolve("b.nt").toString()  // target
        };
        return Collections.singletonMap(
            "input", String.join(File.pathSeparator, inputFiles));
    }

    //
    // Tests
    //

    @Test(timeout = 10 * 1000L)
    public void test1() throws Exception
    {
        testWithFixture(fixtures.get(0), this::extractInputParameters);
    }

    @Test(timeout = 10 * 1000L)
    public void test1_single() throws Exception
    {
        testWithFixture(fixtures.get(0), this::extractInputParametersAsSingletonMap);
    }
}
