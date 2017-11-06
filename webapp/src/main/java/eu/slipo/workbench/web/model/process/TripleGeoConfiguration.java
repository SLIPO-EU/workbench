package eu.slipo.workbench.web.model.process;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.slipo.workbench.web.model.EnumDataFormat;
import eu.slipo.workbench.web.model.EnumTool;

/**
 * TripleGEO configuration
 *
 */
public class TripleGeoConfiguration extends ToolConfiguration {

    public TripleGeoConfiguration() {
        super(EnumTool.TRIPLE_GEO);
    }

    /**
     * Input format
     */
    @JsonDeserialize(using = EnumDataFormat.Deserializer.class)
    private EnumDataFormat inputFormat;

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

    public EnumDataFormat getInputFormat() {
        return inputFormat;
    }

    public String getOntology() {
        return ontology;
    }

    public String getAttrKey() {
        return attrKey;
    }

    public String getAttrName() {
        return attrName;
    }

    public String getAttrCategory() {
        return attrCategory;
    }

    public String getValIgnore() {
        return valIgnore;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public String getAttrX() {
        return attrX;
    }

    public String getAttrY() {
        return attrY;
    }

    public String getFeatureName() {
        return featureName;
    }

    public String getNsFeatureURI() {
        return nsFeatureURI;
    }

    public String getPrefixFeatureNS() {
        return prefixFeatureNS;
    }

    public String getNsGeometryURI() {
        return nsGeometryURI;
    }

    public String getPrefixGeometryNS() {
        return prefixGeometryNS;
    }

    public String getSourceCRS() {
        return sourceCRS;
    }

    public String getTargetCRS() {
        return targetCRS;
    }

    public String getDefaultLang() {
        return defaultLang;
    }

}
