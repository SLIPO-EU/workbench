package eu.slipo.workbench.common.model.tool;

import java.io.File;
import java.io.Serializable;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.function.Function;

import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.Range;
import org.hibernate.validator.constraints.URL;
import org.springframework.expression.ParseException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.google.common.collect.Lists;

import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.common.model.poi.EnumTool;
import eu.slipo.workbench.common.model.tool.output.EnumLimesOutputPart;
import eu.slipo.workbench.common.model.tool.output.EnumOutputType;
import eu.slipo.workbench.common.model.tool.output.OutputNameMapper;

/**
 * Configuration for LIMES
 */

@JsonPropertyOrder({ 
    "prefixes", "PREFIX",
    "source", "SOURCE", "target", "TARGET", 
    "metric", "METRIC", 
    "acceptance", "ACCEPTANCE", 
    "review", "REVIEW",
    "execution", "EXECUTION",
    "outputFormat", "OUTPUT" 
})
@JacksonXmlRootElement(localName = "LIMES")
@eu.slipo.workbench.common.model.tool.serialization.DtdDeclaration(name = "LIMES", href = "limes.dtd")
public class LimesConfiguration extends InterlinkConfiguration<Limes> 
{
    private static final long serialVersionUID = 1L;
    
    public static final String VERSION = "1.3";
    
    private static final int SOURCE_INDEX = 0;
    
    private static final int TARGET_INDEX = 1;
    
    public static final String VAR_NAME_REGEXP = "^[?]\\w[0-9\\w]*$";
    
    private static final SpelExpressionParser spelParser = new SpelExpressionParser();
    
    public enum DataFormat
    {
        N3(EnumDataFormat.N3, "N3"),
        N_TRIPLES(EnumDataFormat.N_TRIPLES, "N-TRIPLES"),
        TURTLE(EnumDataFormat.TURTLE, "TURTLE", "TTL"),
        RDF_XML(EnumDataFormat.RDF_XML, "RDF", "RDF/XML"),
        RDF_XML_ABBREV(EnumDataFormat.RDF_XML_ABBREV, "RDF/XML-ABBREV");
        
        private final EnumDataFormat dataFormat;

        private final List<String> keys;

        private DataFormat(EnumDataFormat dataFormat, String key0, String ...aliases)
        {
            Assert.notNull(dataFormat, "Expected an enum constant for data format");
            this.dataFormat = dataFormat;
            
            Assert.notNull(key0, "Expected a non-null key for data format " + dataFormat);
            LinkedList<String> keys = new LinkedList<>(Arrays.asList(aliases));
            keys.addFirst(key0);
            this.keys = Collections.unmodifiableList(keys);
        }

        public List<String> keys()
        {
            return keys;
        }

        public String key()
        {
            return keys.get(0);
        }

        public EnumDataFormat dataFormat()
        {
            return dataFormat;
        }

        public static DataFormat from(String key)
        {
            Assert.isTrue(!StringUtils.isEmpty(key), "Expected a non-empty key to search for");
            for (DataFormat e: DataFormat.values()) {
                if (e.name().equals(key) || e.keys.indexOf(key) >= 0) {
                    return e;
                }
            }
            return null;
        }

        public static DataFormat from(EnumDataFormat dataFormat)
        {
            Assert.notNull(dataFormat, "Expected a non-null data format to search for");
            for (DataFormat e: DataFormat.values()) {
                if (e.dataFormat == dataFormat) {
                    return e;
                }
            }
            return null;
        }
    }
    
    @JsonPropertyOrder({ 
        "namespace", "NAMESPACE",
        "label", "LABEL"
    })
    public static class Prefix implements Serializable, Comparable<Prefix>
    {
        static final long serialVersionUID = 1L;

        String label;
        
        String namespace;

        Prefix() {}
        
        public Prefix(String label, String namespace)
        {
            Assert.isTrue(!StringUtils.isEmpty(label), "A non-empty label is required");
            Assert.isTrue(!StringUtils.isEmpty(namespace), "A namespace URI is required");
            this.label = label;
            this.namespace = namespace;
        }

        @JsonProperty("namespace")
        @JacksonXmlProperty(localName = "NAMESPACE")
        @NotEmpty
        @URL
        public String getNamespace()
        {
            return namespace;
        }

        public void setNamespace(String namespace)
        {
            this.namespace = namespace;
        }

        @JsonProperty("label")
        @JacksonXmlProperty(localName = "LABEL")
        @NotEmpty
        public String getLabel()
        {
            return label;
        }

        public void setLabel(String label)
        {
            this.label = label;
        }

        @Override
        public String toString()
        {
            return String.format("Prefix [%s=%s]", label, namespace);
        }

        @Override
        public int hashCode()
        {
            return namespace == null? 0 : namespace.hashCode();
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (!(obj instanceof Prefix))
                return false;
            Prefix other = (Prefix) obj;
            return (namespace == null)? 
                (other.namespace == null) : namespace.equals(other.namespace);
        }

        @Override
        public int compareTo(Prefix other)
        {
            if (namespace == null)
                return other.namespace == null? 0 : -1;
            return other.namespace == null? 1 : namespace.compareTo(other.namespace);
        }
    }
   
    protected static class InputSpec implements Serializable
    {
        static final long serialVersionUID = 1L;
        
        static final String BLANK_FILTER = "";
        
        String id;
        
        String varName;
        
        Integer pageSize = -1;
        
        List<String> filterExprs = Collections.singletonList(BLANK_FILTER);
        
        List<String> propertyExprs = Collections.emptyList();
        
        EnumDataFormat dataFormat = EnumDataFormat.N_TRIPLES;
        
        InputSpec() {}
        
        InputSpec(
            String id, 
            String varName, 
            List<String> propertyExprs, 
            List<String> filterExprs, 
            EnumDataFormat dataFormat) 
        {
            Assert.isTrue(!StringUtils.isEmpty(id), "Expected a non-empty input identifier");
            Assert.isTrue(!StringUtils.isEmpty(varName), "Expected a non-empty variable name");
            Assert.isTrue(!propertyExprs.isEmpty(), "Expected a non-empty list of property expressions");
            Assert.isTrue(propertyExprs.stream().noneMatch(StringUtils::isEmpty), 
                "A property expession cannot be empty");
            Assert.notNull(dataFormat, "A data format is required");
            
            this.id = id;
            this.varName = varName;
            this.propertyExprs = Collections.unmodifiableList(propertyExprs);
            this.dataFormat = dataFormat;
            
            // Note: filter expressions need a somewhat special care because Limes expects
            // a single blank filter when actually no filters apply (see DTD declaration) 
            if (filterExprs != null && !filterExprs.isEmpty())
                this.filterExprs = Collections.unmodifiableList(filterExprs);
        }
        
        InputSpec(String id, String varName, List<String> propertyExprs, EnumDataFormat dataFormat)
        {
            this(id, varName, propertyExprs, null, dataFormat);
        }
    }
    
    @JsonPropertyOrder({ 
        "id", "ID",
        "endpoint", "ENDPOINT",
        "var", "VAR",
        "pageSize", "PAGESIZE",
        "restrictions", "RESTRICTION",
        "properties", "PROPERTY",
        "dataFormat", "TYPE",
    })
    public static class Input implements Serializable
    {        
        private static final long serialVersionUID = 1L;
        
        @JsonIgnore
        InputSpec spec;
        
        String path;
        
        Input() 
        {
            this.spec = new InputSpec();
        }
        
        Input(
            String id, 
            String path, 
            String varName, 
            List<String> propertyExprs,
            List<String> filterExprs,
            EnumDataFormat dataFormat) 
        {
            this.spec = new InputSpec(id, varName, propertyExprs, filterExprs, dataFormat);
            this.path = path;
        }
        
        Input(String path, InputSpec spec)
        {
            this(spec.id, path, spec.varName, spec.propertyExprs, spec.filterExprs, spec.dataFormat);
        }
        
        @JsonProperty("endpoint")
        @JacksonXmlProperty(localName = "ENDPOINT")
        public String getPath()
        {
            return path;
        }
        
        @JsonProperty("endpoint")
        @JacksonXmlProperty(localName = "ENDPOINT")
        public void setPath(String path)
        {
            this.path = path;
        }

        @JsonProperty("id")
        @JacksonXmlProperty(localName = "ID")
        @NotEmpty
        public String getId()
        {
            return spec.id;
        }
        
        @JsonProperty("id")
        @JacksonXmlProperty(localName = "ID")
        public void setId(String id)
        {
            this.spec.id = id;
        }
        
        @JsonProperty("var")
        @JacksonXmlProperty(localName = "VAR")
        @NotEmpty
        @Pattern(regexp = VAR_NAME_REGEXP)
        public String getVarName()
        {
            return spec.varName;
        }
        
        @JsonProperty("var")
        @JacksonXmlProperty(localName = "VAR")
        public void setVarName(String varName)
        {
            this.spec.varName = varName;
        }
        
        @JsonProperty("pageSize")
        @JacksonXmlProperty(localName = "PAGESIZE")
        public Integer getPageSize()
        {
            return spec.pageSize;
        }
        
        @JsonProperty("pageSize")
        @JacksonXmlProperty(localName = "PAGESIZE")
        public void setPageSize(Integer pageSize)
        {
            this.spec.pageSize = pageSize;
        }
        
        @JsonProperty("restrictions")
        @JacksonXmlProperty(localName = "RESTRICTION")
        @JacksonXmlElementWrapper(useWrapping = false)
        public List<String> getFilterExprs()
        {
            return spec.filterExprs;
        }
        
        @JsonProperty("restrictions")
        @JacksonXmlProperty(localName = "RESTRICTION")
        public void setFilterExpr(List<String> filterExprs)
        {
            this.spec.filterExprs = filterExprs;
        }
        
        @JsonProperty("properties")
        @JacksonXmlProperty(localName = "PROPERTY")
        @JacksonXmlElementWrapper(useWrapping = false)
        @NotEmpty
        public List<String> getPropertyExprs()
        {
            return spec.propertyExprs;
        }
        
        @JsonProperty("properties")
        @JacksonXmlProperty(localName = "PROPERTY")
        public void setPropertyExprs(List<String> propertyExprs)
        {
            this.spec.propertyExprs = propertyExprs;
        }
        
        @JsonProperty("dataFormat")
        @JacksonXmlProperty(localName = "TYPE")
        @NotNull
        String getDataFormatAsString()
        {
            return DataFormat.from(spec.dataFormat).key();
        }
        
        @JsonProperty("dataFormat")
        @JacksonXmlProperty(localName = "TYPE")
        void setDataFormatFromString(String key)
        {
            DataFormat f = DataFormat.from(key);
            Assert.notNull(f, "The key [" + key + "] does not map to a data format");
            this.spec.dataFormat = f.dataFormat();
        }
        
        @JsonIgnore
        public EnumDataFormat getDataFormat()
        {
            return spec.dataFormat;
        }
    }
    
    @JsonPropertyOrder({
        "threshold", "THRESHOLD",
        "file", "FILE",
        "relation", "RELATION",
    })
    public static class Output implements Serializable
    {
        static final long serialVersionUID = 1L;
        
        static final String DEFAULT_RELATION = "owl:sameAs";
        
        Double threshold;
       
        String path;
        
        String relation;
        
        Output() {}
        
        Output(double threshold, String path, String relation) 
        {
            this.threshold = threshold;
            this.path = path;
            this.relation = StringUtils.isEmpty(relation)? DEFAULT_RELATION : relation;
        }
        
        Output(double threshold, Path path, String relation)
        {
            this(threshold, path.toString(), relation);
        }
        
        Output withAbsolutePath(String dir)
        {
            if (Paths.get(path).isAbsolute()) {
                return this;
            } else {
                final Path dirPath = Paths.get(dir);
                Assert.isTrue(dirPath.isAbsolute(), "Expected an absolute path");
                return new Output(threshold, dirPath.resolve(path).toString(), relation);
            }
        }
       
        @JsonProperty("threshold")
        @JacksonXmlProperty(localName = "THRESHOLD")
        @NotNull
        @Range(min = 0, max = 1)
        public Double getThreshold()
        {
            return threshold;
        }
        
        @JsonProperty("threshold")
        @JacksonXmlProperty(localName = "THRESHOLD")
        public void setThreshold(Double threshold)
        {
            this.threshold = threshold;
        }
        
        @JsonProperty("file")
        @JacksonXmlProperty(localName = "FILE")
        @NotEmpty
        public String getPath()
        {
            return path;
        }
        
        @JsonProperty("file")
        @JacksonXmlProperty(localName = "FILE")
        public void setPath(String path)
        {
            this.path = path;
        }

        @JsonProperty("relation")
        @JacksonXmlProperty(localName = "RELATION")
        @NotEmpty
        public String getRelation()
        {
            return relation;
        }
        
        @JsonProperty("relation")
        @JacksonXmlProperty(localName = "RELATION")
        public void setRelation(String relation)
        {
            this.relation = relation;
        }
    }
    
    @JsonPropertyOrder({
        "rewriter", "REWRITER",
        "planner", "PLANNER",
        "engine", "ENGINE"
    })
    public static class Execution implements Serializable
    {
        static final long serialVersionUID = 1L;
        
        @JsonProperty("rewriter")
        @JacksonXmlProperty(localName = "REWRITER")
        String rewriterName = "default";
        
        @JsonProperty("planner")
        @JacksonXmlProperty(localName = "PLANNER")
        String plannerName = "default";
        
        @JsonProperty("engine")
        @JacksonXmlProperty(localName = "ENGINE")
        String engineName = "default";

        Execution() {}
        
        public Execution(String rewriterName, String plannerName, String engineName)
        {
            this.rewriterName = rewriterName;
            this.plannerName = plannerName;
            this.engineName = engineName;
        }
        
        public String getRewriterName()
        {
            return rewriterName;
        }
        
        public String getPlannerName()
        {
            return plannerName;
        }
        
        public String getEngineName()
        {
            return engineName;
        }
    }
    
    /**
     * A list of aliased XML namespaces
     */
    private TreeSet<Prefix> prefixes;
    
    /**
     * The specification for the "source" (i.e the first) part of the input
     */
    private InputSpec sourceSpec;
    
    /**
     * The specification for the "target" (i.e the second) part of the input
     */
    private InputSpec targetSpec;
    
    /**
     * An expression for a distance metric
     */
    private String metricExpr;
    
    /**
     * The specification for output of accepted pairs
     */
    private Output accepted;
    
    /**
     * The specification for output of pairs that must be reviewed
     */
    private Output review;
    
    /**
     * Configuration for linking engine
     */
    private Execution execution = new Execution();
    
    public LimesConfiguration() 
    {
        this._version = VERSION;
        
        this.input = new ArrayList<>(Arrays.asList(null, null));
        this.inputFormat = EnumDataFormat.N_TRIPLES;
        this.outputFormat = EnumDataFormat.N_TRIPLES;
        
        this.prefixes = new TreeSet<>();
        this.prefixes.add(new Prefix("owl", OWL_NAMESPACE_URI));
        this.prefixes.add(new Prefix("slipo", SLIPO_ONTOLOGY_NAMESPACE_URI));
    }
    
    @JsonIgnore
    @Override
    public Class<Limes> getToolType()
    {
        return Limes.class;
    }
    
    @Override
    @JsonIgnore
    public String getVersion()
    {
        return _version;
    }
    
    @JsonProperty("prefixes")
    @JacksonXmlProperty(localName = "PREFIX")
    @JacksonXmlElementWrapper(useWrapping = false)
    public NavigableSet<Prefix> getPrefixes()
    {
        return prefixes;
    }
    
    @JsonProperty("prefixes")
    @JacksonXmlProperty(localName = "PREFIX")
    public void setPrefixes(Collection<Prefix> prefixes)
    {
        this.prefixes = new TreeSet<>(prefixes);
    }
   
    public void addPrefix(String label, URI namespace)
    {
        Assert.notNull(label, "Expected a non-null label");
        Assert.notNull(namespace, "Expected a non-null namespace URI");
        this.prefixes.add(new Prefix(label, namespace.toString()));
    }
    
    public void addPrefix(String label, String namespaceUri)
    {
        addPrefix(label, URI.create(namespaceUri));
    }
    
    @JsonProperty("source")
    @JacksonXmlProperty(localName = "SOURCE")
    @NotNull
    @Valid
    public Input getSource()
    {
        return new Input(input.get(SOURCE_INDEX), sourceSpec);
    }
    
    @JsonProperty("source")
    @JacksonXmlProperty(localName = "SOURCE")
    @JsonInclude(Include.NON_NULL)
    protected void setSource(Input source)
    {
        Assert.notNull(source, "Expected a non-null source input");
        this.sourceSpec = source.spec;
        this.inputFormat = source.spec.dataFormat;
        
        if (!StringUtils.isEmpty(source.path))
            this.input.set(SOURCE_INDEX, source.path);
    }
    
    @JsonIgnore
    @Override
    public EnumDataFormat getInputFormat()
    {
        return this.inputFormat;
    }
    
    @JsonIgnore
    @AssertTrue
    protected boolean isValidInputFormat()
    {
        return inputFormat == null || inputFormat == sourceSpec.dataFormat;
    }
    
    @JsonIgnore
    @Override
    public void setInputFormat(EnumDataFormat inputFormat)
    {
        this.inputFormat = inputFormat;
    }
    
    @JsonIgnore
    public void setSource(String id, String path, String varName, String propertyExpr, EnumDataFormat dataFormat)
    {
       this.sourceSpec = new InputSpec(id, varName, Collections.singletonList(propertyExpr), dataFormat);
       this.input.set(SOURCE_INDEX, path);
    }
    
    @JsonIgnore
    public void setSource(String id, String path, String varName, String propertyExpr)
    {
        setSource(id, path, varName, propertyExpr, EnumDataFormat.N_TRIPLES);
    }

    @JsonProperty("target")
    @JacksonXmlProperty(localName = "TARGET")
    @NotNull
    @Valid
    public Input getTarget()
    {
        return new Input(input.get(TARGET_INDEX), targetSpec);
    }
    
    @JsonProperty("target")
    @JacksonXmlProperty(localName = "TARGET")
    @JsonInclude(Include.NON_NULL)
    protected void setTarget(Input target)
    {
        Assert.notNull(target, "Expected a non-null source input");
        this.targetSpec = target.spec;
        
        if (!StringUtils.isEmpty(target.path))
            this.input.set(TARGET_INDEX, target.path);
    }
    
    @JsonIgnore
    public void setTarget(String id, String path, String varName, String propertyExpr, EnumDataFormat dataFormat)
    {
        this.targetSpec = new InputSpec(id, varName, Collections.singletonList(propertyExpr), dataFormat);
        this.input.set(TARGET_INDEX, path);
    }
    
    @JsonIgnore
    public void setTarget(String id, String path, String varName, String propertyExpr)
    {
        setTarget(id, path, varName, propertyExpr, EnumDataFormat.N_TRIPLES);
    }
    
    @JsonProperty("input.source")
    @JsonSetter
    public void setSourcePath(String path)
    {
        this.input.set(SOURCE_INDEX, path);
    }
    
    @JsonIgnore
    public String getSourcePath()
    {
        return this.input.get(SOURCE_INDEX);
    }
    
    @JsonIgnore
    public String getTargetPath()
    {
        return this.input.get(TARGET_INDEX);
    }
    
    @JsonProperty("input.target")
    @JsonSetter
    public void setTargetPath(String path)
    {
        this.input.set(TARGET_INDEX, path);
    }
    
    @JsonProperty("input")
    @JsonSetter
    @JsonInclude(Include.NON_NULL)
    protected void setInput(Object input)
    {
        Assert.notNull(input, "A non-null object is expected");
        if (input instanceof Map) {
            // Treat as an input map
            this.setInput((Map<?, ?>) input);
        } else if (input instanceof List) {
            // Treat as an input list
            this.setInput(Lists.transform((List<?>) input, Object::toString));
        } else {
            // Treat as a joined list of inputs
            this.setInput(input.toString());
        }
    }
   
    @JsonIgnore
    @Override
    public List<String> getInput()
    {
        return Collections.unmodifiableList(input);
    }
    
    @JsonIgnore
    public void setInput(Map<?, ?> inputMap)
    {
        if (inputMap.containsKey("source")) {
            setSourcePath(inputMap.get("source").toString());
        }
        if (inputMap.containsKey("target")) {
            setTargetPath(inputMap.get("target").toString());
        }
    }
    
    @JsonIgnore
    @Override
    public void setInput(String inputAsString)
    {
        // Treat as a colon-separated list of paths
        String[] inputPaths = inputAsString.toString().split(File.pathSeparator);
        Assert.isTrue(inputPaths.length == 2, "Expected a pair of input files");
        setSourcePath(inputPaths[0]);
        setTargetPath(inputPaths[1]);
    }
    
    @JsonIgnore
    @Override
    public void setInput(List<String> inputList)
    {
        Assert.notNull(inputList, "A non-null list of inputs is required");
        Assert.isTrue(inputList.size() == 2, "Expected a pair of input files");
        setSourcePath(inputList.get(0).toString());
        setTargetPath(inputList.get(1).toString());
    }
    
    @Override
    public void clearInput()
    {
        setSourcePath(null);
        setTargetPath(null);
    }
    
    @JsonProperty("metric")
    @JacksonXmlProperty(localName = "METRIC")
    @NotEmpty
    public String getMetric()
    {
        return metricExpr;
    }
    
    @JsonProperty("metric")
    @JacksonXmlProperty(localName = "METRIC")
    @JsonInclude(Include.NON_NULL)
    public void setMetric(String expression)
    {
        Assert.isTrue(!StringUtils.isEmpty(expression), "Expected a non-empty metric expression");
        this.metricExpr = expression;
    }
    
    @AssertTrue(message = "The metric expression cannot be parsed")
    protected boolean isValidMetricExpression()
    {
        boolean b = true;
        try {
            spelParser.parseExpression(metricExpr);
        } catch (ParseException e) {
            b = false;
        }
        return b;
    }
    
    @JsonIgnore
    @Override
    public String getOutputDir()
    {
        return outputDir;
    }
    
    @JsonProperty("outputDir")
    @JsonSetter
    @JsonInclude(Include.NON_NULL)
    public void setOutputDir(String dir)
    {
        Assert.isTrue(!StringUtils.isEmpty(dir), "Expected a non-empty directory path");
        this.outputDir = Paths.get(dir).toString();
    }
    
    @JsonIgnore
    @AssertTrue
    protected boolean isOutputDirAbsolute()
    {
        return outputDir == null || Paths.get(outputDir).isAbsolute();
    }
    
    @JsonIgnore
    @NotNull
    @Override
    public EnumDataFormat getOutputFormat()
    {
        return outputFormat;
    }
    
    @JsonIgnore
    public void setOutputFormat(EnumDataFormat format)
    {
        this.outputFormat = format;
    }
    
    @JsonIgnore
    @AssertTrue
    protected boolean isOutputFormatParWithExtensions()
    {
        if (outputFormat != null) {
            String extension = outputFormat.getFilenameExtension();
            if (accepted != null) {
                if (!extension.equals(StringUtils.getFilenameExtension(accepted.path)))
                    return false;
            }
            if (review != null) {
                if (!extension.equals(StringUtils.getFilenameExtension(review.path)))
                    return false;
            }
        }
        return true;
    }
    
    @JsonIgnore
    @Override
    public Map<EnumOutputType, List<String>> getOutputNames()
    {
        Assert.state(accepted != null, "The output spec for `accepted` is null");
        Assert.state(accepted.path != null, "The path for `accepted` is null");
        Assert.state(review != null, "The output spec for `review` is null");
        Assert.state(review.path != null, "The path for `review` is null");
        
        String acceptedFileName = Paths.get(accepted.path).getFileName().toString();
        String reviewFileName = Paths.get(review.path).getFileName().toString();
        
        return Collections.singletonMap(
            EnumOutputType.OUTPUT, Arrays.asList(acceptedFileName, reviewFileName));
    }
    
    @JsonIgnore
    @Override
    public OutputNameMapper<Limes> getOutputNameMapper()
    {
        Assert.state(accepted != null, "The output spec for `accepted` is null");
        Assert.state(accepted.path != null, "The path for `accepted` is null");
        Assert.state(review != null, "The output spec for `review` is null");
        Assert.state(review.path != null, "The path for `review` is null");
        
        final Function<String, String> getName = p -> Paths.get(p).getFileName().toString();
        final Map<EnumLimesOutputPart, List<String>> outputMap = new EnumMap<>(EnumLimesOutputPart.class);
        
        outputMap.put(EnumLimesOutputPart.ACCEPTED, 
            Collections.singletonList(getName.apply(accepted.path)));
        outputMap.put(EnumLimesOutputPart.REVIEW, 
            Collections.singletonList(getName.apply(review.path)));
        
        return input -> outputMap;
    }
    
    @JsonProperty("acceptance")
    @JacksonXmlProperty(localName = "ACCEPTANCE")
    @NotNull
    @Valid
    public Output getAccepted()
    {
        return (outputDir == null || accepted == null)? accepted : accepted.withAbsolutePath(outputDir);
    }
    
    @JsonIgnore
    public String getAcceptedPath()
    {
        Output a = this.getAccepted();
        return a == null? null : a.path;
    }
    
    @JsonProperty("acceptance")
    @JacksonXmlProperty(localName = "ACCEPTANCE")
    @JsonInclude(Include.NON_NULL)
    protected void setAccepted(Output accepted)
    {
        Assert.notNull(accepted, "Expected a specification for output of accepted pairs");
        this.accepted = accepted;
    }
    
    @JsonIgnore
    public void setAccepted(double threshold, String fileName, String relation)
    {
        Assert.isTrue(!StringUtils.isEmpty(fileName), "A non-empty file name is expected");
        Assert.isTrue(Paths.get(fileName).getNameCount() == 1, "A plain file name is expected");
        this.accepted = new Output(threshold, fileName, relation);
    }
    
    @JsonIgnore
    public void setAccepted(double threshold, String path)
    {
        setAccepted(threshold, path, null);
    }
    
    @JsonProperty("review")
    @JacksonXmlProperty(localName = "REVIEW")
    @NotNull
    @Valid
    public Output getReview()
    {
        return (outputDir == null || review == null)? review : review.withAbsolutePath(outputDir);
    }
    
    @JsonIgnore
    public String getReviewPath()
    {
        Output r = this.getReview();
        return r == null? null : r.path;
    }
    
    @JsonProperty("review")
    @JacksonXmlProperty(localName = "REVIEW")
    @JsonInclude(Include.NON_NULL)
    protected void setReview(Output review)
    {
        Assert.notNull(review, "Expected a specification for output of pairs to be reviewed");
        this.review = review;
    }
    
    @JsonIgnore
    public void setReview(double threshold, String fileName, String relation)
    {
        Assert.isTrue(fileName != null && Paths.get(fileName).getNameCount() == 1, 
            "A non-null plain file name is expected");
        this.review = new Output(threshold, fileName, relation);
    }
    
    @JsonIgnore
    public void setReview(double threshold, String path)
    {
        setReview(threshold, path, Output.DEFAULT_RELATION);
    }
    
    @JsonProperty("outputFormat")
    @JacksonXmlProperty(localName = "OUTPUT")
    @NotEmpty
    public String getOutputFormatAsString()
    {
        return DataFormat.from(outputFormat).key();
    }
    
    @JsonProperty("outputFormat")
    @JacksonXmlProperty(localName = "OUTPUT")
    @JsonInclude(Include.NON_NULL)
    public void setOutputFormatFromString(String key)
    {
        DataFormat f = DataFormat.from(key);
        Assert.notNull(f, "The key [" + key + "] does not map to a data format");
        this.outputFormat = f.dataFormat();
    }
    
    @JsonProperty("execution")
    @JacksonXmlProperty(localName = "EXECUTION")
    public Execution getExecutionParams()
    {
        return execution;
    }
    
    @JsonProperty("execution")
    @JacksonXmlProperty(localName = "EXECUTION")
    protected void setExecutionParams(Execution p)
    {
        this.execution = p;
    }
    
    @JsonIgnore
    public void setExecutionParams(String rewriterName, String plannerName, String engineName)
    {
        this.execution = new Execution(rewriterName, plannerName, engineName);
    }
}
