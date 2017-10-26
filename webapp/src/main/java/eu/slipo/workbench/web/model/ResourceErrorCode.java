package eu.slipo.workbench.web.model;

import eu.slipo.workbench.common.model.ErrorCode;

public enum ResourceErrorCode implements ErrorCode
{
    UNKNOWN,
    DATASOURCE_NOT_SUPPORTED,
    ;

    @Override
    public String key()
    {
        return (this.getClass().getSimpleName() + '.' + name());
    }

}
