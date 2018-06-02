package eu.slipo.workbench.rpc.tests.unit.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
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
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.common.model.poi.EnumOperation;
import eu.slipo.workbench.common.model.poi.EnumTool;
import eu.slipo.workbench.common.model.process.CatalogResource;
import eu.slipo.workbench.common.model.process.EnumInputType;
import eu.slipo.workbench.common.model.process.ProcessDefinition;
import eu.slipo.workbench.common.model.process.ProcessDefinitionBuilderFactory;
import eu.slipo.workbench.common.model.process.ProcessInput;
import eu.slipo.workbench.common.model.process.ProcessOutput;
import eu.slipo.workbench.common.model.process.Step;
import eu.slipo.workbench.common.model.resource.FileSystemDataSource;
import eu.slipo.workbench.common.model.resource.ResourceIdentifier;
import eu.slipo.workbench.common.model.resource.ResourceMetadataCreate;
import eu.slipo.workbench.common.model.tool.DeerConfiguration;
import eu.slipo.workbench.common.model.tool.FagiConfiguration;
import eu.slipo.workbench.common.model.tool.LimesConfiguration;
import eu.slipo.workbench.common.model.tool.RegisterToCatalogConfiguration;
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

    private static final ResourceIdentifier RESOURCE_1_ID = ResourceIdentifier.of(1L, 5L);

    private static final String RESOURCE_1_NAME = "resource-example-1";

    private static final ResourceIdentifier RESOURCE_2_ID = ResourceIdentifier.of(19L, 1L);

    private static final String RESOURCE_2_NAME = "resource-example-2";

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

    private ProcessDefinition buildDefinition1a()
    {
        final int TRANSFORM_1_KEY = 1001, TRANSFORM_2_KEY = 1002, LINKS_KEY = 1003;

        ResourceMetadataCreate metadataForTransformed1 =
            new ResourceMetadataCreate("tr-1", "The 1st RDF file");
        ResourceMetadataCreate metadataForTransformed2 =
            new ResourceMetadataCreate("tr-2", "The 2nd RDF file");
        ResourceMetadataCreate metadataForLinks =
            new ResourceMetadataCreate("links-1-2", "A set of sameAs links between 1st and 2nd RDF files");

        ProcessDefinition definition = processDefinitionBuilderFactory.create("proc-1-a")
            .transform("triplegeo-1", b -> b
                .group(1)
                .outputKey(TRANSFORM_1_KEY)
                .source(dataSource1)
                .configuration(sampleTriplegeoConfiguration1))
            .register("register-1", TRANSFORM_1_KEY, "transformed", metadataForTransformed1)
            .transform("triplegeo-2", b -> b
                .group(1)
                .outputKey(TRANSFORM_2_KEY)
                .source(dataSource2)
                .configuration(sampleTriplegeoConfiguration1))
            .register("register-2", TRANSFORM_2_KEY, "transformed", metadataForTransformed2)
            .interlink("link-1-with-2", b -> b
                .group(2)
                .configuration(sampleLimesConfiguration1)
                .left(TRANSFORM_1_KEY, "transformed")
                .right(TRANSFORM_2_KEY, "transformed")
                .outputKey(LINKS_KEY)
                .outputFormat(EnumDataFormat.N_TRIPLES))
            .register("register-links", LINKS_KEY, "accepted", metadataForLinks)
            .build();

        return definition;
    }

    private ProcessDefinition buildDefinition1b()
    {
        final int TRANSFORM_1_KEY = 1001, TRANSFORM_2_KEY = 1002, LINKS_KEY = 1003;
        final int RESOURCE_1_KEY = 1, RESOURCE_2_KEY = 2;

        ResourceMetadataCreate metadataForTransformed1 =
            new ResourceMetadataCreate("tr-1", "The 1st RDF file");
        ResourceMetadataCreate metadataForTransformed2 =
            new ResourceMetadataCreate("tr-2", "The 2nd RDF file");
        ResourceMetadataCreate metadataForLinks =
            new ResourceMetadataCreate("links-1-2", "A set of sameAs links between 1st and 2nd RDF files");

        ProcessDefinition definition = processDefinitionBuilderFactory.create("proc-1-b")
            .resource(RESOURCE_1_NAME, RESOURCE_1_KEY, RESOURCE_1_ID)
            .resource(RESOURCE_2_NAME, RESOURCE_2_KEY, RESOURCE_2_ID)
            .transform("triplegeo-1", b -> b
                .group(1)
                .outputKey(TRANSFORM_1_KEY)
                .input(RESOURCE_1_KEY)
                .configuration(sampleTriplegeoConfiguration1))
            .register("register-1", TRANSFORM_1_KEY, "transformed", metadataForTransformed1)
            .transform("triplegeo-2", b -> b
                .group(1)
                .outputKey(TRANSFORM_2_KEY)
                .input(RESOURCE_2_KEY)
                .configuration(sampleTriplegeoConfiguration1))
            .register("register-2", TRANSFORM_2_KEY, "transformed", metadataForTransformed2)
            .interlink("link-1-with-2", b -> b
                .group(2)
                .configuration(sampleLimesConfiguration1)
                .left(TRANSFORM_1_KEY, "transformed")
                .right(TRANSFORM_2_KEY, "transformed")
                .outputKey(LINKS_KEY)
                .outputFormat(EnumDataFormat.N_TRIPLES))
            .register("register-links", LINKS_KEY, "accepted", metadataForLinks)
            .build();

        return definition;
    }

    private void serializeToJson(ProcessDefinition definition) throws Exception
    {
        String s1 = jsonMapper.writeValueAsString(definition);
        ProcessDefinition definition1 = jsonMapper.readValue(s1, ProcessDefinition.class);
        assertEquals(s1, jsonMapper.writeValueAsString(definition1));
    }

    private void serializeDefault(ProcessDefinition definition) throws Exception
    {
        String definitionAsJson = jsonMapper.writeValueAsString(definition);

        byte[] serializedData = null;
        try (ByteArrayOutputStream dataStream = new ByteArrayOutputStream()) {
            ObjectOutputStream out = new ObjectOutputStream(dataStream);
            out.writeObject(definition);
            out.flush();
            serializedData = dataStream.toByteArray();
        }

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(serializedData));
        ProcessDefinition deserializedDefinition = (ProcessDefinition) in.readObject();

        assertEquals(definitionAsJson, jsonMapper.writeValueAsString(deserializedDefinition));
    }

    private DependencyGraph buildDependencyGraph(ProcessDefinition definition)
    {
        List<ProcessInput> inputs = definition.resources();
        List<Step> steps = definition.steps();

        // Map each output key to the key of processing step (expected to produce it)
        final Map<Integer, Integer> outputKeyToStepKey = inputs.stream()
            .filter(r -> r.getInputType() == EnumInputType.OUTPUT)
            .collect(Collectors.toMap(r -> r.key(), r -> ((ProcessOutput) r).stepKey()));

        // Build dependency graph
        final DependencyGraph graph = DependencyGraphs.create(steps.size());
        for (Step step: steps) {
            for (Integer k: step.inputKeys()) {
                if (outputKeyToStepKey.containsKey(k))
                    graph.addDependency(step.key(), outputKeyToStepKey.get(k));
            }
        }

        System.err.println(DependencyGraphs.toString(graph, v -> definition.stepByKey(v).name()));

        return graph;
    }

    @Test
    public void test1a_checkDefinition() throws Exception
    {
        ProcessDefinition definition1 = buildDefinition1a();
        assertNotNull(definition1);
        assertEquals("proc-1-a", definition1.name());

        List<ProcessInput> resources = definition1.resources();
        assertEquals(3, resources.size());
        assertEquals(3, resources.stream().mapToInt(r -> r.key()).distinct().count());

        List<Step> steps = definition1.steps();
        assertEquals(6, steps.size());
        assertEquals(6, steps.stream().mapToInt(s -> s.key()).distinct().count());
        assertEquals(
            ImmutableSet.of(
                "register-1", "register-2", "register-links",
                "triplegeo-1", "triplegeo-2", "link-1-with-2"),
            steps.stream().map(r -> r.name()).collect(Collectors.toSet()));

        Step step1 = steps.stream().filter(s -> s.name().equals("triplegeo-1")).findFirst().get();
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
        assertEquals(EnumDataFormat.N_TRIPLES, step1.configuration().getOutputFormat());

        Step step2 = steps.stream().filter(s -> s.name().equals("triplegeo-2")).findFirst().get();
        assertEquals(EnumTool.TRIPLEGEO, step2.tool());
        assertEquals(EnumOperation.TRANSFORM, step2.operation());
        assertEquals(EnumDataFormat.N_TRIPLES, step2.outputFormat());
        assertEquals(0, step2.inputKeys().size());
        assertNotNull(step2.outputKey());
        assertEquals(1, step2.sources().size());
        assertEquals(DATASOURCE_2_PATH, ((FileSystemDataSource) step2.sources().get(0)).getPath());
        assertEquals(
            jsonMapper.writeValueAsString(sampleTriplegeoConfiguration1),
            jsonMapper.writeValueAsString(step2.configuration()));
        assertTrue(step2.configuration() instanceof TriplegeoConfiguration);
        assertEquals(EnumDataFormat.N_TRIPLES, step2.configuration().getOutputFormat());

        Step step3 = steps.stream().filter(s -> s.name().equals("link-1-with-2")).findFirst().get();
        assertEquals(EnumTool.LIMES, step3.tool());
        assertEquals(EnumOperation.INTERLINK, step3.operation());
        assertEquals(EnumDataFormat.N_TRIPLES, step3.outputFormat());
        assertEquals(2, step3.inputKeys().size());
        assertEquals(step1.outputKey(), step3.inputKeys().get(0));
        assertEquals(step2.outputKey(), step3.inputKeys().get(1));
        assertEquals("transformed", step3.input().get(0).partKey());
        assertEquals("transformed", step3.input().get(1).partKey());
        assertEquals(
            jsonMapper.writeValueAsString(sampleLimesConfiguration1),
            jsonMapper.writeValueAsString(step3.configuration()));
        assertTrue(step3.configuration() instanceof LimesConfiguration);

        Step stepR1 = steps.stream().filter(s -> s.name().equals("register-1")).findFirst().get();
        assertEquals(EnumTool.REGISTER, stepR1.tool());
        assertEquals(EnumOperation.REGISTER, stepR1.operation());
        assertEquals(Collections.singletonList(step1.outputKey()), stepR1.inputKeys());
        assertEquals("transformed", stepR1.input().get(0).partKey());
        assertNull(stepR1.outputKey());
        assertNotNull(stepR1.configuration());
        assertTrue(stepR1.configuration() instanceof RegisterToCatalogConfiguration);

        Step stepR2 = steps.stream().filter(s -> s.name().equals("register-2")).findFirst().get();
        assertEquals(EnumTool.REGISTER, stepR2.tool());
        assertEquals(EnumOperation.REGISTER, stepR2.operation());
        assertEquals(Collections.singletonList(step2.outputKey()), stepR2.inputKeys());
        assertEquals("transformed", stepR2.input().get(0).partKey());
        assertNull(stepR2.outputKey());
        assertNotNull(stepR2.configuration());
        assertTrue(stepR2.configuration() instanceof RegisterToCatalogConfiguration);

        Step stepR3 = steps.stream().filter(s -> s.name().equals("register-links")).findFirst().get();
        assertEquals(EnumTool.REGISTER, stepR3.tool());
        assertEquals(EnumOperation.REGISTER, stepR3.operation());
        assertEquals(Collections.singletonList(step3.outputKey()), stepR3.inputKeys());
        assertEquals("accepted", stepR3.input().get(0).partKey());
        assertNull(stepR3.outputKey());
        assertNotNull(stepR3.configuration());
        assertTrue(stepR3.configuration() instanceof RegisterToCatalogConfiguration);

        List<CatalogResource> catalogResources = resources.stream()
            .filter(r -> r.getInputType() == EnumInputType.CATALOG)
            .map(CatalogResource.class::cast)
            .collect(Collectors.toList());
        assertEquals(0, catalogResources.size());

        List<ProcessOutput> outputResources = resources.stream()
            .filter(r -> r.getInputType() == EnumInputType.OUTPUT)
            .map(ProcessOutput.class::cast)
            .collect(Collectors.toList());
        assertEquals(3, outputResources.size());
        assertEquals(
            ImmutableSet.of("triplegeo-1", "triplegeo-2", "link-1-with-2"),
            outputResources.stream().map(r -> r.getName()).collect(Collectors.toSet()));
    }

    @Test
    public void test1b_checkDefinition() throws Exception
    {
        ProcessDefinition definition1 = buildDefinition1b();
        assertNotNull(definition1);
        assertEquals("proc-1-b", definition1.name());

        List<ProcessInput> resources = definition1.resources();
        assertEquals(5, resources.size());
        assertEquals(5, resources.stream().mapToInt(r -> r.key()).distinct().count());
        Map<Integer, ProcessInput> resourcesByKey = resources.stream()
            .collect(Collectors.toMap(r -> r.key(), Function.identity()));

        List<Step> steps = definition1.steps();
        assertEquals(6, steps.size());
        assertEquals(6, steps.stream().mapToInt(s -> s.key()).distinct().count());
        assertEquals(
            ImmutableSet.of(
                "register-1", "register-2", "register-links",
                "triplegeo-1", "triplegeo-2", "link-1-with-2"),
            steps.stream().map(r -> r.name()).collect(Collectors.toSet()));

        Step step1 = steps.stream().filter(s -> s.name().equals("triplegeo-1")).findFirst().get();
        assertEquals(EnumTool.TRIPLEGEO, step1.tool());
        assertEquals(EnumOperation.TRANSFORM, step1.operation());
        assertEquals(EnumDataFormat.N_TRIPLES, step1.outputFormat());
        assertEquals(1, step1.inputKeys().size());
        assertNotNull(step1.outputKey());
        assertEquals(0, step1.sources().size());
        assertNull(step1.input().get(0).partKey());
        ProcessInput inp1 = resourcesByKey.get(step1.inputKeys().get(0));
        assertNotNull(inp1);
        assertTrue(inp1 instanceof CatalogResource);
        assertEquals(RESOURCE_1_ID, ((CatalogResource) inp1).getResource());
        assertEquals(
            jsonMapper.writeValueAsString(sampleTriplegeoConfiguration1),
            jsonMapper.writeValueAsString(step1.configuration()));
        assertTrue(step1.configuration() instanceof TriplegeoConfiguration);
        assertEquals(EnumDataFormat.N_TRIPLES, step1.configuration().getOutputFormat());

        Step step2 = steps.stream().filter(s -> s.name().equals("triplegeo-2")).findFirst().get();
        assertEquals(EnumTool.TRIPLEGEO, step2.tool());
        assertEquals(EnumOperation.TRANSFORM, step2.operation());
        assertEquals(EnumDataFormat.N_TRIPLES, step2.outputFormat());
        assertEquals(1, step2.inputKeys().size());
        assertNotNull(step2.outputKey());
        assertEquals(0, step2.sources().size());
        assertNull(step2.input().get(0).partKey());
        ProcessInput inp2 = resourcesByKey.get(step2.inputKeys().get(0));
        assertNotNull(inp2);
        assertTrue(inp2 instanceof CatalogResource);
        assertEquals(RESOURCE_2_ID, ((CatalogResource) inp2).getResource());
        assertEquals(
            jsonMapper.writeValueAsString(sampleTriplegeoConfiguration1),
            jsonMapper.writeValueAsString(step2.configuration()));
        assertTrue(step2.configuration() instanceof TriplegeoConfiguration);
        assertEquals(EnumDataFormat.N_TRIPLES, step2.configuration().getOutputFormat());

        Step step3 = steps.stream().filter(s -> s.name().equals("link-1-with-2")).findFirst().get();
        assertEquals(EnumTool.LIMES, step3.tool());
        assertEquals(EnumOperation.INTERLINK, step3.operation());
        assertEquals(EnumDataFormat.N_TRIPLES, step3.outputFormat());
        assertEquals(2, step3.inputKeys().size());
        assertNotNull(step3.outputKey());
        assertEquals(0, step3.sources().size());
        assertEquals("transformed", step3.input().get(0).partKey());
        assertEquals("transformed", step3.input().get(1).partKey());
        ProcessInput inp3a = resourcesByKey.get(step3.inputKeys().get(0));
        ProcessInput inp3b = resourcesByKey.get(step3.inputKeys().get(1));
        assertNotNull(inp3a);
        assertNotNull(inp3b);
        assertTrue(inp3a instanceof ProcessOutput);
        assertTrue(inp3b instanceof ProcessOutput);
        assertEquals(step1.outputKey(), Integer.valueOf(inp3a.key()));
        assertEquals(step2.outputKey(), Integer.valueOf(inp3b.key()));
        assertEquals(
            jsonMapper.writeValueAsString(sampleLimesConfiguration1),
            jsonMapper.writeValueAsString(step3.configuration()));
        assertTrue(step3.configuration() instanceof LimesConfiguration);
        assertEquals(EnumDataFormat.N_TRIPLES, step3.configuration().getOutputFormat());

        Step stepR1 = steps.stream().filter(s -> s.name().equals("register-1")).findFirst().get();
        assertEquals(EnumTool.REGISTER, stepR1.tool());
        assertEquals(EnumOperation.REGISTER, stepR1.operation());
        assertEquals(Collections.singletonList(step1.outputKey()), stepR1.inputKeys());
        assertEquals("transformed", stepR1.input().get(0).partKey());
        assertNull(stepR1.outputKey());
        assertNotNull(stepR1.configuration());
        assertTrue(stepR1.configuration() instanceof RegisterToCatalogConfiguration);

        Step stepR2 = steps.stream().filter(s -> s.name().equals("register-2")).findFirst().get();
        assertEquals(EnumTool.REGISTER, stepR2.tool());
        assertEquals(EnumOperation.REGISTER, stepR2.operation());
        assertEquals(Collections.singletonList(step2.outputKey()), stepR2.inputKeys());
        assertEquals("transformed", stepR2.input().get(0).partKey());
        assertNull(stepR2.outputKey());
        assertNotNull(stepR2.configuration());
        assertTrue(stepR2.configuration() instanceof RegisterToCatalogConfiguration);

        Step stepR3 = steps.stream().filter(s -> s.name().equals("register-links")).findFirst().get();
        assertEquals(EnumTool.REGISTER, stepR3.tool());
        assertEquals(EnumOperation.REGISTER, stepR3.operation());
        assertEquals(Collections.singletonList(step3.outputKey()), stepR3.inputKeys());
        assertEquals("accepted", stepR3.input().get(0).partKey());
        assertNull(stepR3.outputKey());
        assertNotNull(stepR3.configuration());
        assertTrue(stepR3.configuration() instanceof RegisterToCatalogConfiguration);

        List<CatalogResource> catalogResources = resources.stream()
            .filter(r -> r.getInputType() == EnumInputType.CATALOG)
            .map(CatalogResource.class::cast)
            .collect(Collectors.toList());
        assertEquals(2, catalogResources.size());
        assertEquals(
            ImmutableSet.of(RESOURCE_1_NAME, RESOURCE_2_NAME),
            catalogResources.stream().map(r -> r.getName()).collect(Collectors.toSet()));

        List<ProcessOutput> outputResources = resources.stream()
            .filter(r -> r.getInputType() == EnumInputType.OUTPUT)
            .map(ProcessOutput.class::cast)
            .collect(Collectors.toList());
        assertEquals(3, outputResources.size());
        assertEquals(
            ImmutableSet.of("triplegeo-1", "triplegeo-2", "link-1-with-2"),
            outputResources.stream().map(r -> r.getName()).collect(Collectors.toSet()));

    }

    @Test
    public void test1a_serializeToJson() throws Exception
    {
        serializeToJson(buildDefinition1a());
    }

    @Test
    public void test1b_serializeToJson() throws Exception
    {
        serializeToJson(buildDefinition1b());
    }

    @Test
    public void test1a_serializeDefault() throws Exception
    {
        serializeDefault(buildDefinition1a());
    }

    @Test
    public void test1b_serializeDefault() throws Exception
    {
        serializeDefault(buildDefinition1b());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void test1a_checkDefinitionUnmodifiableSteps1()
    {
        ProcessDefinition definition1 = buildDefinition1a();
        List<Step> steps = definition1.steps();
        steps.remove(0); // attempt to remove a step
    }

    @Test(expected = UnsupportedOperationException.class)
    public void test1a_checkDefinitionUnmodifiableSteps2()
    {
        ProcessDefinition definition1 = buildDefinition1a();
        List<Step> steps = definition1.steps();
        Step step1 = steps.get(0);
        steps.add(step1); // attempt to add a step
    }

    @Test(expected = UnsupportedOperationException.class)
    public void test1a_checkDefinitionUnmodifiableResources1()
    {
        ProcessDefinition definition1 = buildDefinition1a();
        List<ProcessInput> resources = definition1.resources();
        resources.remove(0); // attempt to remove a resource
    }

    @Test(expected = UnsupportedOperationException.class)
    public void test1a_checkDefinitionUnmodifiableResources2()
    {
        ProcessDefinition definition1 = buildDefinition1a();
        List<ProcessInput> resources = definition1.resources();
        ProcessInput res1 = resources.get(0);
        resources.add(res1);
    }

    @Test(expected = IllegalStateException.class)
    public void test1a_checkDefinitionUnmodifiableConfiguration()
    {
        ProcessDefinition definition1 = buildDefinition1a();
        Step step11 = definition1.stepByNodeName("triplegeo-1");
        TriplegeoConfiguration configuration = (TriplegeoConfiguration) step11.configuration();
        configuration.setAttrKey("koukou");
    }

    @Test
    public void test1a_buildDependencyGraph() throws Exception
    {
        ProcessDefinition def1 = buildDefinition1a();
        DependencyGraph graph = buildDependencyGraph(def1);

        List<Step> stepsInTopologicalOrder = Lists.newArrayList(
            Iterables.transform(DependencyGraphs.topologicalSort(graph), k -> def1.stepByKey(k)));

        List<String> stepNames = Lists.transform(stepsInTopologicalOrder, s -> s.name());
        System.err.println(stepNames);

        assertTrue(stepNames.indexOf("triplegeo-1") < stepNames.indexOf("register-1"));
        assertTrue(stepNames.indexOf("triplegeo-2") < stepNames.indexOf("register-2"));
        assertTrue(stepNames.indexOf("link-1-with-2") < stepNames.indexOf("register-links"));
        assertTrue(stepNames.indexOf("triplegeo-1") < stepNames.indexOf("link-1-with-2"));
        assertTrue(stepNames.indexOf("triplegeo-2") < stepNames.indexOf("link-1-with-2"));
    }

    @Test(expected = IllegalStateException.class)
    public void test_checkRegisterUndefinedResource1()
    {
        final int resourceKey = 1; // not a catalog resource, neither is an output from another step
        ProcessDefinition definition = processDefinitionBuilderFactory.create("register-1")
            .register("register-1", resourceKey, new ResourceMetadataCreate("sample", "Another sample"))
            .build();
        System.err.println(definition);
    }

    @Test(expected = IllegalStateException.class)
    public void test_checkInputFromUndefinedResource1()
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


    @Test(expected = IllegalStateException.class)
    public void test_checkInputOfNonExistingOutputPart()
    {
        final int RESOURCE_1_KEY = 1, TRANSFORM_1_KEY = 10;
        final ResourceMetadataCreate metadata = new ResourceMetadataCreate("tr-1", "An RDF file");

        ProcessDefinition definition = processDefinitionBuilderFactory.create("register-1")
            .resource(RESOURCE_1_NAME, RESOURCE_1_KEY, RESOURCE_1_ID)
            .transform("triplegeo-1", builder -> builder
                .input(RESOURCE_1_KEY, "something") // a catalog resource has no parts!
                .configuration(sampleTriplegeoConfiguration1)
                .outputFormat(EnumDataFormat.N_TRIPLES)
                .outputKey(TRANSFORM_1_KEY))
            .register("tr-1", TRANSFORM_1_KEY, "transformed", metadata)
            .build();
        System.err.println(definition);
    }

    @Test(expected = IllegalStateException.class)
    public void test_checkInputOfUnknownOutputPart()
    {
        final int RESOURCE_1_KEY = 1, TRANSFORM_1_KEY = 10;
        final ResourceMetadataCreate metadata = new ResourceMetadataCreate("tr-1", "An RDF file");

        ProcessDefinition definition = processDefinitionBuilderFactory.create("register-1")
            .resource(RESOURCE_1_NAME, RESOURCE_1_KEY, RESOURCE_1_ID)
            .transform("triplegeo-1", builder -> builder
                .input(RESOURCE_1_KEY)
                .configuration(sampleTriplegeoConfiguration1)
                .outputFormat(EnumDataFormat.N_TRIPLES)
                .outputKey(TRANSFORM_1_KEY))
            .register("tr-1", TRANSFORM_1_KEY, "something", metadata) // a non-existing part of output
            .build();
        System.err.println(definition);
    }

    @Test(expected = IllegalStateException.class)
    public void test_checkInputOfInvalidOutputPart()
    {
        final int RESOURCE_1_KEY = 1, TRANSFORM_1_KEY = 10;
        final ResourceMetadataCreate metadata = new ResourceMetadataCreate("tr-1", "An RDF file");

        ProcessDefinition definition = processDefinitionBuilderFactory.create("register-1")
            .resource(RESOURCE_1_NAME, RESOURCE_1_KEY, RESOURCE_1_ID)
            .transform("triplegeo-1", builder -> builder
                .input(RESOURCE_1_KEY)
                .configuration(sampleTriplegeoConfiguration1)
                .outputFormat(EnumDataFormat.N_TRIPLES)
                .outputKey(TRANSFORM_1_KEY))
            .register("tr-1", TRANSFORM_1_KEY, "transformed-metadata", metadata) // a part of KPI output
            .build();
        System.err.println(definition);
    }

    @Test
    public void test1a_scratch() throws Exception
    {
        ProcessDefinition definition1 = buildDefinition1a();

        String s1 = jsonMapper.writeValueAsString(definition1);
        System.err.println(s1);

        ProcessDefinition definition1a = jsonMapper.readValue(s1, ProcessDefinition.class);
        String s2 = jsonMapper.writeValueAsString(definition1a);
        assertEquals(s1, s2);

        final List<ProcessInput> inputs = definition1.resources();
        final List<Step> steps = definition1.steps();
        final DependencyGraph dependencyGraph = buildDependencyGraph(definition1);

        Iterable<Step> sortedSteps = IterableUtils.transformedIterable(
            DependencyGraphs.topologicalSort(dependencyGraph), k -> definition1.stepByKey(k));


    }
}
