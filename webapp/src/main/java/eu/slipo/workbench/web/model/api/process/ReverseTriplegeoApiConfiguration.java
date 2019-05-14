package eu.slipo.workbench.web.model.api.process;

import org.apache.commons.lang3.StringUtils;

import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.common.model.tool.ReverseTriplegeoConfiguration;

public class ReverseTriplegeoApiConfiguration {

    /**
     * A profile for setting default configuration values
     */
    private String profile;

    /**
     * A file containing a user-specified SELECT query (in SPARQL) that will retrieve
     * results from the input RDF triples. This query should conform with the underlying
     * ontology of the input RDF triples.
     */
    private String sparqlFile;

    /**
     * A field delimiter for records (meaningful only for CSV output).
     */
    private String delimiter = "|";

    /**
     * A quote character for records (meaningful only for CSV output)
     */
    private String quote = "\"";

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
     * The expected data format for output.
     */
    private EnumDataFormat outputFormat;

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getSparqlFile() {
        return sparqlFile;
    }

    public void setSparqlFile(String sparqlFile) {
        this.sparqlFile = sparqlFile;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public String getQuote() {
        return quote;
    }

    public void setQuote(String quote) {
        this.quote = quote;
    }

    public String getSourceCRS() {
        return sourceCRS;
    }

    public void setSourceCRS(String sourceCRS) {
        this.sourceCRS = sourceCRS;
    }

    public String getTargetCRS() {
        return targetCRS;
    }

    public void setTargetCRS(String targetCRS) {
        this.targetCRS = targetCRS;
    }

    public String getDefaultLang() {
        return defaultLang;
    }

    public void setDefaultLang(String defaultLang) {
        this.defaultLang = defaultLang;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public EnumDataFormat getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(EnumDataFormat outputFormat) {
        this.outputFormat = outputFormat;
    }

    public void merge(ReverseTriplegeoConfiguration configuration) {
        if (!StringUtils.isBlank(defaultLang)) {
            configuration.setDefaultLang(defaultLang);
        }
        if (!StringUtils.isBlank(delimiter) && outputFormat == EnumDataFormat.CSV) {
            configuration.setDelimiter(delimiter);
        }
        if (!StringUtils.isBlank(encoding)) {
            configuration.setEncoding(encoding);
        }
        if ((outputFormat != null) && (outputFormat != EnumDataFormat.UNDEFINED)) {
            configuration.setOutputFormat(outputFormat);
        } else {
            configuration.setOutputFormat(EnumDataFormat.CSV);
        }
        configuration.setProfile(profile);
        if (outputFormat == EnumDataFormat.CSV) {
            configuration.setQuote(quote);
        }
        configuration.setSourceCRS(sourceCRS);
        if (!StringUtils.isBlank(sparqlFile)) {
            configuration.setSparqlFile(sparqlFile);
        }
        configuration.setTargetCRS(targetCRS);
    }

}
