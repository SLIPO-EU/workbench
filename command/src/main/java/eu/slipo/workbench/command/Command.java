package eu.slipo.workbench.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class Command implements ApplicationRunner
{    
    private static Logger logger = LoggerFactory.getLogger(Command.class);
    
    private static final String DEFAULT_SUBCOMMAND = "help";
    
    @Autowired
    private ApplicationContext applicationContext;
    
    private SubCommand subcommandByName(String name)
    {
        return applicationContext.getBean(name, SubCommand.class);
    }
    
    @Override
    public void run(ApplicationArguments args)
    {
        List<String> pargs = new ArrayList<>(args.getNonOptionArgs());
      
        // Find a proper SubCommand bean to delegate to 
        
        String name; 
        if (pargs.isEmpty()) {
            name = DEFAULT_SUBCOMMAND;
        } else {
            name = pargs.remove(0); // remove and shift to left
        }
        SubCommand subcommand = subcommandByName(name);
        
        // Prepare optional arguments to be forwarded to sub-command
        
        HashMap<String, String> options = new HashMap<>();
        for (String key: args.getOptionNames()) {
            List<String> vals = args.getOptionValues(key);
            // If an option has multiple values, we keep only the 1st
            options.put(key, vals.isEmpty()? null : vals.get(0));
        }
        
        // Delegate to proper form (depending on number of remaining non-option args)
        
        int n = pargs.size();
        if (n == 0)
            subcommand.run(options);
        else if (n == 1)
            subcommand.run(pargs.get(0), options);
        else if (n == 2)
            subcommand.run(pargs.get(0), pargs.get(1), options);
        else {
            throw new IllegalArgumentException(
                "Too many (>2) non-option arguments for subcommand");
        }
    } 

}
