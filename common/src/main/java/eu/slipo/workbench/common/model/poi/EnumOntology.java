package eu.slipo.workbench.common.model.poi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.util.StringUtils;

public enum EnumOntology
{
    GEOSPARQL("GeoSPARQL"),
    WGS84_POS("wgs84_pos"),
    VIRTUOSO("Virtuoso");
    
    private List<String> keys;
    
    /**
     * Create an enumerated instance 
     * 
     * @param key The basic key as an identifier for this ontology
     * @param aliases A list of aliases that also serve as identifiers
     */
    private EnumOntology(String key, String ...aliases)
    {
        this.keys = new ArrayList<>();
        this.keys.add(key);
        this.keys.addAll(Arrays.asList(aliases));
    }
    
    public List<String> keys()
    {
        return Collections.unmodifiableList(keys);
    }
    
    public String key()
    {
        return keys.get(0);
    }
    
    public static EnumOntology fromKey(String key)
    {
        if (!StringUtils.isEmpty(key))
            for (EnumOntology e: EnumOntology.values())
                if (e.name().equals(key) || e.keys.indexOf(key) >= 0)
                    return e;
        
        throw new IllegalArgumentException("No enum constant associated with key: " + key);
    }
}
