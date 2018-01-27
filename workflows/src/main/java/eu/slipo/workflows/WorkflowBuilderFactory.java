package eu.slipo.workflows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * A convenience factory for a {@link Workflow.Builder}
 */
public class WorkflowBuilderFactory implements InitializingBean
{
    /**
     * The root directory for results from workflow jobs. 
     */
    private Path dataDir; 
    
    public void setDataDirectory(Path dataDir)
    {
        Assert.isTrue(dataDir != null && dataDir.isAbsolute(), 
            "Expected a non-empty absolute path");
        this.dataDir = dataDir;
    }
    
    public Workflow.Builder get(UUID id)
    {
        return new Workflow.Builder(id, dataDir);
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        Assert.state(this.dataDir != null, 
            "Expected a non-empty data directory");
        Assert.state(Files.isDirectory(this.dataDir) && Files.isWritable(this.dataDir), 
            "Expected a writable directory path");
    }
}
