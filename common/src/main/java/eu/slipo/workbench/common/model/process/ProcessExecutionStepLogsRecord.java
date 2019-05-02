package eu.slipo.workbench.common.model.process;

import java.io.Serializable;
import java.net.URI;
import java.nio.file.Path;

import org.springframework.util.Assert;

public class ProcessExecutionStepLogsRecord implements Serializable
{
    private static final long serialVersionUID = 1L;

    private long id = -1L;

    private String name;

    private String filePath;

    protected ProcessExecutionStepLogsRecord() {}

    public ProcessExecutionStepLogsRecord(long id, String name, String path)
    {
        Assert.notNull(name, "A name is required");
        Assert.notNull(path, "A path is required");
        this.id = id;
        this.name = name;
        this.filePath = path;
    }

    public ProcessExecutionStepLogsRecord(String name, String path)
    {
        this(-1L, name, path);
    }

    public ProcessExecutionStepLogsRecord(long id, String name, Path path)
    {
        this(id, name, path == null? null : path.toString());
    }

    public ProcessExecutionStepLogsRecord(String name, Path path)
    {
        this(-1L, name, path == null? null : path.toString());
    }

    public ProcessExecutionStepLogsRecord(long id, String name, URI uri)
    {
        this(id, name, uri == null? null : uri.toString());
    }

    public ProcessExecutionStepLogsRecord(String name, URI uri)
    {
        this(-1L, name, uri == null? null : uri.toString());
    }

    public ProcessExecutionStepLogsRecord(ProcessExecutionStepLogsRecord other)
    {
        this.id = other.id;
        this.name = other.name;
        this.filePath = other.filePath;
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getFilePath()
    {
        return filePath;
    }

    public void setFilePath(String filePath)
    {
        this.filePath = filePath;
    }
}
