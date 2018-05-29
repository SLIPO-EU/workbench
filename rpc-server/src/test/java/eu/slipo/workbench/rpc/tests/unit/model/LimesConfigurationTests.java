package eu.slipo.workbench.rpc.tests.unit.model;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Properties;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

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

import eu.slipo.workbench.common.model.tool.Limes;
import eu.slipo.workbench.common.model.tool.LimesConfiguration;
import eu.slipo.workbench.common.model.tool.output.EnumLimesOutputPart;
import eu.slipo.workbench.common.model.tool.output.EnumOutputType;
import eu.slipo.workbench.common.model.tool.output.OutputPart;
import eu.slipo.workbench.common.service.util.JsonBasedPropertiesConverterService;
import eu.slipo.workbench.common.service.util.PropertiesConverterService;

@RunWith(SpringRunner.class)
@ActiveProfiles({ "testing" })
public class LimesConfigurationTests
{
    @TestConfiguration
    public static class Setup
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
        public Validator validator()
        {
            ValidatorFactory validationFactory = Validation.buildDefaultValidatorFactory();
            return validationFactory.getValidator();
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
    Validator validator;

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

    private void serializeAsJson(LimesConfiguration configuration) throws Exception
    {
        String s1 = objectMapper.writerWithDefaultPrettyPrinter()
            .writeValueAsString(configuration);

        LimesConfiguration deserializedConfiguration =
            objectMapper.readValue(s1, LimesConfiguration.class);
        checkEquals(configuration, deserializedConfiguration);

        String s1a = objectMapper.writerWithDefaultPrettyPrinter()
            .writeValueAsString(deserializedConfiguration);
        assertEquals(s1, s1a);
    }

    private void serializeAsXml(LimesConfiguration configuration) throws Exception
    {
        String s1 = xmlMapper.writerWithDefaultPrettyPrinter()
            .writeValueAsString(configuration);

        LimesConfiguration deserializedConfiguration =
            xmlMapper.readValue(s1, LimesConfiguration.class);
        checkEquals(configuration, deserializedConfiguration);

        String s1a = xmlMapper.writerWithDefaultPrettyPrinter()
            .writeValueAsString(deserializedConfiguration);
        assertEquals(s1, s1a);
    }

    private void serializeAsProperties(LimesConfiguration configuration) throws Exception
    {
        Properties p1 = propertiesConverter.valueToProperties(configuration);
        LimesConfiguration deserializedConfiguration =
            propertiesConverter.propertiesToValue(p1, LimesConfiguration.class);
        checkEquals(configuration, deserializedConfiguration);
    }

    private void serializeDefault(LimesConfiguration configuration) throws Exception
    {
        byte[] serializedData = null;
        try (ByteArrayOutputStream dataStream = new ByteArrayOutputStream()) {
            ObjectOutputStream out = new ObjectOutputStream(dataStream);
            out.writeObject(configuration);
            out.flush();
            serializedData = dataStream.toByteArray();
        }

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(serializedData));
        LimesConfiguration deserializedConfiguration = (LimesConfiguration) in.readObject();
        checkEquals(configuration, deserializedConfiguration);
    }

    private void validate(LimesConfiguration configuration)
    {
        Set<ConstraintViolation<LimesConfiguration>> violations = validator.validate(configuration);
        assertEquals(Collections.emptySet(), violations);
    }

    private void setInputMap(LimesConfiguration configuration) throws Exception
    {
        String s1 = objectMapper.writeValueAsString(configuration);
        LimesConfiguration config1a = objectMapper.readValue(s1, LimesConfiguration.class);

        final String sourcePath = "/tmp/a.nt";
        final String targetPath = "/tmp/b.nt";
        final Map<String, String> inputMap = ImmutableMap.of("source", sourcePath, "target", targetPath);

        config1a.setInput(inputMap);

        String s1a = objectMapper.writeValueAsString(config1a);
        LimesConfiguration config1b = objectMapper.readValue(s1a, LimesConfiguration.class);

        final LimesConfiguration.Input sourceInput = config1b.getSource();
        final LimesConfiguration.Input targetInput = config1b.getTarget();

        assertEquals(sourcePath, sourceInput.getPath());
        assertEquals(sourcePath, config1b.getSourcePath());
        assertEquals(configuration.getSource().getId(), config1b.getSource().getId());

        assertEquals(targetPath, targetInput.getPath());
        assertEquals(targetPath, config1b.getTargetPath());
        assertEquals(configuration.getTarget().getId(), config1b.getTarget().getId());

        checkEquals(configuration.getAccepted(), config1b.getAccepted());
        checkEquals(configuration.getReview(), config1b.getReview());
    }

    private void setInputList(LimesConfiguration configuration) throws Exception
    {
        String s1 = objectMapper.writeValueAsString(configuration);
        LimesConfiguration config1a = objectMapper.readValue(s1, LimesConfiguration.class);

        final String sourcePath = "/tmp/a.nt";
        final String targetPath = "/tmp/b.nt";

        config1a.setInput(Arrays.asList(sourcePath, targetPath));

        String s1a = objectMapper.writeValueAsString(config1a);
        LimesConfiguration config1b = objectMapper.readValue(s1a, LimesConfiguration.class);

        final LimesConfiguration.Input sourceInput = config1b.getSource();
        final LimesConfiguration.Input targetInput = config1b.getTarget();

        assertEquals(sourcePath, sourceInput.getPath());
        assertEquals(sourcePath, config1b.getSourcePath());
        assertEquals(configuration.getSource().getId(), config1b.getSource().getId());

        assertEquals(targetPath, targetInput.getPath());
        assertEquals(targetPath, config1b.getTargetPath());
        assertEquals(configuration.getTarget().getId(), config1b.getTarget().getId());

        checkEquals(configuration.getAccepted(), config1b.getAccepted());
        checkEquals(configuration.getReview(), config1b.getReview());
    }

    //
    // Tests
    //

    @Test
    public void test1_serializeAsJson() throws Exception
    {
        serializeAsJson(config1);
    }

    @Test
    public void test1_serializeAsXml() throws Exception
    {
        serializeAsXml(config1);
    }

    @Test
    public void test1_serializeAsProperties() throws Exception
    {
        serializeAsProperties(config1);
    }

    @Test
    public void test1_serializeDefault() throws Exception
    {
        serializeDefault(config1);
    }

    @Test
    public void test1_validate() throws Exception
    {
        validate(config1);
    }

    @Test
    public void test1_setInputMap() throws Exception
    {
        setInputMap(config1);
    }

    @Test
    public void test1_setInputList() throws Exception
    {
        setInputList(config1);
    }

    @Test
    public void test1_getOutputNames() throws Exception
    {
        Map<? extends OutputPart<Limes>, List<String>> outputMap =
            config1.getOutputNameMapper().apply(Arrays.asList("/data/a.nt", "/data/b.nt"));

        assertEquals(Collections.singletonList("accepted.nt"), outputMap.get(EnumLimesOutputPart.ACCEPTED));
        assertEquals(Collections.singletonList("review.nt"), outputMap.get(EnumLimesOutputPart.REVIEW));
    }
}
