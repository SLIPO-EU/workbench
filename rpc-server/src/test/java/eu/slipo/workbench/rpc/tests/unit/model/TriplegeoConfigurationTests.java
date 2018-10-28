package eu.slipo.workbench.rpc.tests.unit.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Collections;
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
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.common.model.tool.ReverseTriplegeo;
import eu.slipo.workbench.common.model.tool.ReverseTriplegeoConfiguration;
import eu.slipo.workbench.common.model.tool.TransformConfiguration;
import eu.slipo.workbench.common.model.tool.TransformTool;
import eu.slipo.workbench.common.model.tool.Triplegeo;
import eu.slipo.workbench.common.model.tool.TriplegeoConfiguration;
import eu.slipo.workbench.common.model.tool.output.EnumReverseTriplegeoOutputPart;
import eu.slipo.workbench.common.model.tool.output.EnumTriplegeoOutputPart;
import eu.slipo.workbench.common.model.tool.output.OutputPart;
import eu.slipo.workbench.common.model.tool.output.OutputSpec;
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

            config.setRegisterFeatures(false);

            return config;
        }

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

        @Bean
        public TriplegeoConfiguration config2a()
        {
            TriplegeoConfiguration config = config2();
            config.setRegisterFeatures(false);
            return config;
        }

        @Bean
        public TriplegeoConfiguration config2b()
        {
            TriplegeoConfiguration config = config2();
            config.setRegisterFeatures(true);
            return config;
        }

        @Bean
        public ReverseTriplegeoConfiguration config1R()
        {
            ReverseTriplegeoConfiguration config = new ReverseTriplegeoConfiguration();
            config.setInputFormat("N-TRIPLES");
            config.setInput(Arrays.asList(
                "/var/local/triplegeo/input/classification.nt",
                "/var/local/triplegeo/input/part1.nt",
                "/var/local/triplegeo/input/part2.nt"));
            config.setSparqlFile("/var/local/triplegeo/query.sparql");
            config.setOutputFormat("CSV");
            config.setOutputDir("/var/local/triplegeo/output");
            config.setDelimiter(";");
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
    TriplegeoConfiguration config2a;

    @Autowired
    TriplegeoConfiguration config2b;

    @Autowired
    ReverseTriplegeoConfiguration config1R;

    private void checkEquals(TriplegeoConfiguration expected, TriplegeoConfiguration actual)
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

    private void checkEquals(ReverseTriplegeoConfiguration expected, ReverseTriplegeoConfiguration actual)
    {
        assertEquals(expected.getInputFormat(), actual.getInputFormat());
        assertEquals(expected.getInput(), actual.getInput());
        assertEquals(expected.getOutputFormat(), actual.getOutputFormat());
        assertEquals(expected.getSerializationFormat(), actual.getSerializationFormat());
        assertEquals(expected.getOutputDir(), actual.getOutputDir());
        assertEquals(expected.getSparqlFile(), actual.getSparqlFile());

        assertEquals(expected.getDelimiter(), actual.getDelimiter());
        assertEquals(expected.getQuote(), actual.getQuote());

        assertEquals(expected.getSourceCRS(), actual.getSourceCRS());
        assertEquals(expected.getTargetCRS(), actual.getTargetCRS());

        assertEquals(expected.getTmpDir(), actual.getTmpDir());
    }

    private void checkEquals(Object expected, Object actual)
    {
        if (expected instanceof TriplegeoConfiguration) {
            assertTrue(actual instanceof TriplegeoConfiguration);
            checkEquals((TriplegeoConfiguration) expected, (TriplegeoConfiguration) actual);
        } else if (expected instanceof ReverseTriplegeoConfiguration) {
            assertTrue(actual instanceof ReverseTriplegeoConfiguration);
            checkEquals((ReverseTriplegeoConfiguration) expected, (ReverseTriplegeoConfiguration) actual);
        } else {
            assertTrue("Expected objects of certain type", false);
        }
    }

    private <T extends TransformConfiguration<?>> void serializeAsJson(
        T configuration, Class<T> configurationType) throws Exception
    {
        String s1 = objectMapper.writerWithDefaultPrettyPrinter()
            .writeValueAsString(configuration);

        T deserializedConfiguration = objectMapper.readValue(s1, configurationType);
        checkEquals(configuration, deserializedConfiguration);

        String s1a = objectMapper.writerWithDefaultPrettyPrinter()
            .writeValueAsString(deserializedConfiguration);
        assertEquals(s1, s1a);
    }

    private <T extends TransformConfiguration<?>> void serializeAsXml(
        T configuration, Class<T> configurationType) throws Exception
    {
        String s1 = xmlMapper.writerWithDefaultPrettyPrinter()
            .writeValueAsString(configuration);

        T deserializedConfiguration = xmlMapper.readValue(s1, configurationType);
        checkEquals(configuration, deserializedConfiguration);

        String s1a = xmlMapper.writerWithDefaultPrettyPrinter()
            .writeValueAsString(deserializedConfiguration);
        assertEquals(s1, s1a);
    }

    private <T extends TransformConfiguration<?>> void serializeAsProperties(
        T configuration, Class<T> configurationType) throws Exception
    {
        Properties p1 = propertiesConverter.valueToProperties(configuration);
        T deserializedConfiguration = propertiesConverter.propertiesToValue(p1, configurationType);
        checkEquals(configuration, deserializedConfiguration);
    }

    private <T extends TransformConfiguration<?>> void serializeDefault(
        T configuration, Class<T> configurationType) throws Exception
    {
        byte[] serializedData = null;
        try (ByteArrayOutputStream dataStream = new ByteArrayOutputStream()) {
            ObjectOutputStream out = new ObjectOutputStream(dataStream);
            out.writeObject(configuration);
            out.flush();
            serializedData = dataStream.toByteArray();
        }

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(serializedData));
        T deserializedConfiguration = configurationType.cast(in.readObject());
        checkEquals(configuration, deserializedConfiguration);
    }

    private <T extends TransformConfiguration<?>> void validate(T configuration)
    {
        Set<ConstraintViolation<T>> violations = validator.validate(configuration);
        assertEquals(Collections.emptySet(), violations);
    }

    //
    // Tests (Triplegeo)
    //

    @Test
    public void test1_serializeAsJson() throws Exception
    {
        serializeAsJson(config1, TriplegeoConfiguration.class);
    }

    @Test
    public void test2a_serializeAsJson() throws Exception
    {
        serializeAsJson(config2a, TriplegeoConfiguration.class);
    }

    @Test
    public void test1_serializeAsXml() throws Exception
    {
        serializeAsXml(config1, TriplegeoConfiguration.class);
    }

    @Test
    public void test2a_serializeAsXml() throws Exception
    {
        serializeAsXml(config2a, TriplegeoConfiguration.class);
    }

    @Test
    public void test1_serializeAsProperties() throws Exception
    {
        serializeAsProperties(config1, TriplegeoConfiguration.class);
    }

    @Test
    public void test2a_serializeAsProperties() throws Exception
    {
        serializeAsProperties(config2a, TriplegeoConfiguration.class);
    }

    @Test
    public void test1_serializeDefault() throws Exception
    {
        serializeDefault(config1, TriplegeoConfiguration.class);
    }

    @Test
    public void test2a_serializeDefault() throws Exception
    {
        serializeDefault(config2a, TriplegeoConfiguration.class);
    }

    @Test
    public void test1_validate() throws Exception
    {
        validate(config1);
    }

    @Test
    public void test2a_validate() throws Exception
    {
        validate(config2a);
    }

    @Test
    public void test1_getSerializationFormat() throws Exception
    {
        assertEquals("N-TRIPLES", config1.getSerializationFormat());
    }

    @Test
    public void test2a_getSerializationFormat() throws Exception
    {
        assertEquals("TURTLE", config2a.getSerializationFormat());
    }

    @Test
    public void test1_getOutputNames() throws Exception
    {
        Multimap<OutputPart<Triplegeo>, OutputSpec> outputMap = config1.getOutputNameMapper()
            .apply(Arrays.asList("/data/p1.csv", "/data/p2.csv"));
        Multimap<OutputPart<Triplegeo>, String> outputNames =
            Multimaps.transformValues(outputMap, OutputSpec::fileName);

        assertEquals(Arrays.asList("p1.nt", "p2.nt"),
            outputNames.get(EnumTriplegeoOutputPart.TRANSFORMED));
        assertEquals(Arrays.asList("p1_metadata.json", "p2_metadata.json"),
            outputNames.get(EnumTriplegeoOutputPart.TRANSFORMED_METADATA));
        assertEquals(Arrays.asList("classification.nt"),
            outputNames.get(EnumTriplegeoOutputPart.CLASSIFICATION));
        assertEquals(Arrays.asList("classification_metadata.json"),
            outputNames.get(EnumTriplegeoOutputPart.CLASSIFICATION_METADATA));
        assertEquals(Collections.emptyList(),
            outputNames.get(EnumTriplegeoOutputPart.REGISTRATION_REQUEST));
    }

    @Test
    public void test2a_getOutputNames() throws Exception
    {
        Multimap<OutputPart<Triplegeo>, OutputSpec> outputMap = config2a.getOutputNameMapper()
            .apply(Arrays.asList("/data/p1.csv", "/data/p2.csv"));
        Multimap<OutputPart<Triplegeo>, String> outputNames =
            Multimaps.transformValues(outputMap, OutputSpec::fileName);

        assertEquals(Arrays.asList("p1.ttl", "p2.ttl"),
            outputNames.get(EnumTriplegeoOutputPart.TRANSFORMED));
        assertEquals(Arrays.asList("p1_metadata.json", "p2_metadata.json"),
            outputNames.get(EnumTriplegeoOutputPart.TRANSFORMED_METADATA));
        assertEquals(Arrays.asList("classification.ttl"),
            outputNames.get(EnumTriplegeoOutputPart.CLASSIFICATION));
        assertEquals(Arrays.asList("classification_metadata.json"),
            outputNames.get(EnumTriplegeoOutputPart.CLASSIFICATION_METADATA));
        assertEquals(Collections.emptyList(),
            outputNames.get(EnumTriplegeoOutputPart.REGISTRATION_REQUEST));
    }

    @Test
    public void test2b_getOutputNames() throws Exception
    {
        Multimap<OutputPart<Triplegeo>, OutputSpec> outputMap = config2b.getOutputNameMapper()
            .apply(Arrays.asList("/data/p1.csv", "/data/p2.csv"));
        Multimap<OutputPart<Triplegeo>, String> outputNames =
            Multimaps.transformValues(outputMap, s -> s.fileName());

        assertEquals(Arrays.asList("p1.ttl", "p2.ttl"),
            outputNames.get(EnumTriplegeoOutputPart.TRANSFORMED));
        assertEquals(Arrays.asList("p1_metadata.json", "p2_metadata.json"),
            outputNames.get(EnumTriplegeoOutputPart.TRANSFORMED_METADATA));
        assertEquals(Arrays.asList("classification.ttl"),
            outputNames.get(EnumTriplegeoOutputPart.CLASSIFICATION));
        assertEquals(Arrays.asList("classification_metadata.json"),
            outputNames.get(EnumTriplegeoOutputPart.CLASSIFICATION_METADATA));
        assertEquals(Arrays.asList("p1.csv", "p2.csv"),
            outputNames.get(EnumTriplegeoOutputPart.REGISTRATION_REQUEST));
    }


    //
    // Tests (reverse Triplegeo)
    //

    @Test
    public void test1R_serializeAsJson() throws Exception
    {
        serializeAsJson(config1R, ReverseTriplegeoConfiguration.class);
    }

    @Test
    public void test1R_serializeAsXml() throws Exception
    {
        serializeAsXml(config1R, ReverseTriplegeoConfiguration.class);
    }

    @Test
    public void test1R_serializeAsProperties() throws Exception
    {
        serializeAsProperties(config1R, ReverseTriplegeoConfiguration.class);
    }

    @Test
    public void test1R_serializeDefault() throws Exception
    {
        serializeDefault(config1R, ReverseTriplegeoConfiguration.class);
    }

    @Test
    public void test1R_validate() throws Exception
    {
        validate(config1R);
    }

    @Test
    public void test1R_getOutputNames() throws Exception
    {
        Multimap<OutputPart<ReverseTriplegeo>, OutputSpec> outputMap = config1R.getOutputNameMapper()
            .apply(Arrays.asList("/data/p1.nt", "/data/p2.nt"));
        Multimap<OutputPart<ReverseTriplegeo>, String> outputNames =
            Multimaps.transformValues(outputMap, OutputSpec::fileName);

        assertEquals(Arrays.asList("points.csv"),
            outputNames.get(EnumReverseTriplegeoOutputPart.TRANSFORMED));
    }
}
