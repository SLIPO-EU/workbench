package eu.slipo.workbench.common.model.tool;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.common.model.tool.TriplegeoConfiguration.DataFormat;
import eu.slipo.workbench.common.model.tool.output.EnumReverseTriplegeoOutputPart;
import eu.slipo.workbench.common.model.tool.output.InputToOutputNameMapper;
import eu.slipo.workbench.common.model.tool.output.OutputPart;
import eu.slipo.workbench.common.model.tool.output.OutputSpec;


/**
 * Configuration for Triplegeo reverse transformation
 *
 * @see https://github.com/SLIPO-EU/TripleGeo/blob/master/config/reverse_options.conf.template
 */
public class ReverseTriplegeoConfiguration extends TransformConfiguration<ReverseTriplegeo>
{
    private static final long serialVersionUID = 2L;

    /**
     * This class represents the configuration of a specific version
     */
    public static final String VERSION = "2.0";

    /**
     * The output name (without any file extension)
     */
    public static final String OUTPUT_NAME = "points";
    
    public class OutputNameMapper implements InputToOutputNameMapper<ReverseTriplegeo>
    {
        private OutputNameMapper() {};

        @Override
        public Multimap<OutputPart<ReverseTriplegeo>, OutputSpec> applyToPath(List<Path> inputList)
        {
            Assert.state(outputFormat != null, "The output format is not specified");
            
            // The reverse transformation produces a single output (e.g. points.zip)
           
            final String outputFileName = OUTPUT_NAME + ".zip";
            final String statsFileName = OUTPUT_NAME + "_metadata" + ".json";
            
            return ImmutableMultimap.of(
                EnumReverseTriplegeoOutputPart.TRANSFORMED, OutputSpec.of(outputFileName, outputFormat),
                EnumReverseTriplegeoOutputPart.TRANSFORMED_METADATA, OutputSpec.of(statsFileName, EnumDataFormat.JSON));
        }
    }

    //
    // Member data
    //

    /**
     * A profile for setting default configuration values
     */
    private String _profile;

    /**
     * A file containing a user-specified SELECT query (in SPARQL) that will retrieve
     * results from the input RDF triples. This query should conform with the underlying
     * ontology of the input RDF triples.
     */
    private String sparqlFile;

    /**
     * A field delimiter for records (meaningful only for CSV output).
     */
    private String delimiter = ";";

    /**
     * A quote character for records (meaningful only for CSV output)
     */
    private String quote= "\"";

    /**
     * The coordinate reference system (CRS) for input data (eg "EPSG:4326")
     */
    private String sourceCRS;

    /**
     * The coordinate reference system (CRS) for output data (e.g "EPSG:4326")
     */
    private String targetCRS;

    /**
     * The default language for labels created in output RDF. The default is "en".
     */
    private String defaultLang = "en";

    /**
     * Optional parameter for the encoding (character set) for strings in the input data.
     * If not specified, UTF-8 encoding is assumed.
     */
    private String encoding = "UTF-8";

    /**
     * A default constructor
     */
    public ReverseTriplegeoConfiguration()
    {
        this._version = VERSION;
        this.inputFormat = EnumDataFormat.N_TRIPLES;
        this.outputFormat = EnumDataFormat.CSV;
    }

    @JsonIgnore
    @Override
    public Class<ReverseTriplegeo> getToolType()
    {
        return ReverseTriplegeo.class;
    }

    @Override
    @JsonIgnore
    public String getVersion()
    {
        return _version;
    }

    //
    // Getters / Setters
    //

    @JsonProperty("profile")
    public void setProfile(String profile)
    {
        this._profile = profile;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("profile")
    public String getProfile()
    {
        return _profile;
    }

    @JsonIgnore
    @Override
    public EnumDataFormat getInputFormat()
    {
        return inputFormat;
    }

    @JsonIgnore
    @Override
    public void setInputFormat(EnumDataFormat inputFormat)
    {
        this.inputFormat = EnumDataFormat.N_TRIPLES;
    }

    @JsonIgnore
    @NotEmpty
    public String getInputFormatAsString()
    {
        DataFormat dataFormat = inputFormat == null? null : DataFormat.from(inputFormat);
        return dataFormat == null? null : dataFormat.key();
    }

    @JsonProperty("serialization")
    @JsonAlias({ "inputFormat" })
    public void setInputFormat(String inputFormat)
    {
        DataFormat f = DataFormat.from(inputFormat);
        Assert.notNull(f, "The key [" + inputFormat + "] does not map to a data format");
        setInputFormat(f.dataFormat());
    }

    @JsonProperty("serialization")
    public String getSerializationFormat()
    {
        return getInputFormatAsString();
    }
    
    @JsonIgnore
    @Override
    public void setInput(List<String> input)
    {
        super.setInput(input);
    }

    @JsonIgnore
    @Override
    public void setInput(String input)
    {
        super.setInput(input);
    }

    @Override
    public void clearInput()
    {
        super.clearInput();
    }

    @JsonIgnore
    @NotNull
    @Override
    public List<String> getInput()
    {
        return this.input;
    }

    @JsonAlias({ "inputFiles", "input" })
    public void setInputFromString(String inputFiles)
    {
        if (!StringUtils.isEmpty(inputFiles)) {
            this.input = Collections.unmodifiableList(
                Arrays.asList(inputFiles.split(File.pathSeparator)));
        } else {
            this.input = Collections.emptyList();
        }
    }

    @JsonProperty("inputFiles")
    public String getInputAsString()
    {
        if (input.isEmpty()) {
            return null;
        }

        return input.stream().collect(Collectors.joining(File.pathSeparator));
    }

    @JsonIgnore
    @Override
    public EnumDataFormat getOutputFormat()
    {
        return outputFormat;
    }

    @Override
    @JsonIgnore
    public void setOutputFormat(EnumDataFormat outputFormat)
    {
        this.outputFormat = outputFormat;
    }

    @JsonProperty("outputFormat")
    @NotEmpty
    public String getOutputFormatAsString()
    {
        DataFormat dataFormat = outputFormat == null? null : DataFormat.from(outputFormat);
        return dataFormat == null? null : dataFormat.key();
    }

    @JsonProperty("outputFormat")
    public void setOutputFormat(String outputFormat)
    {
        DataFormat f = DataFormat.from(outputFormat);
        Assert.notNull(f, "The key [" + outputFormat + "] does not map to a data format");
        setOutputFormat(f.dataFormat());
    }

    @Override
    @JsonProperty("outputDir")
    public void setOutputDir(String outputDir)
    {
        this.outputDir = outputDir;
    }

    @Override
    @JsonProperty("outputDir")
    public String getOutputDir()
    {
        return outputDir;
    }

    @JsonProperty("tmpDir")
    public void setTmpDir(String tmpDir)
    {
        this.tmpDir = tmpDir;
    }

    @JsonProperty("tmpDir")
    public String getTmpDir()
    {
        return tmpDir;
    }

    @JsonIgnore
    @Override
    public InputToOutputNameMapper<ReverseTriplegeo> getOutputNameMapper()
    {
        return new OutputNameMapper();
    }

    @JsonProperty("sparqlFile")
    public void setSparqlFile(String sparqlFile)
    {
        this.sparqlFile = sparqlFile;
    }

    @JsonProperty("sparqlFile")
    @NotEmpty
    public String getSparqlFile()
    {
        return sparqlFile;
    }

    @JsonProperty("delimiter")
    public void setDelimiter(String delimiter)
    {
        this.delimiter = delimiter;
    }

    @JsonProperty("delimiter")
    @NotEmpty
    public String getDelimiter()
    {
        return delimiter;
    }

    @JsonProperty("sourceCRS")
    public void setSourceCRS(String sourceCRS)
    {
        this.sourceCRS = sourceCRS;
    }

    @JsonProperty("sourceCRS")
    @Pattern(regexp = "epsg:(\\d)+", flags = {Pattern.Flag.CASE_INSENSITIVE})
    public String getSourceCRS()
    {
        return sourceCRS;
    }

    @JsonProperty("targetCRS")
    public void setTargetCRS(String targetCRS)
    {
        this.targetCRS = targetCRS;
    }

    @JsonProperty("targetCRS")
    @Pattern(regexp = "epsg:(\\d)+", flags = {Pattern.Flag.CASE_INSENSITIVE})
    public String getTargetCRS()
    {
        return targetCRS;
    }

    @JsonProperty("defaultLang")
    public String getDefaultLang()
    {
        return defaultLang;
    }

    @JsonProperty("defaultLang")
    public void setDefaultLang(String defaultLang)
    {
        this.defaultLang = defaultLang;
    }

    @JsonProperty("encoding")
    public String getEncoding()
    {
        return encoding;
    }

    @JsonProperty("encoding")
    public void setEncoding(String encoding)
    {
        this.encoding = encoding;
    }

    @JsonProperty("quote")
    public String getQuote()
    {
        return quote;
    }

    @JsonProperty("quote")
    public void setQuote(String quote)
    {
        this.quote = quote;
    }
}
