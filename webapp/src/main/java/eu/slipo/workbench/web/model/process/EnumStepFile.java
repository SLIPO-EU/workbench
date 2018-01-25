package eu.slipo.workbench.web.model.process;

/**
 * Step input/output file types
 */
public enum EnumStepFile {
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

    public static EnumStepFile fromString(String value) {
        for (EnumStepFile item : EnumStepFile.values()) {
            if (item.name().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return null;
    }

}
