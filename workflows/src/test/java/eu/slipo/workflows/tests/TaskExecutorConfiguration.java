package eu.slipo.workflows.tests;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class TaskExecutorConfiguration
{
    @Value("${slipo.workflows.task-executor.pool-size:4}")
    Integer corePoolSize;
    
    @Value("${slipo.workflows.task-executor.max-pool-size:8}")
    Integer maxPoolSize;
    
    @Value("${slipo.workflows.task-executor.await-termination-timeout:10}")
    Integer awaitTerminationTimeout;
    
    @Bean
    TaskExecutor taskExecutor() 
    {                
        ThreadPoolTaskExecutor p = new ThreadPoolTaskExecutor();
        p.setCorePoolSize(corePoolSize);
        p.setMaxPoolSize(maxPoolSize);
        p.setThreadNamePrefix("tasks-");
        p.setWaitForTasksToCompleteOnShutdown(true);
        p.setAwaitTerminationSeconds(awaitTerminationTimeout);
        return p;
    }
}
