package eu.slipo.workbench.web.model;

public class TripleGeoConfiguration extends ToolConfiguration {

    public TripleGeoConfiguration() {
        this.operation = EnumOperation.TRANSFORM;
    }

    /**
     * Target ontology
     */
    private String ontology;

    /**
     * Required field name containing unique identifier for each entity (i.e. each record
     * in the shapefile)
     */
    private String attrKey;

    /**
     * Optional field name from which name literals (i.e. strings) will be extracted.
     */
    private String attrName;

    /**
     * Optional field name from which classification literals (e.g. type of points, road
     * classes etc.) will be extracted.
     */
    private String attrCategory;

    /**
     * Required parameter that specifies particular values (e.g. UNK) in attributes that
     * should not be exported as literals
     */
    private String valIgnore;

    /**
     * Required for CSV input only (case-insensitive): specify delimiter character
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
     * Required parameter that specifies a user- defined name for the resources that will
     * be created. Required for constructing the resource URI
     */
    private String featureName;

    /**
     * Optional parameter.Specify the common URI namespace for all generated resources
     */
    private String nsFeatureURI;

    /**
     * Optional parameter.Define a prefix name for the utilized URI namespace (i.e., the
     * previously declared with nsFeatureURI)
     */
    private String prefixFeatureNS;

    /**
     * Optional parameter.Specify the namespace for the underlying geospatial ontology
     */
    private String nsGeometryURI;

    /**
     * Optional parameter.Define a prefix name for the geospatial ontology (i.e., the
     * previously declared with nsGeometryURI)
     */
    private String prefixGeometryNS;

    /**
     * Optional parameter. Input data reference system. Default is EPSG:4326
     */
    private String sourceCRS = "EPSG:4326";

    /**
     * Optional parameter. Output data reference system. Default is EPSG:4326
     */
    private String targetCRS = "EPSG:4326";

    /**
     * Optional parameters.Default locale for the labels created in the output RDF.By
     * default, the value will be English
     */
    private String defaultLang = "en";

    public String getOntology() {
        return ontology;
    }

    public void setOntology(String ontology) {
        this.ontology = ontology;
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

    public String getValIgnore() {
        return valIgnore;
    }

    public void setValIgnore(String valIgnore) {
        this.valIgnore = valIgnore;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
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

    public String getFeatureName() {
        return featureName;
    }

    public void setFeatureName(String featureName) {
        this.featureName = featureName;
    }

    public String getNsFeatureURI() {
        return nsFeatureURI;
    }

    public void setNsFeatureURI(String nsFeatureURI) {
        this.nsFeatureURI = nsFeatureURI;
    }

    public String getPrefixFeatureNS() {
        return prefixFeatureNS;
    }

    public void setPrefixFeatureNS(String prefixFeatureNS) {
        this.prefixFeatureNS = prefixFeatureNS;
    }

    public String getNsGeometryURI() {
        return nsGeometryURI;
    }

    public void setNsGeometryURI(String nsGeometryURI) {
        this.nsGeometryURI = nsGeometryURI;
    }

    public String getPrefixGeometryNS() {
        return prefixGeometryNS;
    }

    public void setPrefixGeometryNS(String prefixGeometryNS) {
        this.prefixGeometryNS = prefixGeometryNS;
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

}
