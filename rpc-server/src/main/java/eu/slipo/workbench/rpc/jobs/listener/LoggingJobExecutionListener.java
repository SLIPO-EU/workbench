package eu.slipo.workbench.rpc.jobs.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;

public class LoggingJobExecutionListener extends JobExecutionListenerSupport
{
    private static Logger logger = LoggerFactory.getLogger(LoggingJobExecutionListener.class); 

    @Override
    public void beforeJob(JobExecution jobExecution)
    {
        JobInstance instance = jobExecution.getJobInstance();
        logger.info("Before job {}#{}: execution-id={} status={} parameters={}", 
            instance.getJobName(), 
            instance.getId(), 
            jobExecution.getId(),
            jobExecution.getStatus(),
            jobExecution.getJobParameters());
    }
    
    @Override
    public void afterJob(JobExecution jobExecution)
    {
        JobInstance instance = jobExecution.getJobInstance();
        logger.info("After job {}#{}: execution-id={} status={} exit-status={}", 
            instance.getJobName(), 
            instance.getId(),
            jobExecution.getId(),
            jobExecution.getStatus(),
            jobExecution.getExitStatus());
    }  
}
