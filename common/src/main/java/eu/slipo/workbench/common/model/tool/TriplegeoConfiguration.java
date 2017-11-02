package eu.slipo.workbench.common.model.tool;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import eu.slipo.workbench.common.model.poi.EnumDataFormat;

// Todo Add bean getters/setters
// Todo Add validation constraints to TriplegeoConfiguration
// Todo Add JSON-related annotations to TriplegeoConfiguration

/**
 * Configuration for the Triplegeo tool
 */
public class TriplegeoConfiguration extends AbstractToolConfiguration
{
    private static final long serialVersionUID = 1L;

    public static class DataFormat
    {
        private final List<String> keys;
        
        public DataFormat(String ...keys)
        {
            Assert.notEmpty(keys, "The keys must not be empty");
            this.keys = Collections.unmodifiableList(Arrays.asList(keys));
        }
        
        public String key()
        {
            return keys.get(0);
        }
        
        public List<String> getKeys()
        {
            return keys;
        }
    }
    
    public static final Map<EnumDataFormat, DataFormat> DATA_FORMATS = initializeDataFormats();

    private static Map<EnumDataFormat, DataFormat> initializeDataFormats()
    {
        EnumMap<EnumDataFormat, DataFormat> map = new EnumMap<>(EnumDataFormat.class);
        
        map.put(EnumDataFormat.CSV, new DataFormat("CSV"));
        map.put(EnumDataFormat.GEOJSON, new DataFormat("GEOJSON"));
        map.put(EnumDataFormat.GPX, new DataFormat("GPX"));
        map.put(EnumDataFormat.N3, new DataFormat("N3"));
        map.put(EnumDataFormat.N_TRIPLES, new DataFormat("N-TRIPLES"));
        map.put(EnumDataFormat.OSM, new DataFormat("OSM"));
        map.put(EnumDataFormat.TURTLE, new DataFormat("TURTLE", "TTL"));
        map.put(EnumDataFormat.RDF_XML, new DataFormat("RDF", "RDF/XML"));
        map.put(EnumDataFormat.RDF_XML_ABBREV, new DataFormat("RDF/XML-ABBREV"));
        map.put(EnumDataFormat.SHAPEFILE, new DataFormat("SHAPEFILE", "SHP"));
        
        return map;
    }
    
    public static EnumDataFormat lookupDataFormat(String key)
    {
        for (EnumDataFormat f: DATA_FORMATS.keySet())
            if (DATA_FORMATS.get(f).keys.indexOf(key) >= 0)
                return f;
        return null;
    }

    private String targetOntology;

    /**
     * The name of the field holding a unique identifier for each input record
     */
    private String attrKey;

    /**
     * The name of the field from which names will be extracted.
     */
    private String attrName;

    /**
     * The name of the field from which a category will be extracted (e.g. type of points, 
     * road classes). 
     */
    private String attrCategory;

    /**
     * A string literal representing an "unknown" value. A field with an "unknown" value
     * will not be extracted.
     */
    private String valIgnore;

    /**
     * A field delimiter for records (meaningful only for CSV input).
     */
    private String delimiter;

    /**
     * Required for CSV input only (case-insensitive): specify attribute holding X-
     * coordinates of point locations
     */
    private String attrX;

    /**
     * Required for CSV input only (case-insensitive): specify attribute holding Y-
     * coordinates of point locations
     */
    private String attrY;

    /**
     * A user-defined name for the resources that will be created. This is required for
     * constructing the resource URI.
     */
    private String featureName;

    /**
     * The common URI namespace for all generated resources
     */
    private String nsFeatureURI;

    /**
     * A prefix name for the utilized URI namespace (i.e. the one declared with nsFeatureURI)
     */
    private String prefixFeatureNS;

    /**
     * The namespace for the underlying geospatial ontology
     */
    private String nsGeometryURI;

    /**
     * A prefix name for the geospatial ontology (i.e., the one declared with nsGeometryURI)
     */
    private String prefixGeometryNS;

    /**
     * The coordinate reference system (CRS) for input data. Default is EPSG:4326
     */
    private String sourceCRS = "EPSG:4326";

    /**
     * The coordinate reference system (CRS) for output data. Default is EPSG:4326
     */
    private String targetCRS = "EPSG:4326";

    /**
     * The default language for labels created in output RDF. The default is "en".
     */
    private String defaultLang = "en";
    
    //
    // Getters / Setters
    //
    
    @JsonProperty("inputFormat")
    @NotNull
    public EnumDataFormat getInputFormat()
    {
        return inputFormat;
    }
    
    @JsonProperty("inputFormat")
    public void setInputFormat(EnumDataFormat inputFormat)
    {
        this.inputFormat = inputFormat;
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
}
