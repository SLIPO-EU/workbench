package eu.slipo.workbench.rpc.tests.unit.model;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.collect.ImmutableMap;

import eu.slipo.workbench.common.model.tool.LimesConfiguration;
import eu.slipo.workbench.common.service.util.JsonBasedPropertiesConverterService;
import eu.slipo.workbench.common.service.util.PropertiesConverterService;

@RunWith(SpringRunner.class)
@ActiveProfiles({ "testing" })
public class LimesConfigurationTests
{
    @TestConfiguration
    public static class Configuration
    {
        @Bean
        public ObjectMapper objectMapper()
        {
            return new ObjectMapper();
        }

        @Bean
        public XmlMapper xmlMapper()
        {
            return new XmlMapper();
        }

        @Bean
        public PropertiesConverterService propertiesConverterService(ObjectMapper objectMapper)
        {
            return new JsonBasedPropertiesConverterService(objectMapper);
        }

        @Bean
        public LimesConfiguration config1()
        {
            LimesConfiguration config = new LimesConfiguration();

            config.addPrefix("example", "http://example.com/schema/def#");

            config.setMetric("trigrams(a.level, b.level)");

            config.setSource("a",
                "/tmp/limes/input/a.nt",
                "?x",
                "slipo:name/slipo:nameType RENAME label");
            config.setTarget("b",
                "/tmp/limes/input/b.nt",
                "?y",
                "slipo:name/slipo:nameType RENAME label");

            config.setOutputDir("/tmp/limes/output");
            config.setOutputFormatFromString("N-TRIPLES");
            config.setAccepted(0.98, "accepted.nt");
            config.setReview(0.95, "review.nt");

            return config;
        }
    }

    @Before
    public void setUp() throws Exception
    {
    }

    @After
    public void tearDown() throws Exception
    {
    }

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ObjectMapper xmlMapper;

    @Autowired
    PropertiesConverterService propertiesConverter;

    @Autowired
    LimesConfiguration config1;

    void checkEquals(LimesConfiguration.Input expected, LimesConfiguration.Input actual)
    {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getPath(), actual.getPath());
        assertEquals(expected.getVarName(), actual.getVarName());
        assertEquals(expected.getPageSize(), actual.getPageSize());
        assertEquals(new HashSet<>(expected.getFilterExprs()), new HashSet<>(actual.getFilterExprs()));
        assertEquals(new HashSet<>(expected.getPropertyExprs()), new HashSet<>(actual.getPropertyExprs()));
        assertEquals(expected.getDataFormat(), actual.getDataFormat());
    }

    void checkEquals(LimesConfiguration.Output expected, LimesConfiguration.Output actual)
    {
        assertEquals(expected.getThreshold(), actual.getThreshold());
        assertEquals(expected.getPath(), actual.getPath());
        assertEquals(expected.getRelation(), actual.getRelation());
    }

    void checkEquals(LimesConfiguration.Execution expected, LimesConfiguration.Execution actual)
    {
        assertEquals(expected.getEngineName(), actual.getEngineName());
        assertEquals(expected.getRewriterName(), actual.getRewriterName());
        assertEquals(expected.getPlannerName(), actual.getPlannerName());
    }

    void checkEquals(LimesConfiguration expected, LimesConfiguration actual)
    {
        NavigableSet<LimesConfiguration.Prefix> actualPrefixes = actual.getPrefixes();
        for (LimesConfiguration.Prefix expectedPrefix: expected.getPrefixes()) {
            LimesConfiguration.Prefix actualPrefix = actualPrefixes.floor(expectedPrefix);
            assertEquals(expectedPrefix.getNamespace(), actualPrefix.getNamespace());
            assertEquals(expectedPrefix.getLabel(), actualPrefix.getLabel());
        }

        checkEquals(expected.getSource(), actual.getSource());
        checkEquals(expected.getTarget(), actual.getTarget());

        assertEquals(expected.getMetric(), actual.getMetric());

        assertEquals(expected.getOutputFormat(), actual.getOutputFormat());
        checkEquals(expected.getAccepted(), actual.getAccepted());
        checkEquals(expected.getReview(), actual.getReview());

        checkEquals(expected.getExecutionParams(), actual.getExecutionParams());
    }

    @Test
    public void test1_serializeAsJson() throws Exception
    {
        String s1 = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(config1);

        LimesConfiguration config1a = objectMapper.readValue(s1, LimesConfiguration.class);
        checkEquals(config1, config1a);

        String s1a = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(config1a);
        assertEquals(s1, s1a);
    }

    @Test
    public void test1_serializeAsXml() throws Exception
    {
        String s1 = xmlMapper.writerWithDefaultPrettyPrinter().writeValueAsString(config1);

        LimesConfiguration config1a = xmlMapper.readValue(s1, LimesConfiguration.class);
        checkEquals(config1, config1a);

        String s1a = xmlMapper.writerWithDefaultPrettyPrinter().writeValueAsString(config1a);
        assertEquals(s1, s1a);
    }

    @Test
    public void test1_serializeAsProperties() throws Exception
    {
        Properties p1 = propertiesConverter.valueToProperties(config1);

        LimesConfiguration config1a =
            propertiesConverter.propertiesToValue(p1, LimesConfiguration.class);
        checkEquals(config1, config1a);
    }

    @Test
    public void test1_serializeDefault() throws Exception
    {
        byte[] serializedData = null;
        try (ByteArrayOutputStream dataStream = new ByteArrayOutputStream()) {
            ObjectOutputStream out = new ObjectOutputStream(dataStream);
            out.writeObject(config1);
            out.flush();
            serializedData = dataStream.toByteArray();
        }

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(serializedData));
        LimesConfiguration config1a = (LimesConfiguration) in.readObject();
        checkEquals(config1, config1a);
    }

    @Test
    public void test1_setInputPaths() throws Exception
    {
        String s1 = objectMapper.writeValueAsString(config1);
        LimesConfiguration config1a = objectMapper.readValue(s1, LimesConfiguration.class);

        final String sourcePath = "/tmp/a-1.nt";
        final String targetPath = "/tmp/b-1.nt";
        final Map<String, String> inputMap = ImmutableMap.of("source", sourcePath, "target", targetPath);

        config1a.setInput(inputMap);
        String s1a = objectMapper.writeValueAsString(config1a);
        LimesConfiguration config1b = objectMapper.readValue(s1a, LimesConfiguration.class);

        final LimesConfiguration.Input sourceInput = config1b.getSource();
        final LimesConfiguration.Input targetInput = config1b.getTarget();

        assertEquals(sourcePath, sourceInput.getPath());
        assertEquals(sourcePath, config1b.getSourcePath());
        assertEquals(targetPath, targetInput.getPath());
        assertEquals(targetPath, config1b.getTargetPath());

        checkEquals(config1.getAccepted(), config1b.getAccepted());
        checkEquals(config1.getReview(), config1b.getReview());
    }
}
