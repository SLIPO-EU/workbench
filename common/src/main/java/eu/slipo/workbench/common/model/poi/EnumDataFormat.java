package eu.slipo.workbench.common.model.poi;

import java.util.List;

/**
 * Enumeration for supported data formats.
 */
public enum EnumDataFormat
{
    /**
     * Comma-Separated Values
     */
    CSV(1, "csv"),
    
    /**
     * GPS Exchange Format
     */
    GPX(2, "gpx"),
    
    /**
     * JSON encoded geographic data structures
     */
    GEOJSON(3, "geojson"),
    
    /**
     * Open Street Maps XML format
     */
    OSM(4, "xml"),
    
    /**
     * ESRI shape file
     */
    SHAPEFILE(5, "shp"),
    
    /**
     * W3C standard RDF serialization format
     */
    RDF_XML(6, "rdf"),
    
    /**
     * A format using the RDF/XML abbreviations to provide a more compact readable format
     */
    RDF_XML_ABBREV(7, "rdf"),
    
    /**
     * Turtle, a compact, human-friendly format
     */
    TURTLE(8, "ttl"),
    
    /**
     * N-Triples, a very simple, easy-to-parse, line-based format that is not as compact
     * as Turtle
     */
    N_TRIPLES(9, "nt"),
    
    /**
     * N3 or Notation3, a non-standard serialization that is very similar to Turtle, but
     * has some additional features, such as the ability to define inference rules
     */
    N3(10, "n3");
    
    private final int value;
    
    /**
     * The default filename extension for files of this data format.
     */
    private final String filenameExtension;
    
    private EnumDataFormat(int value, String filenameExtension) 
    {
        this.value = value;
        this.filenameExtension = filenameExtension;
    }

    public int getValue() 
    {
        return value;
    }
    
    public String getFilenameExtension()
    {
        return filenameExtension;
    }
    
    public static EnumDataFormat fromString(String name)
    {
        if (name != null && name.length() > 0) {
            for (EnumDataFormat e: EnumDataFormat.values())
                if (e.name().equalsIgnoreCase(name))
                    return e;
        }
        
        throw new IllegalArgumentException(
            "No enum constant associated with name: " + name);
    }
    
    public static EnumDataFormat fromInt(int value)
    {
        for (EnumDataFormat e: EnumDataFormat.values())
            if (e.value == value)
                return e;
        
        throw new IllegalArgumentException(
            "No enum constant associated with integer value: " + value);
    }
}
