package eu.slipo.workbench.rpc.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@EnableScheduling
public class TaskExecutorConfiguration
{
    @Value("${slipo.rpc-server.task-executor.pool-size:4}")
    Integer corePoolSize;
    
    @Value("${slipo.rpc-server.task-executor.max-pool-size:8}")
    Integer maxPoolSize;
    
    @Value("${slipo.rpc-server.task-executor.await-termination-timeout:10}")
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
    
    @Bean
    TaskScheduler taskScheduler() 
    {        
        ThreadPoolTaskScheduler p = new ThreadPoolTaskScheduler();
        
        p.setPoolSize(corePoolSize);
        p.setThreadNamePrefix("cron-");
        
        return p;
    }
}
