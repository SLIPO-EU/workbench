package eu.slipo.workbench.common.model.tool;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.common.model.poi.EnumTool;
import eu.slipo.workbench.common.model.tool.output.EnumOutputType;
import eu.slipo.workbench.common.model.tool.output.InputToOutputNameMapper;

public interface ToolConfiguration <T extends AnyTool> extends Serializable
{
    Class<T> getToolType();
    
    /**
     * Get the tool this configuration is intended for
     * @return a constant that represents a tool
     */
    default EnumTool getTool() 
    {
        return EnumTool.fromType(getToolType());
    }
    
    /**
     * Get the (expected) data format for our input
     */
    EnumDataFormat getInputFormat();
    
    /**
     * Set input format
     * @param inputFormat
     */
    void setInputFormat(EnumDataFormat inputFormat);
    
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
     * Get the directory where our expected output will be created (or <tt>null</tt> if not
     * output is expected).
     */
    String getOutputDir();
    
    /**
     * Set output directory. This may be a no-op if a tool does not produce any output.
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
     * Get a function to map input names to (expected) output names
     */
    InputToOutputNameMapper<T> getOutputNameMapper();
}
