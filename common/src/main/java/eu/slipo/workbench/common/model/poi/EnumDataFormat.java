package eu.slipo.workbench.common.model.poi;

/**
 * Enumeration for supported data formats.
 */
public enum EnumDataFormat
{
    /**
     * Comma-Separated Values
     */
    CSV(1),
    
    /**
     * GPS Exchange Format
     */
    GPX(2),
    
    /**
     * JSON encoded geographic data structures
     */
    GEOJSON(3),
    
    /**
     * Open Street Maps XML format
     */
    OSM(4),
    
    /**
     * ESRI shape file
     */
    SHAPEFILE(5),
    
    /**
     * W3C standard RDF serialization format
     */
    RDF_XML(6),
    
    /**
     * A format using the RDF/XML abbreviations to provide a more compact readable format
     */
    RDF_XML_ABBREV(7),
    
    /**
     * Turtle, a compact, human-friendly format
     */
    TURTLE(8),
    
    /**
     * N-Triples, a very simple, easy-to-parse, line-based format that is not as compact
     * as Turtle
     */
    N_TRIPLES(9),
    
    /**
     * N3 or Notation3, a non-standard serialization that is very similar to Turtle, but
     * has some additional features, such as the ability to define inference rules
     */
    N3(10);

    private final int value;

    private EnumDataFormat(int value) 
    {
        this.value = value;
    }

    public int getValue() 
    {
        return value;
    }
    
    public static EnumDataFormat valueOf(int value)
    {
        for (EnumDataFormat e: EnumDataFormat.values())
            if (e.value == value)
                return e;
        throw new IllegalArgumentException(
            "No enum constant associated with integer " + value);
    }
}
