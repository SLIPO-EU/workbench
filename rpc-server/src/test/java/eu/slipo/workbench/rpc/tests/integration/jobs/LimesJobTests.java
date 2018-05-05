package eu.slipo.workbench.rpc.tests.integration.jobs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.google.common.collect.ImmutableMap;

import eu.slipo.workbench.rpc.Application;

@RunWith(SpringRunner.class)
@ActiveProfiles({ "testing" })
@EnableAutoConfiguration
@SpringBootTest(classes = { Application.class }, webEnvironment = WebEnvironment.NONE)
public class LimesJobTests extends AbstractJobTests
{
    private static Logger logger = LoggerFactory.getLogger(LimesJobTests.class);

    private static final String JOB_NAME = "limes";

    @TestConfiguration
    public static class Setup
    {
        @Autowired
        ResourceLoader resourceLoader;

        @Bean
        public List<Fixture> fixtures() throws IOException
        {
            final Resource root = resourceLoader.getResource("classpath:testcases/limes/");

            final List<Fixture> fixtures = new ArrayList<>();

            // Add fixtures from src/test/resources

            for (String fixtureName: Arrays.asList("1")) {
                final Resource dir = root.createRelative(fixtureName + "/");
                Resource inputDir = dir.createRelative("input");
                Resource resultsDir = dir.createRelative("output");
                Resource configRes = dir.createRelative("config.properties");
                Properties parametersMap = new Properties();
                try (InputStream in = configRes.getInputStream()) {
                    parametersMap.load(in);
                }
                fixtures.add(Fixture.create(inputDir, resultsDir, parametersMap));
            }

            return fixtures;
        }
    }

    @Autowired
    @Qualifier("limes.flow")
    private Flow limesFlow;

    @Autowired
    private List<Fixture> fixtures;

    @Override
    protected String configKey()
    {
        return "config";
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
    public void test1_singleInputParameter() throws Exception
    {
        testWithFixture(fixtures.get(0), this::extractInputParametersAsSingletonMap);
    }
}
