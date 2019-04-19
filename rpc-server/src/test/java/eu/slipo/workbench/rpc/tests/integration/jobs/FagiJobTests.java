package eu.slipo.workbench.rpc.tests.integration.jobs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
        public List<Fixture> fixtures() throws IOException
        {
            final Resource root = resourceLoader.getResource("classpath:testcases/fagi/");

            final List<Fixture> fixtures = new ArrayList<>();

            //  Add fixtures from src/test/resources

            for (String fixtureName: Arrays.asList("1")) {
                final Resource dir = root.createRelative(fixtureName + "/");
                Resource inputDir = dir.createRelative("input");
                Resource resultsDir = dir.createRelative("output");
                Resource specResource = dir.createRelative("spec.properties");
                Properties parametersMap = new Properties();
                try (InputStream in = specResource.getInputStream()) {
                    parametersMap.load(in);
                }
                parametersMap.put("rulesSpec", dir.createRelative("rules.xml").getURI());
                fixtures.add(Fixture.create(inputDir, resultsDir, parametersMap));
            }

            return fixtures;
        }
    }

    @Autowired
    @Qualifier("fagi.flow")
    private Flow fagiFlow;

    @Autowired
    private List<Fixture> fixtures;

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
        String inputAsString = Stream.of("a.nt", "b.nt", "links.nt")
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
        testWithFixture(fixtures.get(0), this::extractInputParameters);
    }
}
