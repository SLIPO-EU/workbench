package eu.slipo.workbench.rpc.tests.unit.model;

import org.apache.commons.collections4.set.UnmodifiableSet;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import static org.junit.Assert.*;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.common.model.poi.EnumOperation;
import eu.slipo.workbench.common.model.poi.EnumResourceType;
import eu.slipo.workbench.common.model.poi.EnumTool;
import eu.slipo.workbench.common.model.process.CatalogResource;
import eu.slipo.workbench.common.model.process.EnumInputType;
import eu.slipo.workbench.common.model.process.ProcessDefinition;
import eu.slipo.workbench.common.model.process.ProcessDefinitionBuilder;
import eu.slipo.workbench.common.model.process.ProcessInput;
import eu.slipo.workbench.common.model.process.ProcessOutput;
import eu.slipo.workbench.common.model.process.RegisterStep;
import eu.slipo.workbench.common.model.process.Step;
import eu.slipo.workbench.common.model.process.TransformStep;
import eu.slipo.workbench.common.model.resource.DataSource;
import eu.slipo.workbench.common.model.resource.FileSystemDataSource;
import eu.slipo.workbench.common.model.resource.ResourceIdentifier;
import eu.slipo.workbench.common.model.resource.ResourceMetadataCreate;
import eu.slipo.workbench.common.model.tool.DeerConfiguration;
import eu.slipo.workbench.common.model.tool.MetadataRegistrationConfiguration;
import eu.slipo.workbench.common.model.tool.ToolConfiguration;
import eu.slipo.workbench.common.model.tool.TriplegeoConfiguration;

@RunWith(SpringRunner.class)
@ActiveProfiles({ "testing" })
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ProcessDefinitionTests
{
    private static final String DATASOURCE_1_PATH = "uploads/1.csv";
    
    @TestConfiguration
    public static class Configuration
    {
        @Bean
        public ObjectMapper jsonMapper()
        {
            return new ObjectMapper();
        }
        
        @Bean
        public TriplegeoConfiguration sampleTriplegeoConfiguration1()
        {
            TriplegeoConfiguration configuration = new TriplegeoConfiguration();
            
            configuration.setInputFormat(EnumDataFormat.CSV);
            configuration.setOutputFormat(EnumDataFormat.N_TRIPLES);
            configuration.setOutputDir(Paths.get("/var/local/triplegeo/1"));
            configuration.setTmpDir(Paths.get("/tmp/triplegeo/1"));
            configuration.setAttrX("lon");
            configuration.setAttrX("lat");
            configuration.setFeatureName("points");
            configuration.setAttrKey("id");
            configuration.setAttrName("name");
            configuration.setAttrCategory("type");
            
            return configuration;
        }
        
        @Bean
        public DeerConfiguration sampleDeerConfiguration1()
        {
            return new DeerConfiguration();
        }
        
        @Bean
        public FileSystemDataSource dataSource1()
        {
            return new FileSystemDataSource(DATASOURCE_1_PATH);
        }
    }
    
    @Autowired
    private ObjectMapper jsonMapper;
    
    @Autowired
    private FileSystemDataSource dataSource1;
    
    @Autowired
    private TriplegeoConfiguration sampleTriplegeoConfiguration1;
    
    @Autowired
    private DeerConfiguration sampleDeerConfiguration1;
    
    private ProcessDefinition buildDefinition1()
    {
        final int resourceKey1 = 1, resourceKey2 = 2;
        
        ResourceMetadataCreate metadata1 = 
            new ResourceMetadataCreate("out-1", "A sample output file");
        ResourceMetadataCreate metadata2 = 
            new ResourceMetadataCreate("out-2", "Another sample output file");
        
        ProcessDefinition definition1 = ProcessDefinitionBuilder.create("proc-a-1")
            .resource("resource-a-1.1", 101, ResourceIdentifier.of(1L, 5L))
            .resource("resource-a-1.2", 102, ResourceIdentifier.of(3L, 17L))
            .transform("triplegeo-1", b -> b
                .group(1)
                .outputKey(resourceKey1)
                .source(dataSource1)
                .configuration(sampleTriplegeoConfiguration1))
            .step("enrich-with-deer-1", b -> b
                .group(2)
                .operation(EnumOperation.ENRICHMENT)
                .tool(EnumTool.DEER)
                .configuration(sampleDeerConfiguration1)
                .input(resourceKey1)
                .outputKey(resourceKey2)
                .outputFormat(EnumDataFormat.N_TRIPLES))
            .register("register-1", resourceKey1, metadata1)
            .register("register-2", resourceKey2, metadata2)
            .build();
        
        return definition1;
    }
    
    @Test
    public void test1_checkDefinition1() throws Exception 
    {        
        ProcessDefinition definition1 = buildDefinition1();
        assertNotNull(definition1);
        assertEquals("proc-a-1", definition1.getName());
        
        List<ProcessInput> resources = definition1.getResources();
        assertEquals(4, resources.size());
        assertEquals(4, resources.stream().mapToInt(r -> r.key()).distinct().count());
        
        List<Step> steps = definition1.getSteps();
        assertEquals(4, steps.size());
        assertEquals(4, steps.stream().mapToInt(s -> s.key()).distinct().count());
        assertEquals(
            new HashSet<>(Arrays.asList("register-1", "register-2", "triplegeo-1", "enrich-with-deer-1")),
            steps.stream().map(r -> r.name()).collect(Collectors.toSet()));
        
        Step step1 = steps.stream().filter(s -> s.name().equals("triplegeo-1"))
            .findFirst().get();
        assertTrue(step1 instanceof TransformStep);
        assertEquals(EnumTool.TRIPLEGEO, step1.tool());
        assertEquals(EnumOperation.TRANSFORM, step1.operation());
        assertEquals(EnumDataFormat.N_TRIPLES, step1.outputFormat());
        assertEquals(0, step1.inputKeys().size());
        assertNotNull(step1.outputKey());
        assertEquals(1, step1.sources().size());
        assertEquals(DATASOURCE_1_PATH, ((FileSystemDataSource) step1.sources().get(0)).getPath());
        assertEquals(
            jsonMapper.writeValueAsString(sampleTriplegeoConfiguration1),
            jsonMapper.writeValueAsString(step1.configuration()));
        assertTrue(step1.configuration() instanceof TriplegeoConfiguration);
        assertEquals(EnumDataFormat.N_TRIPLES, 
            ((TriplegeoConfiguration) step1.configuration()).getOutputFormat());
        
        Step step2 = steps.stream().filter(s -> s.name().equals("enrich-with-deer-1"))
            .findFirst().get();
        assertEquals(EnumTool.DEER, step2.tool());
        assertEquals(EnumOperation.ENRICHMENT, step2.operation());
        assertEquals(EnumDataFormat.N_TRIPLES, step2.outputFormat());
        assertEquals(1, step2.inputKeys().size());
        assertEquals(step1.outputKey(), step2.inputKeys().get(0));
        assertEquals(
            jsonMapper.writeValueAsString(sampleDeerConfiguration1),
            jsonMapper.writeValueAsString(step2.configuration()));
        assertTrue(step2.configuration() instanceof DeerConfiguration);
        
        Step step3 = steps.stream().filter(s -> s.name().equals("register-1"))
            .findFirst().get();
        assertTrue(step3 instanceof RegisterStep);
        assertEquals(EnumTool.REGISTER_METADATA, step3.tool());
        assertEquals(EnumOperation.REGISTER, step3.operation());
        assertEquals(Collections.singletonList(step1.outputKey()), step3.inputKeys());
        assertNull(step3.outputKey());
        assertNotNull(step3.configuration());
        assertTrue(step3.configuration() instanceof MetadataRegistrationConfiguration);
        
        Step step4 = steps.stream().filter(s -> s.name().equals("register-2"))
            .findFirst().get();
        assertTrue(step4 instanceof RegisterStep);
        assertEquals(EnumTool.REGISTER_METADATA, step4.tool());
        assertEquals(EnumOperation.REGISTER, step4.operation());
        assertEquals(Collections.singletonList(step2.outputKey()), step4.inputKeys());
        assertNull(step4.outputKey());
        assertNotNull(step4.configuration());
        assertTrue(step4.configuration() instanceof MetadataRegistrationConfiguration);
        
        List<CatalogResource> catalogResources = resources.stream()
            .filter(r -> r.getInputType() == EnumInputType.CATALOG)
            .map(r -> CatalogResource.class.cast(r))
            .collect(Collectors.toList());
        assertEquals(2, catalogResources.size());
        assertEquals(
            new HashSet<>(Arrays.asList("resource-a-1.1", "resource-a-1.2")),
            catalogResources.stream().map(r -> r.getName()).collect(Collectors.toSet()));
        assertEquals(
            new HashSet<>(Arrays.asList(ResourceIdentifier.of(1L, 5L), ResourceIdentifier.of(3L, 17L))),
            catalogResources.stream().map(r -> r.getResource()).collect(Collectors.toSet()));
        
        List<ProcessOutput> outputResources = resources.stream()
            .filter(r -> r.getInputType() == EnumInputType.OUTPUT)
            .map(r -> ProcessOutput.class.cast(r))
            .collect(Collectors.toList());
        assertEquals(2, outputResources.size());
        assertEquals(
            new HashSet<>(Arrays.asList("triplegeo-1", "enrich-with-deer-1")),
            outputResources.stream().map(r -> r.getName()).collect(Collectors.toSet()));
        
        ProcessOutput outputResource1 = outputResources.stream()
            .filter(r -> r.getName().equals("triplegeo-1"))
            .findFirst().get();
        assertEquals(EnumTool.TRIPLEGEO, outputResource1.getTool());
        
        ProcessOutput outputResource2 = outputResources.stream()
            .filter(r -> r.getName().equals("enrich-with-deer-1"))
            .findFirst().get();
        assertEquals(EnumTool.DEER, outputResource2.getTool());
    }
    
    @Test
    public void test1_checkDefinition1ConvertsToJson() throws Exception
    {
        ProcessDefinition definition1 = buildDefinition1();
        assertNotNull(definition1);
        
        String s1 = jsonMapper.writeValueAsString(definition1);
        //System.err.println(s1);
        
        ProcessDefinition definition1a = jsonMapper.readValue(s1, ProcessDefinition.class);
        assertEquals(s1, jsonMapper.writeValueAsString(definition1a));
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void test2_checkDefinition1UnmodifiableSteps1()
    {
        ProcessDefinition definition1 = buildDefinition1();
        assertNotNull(definition1);
        
        List<Step> steps = definition1.getSteps();
        steps.remove(0); // attempt to remove a step
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void test2_checkDefinition1UnmodifiableSteps2()
    {
        ProcessDefinition definition1 = buildDefinition1();
        assertNotNull(definition1);
        
        List<Step> steps = definition1.getSteps();
        Step step1 = steps.get(0);
        steps.add(step1); // attempt to add a step
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void test2_checkDefinition1UnmodifiableResources1()
    {
        ProcessDefinition definition1 = buildDefinition1();
        assertNotNull(definition1);
        
        List<ProcessInput> resources = definition1.getResources();
        resources.remove(0); // attempt to remove a resource
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void test2_checkDefinition1UnmodifiableResources2()
    {
        ProcessDefinition definition1 = buildDefinition1();
        assertNotNull(definition1);
        
        List<ProcessInput> resources = definition1.getResources();
        ProcessInput res1 = resources.get(0);
        resources.add(res1);
    }
    
}
