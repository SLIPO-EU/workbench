package eu.slipo.workbench.rpc.jobs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * A step that intentionally fails to force a stop on a job execution.
 * <p>
 * The basic use of this step is to simulate a stopped/failed step that will 
 * be restarted (in some future time).  
 */
@Component
public class BreakpointStepConfiguration
{
    private static Logger logger = LoggerFactory.getLogger(BreakpointStepConfiguration.class);
    
    @Autowired
    private StepBuilderFactory stepBuilderFactory;
    
    @Bean
    public Step breakpointStep()
    {
        Tasklet tasklet = new Tasklet()
        {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext context)
                throws Exception
            {
                StepExecution stepExecution = context.getStepContext().getStepExecution();
                ExecutionContext executionContext = stepExecution.getExecutionContext();
                
                logger.info(
                    "Stopped on a breakpoint:\n" +
                        " * job-execution-context={}\n" +
                        " * execution-context={}\n",
                    stepExecution.getJobExecution().getExecutionContext(),
                    stepExecution.getExecutionContext());
                
                throw new RuntimeException("This is a breakpoint");
            }
        };
        
        return stepBuilderFactory.get("breakpoint")
            .tasklet(tasklet)
            .build();
    }
}
