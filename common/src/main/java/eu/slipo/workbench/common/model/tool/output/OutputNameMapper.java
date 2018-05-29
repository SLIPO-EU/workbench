package eu.slipo.workbench.common.model.tool.output;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.google.common.collect.Lists;

import eu.slipo.workbench.common.model.tool.AnyTool;

public interface OutputNameMapper <T extends AnyTool> 
    extends Function<List<String>, Map<? extends OutputPart<T>, List<String>>>
{
    default Map<? extends OutputPart<T>, List<String>> applyToPath(List<Path> inputPaths)
    {
        return apply(Lists.transform(inputPaths, Path::toString));
    }
}
