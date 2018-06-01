package eu.slipo.workbench.common.model.tool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.common.model.poi.EnumTool;
import eu.slipo.workbench.common.model.tool.output.InputToOutputNameMapper;


/**
 * An abstract base class for (quite) common tool configuration.
 */
public abstract class AbstractToolConfiguration <T extends AnyTool> implements ToolConfiguration<T>
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
     * The version of the configuration model
     */
    protected String _version;

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

    //
    // Basic getters/setters or helper methods
    //
    
    //
    // Note: Apart from getTool/getToolType/getOutputMapper, do not place serialization-related (e.g. JsonIgnore,
    // JsonProperty) annotations here. This kind of annotation should be present on our concrete subclasses (by 
    // overriding a specific method).
    //
    
    @JsonIgnore
    @Override
    public abstract Class<T> getToolType();
    
    @JsonIgnore
    @Override
    public EnumTool getTool()
    {
        return ToolConfiguration.super.getTool();
    }
    
    @Override
    public EnumDataFormat getInputFormat()
    {
        return inputFormat;
    }
    
    @Override
    public void setInputFormat(EnumDataFormat inputFormat)
    {
        this.inputFormat = inputFormat;
    }
    
    @Override
    public List<String> getInput()
    {
        return input;
    }
    
    @Override
    public void setInput(List<String> input)
    {
        this.input = Collections.unmodifiableList(new ArrayList<>(input));
    }
    
    public void setInput(String input)
    {
        this.input = Collections.singletonList(input);
    }
    
    public void clearInput()
    {
        this.input = Collections.emptyList();
    }
    
    @Override
    public String getOutputDir()
    {
        return outputDir;
    }
    
    @Override
    public void setOutputDir(String dir)
    {
       this.outputDir = dir;
    }

    @Override
    public EnumDataFormat getOutputFormat()
    {
        return outputFormat;
    }
    
    @Override
    public void setOutputFormat(EnumDataFormat dataFormat)
    {
        this.outputFormat = dataFormat;
    }
    
    public String getVersion() 
    {
        return _version;
    }

    public void setVersion(String version) 
    {
        this._version = version;
    }
    
    @JsonIgnore
    @Override
    public abstract InputToOutputNameMapper<T> getOutputNameMapper();
}
