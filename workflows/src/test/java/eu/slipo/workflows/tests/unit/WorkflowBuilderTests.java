package eu.slipo.workflows.tests.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.ListUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import eu.slipo.workflows.Workflow;
import eu.slipo.workflows.WorkflowBuilderFactory;
import eu.slipo.workflows.tests.BatchConfiguration;
import eu.slipo.workflows.tests.TaskExecutorConfiguration;
import eu.slipo.workflows.tests.WorkflowBuilderFactoryConfiguration;

@RunWith(SpringRunner.class)
@EnableAutoConfiguration
@SpringBootTest(
    classes = { 
        TaskExecutorConfiguration.class, 
        BatchConfiguration.class,
        WorkflowBuilderFactoryConfiguration.class
    })
public class WorkflowBuilderTests
{    
    private static class DummyTasklet implements Tasklet
    {
        @Override
        public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
            throws Exception
        {
            System.err.println("Executing dummy step ...");
            return null;
        }
    }
    
    private static Path inputPath = Paths.get("/var/local/slipo-workbench/1.txt");
    
    @Autowired
    @Qualifier("workflowDataDirectory")
    private Path dataDir;
    
    @Autowired
    private StepBuilderFactory stepBuilderFactory;
    
    @Autowired
    private WorkflowBuilderFactory workflowBuilderFactory;
    
    private Step dummyStep;

    private UUID workflowId;
    
    private Workflow workflow;
    
    @Before
    public void setUp() throws Exception
    {
        workflowId = UUID.randomUUID();
        
        dummyStep = stepBuilderFactory.get("dummy")
            .tasklet(new DummyTasklet())
            .build();
        
        workflow = workflowBuilderFactory.get(workflowId)
            .job(c -> c
                .name("A")
                .flow(dummyStep)
                .input(inputPath)
                .output("a1.txt", "a2.txt")
                .parameters(b -> b
                    .addLong("number", 199L)
                    .addString("greeting", "Hello World")))
            .job(c -> c
                .name("X")
                .flow(dummyStep)
                .parameters(b -> b.addString("foo", "Baz"))
                .output("x1.txt"))
            .job(c -> c
                .name("B")
                .flow(dummyStep)
                .input("A", "*.txt")
                .output("b1.txt", "b2.txt"))
            .job(c -> c
                .name("C")
                .flow(dummyStep)
                .input("A", "a1.txt")
                .output("c1.txt", "c2.txt", "c3.txt"))
            .output("B", "b1.txt", "res-b-1.txt")
            .output("C", "c1.txt", "res-c-1.txt")
            .build();
    }

    @After
    public void tearDown() throws Exception
    {
        workflowId = null;
        workflow = null;
    }
    
    @Test
    public void testIdentifier()
    {        
        assertEquals(workflowId, workflow.id());
    }
    
    @Test
    public void testNodesAndDependencies()
    {   
        assertEquals(
            new HashSet<>(Arrays.asList("A", "B", "C", "X")), workflow.nodeNames());
        
        Workflow.JobNode nodeA = workflow.node("A"), 
            nodeB = workflow.node("B"), 
            nodeC = workflow.node("C"), 
            nodeX = workflow.node("X");
        
        assertEquals(nodeA.name(), "A");
        assertEquals(nodeB.name(), "B");
        assertEquals(nodeC.name(), "C");
        assertEquals(nodeX.name(), "X");
        
        // Test dependencies
        
        assertEquals(Collections.emptyList(), IterableUtils.toList(nodeA.dependencies()));
        assertEquals(Arrays.asList(nodeB, nodeC), IterableUtils.toList(nodeA.dependents()));

        assertEquals(Collections.singletonList(nodeA), IterableUtils.toList(nodeB.dependencies()));
        assertEquals(Collections.emptyList(), IterableUtils.toList(nodeB.dependents()));
        
        assertEquals(Collections.singletonList(nodeA), IterableUtils.toList(nodeC.dependencies()));
        assertEquals(Collections.emptyList(), IterableUtils.toList(nodeC.dependents()));
        
        assertEquals(Collections.emptyList(), IterableUtils.toList(nodeX.dependencies()));
        assertEquals(Collections.emptyList(), IterableUtils.toList(nodeX.dependents()));
        
        // Test inputs and outputs for nodes
       
        for (Workflow.JobNode node: workflow.nodes()) {
            Path stagingDir = workflow.stagingDirectory(node.name());
            assertTrue("Expected output of node to be inside its own staging directory", 
                IterableUtils.matchesAll(node.output(), p -> p.startsWith(stagingDir)));
        }
        
        assertEquals(Collections.singletonList(inputPath), nodeA.input());
        
        assertTrue("Expected input of B to contain output of A", 
            nodeB.input().containsAll(nodeA.output()));
        
        assertTrue("Expected input of C to contain output of A named a1.txt", 
            nodeC.input().containsAll(
                ListUtils.select(nodeA.output(), p -> p.getFileName().toString().equals("a1.txt"))));
        
        assertEquals(Collections.emptyList(), nodeX.input());
        
        // Test job-name, flow, and parameters
     
        for (Workflow.JobNode node: workflow.nodes()) {
            final JobParameters parameters = node.parameters();
            final String jobName = node.jobName();
            final Flow flow = node.flow(stepBuilderFactory);
            assertNotNull("A node is expected to have a job name", jobName);
            assertNotNull("A node is expected to be associated to Batch flow", flow);
            assertNotNull("Expected a parameter fpr the workflow identifier", 
                parameters.getString(Workflow.Parameter.WORKFLOW.key()));
            List<Path> inputs = Arrays.stream(
                    parameters.getString(Workflow.Parameter.INPUT.key()).split(File.pathSeparator))
                .filter(s -> !s.isEmpty())
                .map(s -> Paths.get(s))
                .collect(Collectors.toList());
            assertEquals(inputs, node.input());
        }
        
        assertEquals(nodeA.parameters().getString("greeting"), "Hello World");
        assertEquals(nodeA.parameters().getLong("number"), Long.valueOf(199L));
        assertEquals(nodeX.parameters().getString("foo"), "Baz");
    }
    
    @Test(expected = IllegalArgumentException.class) 
    public void testNonExistingName()
    {
        workflow.node("Z");
    }
    
    @Test(expected = UnsupportedOperationException.class) 
    public void testUnmodifiableNames()
    {
        workflow.nodeNames().add("Z");
    }
    
    @Test(expected = UnsupportedOperationException.class) 
    public void testUnmodifiableOutputMapOfPaths()
    {
        workflow.output().put("foo.txt", Paths.get("baz.txt"));
    }
    
    @Test(expected = UnsupportedOperationException.class) 
    public void testUnmodifiableOutputMapOfUris()
    {
        workflow.outputUris().put("foo.txt", URI.create("file:///tmp/baz.txt"));
    }
    
    @Test
    public void testNodesInTopologicalOrder()
    {
        List<String> names = IterableUtils.toList(
            IterableUtils.transformedIterable(
                workflow.nodesInTopologicalOrder(),
                y -> y.name()));
        
        assertTrue("Expected node A to precede node B", 
            names.indexOf("A") < names.indexOf("B"));
        assertTrue("Expected node A to precede node C", 
            names.indexOf("A") < names.indexOf("C"));
    }
    
    @Test
    public void testOutputMap()
    {
        Map<String,Path> outputMap = workflow.output();
        
        assertEquals(
            new HashSet<>(Arrays.asList("res-b-1.txt", "res-c-1.txt")),
            outputMap.keySet());
        
        assertTrue(outputMap.get("res-b-1.txt").startsWith(workflow.stagingDirectory("B")));
        assertTrue(outputMap.get("res-b-1.txt").endsWith("b1.txt"));
        
        assertTrue(outputMap.get("res-c-1.txt").startsWith(workflow.stagingDirectory("C")));
        assertTrue(outputMap.get("res-c-1.txt").endsWith("c1.txt"));
    }
    
    @Test
    public void testPaths()
    {
        Path workflowDir = workflow.dataDirectory();
        Path workflowOutputDir = workflow.outputDirectory();
        
        assertTrue("Expected an absolute path", dataDir.isAbsolute());
        assertTrue("Expected an absolute path", workflowDir.isAbsolute());
        
        assertTrue("Expected workflow\'s data directory inside given data directory", 
            workflowDir.startsWith(dataDir));
        assertTrue("Expected output directory inside workflow\'s data directory", 
            workflowOutputDir.startsWith(workflowDir));
        
        for (String nodeName: workflow.nodeNames()) {
            assertTrue("Expected node\'s staging directory inside workflow\\'s data directory", 
                workflow.stagingDirectory(nodeName).startsWith(workflowDir));
        }
    }

}
