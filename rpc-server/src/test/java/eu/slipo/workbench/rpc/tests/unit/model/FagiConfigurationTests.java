package eu.slipo.workbench.rpc.tests.unit.model;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.common.model.tool.Fagi;
import eu.slipo.workbench.common.model.tool.FagiConfiguration;
import eu.slipo.workbench.common.model.tool.output.EnumFagiOutputPart;
import eu.slipo.workbench.common.model.tool.output.OutputPart;
import eu.slipo.workbench.common.model.tool.output.OutputSpec;
import eu.slipo.workbench.common.service.util.JsonBasedPropertiesConverterService;
import eu.slipo.workbench.common.service.util.PropertiesConverterService;

@RunWith(SpringRunner.class)
@ActiveProfiles({ "testing" })
public class FagiConfigurationTests
{
    @TestConfiguration
    public static class Setup
    {
        @Bean
        public ObjectMapper objectMapper()
        {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
            return objectMapper;
        }

        @Bean
        public XmlMapper xmlMapper()
        {
            XmlMapper xmlMapper = new XmlMapper();
            xmlMapper.registerModule(new JavaTimeModule());
            xmlMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
            return xmlMapper;
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
        public FagiConfiguration config1()
        {
            FagiConfiguration config = new FagiConfiguration();

            config.setInputFormat(EnumDataFormat.N_TRIPLES);

            config.setOutputFormat(EnumDataFormat.N_TRIPLES);
            config.setOutputDir("/var/local/fagi/output");

            config.setLang("el-GR");
            config.setSimilarity("jarowinkler");
            config.setRulesSpec("rules-1.xml");

            config.setLeft("a",
                "input/a.nt", "classpath:defaults/fagi/classification.csv", LocalDate.of(2018, 5, 21));
            config.setRight("b",
                "input/b.nt", "classpath:defaults/fagi/classification.csv", LocalDate.of(2018, 6, 1));
            config.setLinks("links", "input/links.nt");

            config.setTargetMode("aa");
            config.setTarget("target", "fused.nt", "remaining.nt", "review.nt", "stats.json");

            return config;
        }
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
    FagiConfiguration config1;


    void checkEquals(FagiConfiguration.Input expected, FagiConfiguration.Input actual)
    {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getPath(), actual.getPath());
        assertEquals(expected.getCategoriesLocation(), actual.getCategoriesLocation());
        assertEquals(expected.getDate(), actual.getDate());
    }

    void checkEquals(FagiConfiguration.Links expected, FagiConfiguration.Links actual)
    {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getPath(), actual.getPath());
    }

    void checkEquals(FagiConfiguration.Output expected, FagiConfiguration.Output actual)
    {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getOutputDir(), actual.getOutputDir());
        assertEquals(expected.getMode(), actual.getMode());
        assertEquals(expected.getFusedPath(), actual.getFusedPath());
        assertEquals(expected.getRemainingPath(), actual.getRemainingPath());
        assertEquals(expected.getReviewPath(), actual.getReviewPath());
        assertEquals(expected.getStatsPath(), actual.getStatsPath());
    }

    void checkEquals(FagiConfiguration expected, FagiConfiguration actual)
    {
        assertEquals(expected.getInputFormat(), actual.getInputFormat());
        assertEquals(expected.getOutputFormat(), actual.getOutputFormat());

        assertEquals(expected.getSimilarity(), actual.getSimilarity());
        assertEquals(expected.getRulesSpec(), actual.getRulesSpec());

        assertEquals(expected.getLang(), actual.getLang());

        checkEquals(expected.getLeft(), actual.getLeft());
        checkEquals(expected.getRight(), actual.getRight());
        checkEquals(expected.getLinks(), actual.getLinks());

        checkEquals(expected.getTarget(), actual.getTarget());
    }

    private void serializeAsJson(FagiConfiguration configuration) throws Exception
    {
        String s1 = objectMapper.writerWithDefaultPrettyPrinter()
            .writeValueAsString(configuration);

        FagiConfiguration deserializedConfiguration =
            objectMapper.readValue(s1, FagiConfiguration.class);
        checkEquals(configuration, deserializedConfiguration);

        String s1a = objectMapper.writerWithDefaultPrettyPrinter()
            .writeValueAsString(deserializedConfiguration);
        assertEquals(s1, s1a);
    }

    private void serializeAsXml(FagiConfiguration configuration) throws Exception
    {
        String s1 = xmlMapper.writerWithDefaultPrettyPrinter()
            .writeValueAsString(configuration);

        FagiConfiguration deserializedConfiguration =
            xmlMapper.readValue(s1, FagiConfiguration.class);
        checkEquals(configuration, deserializedConfiguration);

        String s1a = xmlMapper.writerWithDefaultPrettyPrinter()
            .writeValueAsString(deserializedConfiguration);
        assertEquals(s1, s1a);
    }

    private void serializeAsProperties(FagiConfiguration configuration) throws Exception
    {
        Properties p1 = propertiesConverter.valueToProperties(configuration);
        FagiConfiguration deserializedConfiguration =
            propertiesConverter.propertiesToValue(p1, FagiConfiguration.class);
        checkEquals(configuration, deserializedConfiguration);
    }

    private void serializeDefault(FagiConfiguration configuration) throws Exception
    {
        byte[] serializedData = null;
        try (ByteArrayOutputStream dataStream = new ByteArrayOutputStream()) {
            ObjectOutputStream out = new ObjectOutputStream(dataStream);
            out.writeObject(configuration);
            out.flush();
            serializedData = dataStream.toByteArray();
        }

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(serializedData));
        FagiConfiguration deserializedConfiguration = (FagiConfiguration) in.readObject();
        checkEquals(configuration, deserializedConfiguration);
    }

    private void validate(FagiConfiguration configuration)
    {
        Set<ConstraintViolation<FagiConfiguration>> violations = validator.validate(configuration);
        assertEquals(Collections.emptySet(), violations);
    }

    private void setInputMap(FagiConfiguration configuration) throws Exception
    {
        String s1 = objectMapper.writeValueAsString(configuration);
        FagiConfiguration config1a = objectMapper.readValue(s1, FagiConfiguration.class);

        final String leftPath = "/var/local/fagi/input/1.nt";
        final String rightPath = "/var/local/fagi/input/2.nt";
        final String linksPath = "/var/local/fagi/input/links-1-2.nt";

        final Map<String, String> inputMap = ImmutableMap.of(
            "left", leftPath, "right", rightPath, "links", linksPath);

        config1a.setInput(inputMap);

        String s1a = objectMapper.writeValueAsString(config1a);
        FagiConfiguration config1b = objectMapper.readValue(s1a, FagiConfiguration.class);

        final FagiConfiguration.Input leftInput = config1b.getLeft();
        final FagiConfiguration.Input rightInput = config1b.getRight();
        final FagiConfiguration.Links links = config1b.getLinks();

        assertEquals(leftPath, leftInput.getPath());
        assertEquals(leftPath, config1b.getLeftPath());
        assertEquals(configuration.getLeft().getId(), config1b.getLeft().getId());

        assertEquals(rightPath, rightInput.getPath());
        assertEquals(rightPath, config1b.getRightPath());
        assertEquals(configuration.getRight().getId(), config1b.getRight().getId());

        assertEquals(linksPath, links.getPath());
        assertEquals(linksPath, config1b.getLinksPath());
        assertEquals(configuration.getLinks().getId(), config1b.getLinks().getId());
    }

    private void setInputList(FagiConfiguration configuration) throws Exception
    {
        String s1 = objectMapper.writeValueAsString(configuration);
        FagiConfiguration config1a = objectMapper.readValue(s1, FagiConfiguration.class);

        final String leftPath = "/var/local/fagi/input/1.nt";
        final String rightPath = "/var/local/fagi/input/2.nt";
        final String linksPath = "/var/local/fagi/input/links-1-2.nt";

        config1a.setInput(Arrays.asList(leftPath, rightPath, linksPath));

        String s1a = objectMapper.writeValueAsString(config1a);
        FagiConfiguration config1b = objectMapper.readValue(s1a, FagiConfiguration.class);

        final FagiConfiguration.Input leftInput = config1b.getLeft();
        final FagiConfiguration.Input rightInput = config1b.getRight();
        final FagiConfiguration.Links links = config1b.getLinks();

        assertEquals(leftPath, leftInput.getPath());
        assertEquals(leftPath, config1b.getLeftPath());
        assertEquals(configuration.getLeft().getId(), config1b.getLeft().getId());

        assertEquals(rightPath, rightInput.getPath());
        assertEquals(rightPath, config1b.getRightPath());
        assertEquals(configuration.getRight().getId(), config1b.getRight().getId());

        assertEquals(linksPath, links.getPath());
        assertEquals(linksPath, config1b.getLinksPath());
        assertEquals(configuration.getLinks().getId(), config1b.getLinks().getId());
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
        Multimap<OutputPart<Fagi>, OutputSpec> outputMap = config1.getOutputNameMapper()
            .apply(Arrays.asList(
                "/var/local/limes/input/a.nt",
                "/var/local/limes/input/b.nt",
                "/var/local/limes/input/links.nt"));
        Multimap<OutputPart<Fagi>, String> outputNames =
            Multimaps.transformValues(outputMap, s -> s.fileName());

        assertEquals(Arrays.asList("fused.nt"), outputNames.get(EnumFagiOutputPart.FUSED));
        assertEquals(Arrays.asList("remaining.nt"), outputNames.get(EnumFagiOutputPart.REMAINING));
        assertEquals(Arrays.asList("review.nt"), outputNames.get(EnumFagiOutputPart.REVIEW));
        assertEquals(Arrays.asList("stats.json"), outputNames.get(EnumFagiOutputPart.STATS));
    }
}
