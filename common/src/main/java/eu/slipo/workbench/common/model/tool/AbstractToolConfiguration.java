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
    
}
