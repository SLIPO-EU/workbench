package eu.slipo.workflows.tests;

import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskExecutor;

@Configuration
@EnableBatchProcessing
@DependsOn("dataSource")
public class BatchConfiguration 
{
    @Autowired
    JobExplorer explorer;
    
    @Autowired
    JobRepository repository;
    
    @Autowired
    JobRegistry registry;
    
    @Bean({ "jobLauncher", "defaultJobLauncher", "asyncJobLauncher"})
    @Primary
    JobLauncher jobLauncher(TaskExecutor taskExecutor)
    {
        // Setup with our task executor (default is SyncTaskExecutor)
        SimpleJobLauncher launcher = new SimpleJobLauncher();
        launcher.setJobRepository(repository);
        launcher.setTaskExecutor(taskExecutor);
        return launcher;
    }
    
    @Bean({"syncJobLauncher"})
    JobLauncher syncJobLauncher()
    {
        // Setup with SyncTaskExecutor
        SimpleJobLauncher launcher = new SimpleJobLauncher();
        launcher.setJobRepository(repository);
        launcher.setTaskExecutor(null);
        return launcher;
    }
    
    @Bean 
    JobOperator jobOperator(JobLauncher launcher)
    {
        // Setup operator to use our asynchronous launcher
        SimpleJobOperator operator = new SimpleJobOperator();
        operator.setJobExplorer(explorer);
        operator.setJobLauncher(launcher);
        operator.setJobRegistry(registry);
        operator.setJobRepository(repository);
        return operator;
    }
 }