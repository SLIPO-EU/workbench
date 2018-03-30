package eu.slipo.workbench.rpc.tests.unit.model;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Properties;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.BeanUtilsBean;
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

import eu.slipo.workbench.common.model.tool.TriplegeoConfiguration;
import eu.slipo.workbench.common.service.util.JsonBasedPropertiesConverterService;
import eu.slipo.workbench.common.service.util.PropertiesConverterService;

@RunWith(SpringRunner.class)
@ActiveProfiles({ "testing" })
public class TriplegeoConfigurationTests
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
        public TriplegeoConfiguration config1()
        {
            TriplegeoConfiguration config = new TriplegeoConfiguration();

            config.setMode(TriplegeoConfiguration.Mode.STREAM);
            config.setInputFormat("CSV");
            config.setInputFromString("/tmp/triplegeo/input/p1.csv:/tmp/triplegeo/input/p2.csv");
            config.setSerializationFormat("N-TRIPLES");
            config.setOutputDir("/tmp/triplegeo/output");

            config.setAttrCategory("category");
            config.setAttrKey("id");
            config.setAttrName("name");
            config.setAttrX("lon");
            config.setAttrX("lat");

            config.setFeatureName("points");

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
    TriplegeoConfiguration config1;

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

        assertEquals(expected.getFeatureName(), actual.getFeatureName());
        assertEquals(expected.getFeatureNamespaceUri(), actual.getFeatureNamespaceUri());
        assertEquals(expected.getFeatureUriPrefix(), actual.getFeatureUriPrefix());

        assertEquals(expected.getDelimiter(), actual.getDelimiter());
        assertEquals(expected.getQuote(), actual.getQuote());

        assertEquals(expected.getSourceCRS(), actual.getSourceCRS());
        assertEquals(expected.getTargetCRS(), actual.getTargetCRS());

        assertEquals(expected.getTmpDir(), actual.getTmpDir());
    }

    @Test
    public void test1_serializeAsJson() throws Exception
    {
        String s1 = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(config1);

        TriplegeoConfiguration config1a = objectMapper.readValue(s1, TriplegeoConfiguration.class);
        checkEquals(config1, config1a);

        String s1a = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(config1a);
        assertEquals(s1, s1a);
    }

    @Test
    public void test1_serializeAsXml() throws Exception
    {
        String s1 = xmlMapper.writerWithDefaultPrettyPrinter().writeValueAsString(config1);

        TriplegeoConfiguration config1a = xmlMapper.readValue(s1, TriplegeoConfiguration.class);
        checkEquals(config1, config1a);

        String s1a = xmlMapper.writerWithDefaultPrettyPrinter().writeValueAsString(config1a);
        assertEquals(s1, s1a);
    }

    @Test
    public void test1_serializeAsProperties() throws Exception
    {
        Properties p1 = propertiesConverter.valueToProperties(config1);

        TriplegeoConfiguration config1a =
            propertiesConverter.propertiesToValue(p1, TriplegeoConfiguration.class);
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
        TriplegeoConfiguration config1a = (TriplegeoConfiguration) in.readObject();
        checkEquals(config1, config1a);
    }
}
