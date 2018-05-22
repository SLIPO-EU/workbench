package eu.slipo.workbench.common.model.tool;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.common.model.poi.EnumOutputType;
import eu.slipo.workbench.common.model.poi.EnumTool;

public interface ToolConfiguration extends Serializable
{
    /**
     * Get the tool this configuration is intended for
     * 
     * @return the constant representing a tool
     */
    EnumTool getTool();
    
    /**
     * Get the (expected) data format for our input
     */
    EnumDataFormat getInputFormat();
    
    /**
     * Set input format
     * 
     * @param inputFormat
     */
    void setInputFormat(EnumDataFormat inputFormat);
    
    /**
     * Set input format (fluent setter)
     * @param inputFormat
     * @return
     */
    default ToolConfiguration withInputFormat(EnumDataFormat inputFormat)
    {
        this.setInputFormat(inputFormat);
        return this;
    }
    
    /**
     * Get a list of our input files
     */
    List<String> getInput();
    
    /**
     * Set input 
     * @param input A list of paths
     */
    void setInput(List<String> input);
    
    /**
     * Set input (fluent setter) 
     * 
     * @param input input A list of paths
     * @return an instance of {@link ToolConfiguration} (may be <tt>this</tt>) bound to given input
     */
    default ToolConfiguration withInput(List<String> input)
    {
        this.setInput(input);
        return this;
    }
    
    /**
     * Get the directory where our expected output will be created (or <tt>null</tt> if not
     * output is expected).
     */
    String getOutputDir();
    
    /**
     * Set output directory. This may be a no-op if a tool does not produce any output.
     * 
     * @param dir A file path
     */
    void setOutputDir(String dir);
    
    /**
     * Get the expected data format for our basic output (or <tt>null</tt> if no output is 
     * expected). 
     */
    EnumDataFormat getOutputFormat();
    
    /**
     * Set output format. This may be a no-op if a tool does not produce any output.
     * @param dataFormat
     */
    void setOutputFormat(EnumDataFormat dataFormat);

    /**
     * Get the list of expected output names (as plain file names) categorized by their 
     * output type. If no output is expected, an empty map should be returned.
     */
    Map<EnumOutputType, List<String>> getOutputNames();
}
