package eu.slipo.workbench.command.subcommand;

import java.util.Map;

import org.springframework.stereotype.Component;

import eu.slipo.workbench.command.Command.SubCommand;

@Component("greet")
public class GreetCommand implements SubCommand
{

    @Override
    public void run(Map<String, String> args)
    {
        System.out.println("Hello Spring!");
    }

}
