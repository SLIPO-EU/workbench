package eu.slipo.workbench.common.model.tool;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.common.model.tool.output.EnumFagiOutputPart;
import eu.slipo.workbench.common.model.tool.output.InputToOutputNameMapper;
import eu.slipo.workbench.common.model.tool.output.OutputPart;
import eu.slipo.workbench.common.model.tool.output.OutputSpec;

/**
 * Configuration for FAGI
 */
@JsonPropertyOrder({
    "inputFormat", "outputFormat", "locale", "similarity", "verbose", "rulesLocation",
    "left", "right", "links", "target"
})
@JacksonXmlRootElement(localName = "specification")
public class FagiConfiguration extends FuseConfiguration<Fagi> 
{
    private static final long serialVersionUID = 1L;

    /**
     * This class represents the configuration of a specific version
     */
    public static final String VERSION = "1.2";
    
    private static final int LEFT_INDEX = 0;
    
    private static final int RIGHT_INDEX = 1;
    
    private static final int LINKS_INDEX = 2;

    public enum Similarity
    {
        SORTED_JAROWINKLER("sortedjarowinkler"),
        JAROWINKLER("jarowinkler"),
        COSINE("cosine"),
        LEVENSHTEIN("levenshtein"),
        JARO("jaro"),
        TWOGRAM("2Gram"),
        LCS("longestcommonsubsequence");
        
        protected final String key;
        
        private Similarity(String key)
        {
            this.key = key;
        }
        
        public static Similarity fromKey(String key)
        {
            for (Similarity s: Similarity.values())
                if (s.key.equals(key) || s.name().equals(key))
                    return s;
            return null;
        }
    }
    
    public enum Mode
    {
        AA("aa_mode"),
        AB("ab_mode"),
        BA("ba_mode"),
        BB("bb_mode"),
        A("a_mode"),
        B("b_mode"),
        L("l_mode");
        
        protected final String key;
        
        private Mode(String key)
        {
            this.key = key;
        }
        
        public static Mode fromKey(String key)
        {
            for (Mode s: Mode.values())
                if (s.key.equalsIgnoreCase(key) || s.name().equalsIgnoreCase(key))
                    return s;
            return null;
        }
    }
    
    public enum DataFormat
    {
        N_TRIPLES(EnumDataFormat.N_TRIPLES, "NT"),
        TURTLE(EnumDataFormat.TURTLE, "TTL"),
        RDF_XML(EnumDataFormat.RDF_XML, "RDF");
        
        protected final EnumDataFormat dataFormat;

        protected final String key;

        private DataFormat(EnumDataFormat dataFormat, String key)
        {
            Assert.notNull(dataFormat, "Expected an enum constant for data format");
            Assert.notNull(key, "Expected a non-null key for data format " + dataFormat);
            this.dataFormat = dataFormat;
            this.key = key;
        }

        public String key()
        {
            return key;
        }

        public EnumDataFormat dataFormat()
        {
            return dataFormat;
        }

        public static DataFormat from(String key)
        {
            Assert.isTrue(!StringUtils.isEmpty(key), "Expected a non-empty key to search for");
            for (DataFormat e: DataFormat.values())
                if (e.name().equals(key) || e.key.equals(key))
                    return e;
            return null;
        }

        public static DataFormat from(EnumDataFormat dataFormat)
        {
            Assert.notNull(dataFormat, "Expected a non-null data format to search for");
            for (DataFormat e: DataFormat.values())
                if (e.dataFormat == dataFormat)
                    return e;
            return null;
        }
    }
    
    public enum LinkFormat
    {
        NT("nt"), 
        CSV("csv"), 
        CSV_UNIQUE_LINKS("csv-unique-links"),
        CSV_ENSEMBLES("csv-ensembles");
        
        private final String key;
        
        private LinkFormat(String key)
        {
            this.key = key;
        }
        
        public String key()
        {
            return this.key;
        }
        
        public static LinkFormat fromKey(String key)
        {
            Assert.isTrue(!StringUtils.isEmpty(key), "Expected a non-empty key to search for");
            for (LinkFormat f: LinkFormat.values())
                if (f.key.equals(key))
                    return f;
            return null;
        }
    }
    
    protected static class InputSpec implements Serializable
    {
        private static final long serialVersionUID = 1L;
        
        /**
         * An identifier for an input
         */
        String id;
        
        /**
         * The location for a file resource containing a classification for this input
         */
        String categoriesLocation;
        
        /**
         * The date of last update
         */
        LocalDate date;
        
        InputSpec() {}
        
        InputSpec(String id)
        {
            this.id = id;
        }
        
        InputSpec(String id, String categoriesLocation, LocalDate date)
        {
            this.id = id;
            this.categoriesLocation = categoriesLocation;
            this.date = date;
        }
    }
    
    @JsonPropertyOrder({ "id", "file", "categories", "date" })
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
        
        Input(String id, String path, String categoriesLocation, LocalDate date)
        {
            this.path = path;
            this.spec = new InputSpec(id, categoriesLocation, date);
        }
        
        Input(String id, String path)
        {
            this(id, path, null, null);
        }
        
        @JsonProperty("file")
        public String getPath()
        {
            return path;
        }
        
        @JsonProperty("file")
        public void setPath(String path)
        {
            this.path = path;
        }
        
        @JsonProperty("id")
        @NotEmpty
        public String getId()
        {
            return spec.id;
        }
        
        @JsonProperty("id")
        public void setId(String id)
        {
            this.spec.id = id;
        }
        
        @JsonProperty("categories")
        public String getCategoriesLocation()
        {
            return spec.categoriesLocation;
        }
        
        @JsonProperty("categories")
        public void setCategoriesLocation(String location)
        {
            this.spec.categoriesLocation = location;
        }
        
        @JsonProperty("date")
        public LocalDate getDate()
        {
            return spec.date;
        }
        
        @JsonProperty("date")
        public void setDate(LocalDate date)
        {
            this.spec.date = date;
        }
    }
    
    protected static class LinksSpec implements Serializable
    {
        private static final long serialVersionUID = 1L;
        
        String id;
        
        LinkFormat format = LinkFormat.NT;
        
        LinksSpec() {}
        
        LinksSpec(String id)
        {
            this.id = id;
        }
    }
    
    @JsonPropertyOrder({ "id", "linksFormat", "file" })
    public static class Links implements Serializable
    {
        private static final long serialVersionUID = 1L;
        
        @JsonIgnore
        LinksSpec spec;
        
        String path;
        
        Links()
        {
            this.spec = new LinksSpec();
        }
        
        Links(String id, String path)
        {
            this.path = path;
            this.spec = new LinksSpec(id);
        }
        
        @JsonProperty("linksFormat")
        public String getLinksFormatAsString()
        {
            return this.spec.format.key();
        }
        
        @JsonProperty("linksFormat")
        public void setLinksFormat(String key)
        {
            setLinksFormat(LinkFormat.fromKey(key));
        }
        
        @JsonIgnore
        public void setLinksFormat(LinkFormat format)
        {
            Assert.notNull(format, "Expected a non-null link format");
            this.spec.format = format;
        }
        
        @JsonProperty("file")
        public String getPath()
        {
            return path;
        }
        
        @JsonProperty("file")
        public void setPath(String path)
        {
            this.path = path;
        }
        
        @JsonProperty("id")
        @NotEmpty
        public String getId()
        {
            return spec.id;
        }
        
        @JsonProperty("id")
        public void setId(String id)
        {
            this.spec.id = id;
        }
    }
    
    @JsonPropertyOrder({ 
        "id", "mode", "outputDir", "fused", "remaining", "ambiguous", "statistics", "fusionLog" 
    })
    public static class Output implements Serializable
    {
        private static final long serialVersionUID = 1L;
        
        public static final String DEFAULT_FUSED_NAME = "fused";
        
        public static final String DEFAULT_REMAINING_NAME = "remaining";
        
        public static final String DEFAULT_REVIEW_NAME = "ambiguous";
        
        public static final String DEFAULT_STATS_NAME = "stats";
        
        public static final String DEFAULT_FUSION_LOG_NAME = "fusionLog";
        
        String id;
        
        Mode mode;
        
        String outputDir;
        
        String fusedPath;
        
        String remainingPath;
        
        String reviewPath;
        
        String statsPath;
        
        String fusionLogPath;
        
        Output() {}

        Output(String id, Mode mode, EnumDataFormat outputFormat)
        {
            final String outputExtension = outputFormat.getFilenameExtension();
            this.id = id;
            this.mode = mode;
            this.outputDir = null;
            this.fusedPath = DEFAULT_FUSED_NAME + '.' + outputExtension ;
            this.remainingPath = DEFAULT_REMAINING_NAME + '.' + outputExtension;
            this.reviewPath = DEFAULT_REVIEW_NAME + '.' + outputExtension;
            this.statsPath = DEFAULT_STATS_NAME + ".json";
            this.fusionLogPath = DEFAULT_FUSION_LOG_NAME + ".txt";
        }
        
        Output(
            String id, Mode mode, String dir, 
            String fusedPath, String remainingPath, String reviewPath)
        {
            this.id = id;
            this.mode = mode;
            this.outputDir = dir;
            this.fusedPath = fusedPath;
            this.remainingPath = remainingPath;
            this.reviewPath = reviewPath;
            this.statsPath = DEFAULT_STATS_NAME + ".json";
            this.fusionLogPath = DEFAULT_FUSION_LOG_NAME + ".txt";
        }
        
        @JsonProperty("id")
        @NotEmpty
        public String getId()
        {
            return id;
        }
        
        @JsonProperty("id")
        public void setId(String id)
        {
            this.id = id;
        }

        @JsonIgnore
        @NotNull
        public Mode getMode()
        {
            return mode;
        }
        
        @JsonIgnore
        public void setMode(Mode mode)
        {
            this.mode = mode;
        }
        
        @JsonProperty("mode")
        public String getModeAsString()
        {
            return mode == null? null : mode.key;
        }
        
        @JsonProperty("mode")
        public void setMode(String key)
        {
            this.mode = Mode.fromKey(key);
        }
        
        @JsonProperty("outputDir")
        public String getOutputDir()
        {
            return outputDir;
        }
        
        @JsonProperty("outputDir")
        public void setOutputDir(String dir)
        {
            this.outputDir = dir;
        }
        
        @JsonProperty("fused")
        @NotEmpty
        public String getFusedPath()
        {
            return fusedPath;
        }
        
        @JsonProperty("fused")
        public void setFusedPath(String fusedPath)
        {
            this.fusedPath = fusedPath;
        }

        @JsonProperty("remaining")
        @NotEmpty
        public String getRemainingPath()
        {
            return remainingPath;
        }
        
        @JsonProperty("remaining")
        public void setRemainingPath(String remainingPath)
        {
            this.remainingPath = remainingPath;
        }
        
        @JsonProperty("ambiguous")
        @NotEmpty
        public String getReviewPath()
        {
            return reviewPath;
        }

        @JsonProperty("ambiguous")
        public void setReviewPath(String reviewPath)
        {
            this.reviewPath = reviewPath;
        }

        @JsonProperty("statistics")
        @NotEmpty
        @Pattern(regexp = ".*[.]json")
        public String getStatsPath()
        {
            return statsPath;
        }

        @JsonProperty("statistics")
        public void setStatsPath(String statsPath)
        {
            this.statsPath = statsPath;
        }
        
        @JsonProperty("fusionLog")
        @NotEmpty
        public String getFusionLogPath()
        {
            return fusionLogPath;
        }

        @JsonProperty("fusionLog")
        public void setFusionLogPath(String fusionLogPath)
        {
            this.fusionLogPath = fusionLogPath;
        }
    }
  
    public class OutputNameMapper implements InputToOutputNameMapper<Fagi>
    {
        private OutputNameMapper() {};
        
        @Override
        public Multimap<OutputPart<Fagi>, OutputSpec> applyToPath(List<Path> input)
        {
            Assert.state(outputFormat != null, "The output format is required");
            Assert.state(target.fusedPath != null, "The path (fusion) is required");
            Assert.state(target.remainingPath != null, "The path (remaining) is required");
            Assert.state(target.reviewPath != null, "The path (review) is required");
            Assert.state(target.statsPath != null, "The path (stats) is required");
            
            return ImmutableMultimap.<OutputPart<Fagi>, OutputSpec>builder()
                .put(EnumFagiOutputPart.FUSED, 
                    OutputSpec.of(Paths.get(target.fusedPath).getFileName(), outputFormat))
                .put(EnumFagiOutputPart.REMAINING, 
                    OutputSpec.of(Paths.get(target.remainingPath).getFileName(), outputFormat))
                .put(EnumFagiOutputPart.REVIEW, 
                    OutputSpec.of(Paths.get(target.reviewPath).getFileName(), outputFormat))
                .put(EnumFagiOutputPart.STATS, 
                    OutputSpec.of(Paths.get(target.statsPath).getFileName()))
                .put(EnumFagiOutputPart.LOG,
                    OutputSpec.of(Paths.get(target.fusionLogPath).getFileName()))
                .build();
        }
    }
    
    /**
     * A profile for setting default configuration values
     */
    private String _profile;
    
    private String lang;
    
    private Similarity similarity;
    
    /**
     * A flag that controls whether an action log should be generated (see target)
     */
    private boolean verbose;
    
    /**
     * The resource location for the XML file holding the ruleset for fusion
     */
    private String rulesSpec;
    
    /**
     * The left-side input specification
     */
    private InputSpec leftSpec;
    
    /**
     * The right-side input specification
     */
    private InputSpec rightSpec;
    
    /**
     * The sameAs links specification 
     */
    private LinksSpec linksSpec;
    
    /**
     * The target (i.e output) specification
     */
    private Output target;
    
    public FagiConfiguration() 
    {
        this._version = VERSION;
        
        this.verbose = false;
        
        this.lang = "en";
        
        this.input = new ArrayList<>(Collections.nCopies(3, null));
        this.inputFormat = EnumDataFormat.N_TRIPLES;
        this.outputFormat = EnumDataFormat.N_TRIPLES;
        
        this.similarity = Similarity.JAROWINKLER;
        
        this.leftSpec = new InputSpec("a");
        this.rightSpec = new InputSpec("b");
        this.linksSpec = new LinksSpec("links");
        
        this.target = new Output("ab", Mode.AA, this.outputFormat);
    }
    
    @JsonIgnore
    @Override
    public Class<Fagi> getToolType()
    {
        return Fagi.class;
    }
    
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("profile")
    public String getProfile()
    {
        return _profile;
    }
    
    @JsonProperty("profile")
    public void setProfile(String profile)
    {
        this._profile = profile;
    }
    
    @JsonProperty("verbose")
    public void setVerbose(boolean verbose)
    {
        this.verbose = verbose;
    }
    
    @JsonProperty("verbose")
    public boolean isVerbose()
    {
        return verbose;
    }
    
    @JsonIgnore
    @NotNull
    @Override
    public EnumDataFormat getInputFormat()
    {
        return inputFormat;
    }

    @JsonIgnore
    @Override
    public void setInputFormat(EnumDataFormat dataFormat)
    {
        this.inputFormat = dataFormat;
    }

    @JsonProperty("inputFormat")
    public String getInputFormatAsString()
    {
        return inputFormat == null? null : DataFormat.from(inputFormat).key();
    }
    
    @JsonProperty("inputFormat")
    public void setInputFormat(String key)
    {
        DataFormat f = DataFormat.from(key);
        Assert.notNull(f, "The key [" + key + "] does not map to a data format");
        this.inputFormat = f.dataFormat();
    }
    
    @JsonIgnore
    @Override
    public List<String> getInput()
    {
        return Collections.unmodifiableList(input);
    }
   
    @JsonIgnore
    @Override
    public void setInput(List<String> inputList)
    {
        Assert.notNull(inputList, "A non-null list of inputs is required");
        Assert.isTrue(inputList.size() == 3, 
            "Expected a triple (left, right, links) of input files");
        setLeftPath(inputList.get(0).toString());
        setRightPath(inputList.get(1).toString());
        setLinksPath(inputList.get(2).toString());
    }

    @JsonIgnore
    @Override
    public void setInput(String inputAsString)
    {
        // Treat as a colon-separated list of paths
        String[] inputPaths = inputAsString.toString().split(File.pathSeparator);
        Assert.isTrue(inputPaths.length == 3, 
            "Expected a triple (left, right, links) of input files");
        setLeftPath(inputPaths[0]);
        setRightPath(inputPaths[1]);
        setLinksPath(inputPaths[2]);
    }

    @JsonIgnore
    public void setInput(Map<?, ?> inputMap)
    {
        Assert.notNull(inputMap, "An input map is expected");
        if (inputMap.containsKey("left")) {
            setLeftPath(inputMap.get("left").toString());
        }
        if (inputMap.containsKey("right")) {
            setRightPath(inputMap.get("right").toString());
        }
        if (inputMap.containsKey("links")) {
            setLinksPath(inputMap.get("links").toString());
        }
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
    
    @Override
    public void clearInput()
    {
        setLeftPath(null);
        setRightPath(null);
        setLinksPath(null);
    }

    @JsonIgnore
    @Override
    public String getOutputDir()
    {
        return outputDir;
    }
    
    @JsonIgnore
    @Override
    public void setOutputDir(String dir)
    {
        this.outputDir = this.target.outputDir = dir;
    }
    
    @JsonIgnore
    @Override
    public InputToOutputNameMapper<Fagi> getOutputNameMapper()
    {
        return new OutputNameMapper();
    }
    
    @JsonIgnore
    @NotNull
    @Override
    public EnumDataFormat getOutputFormat()
    {
        return outputFormat;
    }

    @JsonIgnore
    @AssertTrue
    protected boolean isOutputFormatParWithExtensions()
    {
        if (outputFormat == null)
            return true; // nothing to check
        
        final String extension = outputFormat.getFilenameExtension();
        return Stream.of(target.fusedPath, target.remainingPath, target.reviewPath)
            .filter(Objects::nonNull)
            .allMatch(p -> StringUtils.getFilenameExtension(p).equals(extension));
    }
    
    @JsonIgnore
    @Override
    public void setOutputFormat(EnumDataFormat dataFormat)
    {
        this.outputFormat = dataFormat;
    }
    
    @JsonProperty("outputFormat")
    public String getOutputFormatAsString()
    {
        return outputFormat == null? null : DataFormat.from(outputFormat).key();
    }
    
    @JsonProperty("outputFormat")
    public void setOutputFormat(String key)
    {
        DataFormat f = DataFormat.from(key);
        Assert.notNull(f, "The key [" + key + "] does not map to a data format");
        this.outputFormat = f.dataFormat();
    }
    
    @JsonIgnore
    @Override
    public String getVersion()
    {
        return super.getVersion();
    }

    @JsonIgnore
    @Override
    public void setVersion(String version)
    {
        super.setVersion(version);
    }
    
    @JsonProperty("locale")
    @Pattern(regexp = "([a-z][a-z])([-][A-Z][A-Z])?", flags = {Pattern.Flag.CASE_INSENSITIVE})
    public String getLang()
    {
        return lang;
    }
    
    @JsonProperty("locale")
    public void setLang(String lang)
    {
        this.lang = lang;
    }
    
    @JsonIgnore
    public Similarity getSimilarity()
    {
        return similarity;
    }
    
    @JsonIgnore
    public void setSimilarity(Similarity similarity)
    {
        this.similarity = similarity;
    }
    
    @JsonProperty("similarity")
    public String getSimilarityAsString()
    {
        return similarity == null? null : similarity.key;
    }
    
    @JsonProperty("similarity")
    public void setSimilarity(String key)
    {
        this.similarity = Similarity.fromKey(key);
    }
    
    @JsonProperty("rulesSpec")
    public String getRulesSpec()
    {
        return rulesSpec;
    }
    
    @JsonProperty("rulesSpec")
    public void setRulesSpec(String resourceLocation)
    {
        this.rulesSpec = resourceLocation;
    }
    
    //// Left ////
    
    @JsonProperty("left")
    @NotNull
    @Valid
    public Input getLeft()
    {
        final String path = input.get(LEFT_INDEX);
        return new Input(leftSpec.id, path, leftSpec.categoriesLocation, leftSpec.date);
    }
    
    @JsonProperty("left")
    @JsonInclude(Include.NON_NULL)
    protected void setLeft(Input r)
    {
        Assert.notNull(r, "An input descriptor is required");
        this.leftSpec.id = r.spec.id;
        this.leftSpec.categoriesLocation = r.spec.categoriesLocation;
        this.leftSpec.date = r.spec.date;
        
        if (!StringUtils.isEmpty(r.path))
            this.input.set(LEFT_INDEX, r.path);
    }
    
    @JsonIgnore
    public void setLeft(String id, String path, String categoriesLocation, LocalDate date)
    {
        this.input.set(LEFT_INDEX, path);
        this.leftSpec.id = id;
        this.leftSpec.categoriesLocation = categoriesLocation;
        this.leftSpec.date = date;
    }
    
    @JsonProperty("input.left")
    @JsonSetter
    public void setLeftPath(String path)
    {
        this.input.set(LEFT_INDEX, path);
    }
    
    @JsonIgnore
    public String getLeftPath()
    {
        return input.get(LEFT_INDEX);
    }
    
    @JsonIgnore
    public String getLeftId()
    {
        return leftSpec.id;
    }
    
    //// Right ////
    
    @JsonProperty("right")
    @NotNull
    @Valid
    public Input getRight()
    {
        final String path = input.get(RIGHT_INDEX);
        return new Input(rightSpec.id, path, rightSpec.categoriesLocation, rightSpec.date);
    }
    
    @JsonProperty("right")
    @JsonInclude(Include.NON_NULL)
    protected void setRight(Input r)
    {
        Assert.notNull(r, "An input descriptor is required");
        this.rightSpec.id = r.spec.id;
        this.rightSpec.categoriesLocation = r.spec.categoriesLocation;
        this.rightSpec.date = r.spec.date;
        
        if (!StringUtils.isEmpty(r.path))
            this.input.set(RIGHT_INDEX, r.path);
    }
    
    @JsonIgnore
    public void setRight(String id, String path, String categoriesLocation, LocalDate date)
    {
        this.input.set(RIGHT_INDEX, path);
        this.rightSpec.id = id;
        this.rightSpec.categoriesLocation = categoriesLocation;
        this.rightSpec.date = date;
    }
    
    @JsonProperty("input.right")
    @JsonSetter
    public void setRightPath(String path)
    {
        this.input.set(RIGHT_INDEX, path);
    }
    
    @JsonIgnore
    public String getRightPath()
    {
        return input.get(RIGHT_INDEX);
    }
    
    @JsonIgnore
    public String getRightId()
    {
        return rightSpec.id;
    }
    
    //// Links ////
    
    @JsonProperty("links")
    @NotNull
    @Valid
    public Links getLinks()
    {
        return new Links(linksSpec.id, input.get(LINKS_INDEX));
    }
    
    @JsonProperty("links")
    @JsonInclude(Include.NON_NULL)
    protected void setLinks(Links r)
    {
        Assert.notNull(r, "An input descriptor is required");
        this.linksSpec.id = r.spec.id;
        this.linksSpec.format = r.spec.format;
        
        if (!StringUtils.isEmpty(r.path))
            this.input.set(LINKS_INDEX, r.path);
    }
    
    @JsonIgnore
    public void setLinks(String id, String path)
    {
        this.input.set(LINKS_INDEX, path);
        this.linksSpec.id = id;
    }
    
    @JsonProperty("input.links")
    @JsonSetter
    public void setLinksPath(String path)
    {
        this.input.set(LINKS_INDEX, path);
    }
    
    @JsonIgnore
    public String getLinksPath()
    {
        return input.get(LINKS_INDEX);
    }
    
    @JsonIgnore
    public String getLinksId()
    {
        return linksSpec.id;
    }
    
    //// Target ////
    
    @JsonProperty("target")
    @NotNull
    @Valid
    public Output getTarget()
    {
        return target;
    }
    
    @JsonProperty("target")
    @JsonInclude(Include.NON_NULL)
    protected void setTarget(Output r)
    {
        Assert.notNull(r, "An output descriptor is required");
        this.target.id = r.id;
        this.target.mode = r.mode;
        this.target.fusedPath = r.fusedPath;
        this.target.remainingPath = r.remainingPath;
        this.target.reviewPath = r.reviewPath;
        this.target.statsPath = r.statsPath;
        
        this.outputDir = this.target.outputDir = r.outputDir;
    }
    
    @JsonIgnore
    public void setTarget(
        String id, String fusedName, String remainingName, String reviewName, String statsName)
    {
        Assert.isTrue(!StringUtils.isEmpty(id), "A non-empty id is expected");
        Assert.isTrue(!StringUtils.isEmpty(fusedName), "A non-empty file name is expected");
        Assert.isTrue(Paths.get(fusedName).getNameCount() == 1, "A plain file name is expected");
        Assert.isTrue(!StringUtils.isEmpty(remainingName), "A non-empty file name is expected");
        Assert.isTrue(Paths.get(remainingName).getNameCount() == 1, "A plain file name is expected");
        Assert.isTrue(!StringUtils.isEmpty(reviewName), "A non-empty file name is expected");
        Assert.isTrue(Paths.get(reviewName).getNameCount() == 1, "A plain file name is expected");
        
        this.target.id = id;
        
        this.target.fusedPath = fusedName;
        this.target.remainingPath = remainingName;
        this.target.reviewPath = reviewName;
        
        if (!StringUtils.isEmpty(statsName))
            this.target.statsPath = statsName;
    }
    
    @JsonIgnore
    public void setTargetMode(Mode mode)
    {
        Assert.notNull(mode, "A fusion mode is required");
        this.target.mode = mode;
    }
    
    @JsonIgnore
    public void setTargetMode(String key)
    {
        Assert.notNull(key, "A key (for fusion mode) is required");
        this.target.mode = Mode.fromKey(key);
    }
}
