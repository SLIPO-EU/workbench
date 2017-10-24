package eu.slipo.workbench.rpc.jobs;

import java.util.Map;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.JobParametersValidator;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.StepListener;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.listener.ExecutionContextPromotionListener;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import com.spotify.docker.client.DockerClient;

import eu.slipo.workbench.rpc.jobs.listener.ExecutionContextPromotionListeners;
import eu.slipo.workbench.rpc.jobs.tasklet.docker.CreateContainerTasklet;
import eu.slipo.workbench.rpc.jobs.tasklet.docker.RunContainerTasklet;


public class Job1Config
{
    private static final String JOB_NAME = "job1";
    
    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;
    
    private static class Step1Tasklet implements Tasklet
    {
        private static Logger logger = LoggerFactory.getLogger(Step1Tasklet.class);
        
        @Override
        public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
        {
            StepContext stepContext = chunkContext.getStepContext();
            Map<String, Object> parameters = stepContext.getJobParameters();
            StepExecution stepExecution = stepContext.getStepExecution();
            ExecutionContext stepExecutionContext = stepExecution.getExecutionContext();
            
            logger.info("Executing with parameters={} step-exec-context={} exec-context={}",
                parameters, stepContext.getStepExecutionContext(), stepContext.getJobExecutionContext());
            
            // Retrieve something from step-level execution context
            int chunkIndex = stepExecutionContext.getInt("step1.chunk-index", 0);            
            
            try { Thread.sleep(3500); } // simulate some processing
            catch (InterruptedException ex) {
                logger.info("Interrupted while sleeping!");
            }
            
            chunkIndex++; // pretend that some progress is done
            
            // Note: Can only write to step-level execution context
            stepExecutionContext.putInt("step1.chunk-index", chunkIndex);
            stepExecutionContext.put("step1.key1", "val11");
            stepExecutionContext.put("step1.key2", "val12");
           
            // A chunk is processed; cycle through same step
            logger.info("Done with chunk #{}", chunkIndex);
            return RepeatStatus.continueIf(chunkIndex < 12);
        }
    }
    
    private static class Step2Tasklet implements Tasklet
    {
        private static Logger logger = LoggerFactory.getLogger(Step2Tasklet.class);
        
        @Override
        public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
            throws InterruptedException
        {
            StepContext stepContext = chunkContext.getStepContext();
            Map<String, Object> parameters = stepContext.getJobParameters();
            StepExecution stepExecution = stepContext.getStepExecution();
            ExecutionContext stepExecutionContext = stepExecution.getExecutionContext();
            
            logger.info("Executing with parameters={} step-exec-context={} exec-context={}",
                parameters, stepContext.getStepExecutionContext(), stepContext.getJobExecutionContext());
            
            Thread.sleep(2000);
            
            stepExecutionContext.put("step2.key1", "val21");    
            stepExecutionContext.put("step2.key2", "val22");
    
            return RepeatStatus.FINISHED;
        }
    }
    
    @Bean
    @JobScope
    public CreateContainerTasklet createEchoContainerTasklet(
        DockerClient dockerClient,
        @Value("${slipo.rpc-server.docker.volumes.data-dir}") String dataDir,
        @Value("#{jobExecution.id}") Long executionId,
        @Value("#{jobExecution.jobInstance.id}") Long jobId)
    {
        final String containerName = String.format("workbench-echo-%d", jobId);
        
        return CreateContainerTasklet.builder()
            .client(dockerClient)
            .name(containerName)
            .container(configurer -> configurer
                .image("debian:8.9")
                .volume(Paths.get(dataDir, "echo/output"), Paths.get("/var/local/hello-spring/output"))
                .volume(Paths.get(dataDir, "echo/input"), Paths.get("/var/local/hello-spring/input"), true)
                .env("Foo", "Baz")
                .command("bash", "-c",
                    "echo Started with Foo=${Foo} at $(date);" +
                    "sleep 2; echo Read input file: $(cat /var/local/hello-spring/input/greeting.txt);" +
                    "sleep 2; echo Done at $(date)"))
            .build();
    }
    
    @Bean
    @JobScope
    public RunContainerTasklet runEchoContainerTasklet(
        DockerClient dockerClient,
        @Value("#{jobExecutionContext['echo.containerId']}") String containerId)
    {
        RunContainerTasklet tasklet = RunContainerTasklet.builder()
            .client(dockerClient)
            .checkInterval(1000L)
            .timeout(7000L)
            .container(containerId)
            .removeOnFinished(false)
            .build();
        return tasklet;
    }
    
    @Bean
    public Step createEchoContainerStep(
        @Qualifier("createEchoContainerTasklet") CreateContainerTasklet tasklet) 
        throws Exception
    {
        StepExecutionListener stepContextListener = ExecutionContextPromotionListeners
            .fromKeys("containerId", "containerName").prefix("echo")
            .build();
        
        return stepBuilderFactory.get("createEchoContainer")
            .tasklet(tasklet)
            .listener(tasklet)
            .listener(stepContextListener)
            .build();   
    }
    
    @Bean
    public Step runEchoContainerStep(
        @Qualifier("runEchoContainerTasklet") RunContainerTasklet tasklet) 
        throws Exception
    {       
        return stepBuilderFactory.get("runEchoContainer")
            .tasklet(tasklet)
            .listener(tasklet)
            .build();
    }
    
    private static class Validator implements JobParametersValidator
    {
        @Override
        public void validate(JobParameters parameters) throws JobParametersInvalidException
        {
            // Validate, raise exception on invalid parameters
            
            Long foo = parameters.getLong("foo");
            if (foo == null || foo < 199L)
                throw new JobParametersInvalidException("Expected foo >= 199"); 
        }
    }
    
    private static class ExecutionListener extends JobExecutionListenerSupport
    {
        private static Logger logger = LoggerFactory.getLogger(ExecutionListener.class); 

        @Override
        public void afterJob(JobExecution execution)
        {
            logger.info("After job #{}: status={} exit-status={}", 
                execution.getJobInstance().getId(), execution.getStatus(), execution.getExitStatus());
        }  
    }
    
    @Bean
    private Step step1()
    {
        // A listener to promote (part of) step-level context to execution-level context
        ExecutionContextPromotionListener listener = new ExecutionContextPromotionListener();
        listener.setKeys(new String[] {"step1.key1"});
        listener.setStrict(true);
        try {
            listener.afterPropertiesSet();
        } catch (Exception e) {
            listener = null;
        }
        
        // Build step
        return stepBuilderFactory.get("step1")
            .tasklet(new Step1Tasklet())
            .listener(listener)
            .build();
    }

    @Bean
    private Step step2()
    {
        // A listener to promote (part of) step-level context to execution-level context
        ExecutionContextPromotionListener listener = new ExecutionContextPromotionListener();
        listener.setKeys(new String[] {"step2.key1"});
        listener.setStrict(true);
        try {
            listener.afterPropertiesSet();
        } catch (Exception e) {
            listener = null;
        }
        
        return stepBuilderFactory.get("step2")
            .tasklet(new Step2Tasklet())
            .listener(listener)
            .build();
    }

    @Bean
    public Job job1(
        Step step1, Step step2, Step createEchoContainerStep, Step runEchoContainerStep)
    {
        return jobBuilderFactory.get(JOB_NAME)
            .incrementer(new RunIdIncrementer())
            .validator(new Validator())
            .listener(new ExecutionListener())
            .start(step1)
            //.next(step2)
            .next(createEchoContainerStep)
            .next(runEchoContainerStep)
            .build();
    }
}