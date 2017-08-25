package eu.slipo.workbench.rpc.jobs;

import java.util.Map;

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
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.configuration.JobFactory;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.listener.ExecutionContextPromotionListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * An example JobFactory implementation.
 */
@Component("job1Factory")
public class Job1Factory implements JobFactory
{
    private static Logger logger = LoggerFactory.getLogger(Job1Factory.class); 
    
    private static final String JOB_NAME = "job1";
    
    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;
    
    private static class Step1 implements Tasklet
    {
        private static Logger logger = LoggerFactory.getLogger(Step1.class);
        
        @Override
        public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
        {
            StepContext stepContext = chunkContext.getStepContext();
            Map<String, Object> parameters = stepContext.getJobParameters();
            StepExecution stepExecution = stepContext.getStepExecution();
            ExecutionContext stepExecutionContext = stepExecution.getExecutionContext();
            
            logger.info(
                "Executing with parameters={} step-exec-context={} exec-context={}",
                parameters, stepContext.getStepExecutionContext(), stepContext.getJobExecutionContext());
            
            // Retrieve something from step-level execution context
            int chunkIndex = stepExecutionContext.getInt("step1.chunk-index", 0);
            
            try { Thread.sleep(2000); } // simulate some processing
            catch (InterruptedException ex) {
                logger.info("Interrupted while sleeping!");
            }
            
            chunkIndex++; // pretend that some progress is done
            logger.info("Done with chunk #{}", chunkIndex);
            
            // Note: Can only write to step-level execution context
            stepExecutionContext.putInt("step1.chunk-index", chunkIndex);
            stepExecutionContext.put("step1.key1", "val11");
            stepExecutionContext.put("step1.key2", "val12");
           
            // A chunk is processed; cycle through same step
            return RepeatStatus.continueIf(chunkIndex < 12);
        }
    }
    
    private static class Step2 implements Tasklet
    {
        private static Logger logger = LoggerFactory.getLogger(Step2.class);
        
        @Override
        public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
            throws InterruptedException
        {
            StepContext stepContext = chunkContext.getStepContext();
            Map<String, Object> parameters = stepContext.getJobParameters();
            StepExecution stepExecution = stepContext.getStepExecution();
            ExecutionContext stepExecutionContext = stepExecution.getExecutionContext();
            
            logger.info(
                "Executing with parameters={} step-exec-context={} exec-context={}",
                parameters, stepContext.getStepExecutionContext(), stepContext.getJobExecutionContext());
            
            Thread.sleep(3000);
            
            stepExecutionContext.put("step2.key1", "val21");    
            stepExecutionContext.put("step2.key2", "val22");
    
            return RepeatStatus.FINISHED;
        }
    }
    
    private static class Validator implements JobParametersValidator
    {
        @Override
        public void validate(JobParameters parameters) throws JobParametersInvalidException
        {
            // Todo validate, raise exception on invalid parameters            
        }
    }
    
    private static class ExecutionListener implements JobExecutionListener
    {
        @Override
        public void beforeJob(JobExecution execution)
        {
            // no-op  
        }

        @Override
        public void afterJob(JobExecution execution)
        {
            JobInstance instance = execution.getJobInstance();
            logger.info("After #{}: status={} exit-status={}", 
                instance.getInstanceId(), 
                execution.getStatus(), execution.getExitStatus());
        }  
    }
    
    private Step step1()
    {
        // A listener to promote (part of) step-level context to execution-level context
        ExecutionContextPromotionListener stepContextListener = 
            new ExecutionContextPromotionListener();
        stepContextListener.setKeys(new String[] {"step1.key1"});
        stepContextListener.setStrict(true);
        try {
            stepContextListener.afterPropertiesSet();
        } catch (Exception e) {
            stepContextListener = null;
        }
        
        // Build step
        return stepBuilderFactory.get("step1")
            .tasklet(new Step1())
            .listener(stepContextListener)
            .build();
    }

    private Step step2()
    {
        // A listener to promote (part of) step-level context to execution-level context
        ExecutionContextPromotionListener stepContextListener = 
            new ExecutionContextPromotionListener();
        stepContextListener.setKeys(new String[] {"step2.key1"});
        stepContextListener.setStrict(true);
        try {
            stepContextListener.afterPropertiesSet();
        } catch (Exception e) {
            stepContextListener = null;
        }
        
        return stepBuilderFactory.get("step2")
            .tasklet(new Step2())
            .listener(stepContextListener)
            .build();
    }

    @Override
    public Job createJob()
    {
        // Build job
        return jobBuilderFactory.get(JOB_NAME)
            .incrementer(new RunIdIncrementer())
            .validator(new Validator())
            .start(step1())
            .next(step2())
            .listener(new ExecutionListener())
            .build();
    }

    @Override
    public String getJobName()
    {
        return JOB_NAME;
    }
    
    @Bean
    public Job job1()
    {
        return createJob();
    }
}