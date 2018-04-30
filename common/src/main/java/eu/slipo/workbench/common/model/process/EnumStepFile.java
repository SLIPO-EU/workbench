package eu.slipo.workbench.common.model.process;

import org.springframework.util.Assert;

import eu.slipo.workbench.common.model.poi.EnumOutputType;

/**
 * Step input/output file types
 */
public enum EnumStepFile 
{
    /**
     * Tool configuration
     */
    CONFIGURATION,
    
    /**
     * Input file
     */
    INPUT,
    
    /**
     * Output file
     */
    OUTPUT,
    
    /**
     * Sample data collected during step execution
     */
    SAMPLE,
    
    /**
     * Tool specific or aggregated KPI data
     */
    
    KPI,
    /**
     * Tool specific QA data
     */
    QA,
    ;

    public static EnumStepFile from(EnumOutputType outputType)
    {
        Assert.notNull(outputType, "A non-null outputType is expected");
        
        EnumStepFile e = null;
        switch (outputType) {
        case OUTPUT:
            e = EnumStepFile.OUTPUT;
            break;
        case SAMPLE:
            e = EnumStepFile.SAMPLE;
            break;
        case KPI:
            e = EnumStepFile.KPI;
            break;
        case QA:
            e = EnumStepFile.QA;
            break;
        default:
            Assert.state(false, "Did not expect an outputType of [" + outputType + "]");
            break;
        }
        
        return e;
    }
    
    public static EnumStepFile fromString(String value) 
    {
        for (EnumStepFile item : EnumStepFile.values()) {
            if (item.name().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return null;
    }

}
