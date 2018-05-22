package eu.slipo.workbench.rpc.tests.unit.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.IterableUtils;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.common.model.poi.EnumOperation;
import eu.slipo.workbench.common.model.poi.EnumTool;
import eu.slipo.workbench.common.model.process.CatalogResource;
import eu.slipo.workbench.common.model.process.EnumInputType;
import eu.slipo.workbench.common.model.process.ProcessDefinition;
import eu.slipo.workbench.common.model.process.ProcessDefinitionBuilder;
import eu.slipo.workbench.common.model.process.ProcessDefinitionBuilderFactory;
import eu.slipo.workbench.common.model.process.ProcessInput;
import eu.slipo.workbench.common.model.process.ProcessOutput;
import eu.slipo.workbench.common.model.process.RegisterStep;
import eu.slipo.workbench.common.model.process.Step;
import eu.slipo.workbench.common.model.process.TransformStep;
import eu.slipo.workbench.common.model.resource.FileSystemDataSource;
import eu.slipo.workbench.common.model.resource.ResourceIdentifier;
import eu.slipo.workbench.common.model.resource.ResourceMetadataCreate;
import eu.slipo.workbench.common.model.tool.DeerConfiguration;
import eu.slipo.workbench.common.model.tool.FagiConfiguration;
import eu.slipo.workbench.common.model.tool.LimesConfiguration;
import eu.slipo.workbench.common.model.tool.MetadataRegistrationConfiguration;
import eu.slipo.workbench.common.model.tool.TriplegeoConfiguration;
import eu.slipo.workflows.util.digraph.DependencyGraph;
import eu.slipo.workflows.util.digraph.DependencyGraphs;

@RunWith(SpringRunner.class)
@ActiveProfiles({ "testing" })
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ProcessDefinitionTests
{
    private static final String DATASOURCE_1_PATH = "uploads/1.csv";

    private static final String DATASOURCE_2_PATH = "uploads/2.csv";

    @TestConfiguration
    public static class Setup
    {
        @Bean
        public ObjectMapper jsonMapper()
        {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            return objectMapper;
        }

        @Bean
        public ProcessDefinitionBuilderFactory processDefinitionBuilderFactory(ObjectMapper objectMapper)
        {
            return new ProcessDefinitionBuilderFactory(objectMapper);
        }

        @Bean
        public TriplegeoConfiguration sampleTriplegeoConfiguration1()
        {
            TriplegeoConfiguration config = new TriplegeoConfiguration();

            config.setInputFormat(EnumDataFormat.CSV);
            config.setOutputFormat(EnumDataFormat.N_TRIPLES);
            config.setOutputDir("/var/local/triplegeo/1");
            config.setTmpDir("/tmp/triplegeo/1");
            config.setMappingSpec("classpath:config/triplegeo/profiles/1/mappings.yml");
            config.setClassificationSpec("classpath:config/triplegeo/profiles/1/classification.yml");
            config.setAttrX("lon");
            config.setAttrX("lat");
            config.setFeatureSource("points");
            config.setAttrKey("id");
            config.setAttrName("name");
            config.setAttrCategory("type");

            return config;
        }

        @Bean
        public DeerConfiguration sampleDeerConfiguration1()
        {
            DeerConfiguration config = new DeerConfiguration();
            config.setInputFormat(EnumDataFormat.N_TRIPLES);
            config.setOutputFormat(EnumDataFormat.N_TRIPLES);
            return config;
        }

        @Bean
        public FagiConfiguration sampleFagiConfiguration1()
        {
            FagiConfiguration config = new FagiConfiguration();
            return config;
        }

        @Bean
        public LimesConfiguration sampleLimesConfiguration1()
        {
            LimesConfiguration config = new LimesConfiguration();

            config.setMetric("trigrams(a.level, b.level)");
            config.setSource("a", "/tmp/limes/input/a.nt", "?x",
                "slipo:name/slipo:nameType RENAME label");
            config.setTarget("b", "/tmp/limes/input/b.nt", "?y",
                "slipo:name/slipo:nameType RENAME label");
            config.setOutputDir("/tmp/limes/output");
            config.setOutputFormatFromString("N-TRIPLES");
            config.setAccepted(0.98, "accepted.nt");
            config.setReview(0.95, "review.nt");

            return config;
        }

        @Bean
        public FileSystemDataSource dataSource1()
        {
            return new FileSystemDataSource(DATASOURCE_1_PATH);
        }

        @Bean
        public FileSystemDataSource dataSource2()
        {
            return new FileSystemDataSource(DATASOURCE_2_PATH);
        }
    }

    @Autowired
    private ObjectMapper jsonMapper;

    @Autowired
    private ProcessDefinitionBuilderFactory processDefinitionBuilderFactory;

    @Autowired
    private FileSystemDataSource dataSource1;

    @Autowired
    private FileSystemDataSource dataSource2;

    @Autowired
    private TriplegeoConfiguration sampleTriplegeoConfiguration1;

    @Autowired
    private DeerConfiguration sampleDeerConfiguration1;

    @Autowired
    private LimesConfiguration sampleLimesConfiguration1;

    @Autowired
    private FagiConfiguration sampleFagiConfiguration1;

    private ProcessDefinition buildDefinition1()
    {
        final int resultKey1 = 1, resultKey2 = 2, key1 = 101, key2 = 102;

        ResourceMetadataCreate metadata1 =
            new ResourceMetadataCreate("out-1", "A sample output file");
        ResourceMetadataCreate metadata2 =
            new ResourceMetadataCreate("out-2", "Another sample output file");

        ProcessDefinition definition1 = processDefinitionBuilderFactory.create("proc-a-1")
            .resource("resource-a-1.1", key1, ResourceIdentifier.of(1L, 5L))
            .resource("resource-a-1.2", key2, ResourceIdentifier.of(3L, 17L))
            .transform("triplegeo-1", b -> b
                .group(1)
                .nodeName("triplegeo-1")
                .outputKey(resultKey1)
                .source(dataSource1)
                .outputFormat(EnumDataFormat.N_TRIPLES)
                .configuration(sampleTriplegeoConfiguration1))
            .step("enrich-with-deer-1", b -> b
                .group(2)
                .nodeName("enrich-with-deer-1")
                .operation(EnumOperation.ENRICHMENT)
                .tool(EnumTool.DEER)
                .outputFormat(EnumDataFormat.N_TRIPLES)
                .configuration(sampleDeerConfiguration1)
                .input(resultKey1)
                .outputKey(resultKey2))
            .register("register-1", resultKey1, metadata1)
            .register("register-2", resultKey2, metadata2)
            .build();

        return definition1;
    }

    @Test
    public void test1_checkDefinition1() throws Exception
    {
        ProcessDefinition definition1 = buildDefinition1();
        assertNotNull(definition1);
        assertEquals("proc-a-1", definition1.name());

        List<ProcessInput> resources = definition1.resources();
        assertEquals(4, resources.size());
        assertEquals(4, resources.stream().mapToInt(r -> r.key()).distinct().count());

        List<Step> steps = definition1.steps();
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
        assertEquals(EnumTool.REGISTER, step3.tool());
        assertEquals(EnumOperation.REGISTER, step3.operation());
        assertEquals(Collections.singletonList(step1.outputKey()), step3.inputKeys());
        assertNull(step3.outputKey());
        assertNotNull(step3.configuration());
        assertTrue(step3.configuration() instanceof MetadataRegistrationConfiguration);

        Step step4 = steps.stream().filter(s -> s.name().equals("register-2"))
            .findFirst().get();
        assertTrue(step4 instanceof RegisterStep);
        assertEquals(EnumTool.REGISTER, step4.tool());
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
        ProcessDefinition definition1a = jsonMapper.readValue(s1, ProcessDefinition.class);
        assertEquals(s1, jsonMapper.writeValueAsString(definition1a));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void test2_checkDefinition1UnmodifiableSteps1()
    {
        ProcessDefinition definition1 = buildDefinition1();
        assertNotNull(definition1);

        List<Step> steps = definition1.steps();
        steps.remove(0); // attempt to remove a step
    }

    @Test(expected = UnsupportedOperationException.class)
    public void test2_checkDefinition1UnmodifiableSteps2()
    {
        ProcessDefinition definition1 = buildDefinition1();
        assertNotNull(definition1);

        List<Step> steps = definition1.steps();
        Step step1 = steps.get(0);
        steps.add(step1); // attempt to add a step
    }

    @Test(expected = UnsupportedOperationException.class)
    public void test2_checkDefinition1UnmodifiableResources1()
    {
        ProcessDefinition definition1 = buildDefinition1();
        assertNotNull(definition1);

        List<ProcessInput> resources = definition1.resources();
        resources.remove(0); // attempt to remove a resource
    }

    @Test(expected = UnsupportedOperationException.class)
    public void test2_checkDefinition1UnmodifiableResources2()
    {
        ProcessDefinition definition1 = buildDefinition1();
        assertNotNull(definition1);

        List<ProcessInput> resources = definition1.resources();
        ProcessInput res1 = resources.get(0);
        resources.add(res1);
    }

    @Test(expected = IllegalStateException.class)
    public void test2_checkDefinition1UnmodifiableConfiguration()
    {
        ProcessDefinition definition1 = buildDefinition1();
        assertNotNull(definition1);

        Step step11 = definition1.stepByNodeName("triplegeo-1");

        TriplegeoConfiguration configuration = (TriplegeoConfiguration) step11.configuration();
        configuration.setAttrKey("koukou");
    }

    @Test
    public void test2_checkDefinition1ConfigurationType()
    {
        ProcessDefinition definition1 = buildDefinition1();
        assertNotNull(definition1);

        Step step11 = definition1.stepByNodeName("triplegeo-1");
        Class<?> configurationType = step11.configurationType();
        assertEquals(TriplegeoConfiguration.class, configurationType);
    }

    @Test(expected = IllegalStateException.class)
    public void test3_checkRegisterUndefinedResource1()
    {
        final int resourceKey = 1; // not a catalog resource, neither is an output from another step
        ProcessDefinition definition = processDefinitionBuilderFactory.create("register-1")
            .register("register-1", resourceKey, new ResourceMetadataCreate("sample", "Another sample"))
            .build();
        System.err.println(definition);
    }

    @Test(expected = IllegalStateException.class)
    public void test3_checkInputFromUndefinedResource1()
    {
        final int inputKey1 = 1;
        final int inputKey2 = 2; // not a catalog resource, neither is an output from another step
        final int outputKey = 10;
        ProcessDefinition definition = processDefinitionBuilderFactory.create("register-1")
            .resource("res-1", inputKey1, ResourceIdentifier.of(5L, 27L))
            .transform("triplegeo-1", builder -> builder
                .input(inputKey2)
                .configuration(sampleTriplegeoConfiguration1)
                .outputFormat(EnumDataFormat.N_TRIPLES)
                .outputKey(outputKey))
            .build();
        System.err.println(definition);
    }

    @Test
    public void test99() throws Exception
    {
        final int resourceKey1 = 1, resourceKey2 = 2, resourceKey3 = 3;

        ResourceMetadataCreate metadata1 =
            new ResourceMetadataCreate("out-1", "A sample output file");
        ResourceMetadataCreate metadata2 =
            new ResourceMetadataCreate("out-2", "Another sample output file");
        ResourceMetadataCreate metadata3 =
            new ResourceMetadataCreate("out-3", "Yet another sample output file");


        ProcessDefinition definition1 = processDefinitionBuilderFactory.create("proc-a-1")
            .resource("resource-a-1.1", 101, ResourceIdentifier.of(1L, 5L))
            .resource("resource-a-1.2", 102, ResourceIdentifier.of(3L, 17L))
            .resource("resource-a-1.3", 103, ResourceIdentifier.of(8L, 2L))
            .transform("triplegeo-1", b -> b
                .group(1)
                .outputKey(resourceKey1)
                .source(dataSource1)
                .configuration(sampleTriplegeoConfiguration1))
            .transform("triplegeo-2", b -> b
                .group(1)
                .outputKey(resourceKey2)
                .source(dataSource2)
                .configuration(sampleTriplegeoConfiguration1))
            .step("interlink-1-2", b -> b
                .group(2)
                .operation(EnumOperation.INTERLINK)
                .tool(EnumTool.LIMES)
                .configuration(sampleLimesConfiguration1)
                .input(resourceKey1, resourceKey2)
                .outputKey(resourceKey3)
                .outputFormat(EnumDataFormat.N_TRIPLES))
            .register("register-1", resourceKey1, metadata1)
            .register("register-2", resourceKey2, metadata2)
            .register("register-3", resourceKey3, metadata3)
            .build();

        final List<ProcessInput> inputs = definition1.resources();
        final List<Step> steps = definition1.steps();

        // Map each output key to the key of processing step (expected to produce it)
        final Map<Integer, Integer> outputKeyToStepKey = inputs.stream()
            .filter(r -> r.getInputType() == EnumInputType.OUTPUT)
            .collect(Collectors.toMap(r -> r.key(), r -> ((ProcessOutput) r).stepKey()));

        // Build dependency graph
        final DependencyGraph dependencyGraph = DependencyGraphs.create(steps.size());
        for (Step step: steps) {
            for (Integer k: step.inputKeys()) {
                if (outputKeyToStepKey.containsKey(k))
                    dependencyGraph.addDependency(step.key(), outputKeyToStepKey.get(k));
            }
        }

        System.err.println(
            DependencyGraphs.toString(dependencyGraph, v -> definition1.stepByKey(v).name()));

        Iterable<Step> sortedSteps =
            IterableUtils.transformedIterable(
                DependencyGraphs.topologicalSort(dependencyGraph), k -> definition1.stepByKey(k));

        String s1 = jsonMapper.writeValueAsString(definition1);
        //System.err.println(s1);

        ProcessDefinition definition1a = jsonMapper.readValue(s1, ProcessDefinition.class);
        String s2 = jsonMapper.writeValueAsString(definition1a);
        assertEquals(s1, s2);
    }
}
