package eu.slipo.workbench.rpc.jobs.tasklet.docker;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.spotify.docker.client.messages.ContainerCreation;

/**
 * A tasklet that creates a docker container based on given configuration. 
 */
public class CreateContainerTasklet implements Tasklet
{
    private static Logger logger = LoggerFactory.getLogger(CreateContainerTasklet.class); 
    
    /**
     * The collection of keys used inside our execution context
     */
    public static class Keys
    {
        public static final String CONTAINER_ID = "containerId";
        
        public static final String CONTAINER_NAME = "containerName";
    }
    
    /**
     * A builder for tasklets of enclosing class ({@link CreateContainerTasklet}).
     */
    public static class Builder
    {
        private DockerClient client;
        
        private String containerName;
        
        private ContainerConfigurer containerConfigurer = new ContainerConfigurer();
        
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
         * Set a name for this container
         */
        public Builder name(String containerName)
        {
            this.containerName = containerName;
            return this;
        }
        
        /**
         * Configure the container itself
         */
        public Builder container(Consumer<ContainerConfigurer> configurer)
        {
            Assert.notNull(configurer, "A non-null configurer is expected");
            
            configurer.accept(containerConfigurer);
            return this;
        }
        
        /**
         * Build a tasklet from configuration.
         */
        public CreateContainerTasklet build()
        {
            Assert.state(client != null, 
                "A docker client is required to communicate to docker daemon!");
            
            ContainerConfig config = containerConfigurer.buildConfiguration();
            CreateContainerTasklet tasklet = new CreateContainerTasklet(client, config, containerName);;
            return tasklet;
        }
        
    }
    
    public static Builder builder()
    {
        return new Builder();
    }
    
    private final DockerClient docker;
    
    private final ContainerConfig containerConfig;
    
    private final String containerName;
    
    private CreateContainerTasklet(DockerClient docker, ContainerConfig config, String name) 
    {
        Assert.notNull(docker, "The docker client must be provided");
        Assert.notNull(config, "The container configuration is needed");
        
        this.docker = docker;
        this.containerConfig = config;
        this.containerName = name;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
        throws Exception
    {
        StepContext stepContext = chunkContext.getStepContext();
        StepExecution stepExecution = stepContext.getStepExecution();
        ExecutionContext executionContext = stepExecution.getExecutionContext();
        
        String containerId = executionContext.getString(Keys.CONTAINER_ID, null);
        if (containerId != null) {
            // This is not an error since a stopped step (e.g. on shutdown) will re-execute
            logger.info("The container is already created as {}; Skipping", containerId);
        } else {
            // Create the container from given configuration
            ContainerCreation creation = docker.createContainer(containerConfig, containerName);
            containerId = creation.id();
            logger.info("Created container from image {}: {}", containerConfig.image(), containerId);
            executionContext.putString(Keys.CONTAINER_ID, containerId);
            if (containerName != null)
                executionContext.putString(Keys.CONTAINER_NAME, containerName);
        }
        
        return RepeatStatus.FINISHED;
    }

}
