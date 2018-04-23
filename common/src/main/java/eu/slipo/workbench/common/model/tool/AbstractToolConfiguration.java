package eu.slipo.workbench.common.model.tool;

import java.util.Collections;
import java.util.List;

import eu.slipo.workbench.common.model.poi.EnumDataFormat;


/**
 * An abstract base class for (quite) common tool configuration.
 */
public abstract class AbstractToolConfiguration extends ToolConfigurationSupport
{
    private static final long serialVersionUID = 1L;

    public static final String OWL_NAMESPACE_URI = "http://www.w3.org/2002/07/owl#";

    public static final String GEOSPARQL_NAMESPACE_URI = "http://www.opengis.net/ont/geosparql#";
    
    public static final String XSD_NAMESPACE_URI = "http://www.w3.org/2001/XMLSchema#";
    
    public static final String RDFS_NAMESPACE_URI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    
    public static final String WGS84POS_NAMESPACE_URI = "http://www.w3.org/2003/01/geo/wgs84_pos#";

    public static final String SLIPO_ONTOLOGY_NAMESPACE_URI = "http://slipo.eu/def#";
    
    public static final String SLIPO_FEATURE_NAMESPACE_URI = "http://slipo.eu/id/poi/";
    
    public static final String SLIPO_CLASSIFICATION_NAMESPACE_URI = "http://slipo.eu/id/classification/";
    
    public static final String SLIPO_CLASS_NAMESPACE_URI = "http://slipo.eu/id/term/";
    
    public static final String SLIPO_DATASOURCE_NAMESPACE_URI = "http://slipo.eu/id/poisource/";
    
    /**
     * The list of input files 
     */
    protected List<String> input = Collections.emptyList();
    
    /**
     * The data format that input files conform to.
     */
    protected EnumDataFormat inputFormat;
    
    /**
     * The expected data format for output.
     */
    protected EnumDataFormat outputFormat;
    
    /**
     * The directory where output will be created.
     */
    protected String outputDir;
    
    /**
     * The directory where temporary files (if any) will be created.
     */
    protected String tmpDir;
    
    public EnumDataFormat getOutputFormat()
    {
        return outputFormat;
    }
    
    public EnumDataFormat getInputFormat()
    {
        return inputFormat;
    }
}
