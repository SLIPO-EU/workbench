package eu.slipo.workbench.command.subcommand;

import java.util.Arrays;
import java.util.Map;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import eu.slipo.workbench.command.Command.SubCommand;

@Component("help")
public class HelpCommand implements SubCommand
{
    @Autowired
    ApplicationContext applicationContext;
    
    private static Logger logger = LoggerFactory.getLogger(HelpCommand.class);
    
    @Override
    public void run(Map<String, String> args)
    {
        List<String> names = Arrays.asList(
            applicationContext.getBeanNamesForType(SubCommand.class));
        
        logger.info(
            "The following commands are available: {}", 
            String.join(", ", names));
    }
}
