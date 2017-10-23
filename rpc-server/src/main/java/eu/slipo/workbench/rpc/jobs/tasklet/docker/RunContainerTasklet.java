package eu.slipo.workbench.rpc.jobs.tasklet.docker;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
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
import com.spotify.docker.client.DockerClient.LogsParam;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.ContainerState;

import eu.slipo.workbench.rpc.jobs.tasklet.TimedOutExitStatus;

/**
 * A tasklet that starts a docker container waiting for it to complete.
 */
public class RunContainerTasklet implements Tasklet, StepExecutionListener
{
    private static Logger logger = LoggerFactory.getLogger(RunContainerTasklet.class); 
    
    /** 
     * The default amount of time (in milliseconds) to wait for a stopping container before
     * finally sending a <tt>SIGKILL</tt> signal. 
     */
    public static final long DEFAULT_STOP_TIMEOUT = 5000L;
    
    /**
     * The minimum accepted interval (in milliseconds) for polling container status.
     */
    public static final long MIN_CHECK_INTERVAL = 250L;
    
    /**
     * The default interval (in milliseconds) for polling container status.
     */
    public static final long DEFAULT_CHECK_INTERVAL = 1000L;
    
    /**
     * The default timeout (in milliseconds) waiting for a container to complete.
     */
    public static final long DEFAULT_TIMEOUT = -1L; // no timeout
    
    /**
     * The default behavior on whether to consider a timed-out command as a failed step.
     */
    public static final boolean FAIL_ON_TIMEOUT = true;
    
    /**
     * The default behavior on whether to consider a non-zero exit-code as a failed step.
     */
    public static final boolean FAIL_ON_NON_ZERO_EXIT_CODE = true;
    
    /**
     * The default behavior on whether to remove (i.e. destroy) a finished container.
     */
    public static final boolean REMOVE_ON_FINISHED = false;
    
    /**
     * An invalid exit-code (assuming an exit-code is a unsigned 8-bit integer)
     */
    private static final long NOT_AN_EXIT_CODE = Long.MAX_VALUE;
    
    /**
     * The collection of keys used inside our execution context
     */
    public static class Keys
    {
        public static final String STARTED = "started";
        
        public static final String FINISHED = "finished";
        
        public static final String TIMED_OUT = "timedOut";
        
        public static final String COMMAND_EXIT_CODE = "command.exitCode";
        
        public static final String COMMAND_OUTPUT = "command.output";
    }
    
    /**
     * A builder for tasklets of enclosing class ({@link RunContainerTasklet}).
     */
    public static class Builder
    {
        private DockerClient client;
        
        private String containerId;
        
        private Long timeout;
        
        private Long stopTimeout;
        
        private Long checkInterval;
        
        private Boolean failOnTimeout;
        
        private Boolean failOnNonZeroExitCode;
        
        private Boolean removeOnFinished;
        
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
            Assert.notNull(containerId, "Expected a non-null container ID");
            Assert.isTrue(containerId.length() > 0, "Expected a non empty container ID");
            this.containerId = containerId;
            return this;
        }
        
        /**
         * Set an overall timeout (milliseconds) waiting for a container to complete 
         * (a negative value means no timeout).
         */
        public Builder timeout(long millis)
        {
            this.timeout = millis;
            return this;
        }
        
        /**
         * Set a timeout (in milliseconds) for waiting for a container to stop, after a stop 
         * signal is sent.
         */
        public Builder stopTimeout(long t)
        {
            this.stopTimeout = t;
            return this;
        }
        
        /**
         * Set the polling interval (milliseconds). The minimum allowed value for this
         * is {@link RunContainerTasklet#MIN_CHECK_INTERVAL}.
         */
        public Builder checkInterval(long t)
        {
            this.checkInterval = t < MIN_CHECK_INTERVAL? MIN_CHECK_INTERVAL : t;
            return this;
        }
        
        /**
         * Set whether a timeout on the container's command should bring a step to
         * a <tt>FAILED</tt> batch status (default is <tt>true</tt>).
         * <p>
         * Of course, this setting is meaningless if a timeout is not specified.
         */
        public Builder failOnTimeout(boolean flag)
        {
            failOnTimeout = flag;
            return this;
        }
        
        /**
         * Set whether a non-zero exit-code (reported from the container's command) should
         * bring a step to a <tt>FAILED</tt> batch status (default is <tt>true</tt>). 
         */
        public Builder failOnNonZeroExitCode(boolean flag)
        {
            failOnNonZeroExitCode = flag;
            return this;
        }
        
        /**
         * Set whether a finished container should be removed (default is <tt>true</tt>).
         * <p>The basic reason for not removing a container is to assist debugging by 
         * inspecting the entire state (apart from stdout/stderr which is saved anyway)
         * of the container.     
         */
        public Builder removeOnFinished(boolean flag)
        {
            removeOnFinished = flag;
            return this;
        }
        
        /**
         * Build a tasklet from configuration.
         */
        public RunContainerTasklet build()
        {
            Assert.state(client != null, 
                "A docker client is needed to communicate to docker daemon!");
            
            RunContainerTasklet tasklet = new RunContainerTasklet(client, containerId);
            
            if (checkInterval != null)
                tasklet.setCheckInterval(checkInterval);
            if (timeout != null)
                tasklet.setTimeout(timeout);
            if (stopTimeout != null)
                tasklet.setStopTimeout(stopTimeout);
            
            if (failOnNonZeroExitCode != null)
                tasklet.setFailOnNonZeroExitCode(failOnNonZeroExitCode);
            if (failOnTimeout != null)
                tasklet.setFailOnTimeout(failOnTimeout);
            
            if (removeOnFinished != null)
                tasklet.setRemoveOnFinished(removeOnFinished);
            
            return tasklet;
        }
    }
    
    public static Builder builder()
    {
        return new Builder();
    }
    
    private final DockerClient docker;
    
    private final String containerId;

    private long checkInterval = DEFAULT_CHECK_INTERVAL;

    private long timeout = DEFAULT_TIMEOUT;
    
    private long stopTimeout = DEFAULT_STOP_TIMEOUT;
    
    private boolean failOnTimeout = FAIL_ON_TIMEOUT;
    
    private boolean failOnNonZeroExitCode = FAIL_ON_NON_ZERO_EXIT_CODE;
    
    private boolean removeOnFinished = REMOVE_ON_FINISHED;
    
    private RunContainerTasklet(DockerClient docker, String containerId) 
    {
        Assert.notNull(docker, "The docker client must be provided");
        Assert.notNull(containerId, "A non-null container ID is needed");
        
        this.docker = docker;
        this.containerId = containerId;
    }
    
    private void setCheckInterval(long checkMillis)
    {
        this.checkInterval = checkMillis < MIN_CHECK_INTERVAL? MIN_CHECK_INTERVAL : checkMillis;
    }
    
    public long getCheckInterval()
    {
        return checkInterval;
    }
    
    private void setTimeout(long millis)
    {
        this.timeout = millis;
    }
    
    public long getTimeout()
    {
        return timeout;
    }
    
    private void setStopTimeout(long millis)
    {
        this.stopTimeout = millis;
    }
    
    public long getStopTimeout()
    {
        return stopTimeout;
    }
    
    private void setFailOnNonZeroExitCode(boolean flag)
    {
        this.failOnNonZeroExitCode = flag;
    }
    
    private void setFailOnTimeout(boolean flag)
    {
        this.failOnTimeout = flag;
    }
    
    private void setRemoveOnFinished(boolean flag)
    {
        this.removeOnFinished = flag;
    }
    
    /**
     * Fetch all logs (stdout/stderr) generated from a container. 
     * <p>
     * It is assumed that logs are text-based (encoded as UTF-8). If this is not true,
     * consider redirecting binary output to some other (bind-mounted) file, instead of 
     * writing it directly to stdout/stderr.
     * 
     * @param containerId
     * @throws InterruptedException 
     * @throws DockerException 
     */
    private String fetchLogsFromContainer(String containerId) 
        throws DockerException, InterruptedException
    {
        LogStream outs = docker.logs(containerId, LogsParam.stdout(), LogsParam.stderr());
        
        StringBuilder b = new StringBuilder();
        while (outs.hasNext()) {
            ByteBuffer data = outs.next().content();
            b.append(StandardCharsets.UTF_8.decode(data).toString());
        }
        return b.toString();
    }
    
    /**
     * Resolve the exit-status ({@link ExitStatus}) of this step by examining the 
     * exit-code that a container's command has returned.
     * <p>A non-zero exit-code is mapped to a special case of {@link ExitStatus#FAILED} 
     * status, also following the conventions of Spring-Batch on naming these statuses 
     * (should be prefixed with "FAILED").
     * 
     * @param commandExitCode The exit-code as reported by the container's command (to docker daemon)
     * @return
     */
    private ExitStatus resolveExitStatus(int commandExitCode)
    {
        if (commandExitCode == 0)
            return ExitStatus.COMPLETED; // meaning completed and successful
        
        // This run is considered as failed: return a special case of FAILED status
        final String exitCode = "FAILED-WITH-NONZERO-EXIT-CODE";
        final String exitDescription = String.format(
            "The command exited with a non-zero (%d) code", commandExitCode);
        return new ExitStatus(exitCode, exitDescription);
    }
    
    /**
     * Start and wait for a container to complete. 
     * <p>
     * This task is clearly separated into 2 phases:
     * <ol>
     *   <li>P1: Start the container</li>
     *   <li>P2: Poll the container status (at a fixed rate), waiting for it to complete</li>  
     * </ol>
     */
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
        throws Exception
    {
        StepContext stepContext = chunkContext.getStepContext();
        StepExecution stepExecution = stepContext.getStepExecution();
        ExecutionContext executionContext = stepExecution.getExecutionContext();
        
        long started = executionContext.getLong(Keys.STARTED, -1L);
        long finished = executionContext.getLong(Keys.FINISHED, -1L);
        Assert.state(finished < 0, "Expected a non-started or running container!");
                   
        if (started < 0) {
            // P1: The container is created but not started: start it now
            docker.startContainer(containerId);
            logger.info("Started container {}", containerId);
            started = (new Date()).getTime();
            executionContext.putLong(Keys.STARTED, started);
        } else {
            // P2: The container is started: poll status, check if timed out
            ContainerInfo containerInfo = docker.inspectContainer(containerId);
            ContainerState containerState = containerInfo.state();
            logger.debug("Polled status for container {}: {}", containerId, containerState);
            long elapsedTime = (new Date()).getTime() - started;
            if (containerState.running()) {
                if (timeout > 0 && elapsedTime > timeout) {
                    // Timed out: Mark the step as failed and stop container now
                    logger.error("Timed out at {}ms: Stopping {}", timeout, containerId);
                    docker.stopContainer(containerId, (int) (stopTimeout / 1000));
                    finished = (new Date()).getTime();
                    executionContext.putLong(Keys.FINISHED, finished);
                    executionContext.putLong(Keys.TIMED_OUT, timeout);
                    contribution.setExitStatus(new TimedOutExitStatus(timeout));
                    stepExecution.setStatus(failOnTimeout? BatchStatus.FAILED : BatchStatus.COMPLETED);
                } else {
                    // The container is still running; sleep for one more period
                    logger.debug("The container {} is running: sleeping for {}ms", 
                        containerId, checkInterval);
                    Thread.sleep(checkInterval);
                }
            } else {
                // The container is finished: determine exit-status
                int exitCode = containerState.exitCode();
                logger.info("The container {} has finished: exit-code={} error={}", 
                    containerId, exitCode, containerState.error());
                finished = (new Date()).getTime();
                executionContext.putLong(Keys.FINISHED, finished);
                executionContext.putLong(Keys.COMMAND_EXIT_CODE, exitCode);
                contribution.setExitStatus(resolveExitStatus(exitCode));
                stepExecution.setStatus((failOnNonZeroExitCode && exitCode != 0)? 
                    BatchStatus.FAILED : BatchStatus.COMPLETED);
            }
        }
        
        return RepeatStatus.continueIf(finished < 0);
    }
    
    @Override
    public void beforeStep(StepExecution stepExecution)
    {
        ExecutionContext executionContext = stepExecution.getExecutionContext();
        
        // If this step execution is a restart of a previous failed execution, then we
        // should clear part of our (inherited) context before the actual execution begins
        // (in order to allow the container to be re-started).
        // In any other case (i.e a fresh start or a restart of a stopped execution), no
        // action has to be taken.
        
        long finished = executionContext.getLong(Keys.FINISHED, -1L);
        
        if (finished > 0) {
            // The previous execution has failed (a timeout or a failed command inside container)
            long timedOut = executionContext.getLong(Keys.TIMED_OUT, -1L);
            long exitCode = executionContext.getLong(Keys.COMMAND_EXIT_CODE, NOT_AN_EXIT_CODE);
            Assert.state(timedOut > 0 || (exitCode != NOT_AN_EXIT_CODE && exitCode != 0), 
                "This step was not expected to restart");
            resetExecutionContext(stepExecution);
        }
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution)
    {        
        ExecutionContext executionContext = stepExecution.getExecutionContext();
        
        long finished = executionContext.getLong(Keys.FINISHED, -1L);
        
        // Cleanup if container has finished (either normally or due to a timeout).
        // Note that the afterStep callback will also be invoked for interrupted jobs, so
        // a container should not be destroyed in those cases (as it may be restarted).
        
        if (finished > 0) {
            // The step is marked as finished, successfully or not. Save all output
            // generated from the container (stdout/stderr) into step context.
            String output;
            try {
                output = fetchLogsFromContainer(containerId);
            } catch (DockerException | InterruptedException e) {
                output = null;
                logger.error("Failed to fetch logs for container {}: {}", 
                    containerId, e.getMessage());
            }
            if (output != null)
                executionContext.putString(Keys.COMMAND_OUTPUT, output);

            // Destroy container, if tasklet is configured so.
            // Note: if a container is configured with the auto-remove flag, this
            // is useless (as docker daemon will automatically take care of it).
            if (removeOnFinished) {
                try {
                    docker.removeContainer(containerId);
                } catch (DockerException | InterruptedException e) {
                    logger.error("Failed to destroy container {}: {}",
                        containerId, e.getMessage());
                }
            }
        }
        
        return null; // do not alter exit-status
    }
    
    /**
     * Reset all execution context.
     * 
     * @param stepExecution
     */
    private void resetExecutionContext(StepExecution stepExecution)
    {
        ExecutionContext executionContext = stepExecution.getExecutionContext();
        
        executionContext.remove(Keys.STARTED);
        executionContext.remove(Keys.FINISHED);
        executionContext.remove(Keys.TIMED_OUT);
        executionContext.remove(Keys.COMMAND_EXIT_CODE);
        executionContext.remove(Keys.COMMAND_OUTPUT);
    }
}
