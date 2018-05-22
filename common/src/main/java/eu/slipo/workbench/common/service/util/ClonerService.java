package eu.slipo.workbench.common.service.util;

import java.io.IOException;
import java.io.Serializable;

public interface ClonerService
{
    Object cloneAsBean(Object source) throws IOException;
    
    <T> T cloneAsBean(Object source, Class<T> targetType) throws IOException;
}
