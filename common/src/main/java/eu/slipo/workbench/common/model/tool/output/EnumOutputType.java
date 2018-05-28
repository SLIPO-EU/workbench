package eu.slipo.workbench.common.model.tool.output;

/**
 * Categorize output produced by a tool
 */
public enum EnumOutputType
{
    /**
     * A basic output file, i.e the useful output of a program execution (and the reason
     * for which the program is executed).
     */
    OUTPUT,
    
    /**
     * A sample of the program's basic output. A sample is collected according to some 
     * program-specific logic. 
     */
    SAMPLE,
    
    /**
     * A collection of Key-Performance-Indicator metadata (KPIs) that accompany a program's 
     * execution 
     */
    KPI,
    
    /**
     * A collection of Quality-Assurance (QA) metadata that accompany the program's basic 
     * output.
     */
    QA;
}
