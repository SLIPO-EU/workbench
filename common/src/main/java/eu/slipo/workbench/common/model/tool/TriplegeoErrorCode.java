package eu.slipo.workbench.common.model.tool;

import eu.slipo.workbench.common.model.ErrorCode;

/**
 * Triplegeo configuration error codes
 */
public enum TriplegeoErrorCode implements ErrorCode
{
    INVALID_CRS,
    ;

    @Override
    public String key() {
        return (this.getClass().getSimpleName() + '.' + name());
    }

}
