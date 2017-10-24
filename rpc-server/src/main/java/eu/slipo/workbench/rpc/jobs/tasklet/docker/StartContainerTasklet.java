package eu.slipo.workbench.rpc.jobs.tasklet.docker;

import java.util.Date;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.util.Assert;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerConfig.Healthcheck;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.ContainerState;
import com.spotify.docker.client.messages.ContainerState.Health;

/**
 * A tasklet that starts a docker container (but does not wait for it).
 * The execution of this tasklet ends as soon as docker daemon reports the container
 * as started.
 */
public class StartContainerTasklet implements Tasklet, StepExecutionListener
{
    private static Logger logger = LoggerFactory.getLogger(StartContainerTasklet.class); 
    
    /**
     * The default behavior regarding to health check of a starting container.
     */
    public static boolean CHECK_HEALTH = true;
    
    /**
     * A builder for tasklets of enclosing class ({@link StartContainerTasklet}).
     */
    public static class Builder
    {
        private DockerClient client;
        
        private String containerName;
        
        private String containerId;
                
        private Boolean checkHealth;
        
        private Builder() {}
        
        /**
         * Provide a client to communicate with docker daemon.
         */
        public Builder client(DockerClient client)
        {
            Assert.notNull(client, "A non-null docker client is expected");
            this.client = client;
            return this;
        }
        
        /**
         * Set the container ID (or name) to identify the target container.
         */
        public Builder container(String containerId)
        {
            Assert.notNull(containerId, "A non-null container ID is required");
            this.containerId = containerId;
            return this;
        }
        
        /**
         * Set whether we should check the health status of a container before we consider it 
         * as started (and the task completes successfully).
         * <p>
         * This means that (if, of course, a health check is defined) this task will wait for the 
         * container to transition for STARTING to HEALTHY. If, after the starting period, the 
         * container consecutively fails the health check, it will transition from STARTING to 
         * UNHEALTHY and the task will be considered as failed.   
         */
        public Builder checkHealth(boolean flag)
        {   
            this.checkHealth = flag;
            return this;
        }
        
        /**
         * Build a tasklet from configuration.
         */
        public StartContainerTasklet build()
        {
            Assert.state(client != null, 
                "A docker client is required to communicate to docker daemon!");
            Assert.state(containerId != null, 
                "A target container must be specified by name or ID");
            
            StartContainerTasklet tasklet = new StartContainerTasklet(client, containerId);
            
            if (checkHealth != null)
                tasklet.setCheckHealth(checkHealth);
            
            return tasklet;
        }
    }
    
    public static Builder builder()
    {
        return new Builder();
    }
    
    /**
     * The collection of keys used inside our execution context
     */
    public static class Keys
    {        
        public static final String STARTED = "started";
        
        public static final String HEALTH_STATUS = "healthStatus";
    }
        
    private final DockerClient docker;
    
    private final String containerId;
    
    private boolean checkHealth = CHECK_HEALTH;
    
   
    private StartContainerTasklet(DockerClient docker, String containerId) 
    {
        Assert.notNull(docker, "The docker client must be provided");
        Assert.notNull(containerId, "A target container must be specified");
        this.docker = docker;
        this.containerId = containerId;
    }
    
    public void setCheckHealth(boolean flag)
    {
        this.checkHealth = flag;
    }
    
    /**
     * Determine the check interval (in milliseconds), based on container's configuration.
     */
    private long determineCheckInterval(ContainerConfig containerConfig)
    {
        Healthcheck h = containerConfig.healthcheck();
        Assert.state(h != null, 
            "Expected only health-checking tasklets to poll the container status");
        
        // Convert to milliseconds, as docker API (>=1.29) uses nanoseconds for these durations
        final long NANOS_PER_MILLISECOND = 1000L * 1000L; 
        return h.interval() / NANOS_PER_MILLISECOND;
    }
    
    /**
     * Resolve the exit-status ({@link ExitStatus}) of this step by examining the health status.
     */
    private ExitStatus resolveExitStatus(String healthStatus)
    {
        ExitStatus exitStatus = null;
        
        if (healthStatus.equalsIgnoreCase("healthy")) {
            exitStatus = ExitStatus.COMPLETED;
        } else if (healthStatus.equalsIgnoreCase("unhealthy")) {
            final String exitCode = "FAILED-WITH-UNHEALTHY-STATUS";
            final String exitDescription = "The container has reported an unhealthy status";
            exitStatus = new ExitStatus(exitCode, exitDescription);
        } else {
            Assert.state(false, "Received an unknown health status (" + healthStatus + ")");
        }
        
        return exitStatus;
    }
    
    /**
     * Create (if needed) and start a container.
     * <p>
     * This task is clearly separated into 2 phases:
     * <ol>
     *   <li>P1: Start the container.</li>
     *   <li>P2: Poll status of the container until a health status is known (i.e.
     *     either healthy or unhealthy). Note that an execution will reach through this phase
     *     (P2) only if health checking is enabled and the container has been configured with
     *     a healthcheck command.
     *   </li>
     * </ol>
     */
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
        throws Exception
    {
        StepContext stepContext = chunkContext.getStepContext();
        StepExecution stepExecution = stepContext.getStepExecution();
        ExecutionContext executionContext = stepExecution.getExecutionContext();
        
        long started = executionContext.getLong("started", -1L);
        boolean done = false;
        
        if (started < 0) {
            // P1. Start the container
            docker.startContainer(containerId);
            logger.info("Started container {}", containerId);
            started = (new Date()).getTime();
            executionContext.putLong(Keys.STARTED, started);
            // If not health checking, consider this task as done
            done = !checkHealth;
        } else {
            // P2. Poll container status until a definite health status is known.
            Assert.state(checkHealth, "Expected to be health checking!");
            // Note: We rely on the limited number of retries a healthcheck will perform, 
            // and we do not apply additional timeouts (or maximum number of checks) here.
            ContainerInfo containerInfo = docker.inspectContainer(containerId);
            ContainerState containerState = containerInfo.state();
            logger.debug("Polled status for container {}: {}", containerId, containerState);
            Health health = containerState.health();
            if (health != null) {
                // The container performs health checking and has reported its status
                if (health.status().equalsIgnoreCase("starting")) {
                    // The health status is not decided yet: schedule our next check
                    long checkInterval = determineCheckInterval(containerInfo.config());
                    logger.debug("The container {} is still starting: sleeping for {}ms",
                        containerId, checkInterval);
                    executionContext.putString(Keys.HEALTH_STATUS, "starting");
                    Thread.sleep(checkInterval);
                } else {
                    // A health status is known: determine exit-status for tasklet
                    logger.info("The container {} is started ({})", containerId, health.status());
                    executionContext.putString(Keys.HEALTH_STATUS, health.status());
                    ExitStatus exitStatus = resolveExitStatus(health.status());
                    contribution.setExitStatus(exitStatus);
                    stepExecution.setStatus(exitStatus.getExitCode().startsWith("FAILED")?
                        BatchStatus.FAILED : BatchStatus.COMPLETED);
                    done = true;
                }
            } else {
                // The container doesn't perform any health checking
                done = true;
            }
        }
       
        return RepeatStatus.continueIf(!done);
    }

    @Override
    public void beforeStep(StepExecution stepExecution)
    {
        ExecutionContext executionContext = stepExecution.getExecutionContext();
        
        // If this step execution is a restart of a previous failed execution, then we
        // should clear part of our (inherited) context before the actual execution begins
        
        String healthStatus = executionContext.getString(Keys.HEALTH_STATUS, null);
        if (healthStatus != null && healthStatus.equalsIgnoreCase("unhealthy")) {
            // The previous step execution (from which we recover) has failed with
            // an unhealthy status: reset execution context to allow a clean restart.
            // Note that any garbage left behind (e.g. a stuck container), must be
            // cleaned manually.
            resetExecutionContext(stepExecution);
        }
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution)
    {
        return null;
    }
    
    private void resetExecutionContext(StepExecution stepExecution)
    {
        ExecutionContext executionContext = stepExecution.getExecutionContext();
        
        executionContext.remove(Keys.STARTED);
        executionContext.remove(Keys.HEALTH_STATUS);
    }
}
