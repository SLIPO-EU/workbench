package eu.slipo.workbench.command;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class Command implements ApplicationRunner
{
    public interface SubCommand
    {
        void run(Map<String, String> args);
    }
    
    private static Logger logger = LoggerFactory.getLogger(Command.class);
    
    @Autowired
    ApplicationContext ctx;
    
    @Override
    public void run(ApplicationArguments args) throws Exception
    {
        List<String> p = args.getNonOptionArgs();
      
        // Find a proper SubCommand bean to delegate to 
        
        String subname = p.isEmpty()? "help" : p.get(0);
        SubCommand subcommand = ctx.getBean(subname, SubCommand.class);
        
        logger.debug("About to execute subcommand: {}", subcommand);
        
        // Build proper arguments to feed SubCommand
        
        HashMap<String, String> args1 = new HashMap<>();
        for (String name: args.getOptionNames()) {
            List<String> vals = args.getOptionValues(name);
            args1.put(name, vals.isEmpty()? null : vals.get(0)); // 1st value
        }
        
        // Delegate
        
        subcommand.run(args1);
    } 

}
