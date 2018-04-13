package eu.slipo.workbench.common.model.tool;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.URL;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.common.model.poi.EnumOntology;
import eu.slipo.workbench.common.model.poi.EnumTool;


/**
 * Configuration for the Triplegeo tool
 */
public class TriplegeoConfiguration extends TransformConfiguration
{
    private static final long serialVersionUID = 1L;

    /**
     * The set of available processing modes
     */
    public enum Mode {
        STREAM,
        GRAPH;
    }

    /**
     * The set of data formats ({@link EnumDataFormat}) as expected by Triplegeo
     */
    public enum DataFormat
    {
        CSV(EnumDataFormat.CSV, "CSV"),
        GEOJSON(EnumDataFormat.GEOJSON, "GEOJSON"),
        GPX(EnumDataFormat.GPX, "GPX"),
        N3(EnumDataFormat.N3, "N3"),
        N_TRIPLES(EnumDataFormat.N_TRIPLES, "N-TRIPLES"),
        OSM(EnumDataFormat.OSM, "OSM"),
        TURTLE(EnumDataFormat.TURTLE, "TURTLE", "TTL"),
        RDF_XML(EnumDataFormat.RDF_XML, "RDF", "RDF/XML"),
        RDF_XML_ABBREV(EnumDataFormat.RDF_XML_ABBREV, "RDF/XML-ABBREV"),
        SHAPEFILE(EnumDataFormat.SHAPEFILE, "SHAPEFILE", "SHP");

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

    //
    // Member data
    //

    private Mode mode = Mode.STREAM;

    private EnumOntology targetOntology = EnumOntology.GEOSPARQL;

    /**
     * The name of the field holding a unique identifier for each input record
     */
    private String attrKey = "id";

    /**
     * The name of the field from which names will be extracted.
     */
    private String attrName = "name";

    /**
     * The name of the field from which a category will be extracted (e.g. type of points,
     * road classes).
     */
    private String attrCategory = "type";

    /**
     * Parameter that specifies the name of the geometry column in the input dataset. Omit
     * this parameter if geometry representation is available with columns specifying X,Y
     * coordinates for points; otherwise, this parameter is MANDATORY.
     */
    private String attrGeometry = "shape";

    /**
     * A string literal representing an unknown (i.e. null) value. A field with an unknown value
     * will not be extracted.
     */
    private String nullValue = "UNK";

    /**
     * A field delimiter for records (meaningful only for CSV input).
     */
    private String delimiter = ";";

    /**
     * Mandatory for CSV input only (case-insensitive): specify quote character for string
     * values; Remove for any other types of input data. quote = "
     */
    private String quote= "\"";

    /**
     * Required for CSV input only (case-insensitive): specify attribute holding X-
     * coordinates of point locations
     */
    private String attrX = "lon";

    /**
     * Required for CSV input only (case-insensitive): specify attribute holding Y-
     * coordinates of point locations
     */
    private String attrY = "lat";

    /**
     * A user-defined name for the resources that will be created. This is required for
     * constructing the resource URI.
     */
    private String featureName = "points";

    /**
     * The common URI namespace for all generated resources
     */
    private String featureNamespaceUri = "http://slipo.eu/geodata#";

    /**
     * A prefix name for the utilized URI namespace (i.e. the one declared with nsFeatureURI)
     */
    private String featureUriPrefix = "georesource";

    /**
     * The namespace for the underlying geospatial ontology
     */
    private String geometryNamespaceUri = "http://www.opengis.net/ont/geosparql#";

    /**
     * A prefix name for the geospatial ontology (i.e., the one declared with nsGeometryURI)
     */
    private String geometryUriPrefix = "geo";

    /**
     * The coordinate reference system (CRS) for input data (eg "EPSG:4326")
     */
    private String sourceCRS;

    /**
     * The coordinate reference system (CRS) for output data (e.g "EPSG:4326")
     */
    private String targetCRS;

    /**
     * The default language for labels created in output RDF. The default is "en".
     */
    private String defaultLang = "en";

    /**
     * Optional parameter for the encoding (character set) for strings in the input data.
     * If not specified, UTF-8 encoding is assumed.
     */
    private String encoding = "UTF-8";

    /**
     * A default constructor
     */
    public TriplegeoConfiguration()
    {
        this.outputFormat = EnumDataFormat.N_TRIPLES;
        this.tmpDir = "/tmp";
    }

    @Override
    public TriplegeoConfiguration cloneAsBean() throws ReflectiveOperationException
    {
        return (TriplegeoConfiguration) super.cloneAsBean();
    }

    @JsonIgnore
    @Override
    public EnumTool getTool()
    {
        return EnumTool.TRIPLEGEO;
    }
    
    //
    // Helpers
    //

    /**
     * Set defaults that can be deduced from input format.
     */
    public void useDefaultsForInputFormat()
    {
        Assert.state(inputFormat != null, "The input format must be set before");

        switch (inputFormat)
        {
        case CSV:
            mode = Mode.STREAM;
            attrKey = "id";
            attrName = "name";
            attrCategory = "type";
            break;
        case SHAPEFILE:
            mode = Mode.GRAPH;
            attrKey = "id";
            attrName = "name";
            attrCategory = "type";
            attrGeometry = "shape";
            break;
        case GEOJSON:
            mode = Mode.STREAM;
            attrKey = "id";
            attrName = "name";
            attrCategory = "type";
            break;
        case GPX:
            mode = Mode.STREAM;
            attrKey = "name";
            attrName = "name";
            break;
        default:
            break;
        }
    }

    //
    // Getters / Setters
    //

    @JsonIgnore
    public EnumDataFormat getInputFormat()
    {
        return inputFormat;
    }

    @JsonIgnore
    public void setInputFormat(EnumDataFormat inputFormat)
    {
        this.inputFormat = inputFormat;
    }

    @JsonProperty("inputFormat")
    @NotEmpty
    public String getInputFormatAsString()
    {
        DataFormat dataFormat = inputFormat == null? null : DataFormat.from(inputFormat);
        return dataFormat == null? null : dataFormat.key();
    }

    @JsonProperty("inputFormat")
    public void setInputFormat(String inputFormat)
    {
        DataFormat f = DataFormat.from(inputFormat);
        Assert.notNull(f, "The key [" + inputFormat + "] does not map to a data format");
        setInputFormat(f.dataFormat());
    }

    @JsonIgnore
    public void setInput(List<Path> input)
    {
        this.input = Collections.unmodifiableList(
            input.stream()
                .map(Path::toString)
                .collect(Collectors.toList()));
    }

    @NotEmpty
    @JsonIgnore
    public List<Path> getInput()
    {
        return this.input.stream()
            .map(Paths::get)
            .collect(Collectors.toList());
    }

    @JsonAlias({ "inputFiles", "input" })
    public void setInputFromString(String inputFiles)
    {
        if (!StringUtils.isEmpty(inputFiles)) {
            this.input = Collections.unmodifiableList(
                Arrays.asList(inputFiles.split(File.pathSeparator)));
        }
    }

    @JsonProperty("inputFiles")
    public String getInputAsString()
    {
        if (input.isEmpty()) {
            return null;
        }

        return input.stream()
            .collect(Collectors.joining(File.pathSeparator));
    }

    @JsonIgnore
    public EnumDataFormat getOutputFormat()
    {
        return outputFormat;
    }

    @JsonIgnore
    public void setOutputFormat(EnumDataFormat outputFormat)
    {
        this.outputFormat = outputFormat;
    }

    @JsonProperty("serialization")
    @NotEmpty
    public String getSerializationFormat()
    {
        DataFormat dataFormat = outputFormat == null? null : DataFormat.from(outputFormat);
        return dataFormat == null? null : dataFormat.key();
    }

    @JsonProperty("serialization")
    public void setSerializationFormat(String serializationFormat)
    {
        DataFormat f = DataFormat.from(serializationFormat);
        Assert.notNull(f,
            "The key [" + serializationFormat + "] does not map to a data format");
        setOutputFormat(f.dataFormat());
    }

    @JsonIgnore
    public void setOutputDir(Path outputDir)
    {
        this.outputDir = outputDir == null? null : outputDir.toString();
    }

    @JsonIgnore
    public Path getOutputDir()
    {
        return outputDir == null? null : Paths.get(outputDir);
    }

    @JsonProperty("outputDir")
    public void setOutputDir(String outputDir)
    {
        this.outputDir = outputDir;
    }

    @JsonProperty("outputDir")
    public String getOutputDirAsString()
    {
        return outputDir;
    }

    @JsonIgnore
    public void setTmpDir(Path tmpDir)
    {
        this.tmpDir = tmpDir == null? null : tmpDir.toString();
    }

    @JsonIgnore
    public Path getTmpDir()
    {
        return tmpDir == null? null : Paths.get(tmpDir);
    }

    @JsonProperty("tmpDir")
    public void setTmpDir(String tmpDir)
    {
        this.tmpDir = tmpDir;
    }

    @JsonProperty("tmpDir")
    public String getTmpDirAsString()
    {
        return tmpDir;
    }

    @JsonProperty("mode")
    public void setMode(Mode mode)
    {
        this.mode = mode;
    }

    @JsonProperty("mode")
    @NotNull
    public Mode getMode()
    {
        return mode;
    }

    @JsonIgnore
    public void setTargetOntology(EnumOntology targetOntology)
    {
        this.targetOntology = targetOntology;
    }

    @JsonIgnore
    public EnumOntology getTargetOntology()
    {
        return targetOntology;
    }

    @JsonProperty("targetOntology")
    public void setTargetOntology(String key)
    {
        this.targetOntology = EnumOntology.fromKey(key);
    }

    @JsonProperty("targetOntology")
    public String getTargetOntologyKey()
    {
        return targetOntology.key();
    }

    @JsonProperty("featureName")
    public void setFeatureName(String featureName)
    {
        this.featureName = featureName;
    }

    @JsonProperty("featureName")
    @NotEmpty
    public String getFeatureName()
    {
        return featureName;
    }

    @JsonProperty("nsFeatureURI")
    public void setFeatureNamespaceUri(String uri)
    {
        this.featureNamespaceUri = uri;
    }

    @JsonProperty("nsFeatureURI")
    @URL
    public String getFeatureNamespaceUri()
    {
        return featureNamespaceUri;
    }

    @JsonProperty("prefixFeatureNS")
    public void setFeatureUriPrefix(String prefix)
    {
        this.featureUriPrefix = prefix;
    }

    @JsonProperty("prefixFeatureNS")
    public String getFeatureUriPrefix()
    {
        return featureUriPrefix;
    }

    @JsonProperty("nsGeometryURI")
    public void setGeometryNamespaceUri(String uri)
    {
        this.geometryNamespaceUri = uri;
    }

    @JsonProperty("nsGeometryURI")
    @URL
    public String getGeometryNamespaceUri()
    {
        return geometryNamespaceUri;
    }

    @JsonProperty("prefixGeometryNS")
    public void setGeometryUriPrefix(String prefix)
    {
        this.geometryUriPrefix = prefix;
    }

    @JsonProperty("prefixGeometryNS")
    public String getGeometryUriPrefix()
    {
        return geometryUriPrefix;
    }

    @JsonProperty("attrKey")
    public void setAttrKey(String attrKey)
    {
        this.attrKey = attrKey;
    }

    @JsonProperty("attrKey")
    @NotEmpty
    public String getAttrKey()
    {
        return attrKey;
    }

    @JsonProperty("attrName")
    public void setAttrName(String attrName)
    {
        this.attrName = attrName;
    }

    @JsonProperty("attrName")
    @NotEmpty
    public String getAttrName()
    {
        return attrName;
    }

    @JsonProperty("attrCategory")
    public void setAttrCategory(String attrCategory)
    {
        this.attrCategory = attrCategory;
    }

    @JsonProperty("attrCategory")
    public String getAttrCategory()
    {
        return attrCategory;
    }

    @JsonProperty("attrGeometry")
    public void setAttrGeometry(String attrGeometry)
    {
        this.attrGeometry = attrGeometry;
    }

    @JsonProperty("attrGeometry")
    public String getAttrGeometry()
    {
        return attrGeometry;
    }

    @JsonProperty("valIgnore")
    public void setNullValue(String nullValue)
    {
        this.nullValue = nullValue;
    }

    @JsonProperty("valIgnore")
    public String getNullValue()
    {
        return nullValue;
    }

    @JsonProperty("attrX")
    public void setAttrX(String attrX)
    {
        this.attrX = attrX;
    }

    @JsonProperty("attrX")
    public String getAttrX()
    {
        return attrX;
    }

    @JsonProperty("attrY")
    public void setAttrY(String attrY)
    {
        this.attrY = attrY;
    }

    @JsonProperty("attrY")
    public String getAttrY()
    {
        return attrY;
    }

    @JsonProperty("delimiter")
    public void setDelimiter(String delimiter)
    {
        this.delimiter = delimiter;
    }

    @JsonProperty("delimiter")
    @NotEmpty
    public String getDelimiter()
    {
        return delimiter;
    }

    @JsonProperty("sourceCRS")
    public void setSourceCRS(String sourceCRS)
    {
        this.sourceCRS = sourceCRS;
    }

    @JsonProperty("sourceCRS")
    @Pattern(regexp = "epsg:(\\d)+", flags = {Pattern.Flag.CASE_INSENSITIVE})
    public String getSourceCRS()
    {
        return sourceCRS;
    }

    @JsonProperty("targetCRS")
    public void setTargetCRS(String targetCRS)
    {
        this.targetCRS = targetCRS;
    }

    @JsonProperty("targetCRS")
    @Pattern(regexp = "epsg:(\\d)+", flags = {Pattern.Flag.CASE_INSENSITIVE})
    public String getTargetCRS()
    {
        return targetCRS;
    }

    @JsonProperty("defaultLang")
    public String getDefaultLang()
    {
        return defaultLang;
    }

    @JsonProperty("defaultLang")
    public void setDefaultLang(String defaultLang)
    {
        this.defaultLang = defaultLang;
    }

    @JsonProperty("encoding")
    public String getEncoding()
    {
        return encoding;
    }

    @JsonProperty("encoding")
    public void setEncoding(String encoding)
    {
        this.encoding = encoding;
    }

    @JsonProperty("quote")
    public String getQuote()
    {
        return quote;
    }

    @JsonProperty("quote")
    public void setQuote(String quote)
    {
        this.quote = quote;
    }
}
