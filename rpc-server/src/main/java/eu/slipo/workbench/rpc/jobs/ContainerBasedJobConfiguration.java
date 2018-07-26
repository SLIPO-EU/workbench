package eu.slipo.workbench.rpc.jobs;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import com.spotify.docker.client.DockerClient;

public class ContainerBasedJobConfiguration extends BaseJobConfiguration
{
    @Autowired
    protected DockerClient docker;

    /**
     * The root directory on a container, under which directories/files will be bind-mounted
     */
    protected Path containerDataDir;

    /**
     * A timeout for the container to run
     */
    protected long runTimeout = -1L;

    /**
     * The check interval to poll the container for completion
     */
    protected long checkInterval = -1L;

    /**
     * The upper limit for memory usage inside a container
     */
    protected long memoryLimit = -1L;

    /**
     * The upper limit for memory+swap usage inside a container
     */
    protected long memorySwapLimit = -1L;

    protected void setContainerDataDirectory(String dir)
    {
        Path dirPath = Paths.get(dir);
        Assert.isTrue(dirPath.isAbsolute(), "Expected an absolute path (inside a container)");
        this.containerDataDir = dirPath;
    }
}
