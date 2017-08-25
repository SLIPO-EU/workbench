package eu.slipo.workbench.command.subcommand;

import java.util.Map;

import org.springframework.stereotype.Component;

import eu.slipo.workbench.command.SubCommand;

@Component("greet")
public class GreetCommand implements SubCommand
{
    @Override
    public void run(Map<String, String> args)
    {
        System.out.println("Hello Spring!");
    }

    @Override
    public void run(String a1, Map<String, String> options)
    {
        throw new IllegalArgumentException(
            "Unexpected non-option argument: " + a1);
    }

    @Override
    public void run(String a1, String a2, Map<String, String> options)
    {
        throw new IllegalArgumentException(
            "Unexpected non-option arguments: " + a1 + ", " + a2);
    }

    @Override
    public String getSummary()
    {
        return "greet";
    }

    @Override
    public String getDescription()
    {
        return "Says hello to the world";
    }
}
