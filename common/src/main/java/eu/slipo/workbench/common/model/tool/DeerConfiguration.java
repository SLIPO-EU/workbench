package eu.slipo.workbench.common.model.tool;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimap;

import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.common.model.tool.output.EnumDeerOutputPart;
import eu.slipo.workbench.common.model.tool.output.InputToOutputNameMapper;
import eu.slipo.workbench.common.model.tool.output.OutputPart;
import eu.slipo.workbench.common.model.tool.output.OutputSpec;

/**
 * Configuration for DEER
 */
public class DeerConfiguration extends EnrichConfiguration<Deer>
{
    private static final long serialVersionUID = 1L;

    /**
     * This class represents the configuration of a specific version
     */
    public static final String VERSION = "1.1";
    
    public class OutputNameMapper implements InputToOutputNameMapper<Deer>
    {
        private OutputNameMapper() {}
        
        @Override
        public Multimap<OutputPart<Deer>, OutputSpec> applyToPath(List<Path> inputList)
        {
            Assert.state(outputFormat != null, "The output format is required");
            final String path = "output" + "." + outputFormat.getFilenameExtension();                
            return ImmutableListMultimap.of(
                EnumDeerOutputPart.ENRICHED, OutputSpec.of(Paths.get(path), outputFormat));
        }
    }
    
    /**
     * A profile for setting default configuration values
     */
    private String _profile;
    
    /**
     * The location of the actual configuration (described using an RDF vocabulary).
     * @see https://dice-group.github.io/deer/configuring_deer/
     */
    private String spec;
    
    public DeerConfiguration() 
    {
        this._version = VERSION;
        this.input = Arrays.asList((String) null);
        this.outputDir = "output";
        this.outputFormat = EnumDataFormat.N_TRIPLES;
    }

    @JsonIgnore
    @Override
    public Class<Deer> getToolType()
    {
        return Deer.class;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("profile")
    public String getProfile()
    {
        return _profile;
    }
    
    @JsonProperty("profile")
    public void setProfile(String profile)
    {
        this._profile = profile;
    }
    
    @JsonProperty("inputFormat")
    @Override
    public EnumDataFormat getInputFormat()
    {
        return super.getInputFormat();
    }

    @JsonProperty("inputFormat")
    @Override
    public void setInputFormat(EnumDataFormat inputFormat)
    {
        super.setInputFormat(inputFormat);
    }

    @JsonIgnore
    @NotNull
    @Override
    public List<String> getInput()
    {
        return Collections.unmodifiableList(this.input);
    }

    @JsonIgnore
    @Override
    public void setInput(List<String> inputList)
    {
        Assert.notNull(inputList, "Expected a non-null input list");
        Assert.isTrue(inputList.size() == 1, "Expected a list with a single item");
        this.input.set(0, inputList.get(0));
    }

    @JsonProperty("input")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Override
    public void setInput(String inputPath)
    {
        Assert.isTrue(!StringUtils.isEmpty(inputPath), "Expected a non-empty input path");
        this.input.set(0, inputPath);
    }
    
    @JsonProperty("input")
    public String getInputAsString()
    {
        return this.input.get(0);
    }
    
    @JsonIgnore
    public String getInputPath()
    {
        return this.input.get(0);
    }

    @JsonIgnore
    @Override
    public void clearInput()
    {
        this.input.set(0, null);
    }

    @JsonProperty("outputDir")
    @NotEmpty
    @Override
    public String getOutputDir()
    {
        return super.getOutputDir();
    }

    @JsonProperty("outputDir")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Override
    public void setOutputDir(String dir)
    {
        Assert.notNull(dir, "Expected a non-null directory");
        Assert.isTrue(!Paths.get(dir).isAbsolute(), "Expected a relative directory");
        super.setOutputDir(dir);
    }

    @JsonProperty("outputFormat")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Override
    public void setOutputFormat(EnumDataFormat dataFormat)
    {
        Assert.notNull(dataFormat, "Expected a non-null output format");
        super.setOutputFormat(dataFormat);
    }

    @JsonProperty("outputFormat")
    @NotNull
    @Override
    public EnumDataFormat getOutputFormat()
    {
        return super.getOutputFormat();
    }

    @JsonIgnore
    @Override
    public InputToOutputNameMapper<Deer> getOutputNameMapper()
    {
        return new OutputNameMapper();
    }
    
    @JsonProperty("spec")
    public String getSpec()
    {
        return spec;
    }
    
    @JsonProperty("spec")
    public void setSpec(String spec)
    {
        this.spec = spec;
    }
    
    @JsonIgnore
    @Override
    public String getVersion()
    {
        return super.getVersion();
    }

    @JsonIgnore
    @Override
    public void setVersion(String version)
    {
        super.setVersion(version);
    }
}
