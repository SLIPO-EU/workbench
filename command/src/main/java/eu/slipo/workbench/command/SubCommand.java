package eu.slipo.workbench.command;

import java.util.List;
import java.util.Map;

/**
 * A simple interface for a subcommand.
 *  
 * Supports some common forms of command-line (positional and option-like) arguments.
 */
public interface SubCommand
{
    /**
     * Provide help on command's syntax. 
     * 
     * @return an one-liner summary
     */
    String getSummary();
    
    /**
     * Provide a description for the duties of this command.
     * 
     * @return
     */
    String getDescription();
    
    /**
     * Run with options (no positional arguments).
     * 
     * @param options
     */
    void run(Map<String, List<String>> options);
    
    /**
     * Run with 1 positional (non-option) argument and a map of options.
     * 
     * @param a1 the 1st positional argument
     * @param options
     */
    void run(String a1, Map<String, List<String>> options);
    
    /**
     * Run with 2 positional (non-option) arguments and a map of options.
     * 
     * @param a1 the 1st positional argument
     * @param a2 the 2nd positional argument
     * @param options
     */
    void run(String a1, String a2, Map<String, List<String>> options);
    
    /**
     * Run with 3 positional (non-option) arguments and a map of options.
     * 
     * @param a1 the 1st positional argument
     * @param a2 the 2nd positional argument
     * @param a3 the 3rd positional argument
     * @param options
     */
    void run(String a1, String a2, String a3, Map<String, List<String>> options);
}
