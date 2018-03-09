package eu.slipo.workbench.common.model.tool;

/**
 * Configuration for DEER
 */
@SuppressWarnings("serial")
public class DeerConfiguration extends AbstractToolConfiguration {

    public DeerConfiguration() {
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
