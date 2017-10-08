package eu.slipo.workbench.command.subcommand;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import eu.slipo.workbench.command.SubCommand;

@Component("greet")
public class GreetCommand implements SubCommand
{
    @Override
    public void run(Map<String, List<String>> args)
    {
        System.out.println("Hello Spring!");
    }

    @Override
    public void run(String a1, Map<String, List<String>> options)
    {
        System.err.printf(
            "Unexpected non-option argument (%s). Try `help greet`%n", a1);
    }

    @Override
    public void run(String a1, String a2, Map<String, List<String>> options)
    {
        System.err.printf(
            "Unexpected non-option arguments (%s, %s). Try `help greet`%n",
            a1, a2);
    }

    @Override
    public void run(String a1, String a2, String a3, Map<String, List<String>> options)
    {
        System.err.printf(
            "Unexpected non-option arguments (%s, %s, %s). Try `help greet`%n",
            a1, a2, a3);
    }
    
    @Override
    public String getSummary()
    {
        return "greet";
    }

    @Override
    public String getDescription()
    {
        return "Greet the world";
    }
}
