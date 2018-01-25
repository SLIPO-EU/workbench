package eu.slipo.workbench.rpc.workflow;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * A convenience factory for a {@link Workflow.Builder}
 */
@Component
public class WorkflowBuilderFactory
{
    /**
     * The root directory for results from workflow jobs. 
     */
    private Path dataDir; 
    
    @Autowired
    private void setDataDirectory(@Value("${slipo.rpc-server.workflows.data-dir}") String dir)
    {
        Path path = Paths.get(dir);
        Assert.isTrue(path.isAbsolute(), "Expected an absolute path");
        Assert.isTrue(Files.isDirectory(path) && Files.isWritable(path), 
            "Expected a writable directory path");
        this.dataDir = path;
    }
    
    public Workflow.Builder get(UUID id)
    {
        return new Workflow.Builder(id, dataDir);
    }
}
