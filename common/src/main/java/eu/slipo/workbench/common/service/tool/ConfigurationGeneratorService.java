package eu.slipo.workbench.common.service.tool;

import java.io.IOException;

import eu.slipo.workbench.common.model.tool.EnumConfigurationFormat;

/**
 * A facade service for generating textual configuration from configuration beans.   
 */
public interface ConfigurationGeneratorService
{
    /**
     * Generate configuration from a given configuration bean. 
     * 
     * @param source A bean as a source of properties
     * @param configFormat The desired configuration format
     * @return a string that textually represents the configuration source
     * 
     * @throws IOException if an i/o exception happens during underlying conversions
     * @throws UnsupportedOperationException if given {@code configFormat} is not supported
     *   for the actual configuration object.
     */
    String generate(Object source, EnumConfigurationFormat configFormat)
        throws IOException, UnsupportedOperationException;
}
