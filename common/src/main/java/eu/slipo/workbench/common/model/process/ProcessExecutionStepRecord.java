package eu.slipo.workbench.common.model.process;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.util.Assert;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import eu.slipo.workbench.common.model.poi.EnumOperation;
import eu.slipo.workbench.common.model.poi.EnumTool;
import eu.slipo.workbench.common.model.tool.AnyTool;
import eu.slipo.workbench.common.model.tool.output.OutputPart;

public class ProcessExecutionStepRecord implements Serializable
{
    private static final long serialVersionUID = 1L;

    private long id = -1L;

    private int key;

    private String name;

    private String nodeName;

    private long jobExecutionId = -1L;

    private EnumProcessExecutionStatus status = EnumProcessExecutionStatus.UNKNOWN;

    private EnumTool tool;

    private EnumOperation operation;

    private ZonedDateTime startedOn;

    private ZonedDateTime completedOn;

    private String errorMessage;

    private List<ProcessExecutionStepFileRecord> files;
    
    private List<ProcessExecutionStepLogsRecord> logs;

    protected ProcessExecutionStepRecord() {}

    public ProcessExecutionStepRecord(long id, int key)
    {
        this.id = id;
        this.key = key;
        this.files = new ArrayList<>();
        this.logs = new ArrayList<>();
    }

    public ProcessExecutionStepRecord(int key)
    {
        this(-1L, key);
    }

    public ProcessExecutionStepRecord(ProcessExecutionStepRecord record)
    {
        this(record, true);
    }

    public ProcessExecutionStepRecord(ProcessExecutionStepRecord record, boolean copyFileRecords)
    {
        this.id = record.id;
        this.key = record.key;
        this.name = record.name;
        this.nodeName = record.nodeName;
        this.jobExecutionId = record.jobExecutionId;
        this.status = record.status;
        this.tool = record.tool;
        this.operation = record.operation;
        this.startedOn = record.startedOn;
        this.completedOn = record.completedOn;
        this.errorMessage = record.errorMessage;
        this.files = copyFileRecords?
            (record.files.stream().map(ProcessExecutionStepFileRecord::new)
                .collect(Collectors.toList())):
            (new ArrayList<>(record.files));
        this.logs = copyFileRecords?
            (record.logs.stream().map(ProcessExecutionStepLogsRecord::new)
                .collect(Collectors.toList())):
            (new ArrayList<>(record.logs));
    }

    public long getId()
    {
        return id;
    }

    public int getKey()
    {
        return key;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getNodeName()
    {
        return nodeName;
    }

    public void setNodeName(String nodeName)
    {
        this.nodeName = nodeName;
    }

    public long getJobExecutionId()
    {
        return jobExecutionId;
    }

    public void setJobExecutionId(long jobExecutionId)
    {
        this.jobExecutionId = jobExecutionId;
    }

    public EnumProcessExecutionStatus getStatus() {
        return status;
    }

    public void setStatus(EnumProcessExecutionStatus status) {
        this.status = status;
    }

    public EnumTool getTool() {
        return tool;
    }

    public void setTool(EnumTool tool) {
        this.tool = tool;
    }

    public EnumOperation getOperation() {
        return operation;
    }

    public void setOperation(EnumOperation operation) {
        this.operation = operation;
    }

    public ZonedDateTime getStartedOn() {
        return startedOn;
    }

    public void setStartedOn(ZonedDateTime startedOn) {
        this.startedOn = startedOn;
    }

    public ZonedDateTime getCompletedOn() {
        return completedOn;
    }

    public void setCompletedOn(ZonedDateTime completedOn) {
        this.completedOn = completedOn;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public int numberOfFiles()
    {
        return this.files.size();
    }

    public List<ProcessExecutionStepFileRecord> getFiles()
    {
        return Collections.unmodifiableList(files);
    }

    public Iterable<ProcessExecutionStepFileRecord> getFile(EnumStepFile type) 
    {
        Assert.notNull(type, "Expected a non-null type (EnumStepFile)");
        return Iterables.filter(files, f -> f.getType() == type);
    }

    public ProcessExecutionStepFileRecord getOutputFile(EnumStepFile type, String outputPartKey)
    {
        Assert.notNull(type, "Expected a non-null type (EnumStepFile)");
        Assert.notNull(outputPartKey, "Expected a non-null key for an output part");
        Assert.isTrue(type.isOfOutputType(), "The given type does not map to an output type");
        return files.stream()
            .filter(f -> f.getType() == type && f.getOutputPartKey().equals(outputPartKey))
            .findFirst()
            .orElse(null);
    }
    
    public ProcessExecutionStepFileRecord getOutputFile(EnumStepFile type, OutputPart<? extends AnyTool> outputPart)
    {
        Assert.notNull(outputPart, "Expected a non-null output part");
        Assert.isTrue(tool.getOutputPartEnumeration().isInstance(outputPart), 
            "The output part must be one of the parts defined by underlying tool");
        return getOutputFile(type, outputPart.key());
    }
    
    public void addFile(ProcessExecutionStepFileRecord f)
    {
        this.files.add(f);
    }

    public void addFiles(List<ProcessExecutionStepFileRecord> files)
    {
        this.files.addAll(files);
    }

    public void clearFiles()
    {
        this.files.clear();
    }

    public void setFile(int index, ProcessExecutionStepFileRecord f)
    {
        Assert.isTrue(index < this.files.size(),
            "The given index does not correspond to a file record");
        this.files.set(index, f);
    }
    
    public List<ProcessExecutionStepLogsRecord> getLogs()
    {
        return Collections.unmodifiableList(logs);
    }
    
    public void addLog(ProcessExecutionStepLogsRecord f)
    {
        this.logs.add(f);
    }

    public void addLogs(List<ProcessExecutionStepLogsRecord> logs)
    {
        this.logs.addAll(logs);
    }

    public void clearLogs()
    {
        this.logs.clear();
    }

    @Override
    public String toString()
    {
        return String.format(
            "ProcessExecutionStepRecord [id=%s, key=%s, name=%s, jobExecutionId=%s, status=%s]",
            id, key, name, jobExecutionId, status);
    }
}
