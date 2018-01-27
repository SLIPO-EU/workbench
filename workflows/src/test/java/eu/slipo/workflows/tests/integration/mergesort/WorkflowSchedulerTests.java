package eu.slipo.workflows.tests.integration.mergesort;

import static org.junit.Assert.assertEquals;
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
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StringUtils;

import eu.slipo.workflows.Workflow;
import eu.slipo.workflows.WorkflowExecution;
import eu.slipo.workflows.WorkflowExecutionCompletionListener;
import eu.slipo.workflows.WorkflowExecutionCompletionListenerSupport;
import eu.slipo.workflows.WorkflowExecutionEventListenerSupport;
import eu.slipo.workflows.WorkflowExecutionSnapshot;
import eu.slipo.workflows.jobs.SplitFileJobConfiguration;
import eu.slipo.workflows.service.WorkflowScheduler;
import eu.slipo.workflows.tests.BatchConfiguration;
import eu.slipo.workflows.tests.JobDataConfiguration;
import eu.slipo.workflows.tests.TaskExecutorConfiguration;
import eu.slipo.workflows.tests.WorkflowBuilderFactoryConfiguration;
import eu.slipo.workflows.tests.WorkflowSchedulerConfiguration;

@RunWith(SpringRunner.class)
@EnableAutoConfiguration
@SpringBootTest(
    classes = {    
        TaskExecutorConfiguration.class, 
        BatchConfiguration.class,     
        WorkflowBuilderFactoryConfiguration.class,
        WorkflowSchedulerConfiguration.class,
        
        JobDataConfiguration.class,
        SplitFileJobConfiguration.class,
        
        eu.slipo.workflows.jobs.examples.mergesort.MergeFilesJobConfiguration.class, 
        eu.slipo.workflows.jobs.examples.mergesort.SortFileJobConfiguration.class, 
        eu.slipo.workflows.jobs.examples.mergesort.StatFileJobConfiguration.class,
        eu.slipo.workflows.examples.mergesort.WorkflowBuilderFactory.class,
    })
public class WorkflowSchedulerTests
{
    private static Logger logger = LoggerFactory.getLogger(WorkflowSchedulerTests.class);
    
    private static class Fixture
    {
        Path inputPath;
        
        Path expectedResultPath;

        public Fixture(Path inputPath, Path expectedResultPath)
        {
            this.inputPath = inputPath;
            this.expectedResultPath = expectedResultPath;
        }
        
        public Fixture(URL inputPath, URL expectedResultPath)
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
        
        fixtures.add(new Fixture(
            cls.getResource("/" + fixturePath.resolve("1.txt").toString()),
            cls.getResource("/" + fixturePath.resolve("1-sorted.txt").toString())));
        
        fixtures.add(new Fixture(
            cls.getResource("/" + fixturePath.resolve("2.txt").toString()),
            cls.getResource("/" + fixturePath.resolve("2-sorted.txt").toString())));
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
    private JobRepository jobRepository;
    
    @Autowired
    private WorkflowScheduler workflowScheduler;
    
    @Autowired
    @Qualifier("mergesort.workflowBuilderFactory")
    private eu.slipo.workflows.examples.mergesort.WorkflowBuilderFactory workflowBuilderFactory;
      
    @Before
    public void setUp() throws Exception
    {
    }

    @After
    public void tearDown() throws Exception
    {
    }
    
    @Test(timeout = 60 * 1000L)
    public void testR1k3p80() throws Exception
    {
        Path inputPath = Files.createTempFile("workflowSchedulerTests", ".txt");
        Path expectedResultPath = inputPath.resolveSibling(
            StringUtils.stripFilenameExtension(inputPath.getFileName().toString()) + "-sorted.txt");
        
        generateInputAndExpectedResult(inputPath, expectedResultPath, 1 * 1000 * 1000);
        
        Workflow workflow = workflowBuilderFactory.get(UUID.randomUUID(), inputPath)
            .numParts(80).mergeIn(3)
            .build();
        startAndWaitToComplete(workflow, expectedResultPath);
    }
    
    @Test(timeout = 60 * 1000L)
    public void testR1k10p80() throws Exception
    {
        Path inputPath = Files.createTempFile("workflowSchedulerTests", ".txt");
        Path expectedResultPath = inputPath.resolveSibling(
            StringUtils.stripFilenameExtension(inputPath.getFileName().toString()) + "-sorted.txt");
        
        generateInputAndExpectedResult(inputPath, expectedResultPath, 1 * 1000 * 1000);
        
        Workflow workflow = workflowBuilderFactory.get(UUID.randomUUID(), inputPath)
            .numParts(80).mergeIn(10)
            .build();
        startAndWaitToComplete(workflow, expectedResultPath);
    }
    
    @Test(timeout = 60 * 1000L)
    public void testR3k10p80() throws Exception
    {
        Path inputPath = Files.createTempFile("workflowSchedulerTests", ".txt");
        Path expectedResultPath = inputPath.resolveSibling(
            StringUtils.stripFilenameExtension(inputPath.getFileName().toString()) + "-sorted.txt");
        
        generateInputAndExpectedResult(inputPath, expectedResultPath, 3 * 1000 * 1000);
        
        Workflow workflow = workflowBuilderFactory.get(UUID.randomUUID(), inputPath)
            .numParts(80).mergeIn(10)
            .build();
        startAndWaitToComplete(workflow, expectedResultPath);
    }
    
    @Test(timeout = 60 * 1000L)
    public void test1k5p80() throws Exception
    {
        Fixture f = fixtures.get(0);
        Workflow workflow = workflowBuilderFactory.get(UUID.randomUUID(), f.inputPath)
            .numParts(80).mergeIn(5)
            .build();
        startAndWaitToComplete(workflow, f.expectedResultPath);
    }
    
    @Test(timeout = 60 * 1000L)
    public void test2k5p80() throws Exception
    {
        Fixture f = fixtures.get(1);
        Workflow workflow = workflowBuilderFactory.get(UUID.randomUUID(), f.inputPath)
            .numParts(80).mergeIn(5)
            .build();
        startAndWaitToComplete(workflow, f.expectedResultPath);
    }
    
    @Test(timeout = 60 * 1000L)
    public void test1k5p30() throws Exception
    {
        Fixture f = fixtures.get(0);
        Workflow workflow = workflowBuilderFactory.get(UUID.randomUUID(), f.inputPath)
            .numParts(30).mergeIn(5)
            .build();
        startAndWaitToComplete(workflow, f.expectedResultPath);
    }
    
    @Test(timeout = 60 * 1000L)
    public void test2k5p30() throws Exception
    {
        Fixture f = fixtures.get(1);
        Workflow workflow = workflowBuilderFactory.get(UUID.randomUUID(), f.inputPath)
            .numParts(30).mergeIn(5)
            .build();
        startAndWaitToComplete(workflow, f.expectedResultPath);
    }
    
    @Test(timeout = 60 * 1000L)
    public void test1k2p25() throws Exception
    {
        Fixture f = fixtures.get(0);
        Workflow workflow = workflowBuilderFactory.get(UUID.randomUUID(), f.inputPath)
            .numParts(25).mergeIn(2)
            .build();
        startAndWaitToComplete(workflow, f.expectedResultPath);
    }
    
    @Test(timeout = 60 * 1000L)
    public void test2k2p25() throws Exception
    {
        Fixture f = fixtures.get(1);
        Workflow workflow = workflowBuilderFactory.get(UUID.randomUUID(), f.inputPath)
            .numParts(25).mergeIn(2)
            .build();
        startAndWaitToComplete(workflow, f.expectedResultPath);
    }
    
    @Test(timeout = 60 * 1000L)
    public void test1k2p10() throws Exception
    {
        Fixture f = fixtures.get(0);
        Workflow workflow = workflowBuilderFactory.get(UUID.randomUUID(), f.inputPath)
            .numParts(10).mergeIn(2)
            .build();
        startAndWaitToComplete(workflow, f.expectedResultPath);
    }
    
    @Test(timeout = 60 * 1000L)
    public void test2k2p10() throws Exception
    {
        Fixture f = fixtures.get(1);
        Workflow workflow = workflowBuilderFactory.get(UUID.randomUUID(), f.inputPath)
            .numParts(10).mergeIn(2)
            .build();
        startAndWaitToComplete(workflow, f.expectedResultPath);
    }
    
    @Test(timeout = 60 * 1000L)
    public void test1k10p30() throws Exception
    {
        Fixture f = fixtures.get(0);
        Workflow workflow = workflowBuilderFactory.get(UUID.randomUUID(), f.inputPath)
            .numParts(30).mergeIn(10)
            .build();
        startAndWaitToComplete(workflow, f.expectedResultPath);
    }
    
    @Test(timeout = 60 * 1000L)
    public void test2k10p30() throws Exception
    {
        Fixture f = fixtures.get(1);
        Workflow workflow = workflowBuilderFactory.get(UUID.randomUUID(), f.inputPath)
            .numParts(30).mergeIn(10)
            .build();
        startAndWaitToComplete(workflow, f.expectedResultPath);
    }
      
    private void startAndWaitToComplete(Workflow workflow, Path expectedResultPath) 
        throws Exception
    {
        final UUID workflowId = workflow.id();
        final CountDownLatch done = new CountDownLatch(1);
        
        final Logger logger = LoggerFactory.getLogger(
            WorkflowSchedulerTests.logger.getName() + "$startAndWaitToComplete");
        
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
       
        workflowScheduler.start(workflow, callback);
        done.await();
        
        WorkflowScheduler.ExecutionSnapshot snapshot = workflowScheduler.poll(workflowId);
        WorkflowExecutionSnapshot workflowExecutionSnapshot = snapshot.workflowExecutionSnapshot();
        assertTrue(workflowExecutionSnapshot.isComplete());
        assertEquals(WorkflowScheduler.ExecutionStatus.COMPLETED, snapshot.status());
        
        // Test result
        
        Map<String, Path> outputMap = workflow.output();
        Path resultPath = outputMap.get("r.txt");
        logger.info("The workflow {} is finished (result at {})", workflowId, resultPath);
       
        //System.out.println(workflowExecutionSnapshot.debugGraph());
        
        assertTrue("Expected output result to be a readable file", 
            Files.isRegularFile(resultPath) && Files.isReadable(resultPath));
        
        assertFileEquals(expectedResultPath.toFile(), resultPath.toFile());
        
        // Test node status
        
        WorkflowExecution workflowExecution = new WorkflowExecution(workflow);
        workflowExecution.load(jobRepository);
        assertTrue(workflowExecution.isComplete());
    }
}
