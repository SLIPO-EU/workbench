package eu.slipo.workbench.rpc.service;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.batch.core.JobExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import eu.slipo.workbench.common.model.poi.EnumTool;
import eu.slipo.workbench.common.model.process.CatalogResource;
import eu.slipo.workbench.common.model.process.EnumInputType;
import eu.slipo.workbench.common.model.process.ProcessDefinition;
import eu.slipo.workbench.common.model.process.ProcessExecutionRecord;
import eu.slipo.workbench.common.model.process.ProcessExecutionStartException;
import eu.slipo.workbench.common.model.process.ProcessExecutionStopException;
import eu.slipo.workbench.common.model.process.ProcessInput;
import eu.slipo.workbench.common.model.process.ProcessNotFoundException;
import eu.slipo.workbench.common.model.process.ProcessOutput;
import eu.slipo.workbench.common.model.process.ProcessRecord;
import eu.slipo.workbench.common.model.process.Step;
import eu.slipo.workbench.common.model.resource.DataSource;
import eu.slipo.workbench.common.model.resource.EnumDataSourceType;
import eu.slipo.workbench.common.model.resource.FileSystemDataSource;
import eu.slipo.workbench.common.model.resource.ResourceRecord;
import eu.slipo.workbench.common.model.resource.UploadDataSource;
import eu.slipo.workbench.common.repository.AccountRepository;
import eu.slipo.workbench.common.repository.ProcessRepository;
import eu.slipo.workbench.common.repository.ResourceRepository;
import eu.slipo.workbench.common.service.ProcessOperator;
import eu.slipo.workflows.Workflow;
import eu.slipo.workflows.WorkflowBuilderFactory;
import eu.slipo.workflows.WorkflowExecutionEventListener;
import eu.slipo.workflows.WorkflowExecutionSnapshot;
import eu.slipo.workflows.service.WorkflowScheduler;

@Service
public class DefaultProcessOperator implements ProcessOperator
{
    private static final Logger logger = LoggerFactory.getLogger(DefaultProcessOperator.class);

    @Autowired
    @Qualifier("tempDataDirectory")
    private Path tempDir;

    @Autowired
    @Qualifier("catalogDataDirectory")
    private Path catalogDataDir;

    @Autowired
    private ProcessRepository processRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private WorkflowScheduler workflowScheduler;

    @Autowired
    private WorkflowBuilderFactory workflowBuilderFactory;

    private class ExecutionListener implements WorkflowExecutionEventListener
    {
        private final long executionId;

        public ExecutionListener(long executionId)
        {
            this.executionId = executionId;
        }

        @Override
        public void onSuccess(WorkflowExecutionSnapshot workflowExecutionSnapshot)
        {
            // Todo Auto-generated method stub
        }

        @Override
        public void onFailure(WorkflowExecutionSnapshot workflowExecutionSnapshot)
        {
            // Todo Auto-generated method stub
        }

        @Override
        public void beforeNode(WorkflowExecutionSnapshot snapshot, String nodeName,
            JobExecution jobExecution)
        {
            // Todo Auto-generated method stub
        }

        @Override
        public void afterNode(WorkflowExecutionSnapshot snapshot, String nodeName,
            JobExecution jobExecution)
        {
            // Todo Auto-generated method stub
        }
    }

    /**
     * Build workflow from a process definition.
     *
     * @param workflowId
     * @param definition The definition of a process revision
     * @return
     */
    private Workflow buildWorkflow(UUID workflowId, ProcessDefinition definition)
    {
        final List<ProcessInput> inputs = definition.getResources();
        final List<Step> steps = definition.getSteps();

        // Map each resource key of catalog resource to a resource record

        final Map<Integer, ResourceRecord> resourceKeyToRecord = inputs.stream()
            .filter(r -> r.getInputType() == EnumInputType.CATALOG)
            .collect(Collectors.toMap(r -> r.key(), r -> findResourceRecord((CatalogResource) r)));

        final Map<Integer, Path> resourceKeyToPath = resourceKeyToRecord.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> resolveToPath(e.getValue())));

        // Map each output key to the key of processing step (expected to produce it)

        final BiMap<Integer, Integer> outputKeyToStepKey = HashBiMap.create(inputs.stream()
            .filter(r -> r.getInputType() == EnumInputType.OUTPUT)
            .map(r -> Pair.of(r.key(), ((ProcessOutput) r).stepKey()))
            .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond)));

        // Map step key to step descriptor

        final Map<Integer, Step> stepByKey = steps.stream()
            .collect(Collectors.toMap(Step::key, Function.identity()));

        // Build workflow

        final Workflow.Builder workflowBuilder = workflowBuilderFactory.get(workflowId);

        for (Step step: steps) {
            final EnumTool tool = step.tool();
            switch (tool) {
            case REGISTER_METADATA:
                {
                    // Todo Add a job node to register an output to the catalog
                }
                break;
            case TRIPLEGEO:
                {
                    // Todo Add a job node to transform an input via Triplegeo
                }
                break;
            case LIMES:
            case DEER:
            case FAGI:
                throw new NotImplementedException("todo: a job for a tool of type [" + tool + "]");
            default:
                Assert.state(false, "Did not expect a tool of type [" + tool + "]");
            }
        }

        return workflowBuilder.build();
    }

    /**
     * Resolve a reference to a resource (an instance of {@link CatalogResource}) to a
     * resource record.
     *
     * @param r The reference to a catalog's resource (pair of id and version)
     * @return a full-fledged record as returned from resource repository
     *
     * @throws NoSuchElementException if the pair of (id,version) does not correspond to
     *   a registered resource.
     */
    private ResourceRecord findResourceRecord(CatalogResource r)
    {
        Assert.state(r != null, "Expected a non-null instance of CatalogResource");

        final long id = r.getId(), version = r.getVersion();
        Assert.state(id > 0, "The resource id is not valid");
        Assert.state(version > 0, "The resource version is not valid");

        ResourceRecord record = resourceRepository.findOne(id, version);
        if (record == null)
            throw new NoSuchElementException(
                String.format("The resource does not exist (id=%d, version=%d)", id, version));

        return record;
    }

    private Path resolveToPath(ResourceRecord record)
    {
        Assert.state(record != null, "Expected a non-null record");

        final String p = record.getFilePath();
        Assert.state(!StringUtils.isEmpty(p), "Expected a non-empty relative path");

        return catalogDataDir.resolve(p);
    }

    private Path resolveToPath(DataSource source)
    {
        Assert.state(source != null, "Expected a non-null source");

        Path path = null;

        final EnumDataSourceType type = source.getType();
        switch (type) {
        case UPLOAD:
            path = tempDir.resolve(((UploadDataSource) source).getPath());
            break;
        case FILESYSTEM:
            path = tempDir.resolve(((FileSystemDataSource) source).getPath());
            break;
        case EXTERNAL_URL:
            // Todo A data source of EXTERNAL_URL should be handled in the workflow itself.
            // Add a preparation job nodes (not mapped to processing steps) handling all download tasks.
            throw new UnsupportedOperationException(
                "A data source of [" + type + "] cannot be resolved to a local file path");
        case HARVESTER:
            throw new NotImplementedException(
                "A data source of type [" + type + "] is not supported (yet)");
        default:
            Assert.state(false, "Did not expect a source type of type [" + type + "]");
        }

        return path;
    }

    /**
     * Start the execution of a workflow. The workflow is derived from definition of
     * a process.
     *
     * <p>Note: A workflow is 1-1 mapped to a process record (which in turn is a view of
     * a process revision entity). As a consequence, a process revision can only have a single
     * active execution at a given time (because a workflow does so).
     *
     * @param processRecord The process record (representing a specific revision)
     * @return
     * @throws ProcessNotFoundException
     * @throws ProcessExecutionStartException
     */
    private ProcessExecutionRecord startExecution(ProcessRecord processRecord, int uid)
        throws ProcessNotFoundException, ProcessExecutionStartException
    {
        Assert.state(processRecord != null, "Expected a non-null process record");

        final long id = processRecord.getId();
        final long version = processRecord.getVersion();

        // Build a workflow from the definition of this process

        final UUID workflowId = UUID.nameUUIDFromBytes(
            ByteBuffer.wrap(new byte[16])
                .putLong(id).putLong(version)
                .array());
        Workflow workflow = buildWorkflow(workflowId, processRecord.getDefinition());

        // Todo Create a process-execution entity and associate with captured events

        ProcessExecutionRecord executionRecord = processRepository.createExecution(id, version, uid);

        return executionRecord;
    }

    private void stopExecution(ProcessRecord process)
    {
        // Todo Find and stop workflow associated with process of given (id,version)

        throw new NotImplementedException("Todo");
    }

    private ProcessExecutionRecord pollStatus(ProcessRecord process)
    {
        // Todo Poll the status of workflow associated with process of given (id,version)

        throw new NotImplementedException("Todo");
    }

    @Override
    public ProcessExecutionRecord start(long id, long version, int uid)
        throws ProcessNotFoundException, ProcessExecutionStartException
    {
        Assert.isTrue(uid < 0 || accountRepository.exists(uid), "No user with given id");

        ProcessRecord process = processRepository.findOne(id, version);
        if (process == null)
            throw new ProcessNotFoundException(id, version);

        return startExecution(process, uid);
    }

    @Override
    public void stop(long id, long version)
        throws ProcessNotFoundException, ProcessExecutionStopException
    {
        ProcessRecord r = processRepository.findOne(id, version);
        if (r == null)
            throw new ProcessNotFoundException(id, version);
        stopExecution(r);
    }

    @Override
    public ProcessExecutionRecord poll(long id, long version)
    {
        ProcessRecord r = processRepository.findOne(id, version);
        return r == null? null : pollStatus(r);
    }

}
