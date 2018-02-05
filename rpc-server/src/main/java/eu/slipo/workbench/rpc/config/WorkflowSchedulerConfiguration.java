package eu.slipo.workbench.rpc.config;

import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.support.CronTrigger;

import eu.slipo.workflows.service.EventBasedWorkflowScheduler;
import eu.slipo.workflows.service.WorkflowScheduler;

@Configuration
public class WorkflowSchedulerConfiguration
{
    @Autowired
    private JobRepository jobRepository;
    
    @Autowired
    @Qualifier("asyncJobLauncher")
    private JobLauncher jobLauncher;
    
    @Autowired
    private JobOperator jobOperator;
    
    @Autowired
    private StepBuilderFactory stepBuilderFactory; 
    
    @Autowired
    private JobBuilderFactory jobBuilderFactory;
 
    @Autowired
    private TaskScheduler taskScheduler;
    
    private Trigger cleanupTrigger;
    
    private Long maxDurationAfterUpdate;
    
    @Autowired
    private void setCleanupTrigger(
        @Value("${slipo.rpc-server.workflows.workflow-scheduler.cleanup.cron}") String cronExpression)
    {
        this.cleanupTrigger = new CronTrigger(cronExpression);
    }
    
    @Autowired
    private void setMaxDurationAfterUpdate(
        @Value("${slipo.rpc-server.workflows.workflow-scheduler.cleanup.expire-after-update}") String duration)
    {
        this.maxDurationAfterUpdate = Long.valueOf(duration);
    }
    
    @Bean
    WorkflowScheduler workflowScheduler()
    {
        EventBasedWorkflowScheduler workflowScheduler = new EventBasedWorkflowScheduler();
        
        // Setup workflow scheduler
        
        workflowScheduler.setJobRepository(jobRepository);
        workflowScheduler.setJobLauncher(jobLauncher);
        workflowScheduler.setJobOperator(jobOperator);
        workflowScheduler.setJobBuilderFactory(jobBuilderFactory);
        workflowScheduler.setStepBuilderFactory(stepBuilderFactory);
        workflowScheduler.setMaxDurationAfterUpdate(maxDurationAfterUpdate);
        
        // Schedule periodic cleanup
        
        taskScheduler.schedule(workflowScheduler::cleanup, cleanupTrigger);
        
        // Done
        
        return workflowScheduler;
    }
}