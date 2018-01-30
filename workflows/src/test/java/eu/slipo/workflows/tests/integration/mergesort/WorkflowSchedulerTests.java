package eu.slipo.workflows.tests.integration.mergesort;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.batch.test.AssertFile.assertFileEquals;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.task.TaskExecutor;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StringUtils;

import eu.slipo.workflows.Workflow;
import eu.slipo.workflows.WorkflowExecution;
import eu.slipo.workflows.WorkflowExecutionCompletionListener;
import eu.slipo.workflows.WorkflowExecutionCompletionListenerSupport;
import eu.slipo.workflows.WorkflowExecutionEventListenerSupport;
import eu.slipo.workflows.WorkflowExecutionSnapshot;
import eu.slipo.workflows.service.WorkflowScheduler;
import eu.slipo.workflows.tests.BatchConfiguration;
import eu.slipo.workflows.tests.JobDataConfiguration;
import eu.slipo.workflows.tests.TaskExecutorConfiguration;
import eu.slipo.workflows.tests.WorkflowBuilderFactoryConfiguration;
import eu.slipo.workflows.tests.WorkflowSchedulerConfiguration;
import eu.slipo.workflows.examples.mergesort.MergesortJobConfiguration;
import eu.slipo.workflows.examples.mergesort.MergesortWorkflows;
import eu.slipo.workflows.exception.WorkflowExecutionStartException;

@RunWith(SpringRunner.class)
@EnableAutoConfiguration
@SpringBootTest(
    classes = {    
        TaskExecutorConfiguration.class, 
        BatchConfiguration.class,     
        WorkflowBuilderFactoryConfiguration.class,
        WorkflowSchedulerConfiguration.class,
        JobDataConfiguration.class,
        // job-specific application context
        MergesortJobConfiguration.class,
        MergesortWorkflows.class
    })
public class WorkflowSchedulerTests
{
    private static Logger logger = LoggerFactory.getLogger(WorkflowSchedulerTests.class);
    
    public static final String RESULT_FILENAME = MergesortWorkflows.RESULT_FILENAME;
    
    private static class Fixture
    {
        final Path inputPath;
        
        final Path expectedResultPath;

        Fixture(Path inputPath, Path expectedResultPath)
        {
            this.inputPath = inputPath;
            this.expectedResultPath = expectedResultPath;
        }
        
        Fixture(URL inputPath, URL expectedResultPath)
        {
            this.inputPath = Paths.get(inputPath.getPath());
            this.expectedResultPath = Paths.get(expectedResultPath.getPath());
        }
    }
    
    private static class LoggingExecutionEventListener extends WorkflowExecutionEventListenerSupport
    {
        @Override
        public void beforeNode(
            WorkflowExecutionSnapshot workflowExecutionSnapshot, String nodeName, JobExecution jobExecution)
        {
            Workflow workflow = workflowExecutionSnapshot.workflow();
            logger.info("The workflow node {}/{} reports *before* as {}", 
                workflow.id(), nodeName, jobExecution.getStatus());
        }
        
        @Override
        public void afterNode(
            WorkflowExecutionSnapshot workflowExecutionSnapshot, String nodeName, JobExecution jobExecution)
        {
            Workflow workflow = workflowExecutionSnapshot.workflow();
            logger.debug("workflow {}: {}", workflow.id(), workflowExecutionSnapshot.debugGraph());
            logger.info("The workflow node {}/{} reports *after* as {}", 
                workflow.id(), nodeName, jobExecution.getStatus());
        }
    }
    
    private static class LoggingCompletionListener extends WorkflowExecutionCompletionListenerSupport
    {
        @Override
        public void onSuccess(WorkflowExecutionSnapshot workflowExecutionSnapshot)
        {
            Workflow workflow = workflowExecutionSnapshot.workflow();
            logger.info("The workflow {} is complete; The output is at %s",
                workflow.id(), workflow.outputDirectory());
        }
    }
    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        final Path fixturePath = Paths.get("testcases/integration/mergesort");
        Class<? extends WorkflowSchedulerTests> cls = WorkflowSchedulerTests.class;
        
        // Add fixtures from src/test/resources
        
        fixtures.add(new Fixture(
            cls.getResource("/" + fixturePath.resolve("1.txt").toString()),
            cls.getResource("/" + fixturePath.resolve("1-sorted.txt").toString())));
        
        fixtures.add(new Fixture(
            cls.getResource("/" + fixturePath.resolve("2.txt").toString()),
            cls.getResource("/" + fixturePath.resolve("2-sorted.txt").toString())));
        
        // Add randomly generated fixtures
        
        
        for (long sampleSize: Arrays.asList(1000 * 1000L, 3000 * 1000L)) 
        {
            Path inputPath = 
                Files.createTempFile("workflowSchedulerTests", ".txt");
            Path expectedResultPath = inputPath.resolveSibling(
                StringUtils.stripFilenameExtension(inputPath.getFileName().toString()) + "-sorted.txt");
            generateInputAndExpectedResult(inputPath, expectedResultPath, sampleSize);
            fixtures.add(new Fixture(inputPath, expectedResultPath));
        }
    }

    private static void generateInputAndExpectedResult(Path inputPath, Path expectedResultPath, long sampleSize) 
        throws IOException
    {
        Random random = new Random();
        
        long[] sample = random.longs(sampleSize).toArray();
        
        try (BufferedWriter writer = Files.newBufferedWriter(inputPath)) {
            for (long r: sample) {
                writer.write(Long.toString(r));
                writer.newLine();
            }
        }
        
        Arrays.sort(sample);
        
        try (BufferedWriter writer = Files.newBufferedWriter(expectedResultPath)) {
            for (long r: sample) {
                writer.write(Long.toString(r));
                writer.newLine();
            }
        }
        
        logger.info("Generated a sample of {} longs into {}. The expected result is at {}",
            sampleSize, inputPath, expectedResultPath);
    }
    
    @AfterClass
    public static void tearDownAfterClass() throws Exception
    {
    }
    
    private static List<Fixture> fixtures = new ArrayList<>();
    
    @Autowired
    private TaskExecutor taskExecutor;
    
    @Autowired
    private JobRepository jobRepository;
    
    @Autowired
    private WorkflowScheduler workflowScheduler;
    
    @Autowired
    @Qualifier("mergesort.workflows")
    private MergesortWorkflows mergesortWorkflows;
      
    @Before
    public void setUp() throws Exception
    {
    }

    @After
    public void tearDown() throws Exception
    {
    }
    
    private void startAndWaitToComplete(Workflow workflow, Path expectedResultPath) 
        throws Exception
    {
        startAndWaitToComplete(Collections.singletonMap(workflow, expectedResultPath));
    }
    
    private void startAndWaitToComplete(Map<Workflow,Path> workflowToResult) 
        throws Exception
    {        
        final int n = workflowToResult.size();
        final CountDownLatch done = new CountDownLatch(n);
        
        WorkflowExecutionCompletionListener callback = new WorkflowExecutionCompletionListenerSupport()
        {
            @Override
            public void onSuccess(WorkflowExecutionSnapshot workflowExecutionSnapshot)
            {
                final Workflow workflow = workflowExecutionSnapshot.workflow();
                logger.info("The workflow {} finished successfully", workflow.id());
                done.countDown();
            }
        };
        
        Random rng = new Random();
        
        for (Workflow workflow: workflowToResult.keySet()) {
            taskExecutor.execute(new Runnable() 
            {
                @Override
                public void run()
                {
                    // Simulate some random delay
                    
                    try {
                        Thread.sleep(50L + rng.nextInt(100));
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                        throw new IllegalStateException(ex);
                    }
                    
                    // Submit workflow
                    
                    try {
                        workflowScheduler.start(workflow, callback);
                    } catch (WorkflowExecutionStartException ex) {
                        ex.printStackTrace();
                        throw new IllegalStateException(ex);
                    }
                }
            });
        }
        
        // Wait for everyone
        
        done.await();
      
        // Test status of completed workflows
        
        for (Workflow workflow: workflowToResult.keySet()) {
            WorkflowScheduler.ExecutionSnapshot snapshot = workflowScheduler.poll(workflow.id());
            WorkflowExecutionSnapshot workflowExecutionSnapshot = snapshot.workflowExecutionSnapshot();
            assertTrue(workflowExecutionSnapshot.isComplete());
            assertEquals(WorkflowScheduler.ExecutionStatus.COMPLETED, snapshot.status());
        }
        
        // Test results
        
        for (Map.Entry<Workflow,Path> e: workflowToResult.entrySet()) {
            final Workflow workflow = e.getKey();
            final Path expectedResultPath = e.getValue();
            
            Map<String, Path> outputMap = workflow.output();
            Path resultPath = outputMap.get(RESULT_FILENAME);
            logger.info("The workflow {} is finished (result at {})", workflow.id(), resultPath);
            
            assertTrue("Expected output result to be a readable file", 
                Files.isRegularFile(resultPath) && Files.isReadable(resultPath));
            
            assertFileEquals(expectedResultPath.toFile(), resultPath.toFile());
            
            // Test node status
            
            WorkflowExecution workflowExecution = new WorkflowExecution(workflow);
            workflowExecution.load(jobRepository);
            assertTrue(workflowExecution.isComplete());
        }
    }
    
    //
    // Tests
    //
        
    @Test(timeout = 60 * 1000L)
    public void test1k5p80() throws Exception
    {
        Fixture f = fixtures.get(0);
        Workflow workflow = mergesortWorkflows.getBuilder(UUID.randomUUID(), f.inputPath)
            .numParts(80).mergeIn(5)
            .build();
        startAndWaitToComplete(workflow, f.expectedResultPath);
    }
    
    @Test(timeout = 60 * 1000L)
    public void test2k5p80() throws Exception
    {
        Fixture f = fixtures.get(1);
        Workflow workflow = mergesortWorkflows.getBuilder(UUID.randomUUID(), f.inputPath)
            .numParts(80).mergeIn(5)
            .build();
        startAndWaitToComplete(workflow, f.expectedResultPath);
    }
    
    @Test(timeout = 60 * 1000L)
    public void test1k5p30() throws Exception
    {
        Fixture f = fixtures.get(0);
        Workflow workflow = mergesortWorkflows.getBuilder(UUID.randomUUID(), f.inputPath)
            .numParts(30).mergeIn(5)
            .build();
        startAndWaitToComplete(workflow, f.expectedResultPath);
    }
    
    @Test(timeout = 60 * 1000L)
    public void test2k5p30() throws Exception
    {
        Fixture f = fixtures.get(1);
        Workflow workflow = mergesortWorkflows.getBuilder(UUID.randomUUID(), f.inputPath)
            .numParts(30).mergeIn(5)
            .build();
        startAndWaitToComplete(workflow, f.expectedResultPath);
    }
    
    @Test(timeout = 60 * 1000L)
    public void test1k2p25() throws Exception
    {
        Fixture f = fixtures.get(0);
        Workflow workflow = mergesortWorkflows.getBuilder(UUID.randomUUID(), f.inputPath)
            .numParts(25).mergeIn(2)
            .build();
        startAndWaitToComplete(workflow, f.expectedResultPath);
    }
    
    @Test(timeout = 60 * 1000L)
    public void test2k2p25() throws Exception
    {
        Fixture f = fixtures.get(1);
        Workflow workflow = mergesortWorkflows.getBuilder(UUID.randomUUID(), f.inputPath)
            .numParts(25).mergeIn(2)
            .build();
        startAndWaitToComplete(workflow, f.expectedResultPath);
    }
    
    @Test(timeout = 60 * 1000L)
    public void test1k2p10() throws Exception
    {
        Fixture f = fixtures.get(0);
        Workflow workflow = mergesortWorkflows.getBuilder(UUID.randomUUID(), f.inputPath)
            .numParts(10).mergeIn(2)
            .build();
        startAndWaitToComplete(workflow, f.expectedResultPath);
    }
    
    @Test(timeout = 60 * 1000L)
    public void test2k2p10() throws Exception
    {
        Fixture f = fixtures.get(1);
        Workflow workflow = mergesortWorkflows.getBuilder(UUID.randomUUID(), f.inputPath)
            .numParts(10).mergeIn(2)
            .build();
        startAndWaitToComplete(workflow, f.expectedResultPath);
    }
    
    @Test(timeout = 60 * 1000L)
    public void test1k10p30() throws Exception
    {
        Fixture f = fixtures.get(0);
        Workflow workflow = mergesortWorkflows.getBuilder(UUID.randomUUID(), f.inputPath)
            .numParts(30).mergeIn(10)
            .build();
        startAndWaitToComplete(workflow, f.expectedResultPath);
    }
    
    @Test(timeout = 60 * 1000L)
    public void test2k10p30() throws Exception
    {
        Fixture f = fixtures.get(1);
        Workflow workflow = mergesortWorkflows.getBuilder(UUID.randomUUID(), f.inputPath)
            .numParts(30).mergeIn(10)
            .build();
        startAndWaitToComplete(workflow, f.expectedResultPath);
    }
    
    @Test(timeout = 60 * 1000L)
    public void test3k3p80() throws Exception
    {        
        Fixture f = fixtures.get(2);
        Workflow workflow = mergesortWorkflows.getBuilder(UUID.randomUUID(), f.inputPath)
            .numParts(80).mergeIn(3)
            .build();
        startAndWaitToComplete(workflow, f.expectedResultPath);
    }
    
    @Test(timeout = 60 * 1000L)
    public void test3k10p80() throws Exception
    {
        Fixture f = fixtures.get(2);
        Workflow workflow = mergesortWorkflows.getBuilder(UUID.randomUUID(), f.inputPath)
            .numParts(80).mergeIn(10)
            .build();
        startAndWaitToComplete(workflow, f.expectedResultPath);
    }
    
    @Test(timeout = 60 * 1000L)
    public void test4k3p80() throws Exception
    {        
        Fixture f = fixtures.get(3);
        Workflow workflow = mergesortWorkflows.getBuilder(UUID.randomUUID(), f.inputPath)
            .numParts(80).mergeIn(3)
            .build();
        startAndWaitToComplete(workflow, f.expectedResultPath);
    }
    
    @Test(timeout = 60 * 1000L)
    public void test4k10p150() throws Exception
    {
        Fixture f = fixtures.get(3);
        Workflow workflow = mergesortWorkflows.getBuilder(UUID.randomUUID(), f.inputPath)
            .numParts(150).mergeIn(10)
            .build();
        startAndWaitToComplete(workflow, f.expectedResultPath);
    }
    
    @Test(timeout = 120 * 1000L)
    public void testPx9() throws Exception
    {
        Fixture f1 = fixtures.get(0);
        Fixture f2 = fixtures.get(1);
        Fixture f3 = fixtures.get(2);
        
        Map<Workflow, Path> workflowToResult = new HashMap<>();
        
        // f1
        
        Workflow w1a = mergesortWorkflows.getBuilder(UUID.randomUUID(), f1.inputPath)
            .numParts(90).mergeIn(5)
            .build();
        workflowToResult.put(w1a, f1.expectedResultPath);
        
        Workflow w1b = mergesortWorkflows.getBuilder(UUID.randomUUID(), f1.inputPath)
            .numParts(90).mergeIn(8)
            .build();
        workflowToResult.put(w1b, f1.expectedResultPath);
        
        Workflow w1c = mergesortWorkflows.getBuilder(UUID.randomUUID(), f1.inputPath)
            .numParts(180).mergeIn(12)
            .build();
        workflowToResult.put(w1c, f1.expectedResultPath);

        // f2
        
        Workflow w2a = mergesortWorkflows.getBuilder(UUID.randomUUID(), f2.inputPath)
            .numParts(50).mergeIn(5)
            .build();
        workflowToResult.put(w2a, f2.expectedResultPath);
        
        Workflow w2b = mergesortWorkflows.getBuilder(UUID.randomUUID(), f2.inputPath)
            .numParts(50).mergeIn(12)
            .build();
        workflowToResult.put(w2b, f2.expectedResultPath);
        
        Workflow w2c = mergesortWorkflows.getBuilder(UUID.randomUUID(), f2.inputPath)
            .numParts(40).mergeIn(2)
            .build();
        workflowToResult.put(w2c, f2.expectedResultPath);
        
        // f3
        
        Workflow w3a = mergesortWorkflows.getBuilder(UUID.randomUUID(), f3.inputPath)
            .numParts(50).mergeIn(5)
            .build();
        workflowToResult.put(w3a, f3.expectedResultPath);
        
        Workflow w3b = mergesortWorkflows.getBuilder(UUID.randomUUID(), f3.inputPath)
            .numParts(50).mergeIn(12)
            .build();
        workflowToResult.put(w3b, f3.expectedResultPath);
        
        Workflow w3c = mergesortWorkflows.getBuilder(UUID.randomUUID(), f3.inputPath)
            .numParts(40).mergeIn(2)
            .build();
        workflowToResult.put(w3c, f3.expectedResultPath);
        
        // Submit workflows, expect results
        
        startAndWaitToComplete(workflowToResult);
    }
}
