package eu.slipo.workflows;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.collections4.IterableUtils;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.data.util.Pair;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import eu.slipo.workflows.tasklet.CopyOutputTasklet;
import eu.slipo.workflows.util.digraph.DependencyGraph;
import eu.slipo.workflows.util.digraph.DependencyGraphs;
import eu.slipo.workflows.util.digraph.ExportDependencyGraph;
import eu.slipo.workflows.util.digraph.TopologicalSort.CycleDetected;

public class Workflow
{
    /**
     * A name prefix for names of jobs created from a workflow
     */
    public static final String JOB_NAME_PREFIX = "workflow";
    
    /**
     * The output directory relative to a workflow's data directory
     */
    public static final String OUTPUT_DIR = "output";
    
    /**
     * An enumeration of job parameters reserved for use in a workflow.
     * <p>The following parameters will be overwritten when an actual parameters map
     * (for a job node) is built.   
     */
    public static enum Parameter 
    {
        /**
         * This parameter will hold the colon-separated list of input file paths.  
         */
        INPUT("input"),
        
        /**
         * This parameter will hold the UUID of the workflow that a job is part of.
         */
        WORKFLOW("workflow");
        
        private final String key;
        
        private Parameter(String key)
        {
            this.key = key;
        }
        
        public String key()
        {
            return key;
        }
    }
    
    /**
     * An enumeration of execution-context keys that a workflow is aware of.
     */
    public static enum ContextKey 
    {
        OUTPUT_ARCHIVE(CopyOutputTasklet.OUTPUT_ARCHIVE_KEY),
        
        OUTPUT_DIR(CopyOutputTasklet.OUTPUT_DIR_KEY);
        
        private final String key;
        
        private ContextKey(String key)
        {
            this.key = key;
        }
        
        public String key()
        {
            return key;
        }
    }
    
    /**
     * Represent a result produced by a job of a workflow.
     */
    static class Result
    {
        private static final String SCHEME = "res";
        
        /**
         * The name of a job node expected to produce this result.
         */
        private final String nodeName;
        
        /**
         * The path under which this result is (expected to be) created. This path 
         * should be relative to node's staging output directory.
         * 
         * <p>Note that this path can be null or empty, and in this case it simply
         * declares a dependency to the named job node. 
         */
        private final Path outputPath;

        private Result(String nodeName, Path outputPath)
        {
            Assert.isTrue(!StringUtils.isEmpty(nodeName), 
                "Expected a non-empty name for a job node");
            Assert.isTrue(outputPath == null || !outputPath.isAbsolute(), 
                "Expected a relative output path (or null)");
            this.nodeName = nodeName;
            this.outputPath = outputPath;
        }
        
        /**
         * The name of a job node expected to produce this result.
         */
        public String nodeName()
        {
            return nodeName;
        }
        
        /**
         * The output path relative to node's staging output directory
         */
        public Path outputPath()
        {
            return outputPath;
        }
        
        /**
         * The output path relative to data directory of a workflow 
         */
        public Path path()
        {
            Assert.state(!StringUtils.isEmpty(outputPath), "No output path was given!");
            Path parent = parentPath();
            return parent.resolve(outputPath);
        }
        
        /**
         * The output directory relative to data directory of a workflow. This is the parent 
         * directory of the expected output.
         */
        public Path parentPath()
        {
            return Paths.get("stage", nodeName, "output");
        }
        
        public static Result of(String nodeName)
        {
            return new Result(nodeName, null);
        }
        
        public static Result of(String nodeName, Path outputPath)
        {
            return new Result(nodeName, outputPath);
        }
        
        public static Result of(URI uri)
        {
            Assert.isTrue(uri.getScheme().equals(SCHEME), 
                "The given URI is malformed (does not begin with the expected scheme)");
            String nodeName = uri.getHost();
            Assert.isTrue(!StringUtils.isEmpty(nodeName), 
                "The host (i.e node name) part cannot be empty");
            String path = uri.getPath().replaceFirst("/", "");
            return new Result(nodeName, Paths.get(path));
        }
        
        public static Result parse(URI uri)
        {
            if (!uri.getScheme().equals(SCHEME))
                return null;
            
            String nodeName = uri.getHost();
            String path = uri.getPath().replaceFirst("/", "");
            return new Result(nodeName, Paths.get(path));
        }
        
        public URI toUri()
        {
            return URI.create(toString());
        }
        
        @Override
        public String toString()
        {
            return String.format("%s://%s/%s", 
                SCHEME, nodeName, outputPath == null? "" : outputPath.toString());
        }
    }
    
    /**
     * Represent the configuration of a job participating in a workflow. 
     */
    public static class JobDefinition
    {
        /**
         * A name (unique for a given workflow) for this job node.
         */
        private String name;
        
        /**
         * The actual flow wrapped as a job of a workflow. 
         */
        private Flow flow;
        
        /**
         * A map of parameters to be forwarded to the job.
         */
        private JobParameters parameters;
        
        /** 
         * The list of inputs for this job.
         * 
         * <p>These inputs are given as URIs in one of the following formats:
         * <ul>
         *   <li><tt>file:///&lt;path&gt;</tt>: an external (to parent workflow) file supplied 
         *     as input</li>
         *   <li><tt>res://&lt;dependency-name&gt;/&lt;input-path&gt;</tt>: a result produced by another 
         *     job of the same workflow.</li>
         * </ul>
         * 
         *  @see {@link Result}
         */
        private List<URI> input;
        
        /**
         * A list of output files (relative to this job's output directory) that should be copied
         * to the workflow-level output directory.
         */
        private List<Path> output;

        private JobDefinition(String name, Flow flow, JobParameters parameters) 
        {
            this.name = name;
            this.flow = flow;
            this.parameters = parameters == null? (new JobParameters()) : parameters;
        }

        private void setInput(List<URI> input)
        {
            this.input = Collections.unmodifiableList(input);
        }
       
        private void setOutput(List<Path> output)
        {
            this.output = Collections.unmodifiableList(output);
        }
        
        private JobDefinition withInput(List<URI> input)
        {
            JobDefinition def = new JobDefinition(this.name, this.flow, this.parameters);
            def.input = Collections.unmodifiableList(input);
            def.output = this.output;
            return def;
        }
       
        private JobDefinition withOutput(List<Path> output)
        {
            JobDefinition def = new JobDefinition(this.name, this.flow, this.parameters);
            def.input = this.input;
            def.output = Collections.unmodifiableList(output);
            return def;
        }
        
        private JobDefinition with(List<URI> input, List<Path> output)
        {
            JobDefinition def = new JobDefinition(this.name, this.flow, this.parameters);
            def.input = Collections.unmodifiableList(input);
            def.output = Collections.unmodifiableList(output);
            return def;
        }
        
        public String name()
        {
            return name;
        }
        
        public String nodeName()
        {
            return name;
        }

        public Flow flow()
        {
            return flow;
        }

        public JobParameters parameters()
        {
            return parameters;
        }

        public List<URI> input()
        {
            return input;
        }

        public List<Path> output()
        {
            return output;
        }

        @Override
        public String toString()
        {
            return String.format("JobDefinition [name=%s, input=%s, output=%s]", 
                name, input, output);
        }
    }
    
    /**
     * A builder for a {@link JobDefinition}.  
     */
    public static class JobDefinitionBuilder
    {
        /**
         * The pattern that a name part must conform to. A name may consist of several 
         * dot-delimited parts (e.g. "aggregator.q1").
         */
        public static final Pattern namePartPattern = 
            Pattern.compile("^[a-z0-9][-_a-z0-9]*$", Pattern.CASE_INSENSITIVE);
        
        private String name;
        
        private Flow flow;
        
        private JobParameters parameters;
        
        private List<URI> input = new ArrayList<>();
        
        private List<Path> output = new ArrayList<>();
        
        public JobDefinitionBuilder() {}
                
        /**
         * Name this job. This is the name of the job node and should be unique across 
         * a given workflow.
         * 
         * <p>Note: This name is, in general, different from the name with which a job is 
         * launched as an actual Batch job (and which is decided by a {@link WorkflowScheduler}).
         * 
         * @param name The name of a job node
         */
        public JobDefinitionBuilder name(String name)
        {
            Assert.isTrue(!StringUtils.isEmpty(name), "Expected a name for this job");
            
            String[] nameParts = name.split(Pattern.quote("."));
            Assert.isTrue(Arrays.stream(nameParts)
                    .allMatch(part -> namePartPattern.matcher(part).matches()), 
                "The given name is not valid");
            
            this.name = name;
            return this;
        }
        
        /**
         * Provide the flow to be executed as the trunk of the job. 
         * 
         * @param flow
         */
        public JobDefinitionBuilder flow(Flow flow)
        {
            Assert.notNull(flow, "Expected a non-null flow");
            this.flow = flow;
            return this;
        }
        
        /**
         * Provide a single step to be executed as the trunk of the job.
         * 
         * <p>This is convenience method that simply wraps a {@link Step} into a {@link Flow}.
         * @param flow
         */
        public JobDefinitionBuilder flow(Step step)
        {
            Assert.notNull(step, "Expected a non-null step");
            this.flow = new FlowBuilder<Flow>(step.getName()).start(step).end();
            return this;
        }
        
        /**
         * Provide a map of parameters for the job. These parameters will be forwarded
         * to the actual run of a Batch job.
         * 
         * @param parameters
         */
        public JobDefinitionBuilder parameters(JobParameters parameters)
        {
            Assert.notNull(parameters, "Expected a non-null map of parameters");
            this.parameters = parameters;
            return this;
        }
        
        /**
         * Provide a map of parameters for the job. 
         * <p>This is a lambda-friendly version of {@link JobDefinitionBuilder#parameters(JobParameters)}
         * 
         * @param defaultParameters A map of default parameters to start from (may be null).
         * @param configurer A function to configure a {@link JobParametersBuilder}
         */
        public JobDefinitionBuilder parameters(
            JobParameters defaultParameters, Consumer<JobParametersBuilder> configurer)
        {
            Assert.notNull(configurer, "Expected a non-null configurer function");
            JobParametersBuilder parametersBuilder = defaultParameters == null?
                new JobParametersBuilder() : new JobParametersBuilder(defaultParameters);
            configurer.accept(parametersBuilder);
            this.parameters = parametersBuilder.toJobParameters();
            return this;
        }
        
        /**
         * Provide a map of parameters for the job.
         * @see {@link JobDefinitionBuilder#parameters(JobParameters, Consumer)}
         * 
         * @param configurer A function to configure a {@link JobParametersBuilder}
         */
        public JobDefinitionBuilder parameters(Consumer<JobParametersBuilder> configurer)
        {
            return parameters(null, configurer);
        }
        
        /**
         * Add another input file (external to a workflow).
         * 
         * @param path An absolute file path
         */
        public JobDefinitionBuilder input(Path path)
        {
            Assert.isTrue(path != null && path.isAbsolute(), 
                "Expected a non-null absolute file path");
            this.input.add(path.toUri());
            return this;
        }
        
        public JobDefinitionBuilder input(String path)
        {
            return input(Paths.get(path));
        }
        
        /**
         * Add another input file as an expected (i.e. promised) result from a job we depend on.
         * 
         * @param dependencyName The name of the job node that we depend on (and must precede)
         * @param path A path that will be resolved relative to the output directory
         *   of the dependency. An empty or null path can be used to just declare a dependency
         *   (in the meaning of precedence). A glob-style path can also be used here and will be
         *   expanded to matching paths when the workflow is built.
         */
        public JobDefinitionBuilder input(String dependencyName, Path path)
        {
            Result res = Result.of(dependencyName, path);
            this.input.add(res.toUri());
            return this;
        }
        
        public JobDefinitionBuilder input(String dependencyName, String path)
        {
            return input(dependencyName, Paths.get(path));
        }
                
        /**
         * Add a bunch of dependencies having a common relative (to dependency) path.
         * 
         * <p>This is just a convenience method that iterates on given dependencies adding
         * each pair of (dependency, path) to our input specification. 
         *   
         * @param dependencyNames A collection of dependencies
         * @param path The common dependency-relative path
         * @return
         */
        public JobDefinitionBuilder input(Collection<String> dependencyNames, Path path)
        {
            for (String dependencyName: dependencyNames)
                input(dependencyName, path);
            return this;
        }
        
        /**
         * @see {@link JobDefinitionBuilder#input(Collection, Path)}
         * @param dependencyNames
         * @param path
         */
        public JobDefinitionBuilder input(Collection<String> dependencyNames, String path)
        {
            return input(dependencyNames, Paths.get(path));
        }
        
        /**
         * Designate a file as an the output of this job.
         * <p>An output of a job is made accessible to other parts of the same workflow
         * by making a copy (or link) of it inside a staging output directory.
         * 
         * @param path A path relative to the output directory of the job
         */
        public JobDefinitionBuilder output(Path path)
        {   
            Assert.isTrue(path != null && !path.isAbsolute(), 
                "Expected a non-empty relative file path");
            this.output.add(path);
            return this;
        }
        
        /**
         * @see {@link JobDefinitionBuilder#output(Collection))}
         */
        public JobDefinitionBuilder output(Path ...paths)
        {
            return output(Arrays.asList(paths));
        }
        
        /**
         * Designate a collection of files as an output of this job.
         * @see {@link JobDefinitionBuilder#output(Path)}
         * 
         * @param paths A collection of paths, relative to the output directory of the job
         */
        public JobDefinitionBuilder output(Collection<Path> paths)
        {
            Assert.isTrue(paths.stream().allMatch(p -> p != null && !p.isAbsolute()), 
                "Expected a non-empty relative file path");
            this.output.addAll(paths);
            return this;
        }
        
        /**
         * @see {@link JobDefinitionBuilder#output(Collection))}
         */
        public JobDefinitionBuilder output(String ...paths)
        {
            Stream<Path> s = Arrays.stream(paths).map(Paths::get);
            return output(s.collect(Collectors.toList()));
        }
        
        public JobDefinitionBuilder output(String path)
        {
            return output(Paths.get(path));
        }
        
        public JobDefinition build()
        {
            Assert.state(!StringUtils.isEmpty(name), "A non-empty name is required for a job node");
            Assert.state(flow != null, "A trunk flow is required for a job");
            
            JobDefinition def = new JobDefinition(name, flow, parameters);
            def.setInput(new ArrayList<>(input));
            def.setOutput(new ArrayList<>(output));
            return def;
        }
    }
    
    /**
     * Represent a job that participates in a workflow.
     * 
     * <p>A {@link WorkflowScheduler} must map it to a single {@link JobInstance}, and
     * start it when its dependencies are complete.
     */
    public class JobNode
    {
        /**
         * The node (i.e. vertex) number for this node in parent workflow
         */
        private final int vertex;
        
        private JobNode(int vertex)
        {
            this.vertex = vertex;
        }
        
        protected int vertex()
        {
            return vertex;
        }
        
        /**
         * Get the name of the job node
         */
        public String name()
        {
            return defs.get(vertex).name();
        }
        
        /**
         * A name as a (suggestion of a) job name passed to Batch
         */
        public String jobName()
        {
            return JOB_NAME_PREFIX + "." + (defs.get(vertex).flow().getName());
        }
        
        /**
         * Build a flow to be executed inside a Batch job. 
         * 
         * <p>Note that this flow will not be the same as the one given to the original 
         * {@link JobDefinition}, as it is enhanced (by wrapping it into another flow) to 
         * support data exchange between jobs of same workflow.
         * 
         * @param stepBuilderFactory
         */
        public Flow flow(StepBuilderFactory stepBuilderFactory)
        {
            final JobDefinition def = defs.get(vertex);
            final Flow trunkFlow = def.flow();
            final String flowName = JOB_NAME_PREFIX + "." + trunkFlow.getName();
            final List<Path> outputPaths = def.output();
            
            Flow flow = null;
            if (!outputPaths.isEmpty()) {
                // Append a step to copy output into node's staging directory
                Path targetDir = Result.of(def.name()).parentPath();
                Tasklet copyOutputTasklet = 
                    new CopyOutputTasklet(dataDir.resolve(targetDir), outputPaths); 
                Step copyOutputStep = stepBuilderFactory.get(JOB_NAME_PREFIX + ".copyOutput")
                    .tasklet(copyOutputTasklet)
                    .build();
                flow = new FlowBuilder<Flow>(flowName)
                    .start(trunkFlow)
                    .next(copyOutputStep)
                    .end();
            } else {
                // No output from this job: just rename flow
                flow = new FlowBuilder<Flow>(flowName).start(trunkFlow).end();
            }
            
            return flow;
        }
        
        /**
         * The map of parameters to be passed to a Batch job.
         * 
         * <p>More specifically, these parameters are: 
         * <ul>
         *    <li>all parameters given to original {@link JobDefinition}</li>
         *    <li>an <tt>input</tt> parameter containing a colon-separated list of inputs 
         *      (resolved to absolute filesystem paths)</li>
         *    <li>a <tt>workflow</tt> parameter containing the id of parent workflow</li>  
         * </ul>    
         */
        public JobParameters parameters()
        {
            final JobDefinition def = defs.get(vertex);
            final Stream<String> input = def.input().stream()
                .map(uri -> toAbsolutePath(uri).toString());
            return new JobParametersBuilder(def.parameters())
                .addString(Parameter.WORKFLOW.key, id.toString())
                .addString(Parameter.INPUT.key, 
                    input.collect(Collectors.joining(File.pathSeparator)))
                .toJobParameters();
        }
        
        /**
         * List our (expected) inputs as absolute filesystem paths.
         * 
         * <p>Note that the corresponding files may or may not exist: only when dependencies
         * are fulfilled, these files are expected to to exist and to be readable.
         */
        public List<Path> input()
        {
            final JobDefinition def = defs.get(vertex);
            return def.input().stream()
                .map(uri -> toAbsolutePath(uri))
                .collect(Collectors.toList());
        }
        
        private Path toAbsolutePath(URI uri)
        {
            Path inputPath = null;
            String scheme = uri.getScheme();
            if (scheme.equals("file")) {
                inputPath = Paths.get(uri);
            } else if (scheme.equals(Result.SCHEME)) {
                inputPath = dataDir.resolve(Result.of(uri).path());
            } else {
                Assert.state(false, "Encountered an unknown URI scheme [" + scheme + "]");
            }
            return inputPath;
        }
        
        /**
         * List our (expected) outputs as absolute filesystem paths.
         */
        public List<Path> output()
        {
            final JobDefinition def = defs.get(vertex);
            final String name = def.name();
            return def.output().stream()
                .map(p -> dataDir.resolve(Result.of(name, p).path()))
                .collect(Collectors.toList());
        }

        /**
         * Get an iterable of our dependencies
         */
        public Iterable<JobNode> dependencies()
        {
            final Workflow workflow = Workflow.this;
            return IterableUtils.transformedIterable(
                dependencyGraph.dependencies(vertex), v -> workflow.new JobNode(v));
        }

        /**
         * Get an iterable of our dependents
         */
        public Iterable<JobNode> dependents()
        {
            final Workflow workflow = Workflow.this;
            return IterableUtils.transformedIterable(
                dependencyGraph.dependents(vertex), v -> workflow.new JobNode(v));
        }

        @Override
        public String toString()
        {
            return String.format("%s/%s", id, name());
        }

        @Override
        public int hashCode()
        {
            final int P = 31;
            int result = 1;
            result = P * result + id.hashCode();
            result = P * result + vertex;
            return result;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
                return true;
            if (o == null || !(o instanceof JobNode))
                return false;
            JobNode other = (JobNode) o;
            return (other.workflow().equals(Workflow.this) && other.vertex == vertex);
        }
        
        private Workflow workflow()
        {
            return Workflow.this;
        }
    }
 
    /**
     * A builder for a {@link Workflow}
     */
    public static class Builder
    {        
        private static final AntPathMatcher pathMatcher = new AntPathMatcher(); 
        
        private final Path dataDir;
        
        private final UUID id;
        
        private List<JobDefinition> defs = new ArrayList<>();
        
        private Map<String,URI> output = new HashMap<>();
        
        private List<WorkflowExecutionListener> listeners = new ArrayList<>();
        
        Builder(UUID id, Path dataDir)
        {
            Assert.notNull(id, "Expected a non-null identifier");
            Assert.notNull(dataDir, "Expected a non-null directory path");
            this.id = id;
            this.dataDir = dataDir;
        }
        
        public Builder job(JobDefinition definition)
        {
            Assert.notNull(definition, "Expected a non-empty job definition");
            Assert.isTrue(defs.stream().noneMatch(y -> y.name().equals(definition.name())),
                "A job node with same name is already defined!");
            this.defs.add(definition);
            return this;
        }
        
        public Builder job(Consumer<JobDefinitionBuilder> configurer)
        {
            Assert.notNull(configurer, "Expected a configurer for a job");
            JobDefinitionBuilder definitionBuilder = new JobDefinitionBuilder();
            configurer.accept(definitionBuilder);
            return job(definitionBuilder.build());
        }

        /**
         * Designate an output of a job node as (part of) output of the entire workflow. 
         * 
         * @param nodeName The name of the job node that produces this output.
         * @param path The output path relative to node's staging output directory
         * @param fileName The destination file to copy this output to. If <tt>null</tt>,
         *   the file name of the source path (i.e.{@link path}) will be used. 
         */
        public Builder output(String nodeName, Path path, String fileName)
        {
            Assert.isTrue(!StringUtils.isEmpty(nodeName), "Expected a name (nodeName) for a job node");
            Assert.notNull(path, "Expected an output path");
            
            if (fileName == null)
                fileName = path.getFileName().toString();
            
            Assert.isTrue(!StringUtils.isEmpty(fileName), "Expected a non-empty fileName");
            Assert.isTrue(!output.containsKey(fileName), String.format( 
                "An output file named as [%s] is already present", fileName));
            
            URI outputUri = Result.of(nodeName, path).toUri();
            
            output.put(fileName, outputUri);
            
            return this;
        }
        
        public Builder output(String nodeName, Path path)
        {
            return output(nodeName, path, null);
        }
        
        public Builder output(String nodeName, String path)
        {
            return output(nodeName, Paths.get(path), null);
        }
        
        public Builder output(String nodeName, String path, String fileName)
        {
            return output(nodeName, Paths.get(path), fileName);
        }
        
        /**
         * Register an execution listener.
         */
        public Builder listener(WorkflowExecutionListener listener)
        {
            Assert.notNull(listener, "Expected a non-null listener");
            listeners.add(listener);
            return this;
        }
        
        /**
         * Build a job definition by expanding glob-style input wildcards (if any).
         */
        private JobDefinition expandDef(JobDefinition def)
        {
            // Check if any glob-style inputs are present in this job definition
            
            boolean shouldExpand = def.input().stream()
                .map(Result::parse)
                .anyMatch(res -> res != null && pathMatcher.isPattern(res.outputPath().toString()));
            
            if (!shouldExpand)
                return def;
            
            // The definition contains input with glob-style wildcards to be expanded
            
            List<URI> input = new ArrayList<>();
            for (URI uri: def.input()) {
                final Result res = Result.parse(uri);
                if (res != null && pathMatcher.isPattern(res.outputPath().toString())) {
                    // Expand to matching outputs from our dependency
                    final JobDefinition dependency = defByName(res.nodeName());
                    final String pattern = res.outputPath().toString();
                    Assert.state(dependency != null, String.format(
                        "No job named as [%s]", res.nodeName()));
                    List<URI> expandedUris = dependency.output().stream()
                        .filter(p -> pathMatcher.match(pattern, p.toString()))
                        .map(p -> Result.of(res.nodeName(), p).toUri())
                        .collect(Collectors.toList());
                    input.addAll(expandedUris);
                } else {
                    input.add(uri);
                }
            }
            
            return def.withInput(input);
        }
        
        private JobDefinition defByName(String name)
        {
            Optional<JobDefinition> optionalDef = this.defs.stream()
                .filter(y -> y.name().equals(name))
                .findFirst();
            return optionalDef.isPresent()? optionalDef.get() : null;
        }
        
        public Workflow build()
        {        
            Assert.state(!defs.isEmpty(), "The workflow contains no job definitions");
            
            final UUID workflowId = this.id;
            final Path workflowDataDir = this.dataDir.resolve(workflowId.toString());
            
            final List<JobDefinition> defs = this.defs.stream()
                .map(def -> expandDef(def))
                .collect(Collectors.toList());
            
            final Map<String,URI> output = new HashMap<>(this.output);
            
            Workflow workflow = new Workflow(workflowId, workflowDataDir, defs, output);
            
            if (!this.listeners.isEmpty())
                workflow.setListeners(new ArrayList<>(this.listeners));
            
            return workflow;
        }
    }
    
    private final UUID id;
    
    /**
     * The data directory for this workflow. 
     */
    private final Path dataDir;
    
    /**
     * The definitions for the jobs that comprise this workflow 
     */
    private final List<JobDefinition> defs;
    
    /**
     * An 1-1 map of node names to integers in <tt>[0..N-1]</tt>, where <tt>N</tt> 
     * is the number of nodes.
     */
    private final Map<String, Integer> nameMap;
    
    /**
     * The dependency graph of this workflow.
     */
    private final DependencyGraph dependencyGraph;
    
    /**
     * A list of registered execution listeners
     */
    private List<WorkflowExecutionListener> listeners = Collections.emptyList();
    
    /**
     * A list of intermediate results that are to be considered as final results of
     * the entire workflow.
     */
    private final Map<String,URI> output;

    private Workflow(UUID id, Path dataDir, List<JobDefinition> defs, Map<String,URI> output)
    {
        this.id = id;
        this.dataDir = dataDir;
        this.defs = Collections.unmodifiableList(defs);
        
        final int n = defs.size();
        
        // Map node names to integer vertices
        
        this.nameMap = Collections.unmodifiableMap(
            IntStream.range(0, n)
                .mapToObj(v -> Pair.of(defs.get(v).name(), v))
                .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond)));
        
        // Validate and build dependency graph
        
        this.dependencyGraph = DependencyGraphs.create(n);
        for (JobDefinition def: defs) {
            final int v = this.nameMap.get(def.name()); 
            for (URI inputUri: def.input()) {
                if (inputUri.getScheme().equals(Result.SCHEME)) {
                    Result res = Result.of(inputUri);
                    String dependencyName = res.nodeName();
                    int u = this.nameMap.getOrDefault(dependencyName, -1);
                    Assert.state(u >= 0, 
                        String.format("The job node [%s] depends on an undefined node named as [%s]", 
                            def.name(), dependencyName));
                    Assert.state(defs.get(u).output().contains(res.outputPath()),
                        String.format("The job node [%s] depends on an unknown result: %s", 
                            def.name(), res));
                    this.dependencyGraph.addDependency(v, u);
                }
            }
        }
        
        // Check if dependency graph is a DAG
        
        try {
            DependencyGraphs.check(this.dependencyGraph);
        } catch (CycleDetected cycle) {
            Assert.state(false, String.format(
                "The workflow has cyclic dependencies on job node [%s]", 
                defs.get(cycle.root()).name()));
        }
        
        // Assign what is considered as the output of the workflow.
        // Check that each output URI is a designated output from referenced job node
        
        for (URI outputUri: output.values()) {
            final Result res = Result.of(outputUri);
            final int v = nameMap.getOrDefault(res.nodeName(), -1); 
            Assert.isTrue(v >= 0, 
                String.format("The node [%s] does not exist", res.nodeName()));
            Assert.state(defs.get(v).output().contains(res.outputPath()),
                String.format("The node [%s] does not output at %s", res.nodeName(), res.outputPath()));
        }
        
        this.output = Collections.unmodifiableMap(output);
    } 
    
    private void setListeners(List<WorkflowExecutionListener> listeners)
    {
        Assert.notEmpty(listeners, "Expected a non-empty list of listeners");
        this.listeners = Collections.unmodifiableList(listeners);
    }
    
    protected Iterable<Integer> vertices()
    {
        final int n = defs.size();
        return () -> IntStream.range(0, n).boxed().iterator();
    }
    
    public UUID id()
    {
        return id;
    }
    
    /**
     * The data directory for this workflow.
     */
    public Path dataDirectory()
    {
        return dataDir;
    }
    
    /**
     * The staging directory for a given job node.
     *  
     * @param nodeName The name of the job node
     */
    public Path stagingDirectory(String nodeName)
    {
        int vertex = nameMap.getOrDefault(nodeName, -1);
        Assert.isTrue(vertex >= 0, "No job node is named as [" + nodeName + "]");
        Path dir = Paths.get("stage", nodeName);
        return dataDir.resolve(dir);
    }
    
    /**
     * The output directory for this workflow. This will always be under data directory
     * (i.e {@link Workflow#dataDirectory()}).
     * 
     * <p>The output directory is the destination for results designated as output of the entire
     * workflow (and not just as a node's output result). 
     */
    public Path outputDirectory()
    {
        return dataDir.resolve(OUTPUT_DIR);
    }
    
    /**
     * List names of job nodes of this workflow
     */
    public Set<String> nodeNames()
    {
        return nameMap.keySet();
    }
    
    /**
     * The number of job nodes in this workflow 
     */
    public int size()
    {
        return defs.size();
    }
    
    /**
     * Map (expected) output files to absolute filesystem paths
     */
    public Map<String,Path> output()
    {
        return Collections.unmodifiableMap(
            output.entrySet().stream()
                .map(entry -> Pair.of(
                    entry.getKey(), 
                    dataDir.resolve(Result.of(entry.getValue()).path())))
                .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond)));
    }
    
    /**
     * Map (expected) output files to result URIs
     */
    public Map<String,URI> outputUris()
    {
        return output;
    }
    
    /**
     * Get the job node by its name
     * 
     * @param name The name of this job node as it was assigned in the original 
     * {@link JobDefinition}.
     */
    public JobNode node(String name)
    {
        int vertex = nameMap.getOrDefault(name, -1);
        Assert.isTrue(vertex >= 0, "No job node is named as [" + name + "]");
        return new JobNode(vertex);
    }
    
    /**
     * Get the job node by its vertex number. 
     * 
     * <p>Note: This method is designed for internal use from cooperating classes of 
     * same package. 
     * 
     * @param vertex The vertex number
     */
    protected JobNode node(int vertex)
    {
        Assert.state(vertex >= 0 && vertex < defs.size(), "The vertex number is invalid");
        return new JobNode(vertex);
    }
    
    /**
     * Iterate on the set of job nodes. 
     * 
     * <p>The iteration order is same with the order of definition of each job (when 
     * the workflow was built). 
     */
    public Iterable<JobNode> nodes()
    {
        final Workflow workflow = this;
        final int n = defs.size();
        return () -> IntStream.range(0, n)
            .mapToObj(v -> workflow.new JobNode(v))
            .iterator();
    }
    
    /**
     * Iterate on the set of job nodes
     * 
     * <p>The iteration order follows a topological ordering of nodes (this is not unique).
     */
    public Iterable<JobNode> nodesInTopologicalOrder()
    {
        final Workflow workflow = this;
        Iterable<Integer> vertices = null; 
        try {
            vertices = DependencyGraphs.topologicalSort(dependencyGraph);
        } catch (CycleDetected e) {
            Assert.state(false, "Did not expect a cycle of dependencies");
        }
        return IterableUtils.transformedIterable(vertices, v -> workflow.new JobNode(v));
    }
    
    public List<WorkflowExecutionListener> getListeners()
    {
        return listeners;
    }
    
    @Override
    public String toString()
    {
        return String.format("Workflow [id=%s, dataDir=%s, names=%s]", 
            id, dataDir, nodeNames());
    }

    public String debugGraph()
    {
        return DependencyGraphs.toString(
            dependencyGraph, 
            u -> defs.get(u).name(), 
            ExportDependencyGraph.Direction.LR);
    }
    
    protected String debugGraph(IntFunction<ExportDependencyGraph.NodeAttributes> styleMapper)
    {
        return DependencyGraphs.toString(
            dependencyGraph, 
            u -> defs.get(u).name(), 
            styleMapper,
            ExportDependencyGraph.Direction.LR);
    }
}
