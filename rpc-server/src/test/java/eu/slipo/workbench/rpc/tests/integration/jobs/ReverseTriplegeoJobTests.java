package eu.slipo.workbench.rpc.tests.integration.jobs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
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
import org.springframework.util.StringUtils;

import eu.slipo.workbench.rpc.Application;

@RunWith(SpringRunner.class)
@ActiveProfiles({ "testing" })
@EnableAutoConfiguration
@SpringBootTest(classes = { Application.class }, webEnvironment = WebEnvironment.NONE)
public class ReverseTriplegeoJobTests extends AbstractJobTests
{
    private static Logger logger = LoggerFactory.getLogger(ReverseTriplegeoJobTests.class);

    private static final String JOB_NAME = "reverseTriplegeo";

    @TestConfiguration
    public static class Setup
    {
        @Autowired
        ResourceLoader resourceLoader;

        @Bean
        public List<Fixture> fixtures() throws IOException
        {
            final Resource root = resourceLoader.getResource("classpath:testcases/reverseTriplegeo/");

            final List<Fixture> fixtures = new ArrayList<>();

            // Add fixtures from src/test/resources

            for (String fixtureName: Arrays.asList("1")) {
                final Resource dir = root.createRelative(fixtureName + "/");
                Resource inputDir = dir.createRelative("input");
                Resource resultsDir = dir.createRelative("output");
                Resource optionsRes = dir.createRelative("options.conf");
                Properties parametersMap = new Properties();
                try (InputStream in = optionsRes.getInputStream()) {
                    parametersMap.load(in);
                }
                Resource queryRes = dir.createRelative("query.sparql");
                parametersMap.put("sparqlFile", queryRes.getURI());
                fixtures.add(Fixture.create(inputDir, resultsDir, parametersMap));
            }

            return fixtures;
        }
    }

    @Autowired
    @Qualifier("reverseTriplegeo.flow")
    private Flow reverseTriplegeoFlow;

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
        return "options";
    }

    @Override
    protected Flow jobFlow()
    {
        return reverseTriplegeoFlow;
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
                .filter(Files::isRegularFile)
                .filter(p -> "nt".equalsIgnoreCase(
                    StringUtils.getFilenameExtension(p.getFileName().toString())))
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

    @Test(timeout = 15 * 1000L)
    public void test1() throws Exception
    {
        testWithFixture(fixtures.get(0), this::extractInputParameters);
    }
}
