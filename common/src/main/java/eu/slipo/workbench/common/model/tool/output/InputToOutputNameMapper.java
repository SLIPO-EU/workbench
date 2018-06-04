package eu.slipo.workbench.common.model.tool.output;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import eu.slipo.workbench.common.model.tool.AnyTool;

@FunctionalInterface
public interface InputToOutputNameMapper <T extends AnyTool> 
{
    /**
     * Return an output map for a given input
     * 
     * @param input A list of input paths
     * @return a multimap mapping an output part ({@link OutputPart}) to a list of output paths
     */
    Multimap<OutputPart<T>, OutputSpec> applyToPath(List<Path> input);
    
    /**
     * Return an output map for a given input
     * @see InputToOutputNameMapper#applyToPath(List)
     */
    default Multimap<OutputPart<T>, OutputSpec> apply(List<String> inputPaths)
    {
        return applyToPath(Lists.transform(inputPaths, Paths::get));
    }
}
