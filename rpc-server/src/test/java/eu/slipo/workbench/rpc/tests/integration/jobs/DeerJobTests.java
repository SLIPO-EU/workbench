package eu.slipo.workbench.rpc.tests.integration.jobs;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.rpc.Application;

@RunWith(SpringRunner.class)
@ActiveProfiles({ "testing" })
@EnableAutoConfiguration
@SpringBootTest(classes = { Application.class }, webEnvironment = WebEnvironment.NONE)
public class DeerJobTests extends AbstractJobTests
{
    private static Logger logger = LoggerFactory.getLogger(DeerJobTests.class);

    private static final String JOB_NAME = "deer";

    @TestConfiguration
    public static class Setup
    {
        @Autowired
        ResourceLoader resourceLoader;

        @Bean
        public List<Fixture> fixtures() throws IOException
        {
            final Resource root = resourceLoader.getResource("classpath:testcases/deer/");

            final List<Fixture> fixtures = new ArrayList<>();

            for (String fixtureName: Arrays.asList("1")) {
                final Resource dir = root.createRelative(fixtureName + "/");
                Resource inputDir = dir.createRelative("input");
                Resource resultsDir = dir.createRelative("output");
                Properties parametersMap = new Properties();
                parametersMap.put("inputFormat", EnumDataFormat.N_TRIPLES.name());
                parametersMap.put("outputFormat", EnumDataFormat.N_TRIPLES.name());
                parametersMap.put("spec", dir.createRelative("config.ttl").getURI());
                fixtures.add(Fixture.create(inputDir, resultsDir, parametersMap));
            }

            return fixtures;
        }
    }

    @Autowired
    @Qualifier("deer.flow")
    private Flow deerFlow;

    @Autowired
    private List<Fixture> fixtures;

    @Override
    protected String jobName()
    {
        return JOB_NAME;
    }

    @Override
    protected String configKey()
    {
        return "config";
    }

    @Override
    protected Flow jobFlow()
    {
        return deerFlow;
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
        final Path inputPath =  f.inputDir.resolve("fused.nt");
        return Collections.singletonMap("input", inputPath.toString());
    }

    @Override
    protected boolean checkForEqualResults()
    {
        return false;
    }

    //
    // Tests
    //

    @Test(timeout = 30 * 1000L)
    public void test1() throws Exception
    {
        testWithFixture(fixtures.get(0), this::extractInputParameters);
    }
}
