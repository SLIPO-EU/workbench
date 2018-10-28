package eu.slipo.workbench.common.model.tool.output;

import java.nio.file.Path;

import javax.annotation.Nullable;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import eu.slipo.workbench.common.model.poi.EnumDataFormat;

/**
 * A simple specification for an output result
 */
public class OutputSpec
{
    private final String fileName;
 
    /**
     * The data format (applicable only to output results, not to KPIs or other execution
     * metadata)
     */
    @Nullable
    private final EnumDataFormat dataFormat;

    private OutputSpec(String fileName, EnumDataFormat dataFormat)
    {
        this.fileName = fileName;
        this.dataFormat = dataFormat;
    }
    
    public String fileName()
    {
        return fileName;
    }
    
    public EnumDataFormat dataFormat()
    {
        return dataFormat;
    }
    
    public static OutputSpec of(String fileName, EnumDataFormat dataFormat)
    {
        Assert.isTrue(!StringUtils.isEmpty(fileName), "A non-empty name is required");
        return new OutputSpec(fileName, dataFormat);
    }
    
    public static OutputSpec of(Path path, EnumDataFormat dataFormat)
    {
        Assert.notNull(path, "A path is required");
        Assert.isTrue(path.getNameCount() == 1,
            "A path for a plain file (not nested inside a directory) is expected");
        return of(path.toString(), dataFormat);
    }
    
    public static OutputSpec of(String fileName)
    {
        return of(fileName, null);
    }
    
    public static OutputSpec of(Path path)
    {
        return of(path, null);
    }

    @Override
    public String toString()
    {
        return String.format(
            "OutputSpec [fileName=`%s`, dataFormat=%s]", fileName, dataFormat);
    }
}
