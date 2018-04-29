package eu.slipo.workbench.common.service.util;

import java.io.IOException;
import java.io.Serializable;

public interface ClonerService
{
    <B extends Serializable> B cloneAsBean(B source) throws IOException;
}
