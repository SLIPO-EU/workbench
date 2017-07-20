package eu.slipo.workbench.command.subcommand;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import eu.slipo.workbench.command.Command.SubCommand;

@Component("help")
public class HelpCommand implements SubCommand
{
    @Autowired
    ApplicationContext ctx;
    
    @Override
    public void run(Map<String, String> args)
    {
        System.out.println("Available commands:");
        for (String name: ctx.getBeanNamesForType(SubCommand.class))
            System.out.println("  * " + name);
    }

}
