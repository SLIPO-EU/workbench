package eu.slipo.workbench.common.model.tool;

import java.net.URL;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMultimap;

import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.common.model.poi.EnumTool;
import eu.slipo.workbench.common.model.tool.output.EnumImportDataOutputPart;
import eu.slipo.workbench.common.model.tool.output.InputToOutputNameMapper;
import eu.slipo.workbench.common.model.tool.output.OutputSpec;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ImportDataConfiguration implements ToolConfiguration<ImportData>
{
    private static final long serialVersionUID = 1L;
    
    /**
     * Indicate whether the URL fragment (also called reference) should be used as part
     * of the output name.
     */
    private static final boolean useFragmentForOutputName = true;
    
    /**
     * The source URL
     */
    private URL url;
    
    /**
     * The (expected) data format for the resource pointed by url
     */
    private EnumDataFormat dataFormat;
    
    private String outputDir;
    
    public ImportDataConfiguration() {}
    
    public ImportDataConfiguration(URL url, EnumDataFormat dataFormat) 
    {
        Assert.notNull(url, "A source URL is required");
        this.url = url;
        this.dataFormat = dataFormat;
    }
    
    public ImportDataConfiguration(URL url)
    {
        this(url, null);
    }
    
    @JsonIgnore
    @Override
    public Class<ImportData> getToolType()
    {
        return ImportData.class;
    }
    
    @JsonIgnore
    @Override
    public EnumTool getTool()
    {
        return ToolConfiguration.super.getTool();
    }

    @JsonProperty("dataFormat")
    @Override
    public EnumDataFormat getInputFormat()
    {
        return dataFormat;
    }

    @JsonProperty("dataFormat")
    @Override
    public void setInputFormat(EnumDataFormat inputFormat)
    {
        // The input format is always same as output format (no transformation)
        this.dataFormat = inputFormat;
    }

    @JsonIgnore
    @Override
    public List<String> getInput()
    {
        return Collections.emptyList(); // no direct input for this operation
    }

    @JsonIgnore
    @Override
    public void setInput(List<String> input)
    {
        // no-op
    }
    
    @JsonProperty("url")
    @NotNull
    public URL getUrl()
    {
        return url;
    }
    
    @JsonProperty("url")
    public void setUrl(URL url)
    {
        this.url = url;
    }
    
    @JsonProperty("outputDir")
    @Override
    public String getOutputDir()
    {
        return outputDir;
    }

    @JsonProperty("outputDir")
    @Override
    public void setOutputDir(String dir)
    {
        this.outputDir = dir;
    }

    @JsonIgnore
    @Override
    public EnumDataFormat getOutputFormat()
    {
        return dataFormat;
    }

    @JsonIgnore
    @Override
    public void setOutputFormat(EnumDataFormat dataFormat)
    {
        // The input format is always same as output format (no transformation)
        this.dataFormat = dataFormat;
    }

    @JsonProperty("outputName")
    @NotEmpty
    public String getOutputName()
    {
        Assert.state(url != null, "A non-null URL is expected");
        
        final String path = url.getPath();
        final String fragment = url.getRef();
        Assert.state(!StringUtils.isEmpty(path), "A non-empty path is expected");
        
        String fileName = Paths.get(path).getFileName().toString();
        
        if (fragment != null && useFragmentForOutputName) {
            String name = StringUtils.stripFilenameExtension(fileName);
            String extension = fileName.substring(1 + name.length());
            fileName = String.format("%s-%s.%s", name, fragment, extension);    
        }
        
        return fileName;
    }
    
    @JsonIgnore
    @Override
    public InputToOutputNameMapper<ImportData> getOutputNameMapper()
    {
        final String outputName = getOutputName();
        final OutputSpec outputSpec = OutputSpec.of(outputName, dataFormat);
        return input -> ImmutableMultimap.of(EnumImportDataOutputPart.DOWNLOAD, outputSpec);
    }
}
