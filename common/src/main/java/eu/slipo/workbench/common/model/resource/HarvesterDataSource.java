package eu.slipo.workbench.common.model.resource;

/**
 * A data source that uses a harvester implementation
 */
public class HarvesterDataSource extends DataSource {

    public HarvesterDataSource() {
        super(EnumDataSourceType.HARVESTER);
    }

}
