package eu.slipo.workbench.command.subcommand;

import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;

import java.util.List;

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
    
    private Map<String, SubCommand> subcommands()
    {
        return applicationContext.getBeansOfType(SubCommand.class);
    }
    
    @Override
    public void run(Map<String, String> args)
    {
        for (Entry<String, SubCommand> p: subcommands().entrySet()) {
            SubCommand c = p.getValue();
            System.out.printf("%-15.12s %s%n", p.getKey(), c.getDescription());
        }
    }

    @Override
    public void run(String subcommandName, Map<String, String> options)
    {
        // Print help on a given subcommand
        SubCommand c = subcommandName.equals("help")? 
            this : subcommandByName(subcommandName);
        if (c == null) {
            System.out.printf("No such command: %s%n", subcommandName);
        } else {
            System.out.println(c.getSummary());
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
        return "List available commands or get help on a specific command";
    }
}
