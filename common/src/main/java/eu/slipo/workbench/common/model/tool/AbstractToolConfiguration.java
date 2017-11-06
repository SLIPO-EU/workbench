package eu.slipo.workbench.common.model.tool;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.slipo.workbench.common.model.poi.EnumDataFormat;


/**
 * An abstract base class for (quite) common tool configuration.
 */
@SuppressWarnings("serial")
public abstract class AbstractToolConfiguration extends ToolConfigurationSupport
{
    /**
     * The list of input files 
     */
    private List<Path> input = Collections.emptyList();
    
    @JsonIgnore
    protected List<Path> getInput()
    {
        return input;
    }
    
    @JsonIgnore
    protected void setInput(List<Path> input)
    {
        this.input = Collections.unmodifiableList(new ArrayList<>(input));
    }
    
    @JsonIgnore
    protected void setInput(Path[] input)
    {
        this.input = Collections.unmodifiableList(
            Arrays.stream(input).collect(Collectors.toList()));
    }
    
    @JsonIgnore
    protected void setInput(String[] input)
    {
        this.input = Collections.unmodifiableList(
            Arrays.stream(input).map(Paths::get).collect(Collectors.toList()));
    }
    
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
    protected Path outputDir;
    
    /**
     * The directory where temporary files (if any) will be created.
     */
    protected Path tmpDir;
    
}
