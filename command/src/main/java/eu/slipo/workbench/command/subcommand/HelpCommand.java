package eu.slipo.workbench.command.subcommand;

import java.util.Arrays;
import java.util.Map;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import eu.slipo.workbench.command.SubCommand;

@Component("help")
public class HelpCommand implements SubCommand
{
    @Autowired
    ApplicationContext applicationContext;
    
    private static Logger logger = LoggerFactory.getLogger(HelpCommand.class);
    
    private SubCommand subcommandByName(String name)
    {
        SubCommand c;
        try {
            c = applicationContext.getBean(name, SubCommand.class);
        } catch (BeansException ex) {
            c = null;
        }
        return c;
    }
    
    private List<String> subcommandNames()
    {
        return Arrays.asList(applicationContext.getBeanNamesForType(SubCommand.class));
    }
    
    @Override
    public void run(Map<String, String> args)
    {
        logger.info(
            "The following subcommands are available: {}", 
            String.join(", ", subcommandNames()));
    }

    @Override
    public void run(String subcommandName, Map<String, String> options)
    {
        // Print help on a given subcommand
        SubCommand c = subcommandByName(subcommandName);
        if (c == null) {
            logger.info("No such command: {}", subcommandName);
        } else {
            logger.info("syntax: {}", c.getSummary());
        }
    }

    @Override
    public void run(String subcommandName, String a2, Map<String, String> options)
    {
        // ignore any extra non-option arguments, just print help on subcommand
        run(subcommandName, options);
    }
    
    @Override
    public String getSummary()
    {
        return "help [<subcommand-name>]";
    }

    @Override
    public String getDescription()
    {
        return "List available commands or help on a specific command";
    }
}
