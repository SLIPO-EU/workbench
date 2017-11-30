package eu.slipo.workbench.web.model.process;

import eu.slipo.workbench.web.model.EnumTool;
import eu.slipo.workbench.web.model.resource.DataSource;

/**
 * TripleGEO configuration
 *
 */
public class TripleGeoConfiguration extends ToolConfiguration {

    private DataSource dataSource;

    private TripleGeoSettings settings;

    public TripleGeoConfiguration() {
        super(EnumTool.TRIPLE_GEO);
    }

    public TripleGeoConfiguration(DataSource dataSource, TripleGeoSettings settings) {
        super(EnumTool.TRIPLE_GEO);

        this.dataSource = dataSource;
        this.settings = settings;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public TripleGeoSettings getSettings() {
        return settings;
    }

    public void setSettings(TripleGeoSettings settings) {
        this.settings = settings;
    }

}
