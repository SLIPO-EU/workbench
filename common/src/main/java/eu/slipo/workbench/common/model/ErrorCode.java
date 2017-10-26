package eu.slipo.workbench.common.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = GenericErrorCode.class)
public interface ErrorCode
{
    String key();
}
