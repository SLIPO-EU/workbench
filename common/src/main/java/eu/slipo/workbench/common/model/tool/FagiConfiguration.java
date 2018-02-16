package eu.slipo.workbench.common.model.tool;

/**
 * Configuration for FAGI
 */
@SuppressWarnings("serial")
public class FagiConfiguration extends AbstractToolConfiguration {

    public FagiConfiguration() {
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
