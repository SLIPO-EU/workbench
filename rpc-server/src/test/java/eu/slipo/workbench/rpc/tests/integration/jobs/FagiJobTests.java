package eu.slipo.workbench.rpc.tests.integration.jobs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

import eu.slipo.workbench.rpc.Application;

@RunWith(SpringRunner.class)
@ActiveProfiles({ "testing" })
@EnableAutoConfiguration
@SpringBootTest(classes = { Application.class }, webEnvironment = WebEnvironment.NONE)
public class FagiJobTests extends AbstractJobTests
{
    private static Logger logger = LoggerFactory.getLogger(FagiJobTests.class);

    private static final String JOB_NAME = "fagi";

    @TestConfiguration
    public static class Setup
    {
        @Autowired
        ResourceLoader resourceLoader;

        @Bean
        public Map<String, Fixture> fixtures() throws IOException
        {
            final Resource root = resourceLoader.getResource("classpath:testcases/fagi/");

            final Map<String, Fixture> fixtures = new LinkedHashMap<>();

            //  Add fixtures from src/test/resources

            for (String fixtureName: Arrays.asList("1", "1-csv")) {
                final Resource dir = root.createRelative(fixtureName + "/");
                Resource inputDir = dir.createRelative("input");
                Resource resultsDir = dir.createRelative("output");
                Resource specResource = dir.createRelative("spec.properties");
                Properties parametersMap = new Properties();
                try (InputStream in = specResource.getInputStream()) {
                    parametersMap.load(in);
                }
                parametersMap.put("rulesSpec", dir.createRelative("rules.xml").getURI());
                fixtures.put(fixtureName, Fixture.create(inputDir, resultsDir, parametersMap));
            }

            return fixtures;
        }
    }

    @Autowired
    @Qualifier("fagi.flow")
    private Flow fagiFlow;

    @Autowired
    private Map<String, Fixture> fixtures;

    @Override
    protected String configKey()
    {
        return "rules";
    }

    @Override
    protected Flow jobFlow()
    {
        return fagiFlow;
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
        final String leftFileName = "a.nt";
        final String rightFileName = "b.nt";
        // The links file may be provided either as NT or CSV
        final String linksFileName = "nt".equalsIgnoreCase(f.parameters.getString("links.linksFormat"))?
            "links.nt" : "links.csv";

        String inputAsString = Stream.of(leftFileName, rightFileName, linksFileName)
            .map(name -> f.inputDir.resolve(name).toString())
            .collect(Collectors.joining(File.pathSeparator));

        return Collections.singletonMap("input", inputAsString);
    }

    @Override
    protected boolean checkForEqualResults()
    {
        return false;
    }

    //
    // Tests
    //

    @Test(timeout = 25 * 1000L)
    public void test1() throws Exception
    {
        testWithFixture(fixtures.get("1"), this::extractInputParameters);
    }

    @Test(timeout = 25 * 1000L)
    public void test1_csvLinks() throws Exception
    {
        testWithFixture(fixtures.get("1-csv"), this::extractInputParameters);
    }
}
