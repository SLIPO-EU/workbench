package eu.slipo.workbench.common.model.tool;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.Range;
import org.hibernate.validator.constraints.URL;
import org.springframework.expression.ParseException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import eu.slipo.workbench.common.model.poi.EnumDataFormat;

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
public class LimesConfiguration extends AbstractToolConfiguration 
{
    private static final long serialVersionUID = 1L;

    private static final SpelExpressionParser spelParser = new SpelExpressionParser();
    
    private enum DataFormat
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
    public static class Prefix
    {
        String label;
        
        String namespace;

        Prefix() {}
        
        public Prefix(String label, String namespace)
        {
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
    public static class InputSpec
    {
        static final String VAR_NAME_REGEXP = "^[?]\\w[0-9\\w]*$";
                
        String id;
        
        String endpoint;
        
        String varName;
        
        Integer pageSize = -1;
        
        List<String> filterExprs = Collections.emptyList();
        
        List<String> propertyExprs = Collections.emptyList();
        
        EnumDataFormat dataFormat = EnumDataFormat.N_TRIPLES;
        
        InputSpec() {}
        
        InputSpec(String id, Path path, String varName, String propertyExpr, EnumDataFormat dataFormat) 
        {
            Assert.isTrue(!StringUtils.isEmpty(id), "Expected a non-empty input identifier");
            Assert.isTrue(path != null && path.isAbsolute(), "Expected an absolute path");
            Assert.isTrue(!StringUtils.isEmpty(varName), "Expected a non-empty variable name");
            Assert.isTrue(!StringUtils.isEmpty(propertyExpr), "Expected a non-empty property expression");
            Assert.notNull(dataFormat, "A data format is required");
            this.id = id;
            this.endpoint = path.toString();
            this.varName = varName;
            this.propertyExprs = Collections.singletonList(propertyExpr);
            this.dataFormat = dataFormat;
        }
        
        @JsonProperty("id")
        @JacksonXmlProperty(localName = "ID")
        @NotEmpty
        public String getId()
        {
            return id;
        }
        
        @JsonProperty("id")
        @JacksonXmlProperty(localName = "ID")
        public void setId(String id)
        {
            this.id = id;
        }
        
        @JsonProperty("endpoint")
        @JacksonXmlProperty(localName = "ENDPOINT")
        @NotEmpty
        public String getEndpoint()
        {
            return endpoint;
        }
        
        @JsonProperty("endpoint")
        @JacksonXmlProperty(localName = "ENDPOINT")
        public void setEndpoint(String endpoint)
        {
            this.endpoint = endpoint;
        }
        
        @JsonIgnore
        public void setEndpoint(Path inputPath)
        {
            this.endpoint = inputPath.toString();
        }
        
        @JsonProperty("var")
        @JacksonXmlProperty(localName = "VAR")
        @NotEmpty
        @Pattern(regexp = VAR_NAME_REGEXP)
        public String getVarName()
        {
            return varName;
        }
        
        @JsonProperty("var")
        @JacksonXmlProperty(localName = "VAR")
        public void setVarName(String varName)
        {
            this.varName = varName;
        }
        
        @JsonProperty("pageSize")
        @JacksonXmlProperty(localName = "PAGESIZE")
        public Integer getPageSize()
        {
            return pageSize;
        }
        
        @JsonProperty("pageSize")
        @JacksonXmlProperty(localName = "PAGESIZE")
        public void setPageSize(Integer pageSize)
        {
            this.pageSize = pageSize;
        }
        
        @JsonProperty("restrictions")
        @JacksonXmlProperty(localName = "RESTRICTION")
        @JacksonXmlElementWrapper(useWrapping = false)
        public List<String> getFilterExprs()
        {
            return filterExprs;
        }
        
        @JsonProperty("restrictions")
        @JacksonXmlProperty(localName = "RESTRICTION")
        public void setFilterExpr(List<String> filterExprs)
        {
            this.filterExprs = filterExprs;
        }
        
        @JsonProperty("properties")
        @JacksonXmlProperty(localName = "PROPERTY")
        @JacksonXmlElementWrapper(useWrapping = false)
        @NotEmpty
        public List<String> getPropertyExprs()
        {
            return propertyExprs;
        }
        
        @JsonProperty("properties")
        @JacksonXmlProperty(localName = "PROPERTY")
        public void setPropertyExprs(List<String> propertyExprs)
        {
            this.propertyExprs = propertyExprs;
        }
        
        @JsonProperty("dataFormat")
        @JacksonXmlProperty(localName = "TYPE")
        @NotNull
        String getDataFormatAsString()
        {
            return DataFormat.from(dataFormat).key();
        }
        
        @JsonProperty("dataFormat")
        @JacksonXmlProperty(localName = "TYPE")
        void setDataFormatFromString(String key)
        {
            DataFormat f = DataFormat.from(key);
            Assert.notNull(f, "The key [" + key + "] does not map to a data format");
            this.dataFormat = f.dataFormat();
        }
        
        /**
         * Check that the given endpoint is given as an absolute path. 
         * Note: This is a limitation applied by SLIPO workbench (not the tool itself).
         */
        @AssertTrue
        boolean isEndpointGivenAsAbsolutePath()
        {
            return !StringUtils.isEmpty(endpoint) && Paths.get(endpoint).isAbsolute();
        }
    }
    
    @JsonPropertyOrder({
        "threshold", "THRESHOLD",
        "file", "FILE",
        "relation", "RELATION",
    })
    public static class OutputSpec
    {
        static final String DEFAULT_RELATION = "owl:sameAs";
        
        Double threshold;
       
        String path;
        
        String relation;
        
        OutputSpec() {}
        
        OutputSpec(double threshold, String path, String relation) 
        {
            Assert.isTrue(threshold > 0 && threshold < 1, "A threshold is expected as a number in (0, 1)");
            Assert.isTrue(!StringUtils.isEmpty(path), "Expected a non-empty path");
            Assert.isTrue(!StringUtils.isEmpty(relation), "Expected a non-empty relation");
            this.threshold = threshold;
            this.path = path;
            this.relation = relation;
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
        
        @AssertTrue
        boolean isAbsolutePath()
        {
            return !StringUtils.isEmpty(path) && Paths.get(path).isAbsolute();
        }
    }
    
    @JsonPropertyOrder({
        "rewriter", "REWRITER",
        "planner", "PLANNER",
        "engine", "ENGINE"
    })
    public static class Execution
    {
        @JsonProperty("rewriter")
        @JacksonXmlProperty(localName = "REWRITER")
        String rewriterName = "default";
        
        @JsonProperty("planner")
        @JacksonXmlProperty(localName = "PLANNER")
        String plannerName = "default";
        
        @JsonProperty("engine")
        @JacksonXmlProperty(localName = "ENGINE")
        String engineName = "default";
    }
    
    /**
     * A list of aliased XML namespaces
     */
    private List<Prefix> prefixes;
    
    /**
     * The specification for the "source" (i.e the first) part of the input
     */
    private InputSpec source;
    
    /**
     * The specification for the "target" (i.e the second) part of the input
     */
    private InputSpec target;
    
    /**
     * An expression for a distance metric
     */
    private String metricExpr;
    
    /**
     * The specification for output of accepted pairs
     */
    private OutputSpec accepted;
    
    /**
     * The specification for output of pairs that must be reviewed
     */
    private OutputSpec review;
    
    /**
     * Configuration for linking engine
     */
    private Execution execution = new Execution();
    
    public LimesConfiguration() 
    {
        this.input = new ArrayList<>(Arrays.asList(null, null));
        this.inputFormat = EnumDataFormat.N_TRIPLES;
        this.outputFormat = EnumDataFormat.N_TRIPLES;
        
        this.prefixes = new ArrayList<>();
        this.prefixes.add(new Prefix("owl", "http://www.w3.org/2002/07/owl#"));
        this.prefixes.add(new Prefix("slipo", "http://slipo.eu/def#"));
    }
    
    @JsonProperty("prefixes")
    @JacksonXmlProperty(localName = "PREFIX")
    @JacksonXmlElementWrapper(useWrapping = false)
    public List<Prefix> getPrefixes()
    {
        return prefixes;
    }
   
    public void addPrefix(String label, URI namespace)
    {
        Assert.notNull(label, "Expected a non-null label");
        Assert.notNull(namespace, "Expected a non-null namespace URI");
        this.prefixes.add(new Prefix(label, namespace.toString()));
    }
    
    @JsonProperty("source")
    @JacksonXmlProperty(localName = "SOURCE")
    @NotNull
    @Valid
    public InputSpec getSource()
    {
        return source;
    }
    
    @JsonProperty("source")
    @JacksonXmlProperty(localName = "SOURCE")
    @JsonInclude(Include.NON_NULL)
    public void setSource(InputSpec source)
    {
        Assert.notNull(source, "Expected a non-null source input specification");
        this.source = source;
        this.input.set(0, source.endpoint);
        this.inputFormat = source.dataFormat;
    }
    
    @JsonIgnore
    public void setSource(String id, Path path, String varName, String propertyExpr, EnumDataFormat dataFormat)
    {
       InputSpec source = new InputSpec(id, path, varName, propertyExpr, dataFormat);
       setSource(source);
    }
    
    @JsonIgnore
    public void setSource(String id, Path path, String varName, String propertyExpr)
    {
        setSource(id, path, varName, propertyExpr, EnumDataFormat.N_TRIPLES);
    }

    @JsonProperty("target")
    @JacksonXmlProperty(localName = "TARGET")
    @NotNull
    @Valid
    public InputSpec getTarget()
    {
        return target;
    }
    
    @JsonProperty("target")
    @JacksonXmlProperty(localName = "TARGET")
    @JsonInclude(Include.NON_NULL)
    public void setTarget(InputSpec target)
    {
        Assert.notNull(target, "Expected a non-null target input specification");
        this.target = target;
        this.input.set(1, target.endpoint);
    }
    
    @JsonIgnore
    public void setTarget(String id, Path path, String varName, String propertyExpr, EnumDataFormat dataFormat)
    {
        InputSpec target = new InputSpec(id, path, varName, propertyExpr, dataFormat);  
        setTarget(target);
    }
    
    @JsonIgnore
    public void setTarget(String id, Path path, String varName, String propertyExpr)
    {
        setTarget(id, path, varName, propertyExpr, EnumDataFormat.N_TRIPLES);
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
    boolean isMetricExpressionValid()
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
    public String getOutputDir()
    {
        return outputDir;
    }
    
    @JsonIgnore
    public void setOutputDir(String dir)
    {
        Assert.isTrue(!StringUtils.isEmpty(dir), "Expected a non-empty directory path");
        Path dirPath = Paths.get(dir);
        this.outputDir = dirPath.toString();
        
        // If outputs are specified as relative paths, resolve against output directory
        
        if (this.accepted != null) {
           this.accepted.path = dirPath.resolve(this.accepted.path).toString();
        }
        
        if (this.review != null) {
            this.review.path = dirPath.resolve(this.review.path).toString();
        }
    }
    
    @JsonIgnore
    @NotNull
    public EnumDataFormat getOutputFormat()
    {
        return outputFormat;
    }
    
    @JsonIgnore
    public void setOutputFormat(EnumDataFormat format)
    {
        this.outputFormat = format;
    }
    
    @JsonProperty("acceptance")
    @JacksonXmlProperty(localName = "ACCEPTANCE")
    @NotNull
    @Valid
    public OutputSpec getAccepted()
    {
        return accepted;
    }
    
    @JsonProperty("acceptance")
    @JacksonXmlProperty(localName = "ACCEPTANCE")
    @JsonInclude(Include.NON_NULL)
    public void setAccepted(OutputSpec accepted)
    {
        Assert.notNull(accepted, "Expected a specification for output of accepted pairs");
        this.accepted = accepted;
    }
    
    @JsonIgnore
    public void setAccepted(double threshold, Path path, String relation)
    {
        if (outputDir != null)
            path = Paths.get(outputDir).resolve(path);
        this.accepted = new OutputSpec(threshold, path.toString(), relation);
    }
    
    @JsonIgnore
    public void setAccepted(double threshold, Path path)
    {
        setAccepted(threshold, path, OutputSpec.DEFAULT_RELATION);
    }
    
    @JsonProperty("review")
    @JacksonXmlProperty(localName = "REVIEW")
    @NotNull
    @Valid
    public OutputSpec getReview()
    {
        return review;
    }
    
    @JsonProperty("review")
    @JacksonXmlProperty(localName = "REVIEW")
    @JsonInclude(Include.NON_NULL)
    public void setReview(OutputSpec review)
    {
        Assert.notNull(review, "Expected a specification for output of pairs to be reviewed");
        this.review = review;
    }
    
    @JsonIgnore
    public void setReview(double threshold, Path path, String relation)
    {
        if (outputDir != null)
            path = Paths.get(outputDir).resolve(path);
        this.review = new OutputSpec(threshold, path.toString(), relation);
    }
    
    @JsonIgnore
    public void setReview(double threshold, Path path)
    {
        setReview(threshold, path, OutputSpec.DEFAULT_RELATION);
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
    public void setExecutionParams(Execution p)
    {
        this.execution = p;
    }
}
