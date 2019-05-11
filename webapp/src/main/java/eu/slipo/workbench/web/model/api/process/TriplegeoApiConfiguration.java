package eu.slipo.workbench.web.model.api.process;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.common.model.tool.TriplegeoConfiguration;
import eu.slipo.workbench.common.model.tool.TriplegeoConfiguration.EnumLevel;

public class TriplegeoApiConfiguration {

    /**
     * A profile for setting default configuration values
     */
    private String profile;

    /**
     * The data format that input files conform to.
     */
    private EnumDataFormat inputFormat;

    /**
     * Optional parameter for the encoding (character set) for strings in the input data.
     * If not specified, UTF-8 encoding is assumed.
     */
    private String encoding;

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
     * Parameter that specifies the name of the geometry column in the input dataset. Omit
     * this parameter if geometry representation is available with columns specifying X,Y
     * coordinates for points; otherwise, this parameter is MANDATORY.
     */
    private String attrGeometry;

    /**
     * A field delimiter for records (meaningful only for CSV input).
     */
    private String delimiter;

    /**
     * Mandatory for CSV input only (case-insensitive): specify quote character for string
     * values; Remove for any other types of input data.
     */
    private String quote;

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
     * A name for the data source provider of input features
     */
    private String featureSource;

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
    private String defaultLang;

    /**
     * A resource location of a YML file containing mappings from input schema to RDF
     * according to a custom ontology.
     */
    private String mappingSpec;

    /**
     * A resource location of a YML/CSV file describing a classification scheme.
     */
    private String classificationSpec;

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public EnumDataFormat getInputFormat() {
        if (inputFormat == null) {
            return EnumDataFormat.UNDEFINED;
        }
        return inputFormat;
    }

    public void setInputFormat(EnumDataFormat inputFormat) {
        this.inputFormat = inputFormat;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public String getAttrKey() {
        return attrKey;
    }

    public void setAttrKey(String attrKey) {
        this.attrKey = attrKey;
    }

    public String getAttrName() {
        return attrName;
    }

    public void setAttrName(String attrName) {
        this.attrName = attrName;
    }

    public String getAttrCategory() {
        return attrCategory;
    }

    public void setAttrCategory(String attrCategory) {
        this.attrCategory = attrCategory;
    }

    public String getAttrGeometry() {
        return attrGeometry;
    }

    public void setAttrGeometry(String attrGeometry) {
        this.attrGeometry = attrGeometry;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public String getQuote() {
        return quote;
    }

    public void setQuote(String quote) {
        this.quote = quote;
    }

    public String getAttrX() {
        return attrX;
    }

    public void setAttrX(String attrX) {
        this.attrX = attrX;
    }

    public String getAttrY() {
        return attrY;
    }

    public void setAttrY(String attrY) {
        this.attrY = attrY;
    }

    public String getFeatureSource() {
        return featureSource;
    }

    public void setFeatureSource(String featureSource) {
        this.featureSource = featureSource;
    }

    public String getSourceCRS() {
        return sourceCRS;
    }

    public void setSourceCRS(String sourceCRS) {
        this.sourceCRS = sourceCRS;
    }

    public String getTargetCRS() {
        return targetCRS;
    }

    public void setTargetCRS(String targetCRS) {
        this.targetCRS = targetCRS;
    }

    public String getDefaultLang() {
        return defaultLang;
    }

    public void setDefaultLang(String defaultLang) {
        this.defaultLang = defaultLang;
    }

    public String getMappingSpec() {
        return mappingSpec;
    }

    public void setMappingSpec(String mappingSpec) {
        this.mappingSpec = mappingSpec;
    }

    public String getClassificationSpec() {
        return classificationSpec;
    }

    public void setClassificationSpec(String classificationSpec) {
        this.classificationSpec = classificationSpec;
    }

    public void merge(TriplegeoConfiguration configuration) {
        configuration.setLevel(EnumLevel.ADVANCED);

        if(StringUtils.isBlank(profile)) {
            // If profile is not set, several fields require additional configuration
            configuration.setNamespaceUris(
                Arrays.asList(new String[] {
                    "http://slipo.eu/def#",
                    "http://www.opengis.net/ont/geosparql#",
                    "http://www.w3.org/2001/XMLSchema#",
                    "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
                    "http://www.w3.org/2003/01/geo/wgs84_pos#"
                })
            );
            configuration.setPrefixKeysFromString("slipo, geo, xsd, rdfs, wgs84_pos");
            configuration.setClassifyByName(false);
        } else {
            configuration.setProfile(profile);
        }

        if (!StringUtils.isBlank(mappingSpec)) {
            configuration.setMappingSpec(mappingSpec);
        }
        if (!StringUtils.isBlank(classificationSpec)) {
            configuration.setClassificationSpec(classificationSpec);
        }

        if ((inputFormat != null) && (inputFormat != EnumDataFormat.UNDEFINED)) {
            configuration.setInputFormat(inputFormat);
        }
        if (!StringUtils.isBlank(encoding)) {
            configuration.setEncoding(encoding);
        }
        if (!StringUtils.isBlank(attrKey)) {
            configuration.setAttrKey(attrKey);
        }
        if (!StringUtils.isBlank(attrName)) {
            configuration.setAttrName(attrName);
        }
        if (!StringUtils.isBlank(attrCategory)) {
            configuration.setAttrCategory(attrCategory);
        }
        if ((inputFormat == EnumDataFormat.CSV)) {
            configuration.setDelimiter(delimiter);
        }
        if ((inputFormat == EnumDataFormat.CSV)) {
            configuration.setQuote(quote);
        }
        configuration.setAttrGeometry(attrGeometry);
        if (!StringUtils.isBlank(attrX)) {
            configuration.setAttrX(attrX);
        }
        if (!StringUtils.isBlank(attrY)) {
            configuration.setAttrY(attrY);
        }
        if (!StringUtils.isBlank(featureSource)) {
            configuration.setFeatureSource(featureSource);
        }
        if (!StringUtils.isBlank(sourceCRS)) {
            configuration.setSourceCRS(sourceCRS);
        }
        if (!StringUtils.isBlank(targetCRS)) {
            configuration.setTargetCRS(targetCRS);
        }
        if (!StringUtils.isBlank(defaultLang)) {
            configuration.setDefaultLang(defaultLang);
        }
    }

}
