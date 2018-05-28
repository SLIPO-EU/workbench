package eu.slipo.workbench.common.model.tool.output;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import eu.slipo.workbench.common.model.tool.AnyTool;

public interface OutputNameMapper <T extends AnyTool> 
    extends Function<List<String>, Map<? extends OutputPart<T>, List<String>>>
{
}
