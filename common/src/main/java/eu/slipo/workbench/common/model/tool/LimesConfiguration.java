package eu.slipo.workbench.common.model.tool;

/**
 * Configuration for LIMES
 */
@SuppressWarnings("serial")
public class LimesConfiguration extends AbstractToolConfiguration {

    public LimesConfiguration() {
    }

    // TODO: Remove
    private String temp;

    public String getTemp() {
        return temp;
    }

    public void setTemp(String temp) {
        this.temp = temp;
    }

}
