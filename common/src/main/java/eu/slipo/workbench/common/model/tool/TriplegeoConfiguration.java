package eu.slipo.workbench.common.model.tool;

import static org.springframework.util.StringUtils.stripFilenameExtension;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.URL;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.common.model.poi.EnumSpatialOntology;
import eu.slipo.workbench.common.model.tool.output.EnumTriplegeoOutputPart;
import eu.slipo.workbench.common.model.tool.output.InputToOutputNameMapper;
import eu.slipo.workbench.common.model.tool.output.OutputPart;
import eu.slipo.workbench.common.model.tool.output.OutputSpec;


/**
 * Configuration for the Triplegeo tool.
 *
 * @see https://github.com/SLIPO-EU/TripleGeo/blob/master/config/file_options.conf.template
 */
@JsonPropertyOrder(alphabetic=true)
public class TriplegeoConfiguration extends TransformConfiguration<Triplegeo>
{
    private static final long serialVersionUID = 2L;

    /**
     * This class represents the configuration of a specific version
     */
    public static final String VERSION = "1.8";

    /**
     * Available configuration levels
     */
    public enum EnumLevel {
        /**
         * Mappings are generated automatically
         */
        AUTO,
        /**
         * Mappings are set by selecting existing profiles
         */
        SIMPLE,
        /**
         * Mappings defined in profiles can be overridden by selecting user files
         */
        ADVANCED,
        ;
    }

    /**
     * The set of available processing modes (see Triplegeo documentation)
     */
    public enum Mode {
        STREAM,
        GRAPH,
        RML;
    }

    /**
     * The set of data formats ({@link EnumDataFormat}) as expected by Triplegeo
     */
    public enum DataFormat
    {
        CSV(EnumDataFormat.CSV, "CSV"),
        GEOJSON(EnumDataFormat.GEOJSON, "GEOJSON"),
        JSON(EnumDataFormat.JSON, "JSON"),
        GPX(EnumDataFormat.GPX, "GPX"),
        N3(EnumDataFormat.N3, "N3"),
        N_TRIPLES(EnumDataFormat.N_TRIPLES, "N-TRIPLES"),
        OSM_XML(EnumDataFormat.OSM_XML, "OSM_XML"),
        OSM_PBF(EnumDataFormat.OSM_PBF, "OSM_PBF"),
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

    public static class Prefix
    {
        String label;

        String uri;

        public Prefix(String label, String uri)
        {
            this.label = label;
            this.uri = uri;
        }

        @NotEmpty
        public String getLabel()
        {
            return label;
        }

        @NotEmpty
        @URL
        public String getUri()
        {
            return uri;
        }

        @Override
        public String toString()
        {
            return String.format("Prefix [label=%s, uri=%s]", label, uri);
        }
    }

    public class OutputNameMapper implements InputToOutputNameMapper<Triplegeo>
    {
        private OutputNameMapper() {};

        @Override
        public Multimap<OutputPart<Triplegeo>, OutputSpec> applyToPath(List<Path> inputList)
        {
            Assert.state(outputFormat != null, "The output format is not specified");

            final String extension = outputFormat.getFilenameExtension();
            final ImmutableMultimap.Builder<OutputPart<Triplegeo>, OutputSpec> outputMapBuilder =
                ImmutableMultimap.builder();

            // Each input yields an RDF output and a JSON metadata file

            for (Path inputPath: inputList) {
                String inputName = stripFilenameExtension(inputPath.getFileName().toString());
                outputMapBuilder.put(EnumTriplegeoOutputPart.TRANSFORMED,
                    OutputSpec.of(inputName + "." + extension, outputFormat));
                outputMapBuilder.put(EnumTriplegeoOutputPart.TRANSFORMED_METADATA,
                    OutputSpec.of(inputName + "_metadata" + ".json"));
                if (registerFeatures) {
                    // An additional CSV is generated as a registration request payload
                    outputMapBuilder.put(EnumTriplegeoOutputPart.REGISTRATION_REQUEST,
                        OutputSpec.of(inputName + ".csv", EnumDataFormat.CSV));
                }
            }

            // An output file with classification (in RDF format) is always produced, even if a
            // classification spec is not directly provided (as configuration)

            outputMapBuilder.put(EnumTriplegeoOutputPart.CLASSIFICATION,
                OutputSpec.of("classification" + "." + extension, outputFormat));
            outputMapBuilder.put(EnumTriplegeoOutputPart.CLASSIFICATION_METADATA,
                OutputSpec.of("classification_metadata" + ".json"));

            // Done

            return outputMapBuilder.build();
        }
    }

    //
    // Member data
    //

    /**
     * A profile for setting default configuration values
     */
    private String _profile;

    private EnumLevel _level;

    /**
     * Custom mappings selected manually by the user
     */
    private List<TriplegeoFieldMapping> userMappings;

    private Mode mode = Mode.STREAM;

    private EnumSpatialOntology targetGeoOntology = EnumSpatialOntology.GEOSPARQL;

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
    private String attrGeometry;

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
     * A resource location of a YML file containing mappings from input schema to RDF according to a
     * custom ontology.
     */
    private String mappingSpec;

    /**
     * Custom mappings from input schema to RDF according to a custom ontology.
     */
    private String mappingSpecText;

    /**
     * A resource location of a YML/CSV file describing a classification scheme.
     */
    private String classificationSpec;

    /**
     * Indicate whether the data features specify their category based on its identifier in the classification scheme (false)
     * or the actual name of the category (true). By default, transformation uses identifiers of categories in the
     * classification scheme.
     */
    private Boolean classifyByName = true;

    /**
     * A name for the data source provider of input features
     */
    private String featureSource;

    /**
     * The namespace of the underlying ontology (used in creating properties for RDF triples)
     */
    private String ontologyNamespaceUri = SLIPO_ONTOLOGY_NAMESPACE_URI;

    /**
     * The namespace of underlying geospatial ontology
     */
    private String geometryNamespaceUri = GEOSPARQL_NAMESPACE_URI;

    /**
     * The namespace for all generated resources
     */
    private String featureNamespaceUri = SLIPO_FEATURE_NAMESPACE_URI;

    /**
     * The namespace for the classification scheme
     */
    private String classificationNamespaceUri = SLIPO_CLASSIFICATION_NAMESPACE_URI;

    /**
     * The namespace for categories under the classification scheme
     */
    private String classNamespaceUri = SLIPO_CLASS_NAMESPACE_URI;

    /**
     * The namespace for the data source provider
     */
    private String datasourceNamespaceUri = SLIPO_DATASOURCE_NAMESPACE_URI;

    // Note:
    // The namespace prefixes are defined in a bit awkward way: 2 separate lists where i-th
    // item of 1st list (aliases) is the corresponds to the i-th item of 2nd list (URIs).
    // They should be probably stored as a map or a list of Prefix objects; but until the target
    // configuration format changes, we will keep the 2 lists just to enable simpler deserialization

    /**
     * The list of aliases for namespace URIs (must correspond to listed namespaces!)
     */
    private List<String> prefixKeys = new ArrayList<>();

    /**
     * The list of namespaces for which a prefix (an alias) is defined
     */
    private List<String> prefixedNamespaces = new ArrayList<>();

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
     * Indicates whether to export a CSV file with records for the SLIPO Registry
     */
    private boolean registerFeatures = true;

    /**
     * Spatial filter to select input geometries contained within the specified polygon
     */
    private String spatialExtent;

    /**
     * A default constructor
     */
    public TriplegeoConfiguration()
    {
        this._version = VERSION;
        this.outputFormat = EnumDataFormat.N_TRIPLES;
    }

    @JsonIgnore
    @Override
    public Class<Triplegeo> getToolType()
    {
        return Triplegeo.class;
    }

    @Override
    @JsonIgnore
    public String getVersion()
    {
        return _version;
    }

    //
    // Helpers
    //

    /**
     * Set defaults that can be deduced from input format.
     */
    public void setDefaultsForInputFormat()
    {
        Assert.state(inputFormat != null, "The input format must be set before");

        switch (inputFormat)
        {
        case CSV:
            mode = Mode.STREAM;
            break;
        case SHAPEFILE:
            mode = Mode.GRAPH;
            break;
        case GEOJSON:
            mode = Mode.STREAM;
            break;
        case GPX:
            mode = Mode.STREAM;
            break;
        default:
            break;
        }
    }

    public void setDefaultsForPrefixes()
    {
        this.addPrefix("slipo", SLIPO_ONTOLOGY_NAMESPACE_URI);
        this.addPrefix("geo", GEOSPARQL_NAMESPACE_URI);
        this.addPrefix("xsd", XSD_NAMESPACE_URI);
        this.addPrefix("rdfs", RDFS_NAMESPACE_URI);
        this.addPrefix("wgs84_pos", WGS84POS_NAMESPACE_URI);
    }

    //
    // Getters / Setters
    //

    @JsonProperty("level")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public EnumLevel getLevel() 
    {
        return _level;
    }

    @JsonProperty("level")
    public void setLevel(EnumLevel level) 
    {
        this._level = level;
    }

    @JsonProperty("userMappings")
    public List<TriplegeoFieldMapping> getUserMappings() {
        return userMappings;
    }

    @JsonProperty("userMappings")
    public void setUserMappings(List<TriplegeoFieldMapping> userMappings) {
        this.userMappings = userMappings;
    }

    @JsonProperty("profile")
    public void setProfile(String profile)
    {
        this._profile = profile;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("profile")
    public String getProfile()
    {
        return _profile;
    }

    @JsonIgnore
    @Override
    public EnumDataFormat getInputFormat()
    {
        return inputFormat;
    }

    @JsonIgnore
    @Override
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
    @Override
    public void setInput(List<String> input)
    {
        super.setInput(input);
    }

    @JsonIgnore
    @Override
    public void setInput(String input)
    {
        super.setInput(input);
    }

    @Override
    public void clearInput()
    {
        super.clearInput();
    }

    @JsonIgnore
    @NotNull
    @Override
    public List<String> getInput()
    {
        return this.input;
    }

    @JsonAlias({ "inputFiles", "input" })
    public void setInputFromString(String inputFiles)
    {
        if (!StringUtils.isEmpty(inputFiles)) {
            this.input = Collections.unmodifiableList(
                Arrays.asList(inputFiles.split(File.pathSeparator)));
        } else {
            this.input = Collections.emptyList();
        }
    }

    @JsonProperty("inputFiles")
    public String getInputAsString()
    {
        if (input.isEmpty()) {
            return null;
        }

        return input.stream().collect(Collectors.joining(File.pathSeparator));
    }

    @JsonIgnore
    @Override
    public EnumDataFormat getOutputFormat()
    {
        return outputFormat;
    }

    @Override
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
    protected void setSerializationFormat(String serializationFormat)
    {
        DataFormat f = DataFormat.from(serializationFormat);
        Assert.notNull(f,
            "The key [" + serializationFormat + "] does not map to a data format");
        setOutputFormat(f.dataFormat());
    }

    @Override
    @JsonProperty("outputDir")
    public void setOutputDir(String outputDir)
    {
        this.outputDir = outputDir;
    }

    @Override
    @JsonProperty("outputDir")
    public String getOutputDir()
    {
        return outputDir;
    }

    @JsonProperty("tmpDir")
    public void setTmpDir(String tmpDir)
    {
        this.tmpDir = tmpDir;
    }

    @JsonProperty("tmpDir")
    public String getTmpDir()
    {
        return tmpDir;
    }

    @JsonIgnore
    @Override
    public InputToOutputNameMapper<Triplegeo> getOutputNameMapper()
    {
        return new OutputNameMapper();
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

    @JsonProperty("mappingSpec")
    public void setMappingSpec(String mappingSpec)
    {
        this.mappingSpec = mappingSpec;
    }

    @JsonProperty("mappingSpec")
    @NotNull
    public String getMappingSpec()
    {
        return mappingSpec;
    }

    @JsonProperty("mappingSpecText")
    public void setMappingSpecText(String mappingSpecText)
    {
        this.mappingSpecText = mappingSpecText;
    }

    @JsonProperty("mappingSpecText")
    public String getMappingSpecText()
    {
        return mappingSpecText;
    }

    @JsonProperty("classificationSpec")
    public void setClassificationSpec(String classificationSpec)
    {
        this.classificationSpec = classificationSpec;
    }

    @JsonProperty("classificationSpec")
    public String getClassificationSpec()
    {
        return classificationSpec;
    }

    @JsonProperty("classifyByName")
    @JsonInclude(Include.NON_NULL)
    public void setClassifyByName(boolean classifyByName)
    {
        this.classifyByName = classifyByName;
    }

    @JsonProperty("classifyByName")
    public boolean getClassifyByName()
    {
        return classifyByName;
    }

    @JsonIgnore
    public void setTargetGeoOntology(EnumSpatialOntology targetOntology)
    {
        this.targetGeoOntology = targetOntology;
    }

    @JsonIgnore
    public EnumSpatialOntology getTargetGeoOntology()
    {
        return targetGeoOntology;
    }

    @JsonProperty("targetGeoOntology")
    public void setTargetGeoOntology(String key)
    {
        this.targetGeoOntology = EnumSpatialOntology.fromKey(key);
    }

    @JsonProperty("targetGeoOntology")
    public String getTargetGeoOntologyAsString()
    {
        return targetGeoOntology.key();
    }

    @JsonProperty("featureSource")
    public void setFeatureSource(String featureSource)
    {
        this.featureSource = featureSource;
    }

    @JsonProperty("featureSource")
    @NotEmpty
    public String getFeatureSource()
    {
        return featureSource;
    }

    @JsonProperty("nsOntology")
    public void setOntologyNamespaceUri(String ontologyNamespaceUri)
    {
        this.ontologyNamespaceUri = ontologyNamespaceUri;
    }

    @JsonProperty("nsOntology")
    @NotEmpty
    @URL
    public String getOntologyNamespaceUri()
    {
        return ontologyNamespaceUri;
    }

    @JsonProperty("nsFeatureURI")
    public void setFeatureNamespaceUri(String uri)
    {
        this.featureNamespaceUri = uri;
    }

    @JsonProperty("nsFeatureURI")
    @NotEmpty
    @URL
    public String getFeatureNamespaceUri()
    {
        return featureNamespaceUri;
    }

    @JsonProperty("nsGeometry")
    public void setGeometryNamespaceUri(String uri)
    {
        this.geometryNamespaceUri = uri;
    }

    @JsonProperty("nsGeometry")
    @NotEmpty
    @URL
    public String getGeometryNamespaceUri()
    {
        return geometryNamespaceUri;
    }

    @JsonProperty("nsClassificationURI")
    public void setClassificationNamespaceUri(String classificationNamespaceUri)
    {
        this.classificationNamespaceUri = classificationNamespaceUri;
    }

    @JsonProperty("nsClassificationURI")
    @NotEmpty
    @URL
    public String getClassificationNamespaceUri()
    {
        return classificationNamespaceUri;
    }

    @JsonProperty("nsClassURI")
    public void setClassNamespaceUri(String classNamespaceUri)
    {
        this.classNamespaceUri = classNamespaceUri;
    }

    @JsonProperty("nsClassURI")
    @NotEmpty
    @URL
    public String getClassNamespaceUri()
    {
        return classNamespaceUri;
    }

    @JsonProperty("nsDataSourceURI")
    public void setDatasourceNamespaceUri(String datasourceNamespaceUri)
    {
        this.datasourceNamespaceUri = datasourceNamespaceUri;
    }

    @JsonProperty("nsDataSourceURI")
    @URL
    public String getDatasourceNamespaceUri()
    {
        return datasourceNamespaceUri;
    }

    public void addPrefix(String prefix, String uri)
    {
        Assert.isTrue(!StringUtils.isEmpty(prefix), "A non-empty prefix is required");
        Assert.isTrue(!StringUtils.isEmpty(uri), "A non-empty namespace URI is required");
        this.prefixKeys.add(prefix);
        this.prefixedNamespaces.add(uri);
    }

    @JsonIgnore
    public void setPrefixes(Map<String, String> prefixes)
    {
        Assert.notNull(prefixes, "Expected a non-null map of prefixes");
        prefixes.forEach(this::addPrefix);
    }

    @JsonIgnore
    public Map<String, String> getPrefixes()
    {
        final List<String> keys = this.prefixKeys;
        final List<String> values = this.prefixedNamespaces;

        final int n = keys.size();
        if (n != values.size()) {
            return null; // lists are expected to have same size
        }

        return IntStream.range(0, n).boxed()
            .collect(Collectors.toMap(keys::get, values::get));
    }

    @JsonIgnore
    @NotNull
    @Valid
    protected List<Prefix> getPrefixList()
    {
        final Map<String, String> prefixes = getPrefixes();
        if (prefixes == null) {
            return null; // A map could not be constructed
        }

        final Set<String> namespaceUris = new HashSet<>(prefixes.values());
        if (namespaceUris.size() < prefixes.size()) {
            return null; // namespace URIs contain duplicates!
        }

        return prefixes.entrySet().stream()
            .map(e -> new Prefix(e.getKey(), e.getValue()))
            .collect(Collectors.toList());
    }

    @JsonIgnore
    @NotNull
    public List<String> getPrefixKeys()
    {
        return Collections.unmodifiableList(this.prefixKeys);
    }

    public void setPrefixKeys(List<String> keys)
    {
        this.prefixKeys = new ArrayList<>(keys);
    }

    @JsonProperty("prefixes")
    public String getPrefixKeysAsString()
    {
        return this.prefixKeys.stream().collect(Collectors.joining(", "));
    }

    @JsonProperty("prefixes")
    public void setPrefixKeysFromString(String s)
    {
        if (StringUtils.isEmpty(s)) {
            this.prefixKeys = Collections.emptyList();
            return;
        }

        this.prefixKeys = Arrays.stream(s.split(",[ ]*"))
            .map(String::trim)
            .collect(Collectors.toCollection(ArrayList::new));
    }

    @JsonIgnore
    @NotNull
    public List<String> getNamespaceUris()
    {
        return Collections.unmodifiableList(this.prefixedNamespaces);
    }

    public void setNamespaceUris(List<String> uris)
    {
        this.prefixedNamespaces = new ArrayList<>(uris);
    }

    @JsonProperty("namespaces")
    public String getNamespaceUrisAsString()
    {
        return this.prefixedNamespaces.stream().collect(Collectors.joining(", "));
    }

    @JsonProperty("namespaces")
    public void setNamespaceUrisFromString(String s)
    {
        if (StringUtils.isEmpty(s)) {
            this.prefixedNamespaces = Collections.emptyList();
            return;
        }

        this.prefixedNamespaces = Arrays.stream(s.split(",[ ]*"))
            .map(String::trim)
            .collect(Collectors.toCollection(ArrayList::new));
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

    @JsonProperty("registerFeatures")
    public boolean getRegisterFeatures()
    {
        return registerFeatures;
    }

    @JsonProperty("registerFeatures")
    public void setRegisterFeatures(boolean registerFeatures)
    {
        this.registerFeatures = registerFeatures;
    }

    @JsonProperty("spatialExtent")
    public String getSpatialExtent()
    {
        return spatialExtent;
    }

    @JsonProperty("spatialExtent")
    public void setSpatialExtent(String spatialExtent)
    {
        this.spatialExtent = spatialExtent;
    }
}
