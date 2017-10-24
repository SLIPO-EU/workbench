package eu.slipo.workbench.rpc.jobs.tasklet.docker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.util.Assert;

import com.spotify.docker.client.DockerClient;

/**
 * A tasklet that removes a docker container
 */
public class RemoveContainerTasklet implements Tasklet
{
    private static Logger logger = LoggerFactory.getLogger(RemoveContainerTasklet.class); 
    
    /**
     * A builder for tasklets of enclosing class ({@link RemoveContainerTasklet}).
     */
    public static class Builder
    {
        private DockerClient client;
        
        private String containerId;
        
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
         * Set the container ID (or name) to identify the target container 
         */
        public Builder container(String containerId)
        {
            Assert.notNull(containerId, "A non-null container ID is required");
            this.containerId = containerId;
            return this;
        }
        
        public RemoveContainerTasklet build()
        {
            Assert.state(client != null, 
                "A docker client is required to communicate to docker daemon!");
            Assert.state(containerId != null, 
                "A container ID must be given in order to stop a container");
            
            return new RemoveContainerTasklet(client, containerId);
        }
    }
    
    public static Builder builder()
    {
        return new Builder();
    }
    
    private final DockerClient docker;
    
    private final String containerId;
    
    private RemoveContainerTasklet(DockerClient docker, String containerId)
    {
        Assert.notNull(docker, "The docker client must be provided");
        Assert.notNull(containerId, "A container ID must be specified");
        
        this.docker = docker;
        this.containerId = containerId;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
        throws Exception
    {
        docker.removeContainer(containerId);
        logger.info("Removed container {}", containerId);
        
        return RepeatStatus.FINISHED;
    }
}
