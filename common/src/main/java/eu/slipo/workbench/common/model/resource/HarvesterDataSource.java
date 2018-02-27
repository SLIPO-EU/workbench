package eu.slipo.workbench.common.model.resource;

/**
 * A data source that uses a harvester implementation
 */
public class HarvesterDataSource extends DataSource 
{
    private static final long serialVersionUID = 1L;

    public HarvesterDataSource() {
        super(EnumDataSourceType.HARVESTER);
    }
    
    @Override
    public int hashCode()
    {
        return 0;
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null || !(obj instanceof HarvesterDataSource))
            return false;
        return true;
    }
}
