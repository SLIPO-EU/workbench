package eu.slipo.workbench.common.model.poi;

/**
 * Enumeration for supported data formats.
 */
public enum EnumDataFormat
{
    UNDEFINED(null),
    
    /**
     * Comma-Separated Values
     */
    CSV("csv"),
    
    /**
     * GPS Exchange Format
     */
    GPX("gpx"),
    
    /**
     * JSON encoded geographic data structures
     */
    GEOJSON("geojson"),
    
    /**
     * Open Street Maps XML format
     */
    OSM("xml"),
    
    /**
     * ESRI shape file
     */
    SHAPEFILE("shp"),
    
    /**
     * W3C standard RDF serialization format
     */
    RDF_XML("rdf"),
    
    /**
     * A format using the RDF/XML abbreviations to provide a more compact readable format
     */
    RDF_XML_ABBREV("rdf"),
    
    /**
     * Turtle, a compact, human-friendly format
     */
    TURTLE("ttl"),
    
    /**
     * N-Triples, a very simple, easy-to-parse, line-based format that is not as compact
     * as Turtle
     */
    N_TRIPLES("nt"),
    
    /**
     * N3 or Notation3, a non-standard serialization that is very similar to Turtle, but
     * has some additional features, such as the ability to define inference rules
     */
    N3("n3");
    
    /**
     * The default filename extension for files of this data format.
     */
    private final String filenameExtension;
    
    private EnumDataFormat(String filenameExtension) 
    {
        this.filenameExtension = filenameExtension;
    }
    
    public String getFilenameExtension()
    {
        return filenameExtension;
    }
    
    public String getKey() 
    {
        return (this.getClass().getSimpleName() + '.' + name());
    }
    
    public static EnumDataFormat fromString(String name)
    {
        if (name != null && name.length() > 0) {
            for (EnumDataFormat e: EnumDataFormat.values())
                if (e.name().equalsIgnoreCase(name))
                    return e;
        }
        return null;
    }

}
