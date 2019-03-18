package eu.slipo.workbench.web.model.api.process;

import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.common.model.tool.TriplegeoConfiguration;

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
     * values; Remove for any other types of input data.
     */
    private String quote = "\"";

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
    private String defaultLang = "en";

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public EnumDataFormat getInputFormat() {
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

    public void merge(TriplegeoConfiguration configuration) {
        configuration.setProfile(profile);
        configuration.setInputFormat(inputFormat);
        configuration.setEncoding(encoding);
        configuration.setAttrKey(attrKey);
        configuration.setAttrName(attrName);
        configuration.setAttrCategory(attrCategory);
        configuration.setAttrGeometry(attrGeometry);
        configuration.setDelimiter(delimiter);
        configuration.setQuote(quote);
        configuration.setAttrX(attrX);
        configuration.setAttrY(attrY);
        configuration.setFeatureSource(featureSource);
        configuration.setSourceCRS(sourceCRS);
        configuration.setTargetCRS(targetCRS);
        configuration.setDefaultLang(defaultLang);

        configuration.setClassificationSpec(null);
        configuration.setMappingSpec(null);
    }

}
