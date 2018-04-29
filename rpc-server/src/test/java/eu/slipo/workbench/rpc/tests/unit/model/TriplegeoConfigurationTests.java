package eu.slipo.workbench.rpc.tests.unit.model;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Map;

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

import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.common.model.poi.EnumOutputType;
import eu.slipo.workbench.common.model.tool.TriplegeoConfiguration;
import eu.slipo.workbench.common.service.util.JsonBasedPropertiesConverterService;
import eu.slipo.workbench.common.service.util.PropertiesConverterService;

@RunWith(SpringRunner.class)
@ActiveProfiles({ "testing" })
public class TriplegeoConfigurationTests
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
        public TriplegeoConfiguration config1()
        {
            TriplegeoConfiguration config = new TriplegeoConfiguration();

            config.setMode(TriplegeoConfiguration.Mode.STREAM);
            config.setInputFormat("CSV");
            config.setInputFromString("/tmp/triplegeo/input/p1.csv:/tmp/triplegeo/input/p2.csv");
            config.setOutputFormat(EnumDataFormat.N_TRIPLES);
            config.setOutputDir("/tmp/triplegeo/output");
            config.setMappingSpec("/tmp/triplegeo/mappings.yml");
            config.setClassificationSpec("/tmp/triplegeo/classification.yml");

            config.setAttrCategory("category");
            config.setAttrKey("id");
            config.setAttrName("name");
            config.setAttrX("lon");
            config.setAttrX("lat");

            config.setFeatureSource("points");
            config.addPrefix("foo", "http://example.com/foo#");

            return config;
        }

        @Bean
        public TriplegeoConfiguration config2()
        {
            TriplegeoConfiguration config = new TriplegeoConfiguration();

            config.setMode(TriplegeoConfiguration.Mode.STREAM);
            config.setInputFormat(EnumDataFormat.CSV);
            config.setInput(Arrays.asList(
                "/tmp/triplegeo/input/p1.csv",
                "/tmp/triplegeo/input/p2.csv"));
            config.setOutputFormat(EnumDataFormat.TURTLE);;
            config.setOutputDir("/tmp/triplegeo/output");
            config.setMappingSpec("/tmp/triplegeo/mappings.yml");
            config.setClassificationSpec("/tmp/triplegeo/classification.yml");

            config.setAttrCategory("category");
            config.setAttrKey("id");
            config.setAttrName("name");
            config.setAttrX("lon");
            config.setAttrX("lat");

            config.setFeatureSource("points");
            config.addPrefix("foo", "http://example.com/foo#");

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
    TriplegeoConfiguration config1;

    @Autowired
    TriplegeoConfiguration config2;

    void checkEquals(TriplegeoConfiguration expected, TriplegeoConfiguration actual)
    {
        assertEquals(expected.getMode(), actual.getMode());
        assertEquals(expected.getInputFormat(), actual.getInputFormat());
        assertEquals(expected.getInput(), actual.getInput());
        assertEquals(expected.getOutputFormat(), actual.getOutputFormat());
        assertEquals(expected.getSerializationFormat(), actual.getSerializationFormat());
        assertEquals(expected.getOutputDir(), actual.getOutputDir());

        assertEquals(expected.getAttrCategory(), actual.getAttrCategory());
        assertEquals(expected.getAttrKey(), actual.getAttrKey());
        assertEquals(expected.getAttrName(), actual.getAttrName());
        assertEquals(expected.getAttrX(), actual.getAttrX());
        assertEquals(expected.getAttrY(), actual.getAttrY());

        assertEquals(expected.getTargetGeoOntology(), actual.getTargetGeoOntology());

        assertEquals(expected.getMappingSpec(), actual.getMappingSpec());

        assertEquals(expected.getClassificationSpec(), actual.getClassificationSpec());
        assertEquals(expected.getClassifyByName(), actual.getClassifyByName());

        assertEquals(expected.getFeatureSource(), actual.getFeatureSource());
        assertEquals(expected.getFeatureNamespaceUri(), actual.getFeatureNamespaceUri());
        assertEquals(expected.getGeometryNamespaceUri(), actual.getGeometryNamespaceUri());
        assertEquals(expected.getClassNamespaceUri(), actual.getClassNamespaceUri());
        assertEquals(expected.getClassificationNamespaceUri(), actual.getClassificationNamespaceUri());
        assertEquals(expected.getDatasourceNamespaceUri(), actual.getDatasourceNamespaceUri());

        assertEquals(expected.getPrefixes(), actual.getPrefixes());

        assertEquals(expected.getDelimiter(), actual.getDelimiter());
        assertEquals(expected.getQuote(), actual.getQuote());

        assertEquals(expected.getSourceCRS(), actual.getSourceCRS());
        assertEquals(expected.getTargetCRS(), actual.getTargetCRS());

        assertEquals(expected.getTmpDir(), actual.getTmpDir());
    }

    private void serializeAsJson(TriplegeoConfiguration configuration) throws Exception
    {
        String s1 = objectMapper.writerWithDefaultPrettyPrinter()
            .writeValueAsString(configuration);

        TriplegeoConfiguration deserializedConfiguration =
            objectMapper.readValue(s1, TriplegeoConfiguration.class);
        checkEquals(configuration, deserializedConfiguration);

        String s1a = objectMapper.writerWithDefaultPrettyPrinter()
            .writeValueAsString(deserializedConfiguration);
        assertEquals(s1, s1a);
    }

    private void serializeAsXml(TriplegeoConfiguration configuration) throws Exception
    {
        String s1 = xmlMapper.writerWithDefaultPrettyPrinter()
            .writeValueAsString(configuration);

        TriplegeoConfiguration deserializedConfiguration =
            xmlMapper.readValue(s1, TriplegeoConfiguration.class);
        checkEquals(configuration, deserializedConfiguration);

        String s1a = xmlMapper.writerWithDefaultPrettyPrinter()
            .writeValueAsString(deserializedConfiguration);
        assertEquals(s1, s1a);
    }

    private void serializeAsProperties(TriplegeoConfiguration configuration) throws Exception
    {
        Properties p1 = propertiesConverter.valueToProperties(configuration);
        TriplegeoConfiguration deserializedConfiguration =
            propertiesConverter.propertiesToValue(p1, TriplegeoConfiguration.class);
        checkEquals(configuration, deserializedConfiguration);
    }

    private void serializeDefault(TriplegeoConfiguration configuration) throws Exception
    {
        byte[] serializedData = null;
        try (ByteArrayOutputStream dataStream = new ByteArrayOutputStream()) {
            ObjectOutputStream out = new ObjectOutputStream(dataStream);
            out.writeObject(configuration);
            out.flush();
            serializedData = dataStream.toByteArray();
        }

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(serializedData));
        TriplegeoConfiguration deserializedConfiguration = (TriplegeoConfiguration) in.readObject();
        checkEquals(configuration, deserializedConfiguration);
    }

    private void validate(TriplegeoConfiguration configuration)
    {
        Set<ConstraintViolation<TriplegeoConfiguration>> violations = validator.validate(configuration);
        assertEquals(Collections.emptySet(), violations);
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
    public void test2_serializeAsJson() throws Exception
    {
        serializeAsJson(config2);
    }

    @Test
    public void test1_serializeAsXml() throws Exception
    {
        serializeAsXml(config1);
    }

    @Test
    public void test2_serializeAsXml() throws Exception
    {
        serializeAsXml(config2);
    }

    @Test
    public void test1_serializeAsProperties() throws Exception
    {
        serializeAsProperties(config1);
    }

    @Test
    public void test2_serializeAsProperties() throws Exception
    {
        serializeAsProperties(config2);
    }

    @Test
    public void test1_serializeDefault() throws Exception
    {
        serializeDefault(config1);
    }

    @Test
    public void test2_serializeDefault() throws Exception
    {
        serializeDefault(config2);
    }

    @Test
    public void test1_validate() throws Exception
    {
        validate(config1);
    }

    @Test
    public void test2_validate() throws Exception
    {
        validate(config2);
    }

    @Test
    public void test1_getSerializationFormat() throws Exception
    {
        assertEquals("N-TRIPLES", config1.getSerializationFormat());
    }

    @Test
    public void test2_getSerializationFormat() throws Exception
    {
        assertEquals("TURTLE", config2.getSerializationFormat());
    }

    @Test
    public void test1_getOutputNames() throws Exception
    {
        Map<EnumOutputType, List<String>> outputNamesByType = config1.getOutputNames();

        assertEquals(
            Arrays.asList("p1.nt", "p2.nt", "classification.nt"),
            outputNamesByType.get(EnumOutputType.OUTPUT));

        assertEquals(
            Arrays.asList("p1_metadata.nt", "p2_metadata.nt", "classification_metadata.nt"),
            outputNamesByType.get(EnumOutputType.KPI));
    }

    @Test
    public void test2_getOutputNames() throws Exception
    {
        Map<EnumOutputType, List<String>> outputNamesByType = config2.getOutputNames();

        assertEquals(
            Arrays.asList("p1.ttl", "p2.ttl", "classification.ttl"),
            outputNamesByType.get(EnumOutputType.OUTPUT));

        assertEquals(
            Arrays.asList("p1_metadata.ttl", "p2_metadata.ttl", "classification_metadata.ttl"),
            outputNamesByType.get(EnumOutputType.KPI));
    }
}
