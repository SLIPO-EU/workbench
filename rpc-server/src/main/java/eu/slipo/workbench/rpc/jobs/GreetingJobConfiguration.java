package eu.slipo.workbench.rpc.jobs;

import java.util.Map;

import javax.annotation.PostConstruct;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
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
import org.springframework.batch.core.configuration.JobFactory;
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
import org.springframework.stereotype.Component;

import com.spotify.docker.client.DockerClient;

import eu.slipo.workbench.rpc.jobs.listener.ExecutionContextPromotionListeners;
import eu.slipo.workbench.rpc.jobs.tasklet.docker.CreateContainerTasklet;
import eu.slipo.workbench.rpc.jobs.tasklet.docker.RunContainerTasklet;

@Component
public class GreetingJobConfiguration
{
    private static final String JOB_NAME = "greeting";

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private Path jobDataDirectory;

    private Path dataDir;

    @PostConstruct
    private void setupDataDirectory() throws IOException
    {
        this.dataDir = jobDataDirectory.resolve("greeting");
        try {
            Files.createDirectory(dataDir);
        } catch (FileAlreadyExistsException e) {}
    }

    private static class Step1Tasklet implements Tasklet
    {
        private static Logger logger = LoggerFactory.getLogger(Step1Tasklet.class);

        @Override
        public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
        {
            StepContext stepContext = chunkContext.getStepContext();
            ExecutionContext executionContext = stepContext.getStepExecution().getExecutionContext();

            logger.info("Executing with parameters={} step-exec-context={} exec-context={}",
                stepContext.getJobParameters(), executionContext, stepContext.getJobExecutionContext());

            int chunkIndex = executionContext.getInt("step1.chunk-index", 0);

            try { Thread.sleep(3500); } // simulate some processing
            catch (InterruptedException ex) {
                logger.info("Interrupted while sleeping!");
            }
            chunkIndex++; // pretend that some progress is done

            executionContext.putInt("step1.chunk-index", chunkIndex);
            executionContext.put("step1.key1", "val11");
            executionContext.put("step1.key2", "val12");
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
            ExecutionContext executionContext = stepContext.getStepExecution().getExecutionContext();

            logger.info("Executing with parameters={} step-exec-context={} exec-context={}",
                stepContext.getJobParameters(), executionContext, stepContext.getJobExecutionContext());

            Thread.sleep(15000L);
            executionContext.put("step2.key1", "val21");
            executionContext.put("step2.key2", "val22");
            return RepeatStatus.FINISHED;
        }
    }

    @Bean("greeting.createEchoContainerTasklet")
    @JobScope
    public CreateContainerTasklet createEchoContainerTasklet(
        DockerClient dockerClient,
        @Value("#{jobExecution.id}") Long executionId,
        @Value("#{jobExecution.jobInstance.id}") Long jobId)
    {
        final String containerName = String.format("workbench-echo-%d", jobId);

        return CreateContainerTasklet.builder()
            .client(dockerClient)
            .name(containerName)
            .container(configurer -> configurer
                .image("debian:8.9")
                .volume(dataDir.resolve("echo/output"), Paths.get("/var/local/hello-spring/output"))
                .volume(dataDir.resolve("echo/input"), Paths.get("/var/local/hello-spring/input"), true)
                .env("Foo", "Baz")
                .command("bash", "-c",
                    "echo Started with Foo=${Foo} at $(date);" +
                    "sleep 2; echo Read input file: $(cat /var/local/hello-spring/input/greeting.txt);" +
                    "sleep 2; echo Done at $(date)"))
            .build();
    }

    @Bean("greeting.runEchoContainerTasklet")
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

    @Bean("greeting.createEchoContainerStep")
    public Step createEchoContainerStep(
        @Qualifier("greeting.createEchoContainerTasklet") CreateContainerTasklet tasklet)
        throws Exception
    {
        StepExecutionListener stepContextListener = ExecutionContextPromotionListeners.builder()
            .keys("containerId", "containerName")
            .prefix("echo")
            .build();

        return stepBuilderFactory.get("createEchoContainer")
            .tasklet(tasklet)
            .listener(tasklet)
            .listener(stepContextListener)
            .build();
    }

    @Bean("greeting.runEchoContainerStep")
    public Step runEchoContainerStep(
        @Qualifier("greeting.runEchoContainerTasklet") RunContainerTasklet tasklet)
        throws Exception
    {
        return stepBuilderFactory.get("runEchoContainer")
            .tasklet(tasklet)
            .listener(tasklet)
            .build();
    }

    @Bean("greeting.step1")
    private Step step1()
    {
        return stepBuilderFactory.get("step1")
            .tasklet(new Step1Tasklet())
            .listener(ExecutionContextPromotionListeners.fromKeys("step1.key1"))
            .build();
    }

    @Bean("greeting.step2")
    private Step step2()
    {
        return stepBuilderFactory.get("step2")
            .tasklet(new Step2Tasklet())
            .listener(ExecutionContextPromotionListeners.fromKeys("step2.key1"))
            .build();
    }

    @Bean("greeting.job")
    public Job job(
        @Qualifier("greeting.step1") Step step1,
        @Qualifier("greeting.step2") Step step2,
        @Qualifier("greeting.createEchoContainerStep") Step createEchoContainerStep,
        @Qualifier("greeting.runEchoContainerStep") Step runEchoContainerStep)
    {
        JobExecutionListener listener = new JobExecutionListenerSupport() {
            @Override
            public void afterJob(JobExecution execution)
            {
                System.err.printf(" ** After job #%d: status=%s exit-status=%s%n",
                    execution.getJobInstance().getId(),
                    execution.getStatus(), execution.getExitStatus());
            }
        };

        JobParametersValidator parametersValidator = new JobParametersValidator() {
            @Override
            public void validate(JobParameters parameters) throws JobParametersInvalidException
            {
                if (parameters.getLong("foo", 0L) < 199L)
                    throw new JobParametersInvalidException("Expected foo >= 199");
            }
        };

        return jobBuilderFactory.get(JOB_NAME)
            .incrementer(new RunIdIncrementer())
            .validator(parametersValidator)
            .listener(listener)
            .start(step1)
            .next(step2)
            .next(createEchoContainerStep)
            .next(runEchoContainerStep)
            .build();
    }

    @Bean("greeting.jobFactory")
    public JobFactory jobFactory(@Qualifier("greeting.job") Job job)
    {
        return new JobFactory()
        {
            @Override
            public String getJobName()
            {
                return "greeting";
            }

            @Override
            public Job createJob()
            {
                return job;
            }
        };
    }
}
