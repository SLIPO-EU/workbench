package eu.slipo.workbench.rpc.jobs.tasklet.docker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.util.Assert;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.ContainerState;

/**
 * A tasklet that stops a docker container.
 */
public class StopContainerTasklet implements Tasklet
{
    private static Logger logger = LoggerFactory.getLogger(StopContainerTasklet.class); 
    
    /**
     * @see {@link RunContainerTasklet#DEFAULT_STOP_TIMEOUT}
     */
    private static final long DEFAULT_STOP_TIMEOUT = 5000L;
    
    /**
     * A builder for tasklets of enclosing class ({@link StartContainerTasklet}).
     */
    public static class Builder
    {
        private DockerClient client;
        
        private String containerId;
        
        private Long timeout;
        
        private Boolean remove;

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
         * Set a timeout for waiting for a container to stop (after the proper signal is sent).
         */
        public Builder timeout(long millis)
        {
            this.timeout = millis;
            return this;
        }
        
        /**
         * Set whether we should also remove the container (just after stopping it).
         */
        public Builder remove(boolean flag)
        {
            this.remove = flag;
            return this;
        }
        
        /**
         * Set the container ID (or name) to identify the target container 
         */
        public Builder container(String containerId)
        {
            Assert.notNull(containerId, "A non-null container ID is required");
            this.containerId = containerId;
            return this;
        }
        
        public StopContainerTasklet build()
        {
            Assert.state(client != null, 
                "A docker client is required to communicate to docker daemon!");
            Assert.state(containerId != null, 
                "A container ID must be given in order to stop a container");
            
            StopContainerTasklet tasklet = new StopContainerTasklet(client, containerId);
            
            if (timeout != null)
                tasklet.setTimeout(timeout);
            if (remove != null)
                tasklet.setRemove(remove);
            
            return tasklet;
        }
    }
    
    public static Builder builder()
    {
        return new Builder();
    }
    
    private final DockerClient docker;
    
    private final String containerId;
    
    private long timeout = DEFAULT_STOP_TIMEOUT;
    
    private boolean remove = false;
    
    public StopContainerTasklet(DockerClient docker, String containerId)
    {
        Assert.notNull(docker, "The docker client must be provided");
        Assert.notNull(containerId, "A container ID must be specified");
        
        this.docker = docker;
        this.containerId = containerId;
    }
    
    private void setTimeout(long millis)
    {
        this.timeout = millis;
    }
    
    public long getTimeout()
    {
        return timeout;
    }
    
    private void setRemove(boolean flag)
    {
        this.remove = flag;
    }
    
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
        throws Exception
    {
        ContainerInfo containerInfo = docker.inspectContainer(containerId);
        ContainerState containerState = containerInfo.state();
        
        if (containerState.running()) {
            logger.info("Stopping {}", containerId);
            docker.stopContainer(containerId, (int) (timeout / 1000));
        } else {
            logger.warn("The container {} does not appear to be running", containerId);
        }
        
        if (remove) {
            docker.removeContainer(containerId);
            logger.info("Removed container {}", containerId);            
        }
        
        return RepeatStatus.FINISHED;
    }
}
