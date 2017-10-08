package eu.slipo.workbench.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import eu.slipo.workbench.common.model.ApplicationException;

@Component
public class Command implements ApplicationRunner
{    
    private static Logger logger = LoggerFactory.getLogger(Command.class);
    
    private static final String DEFAULT_SUBCOMMAND = "help";
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Autowired
    private MessageSource messageSource;
    
    private SubCommand subcommandByName(String name)
    {
        return applicationContext.getBean(name, SubCommand.class);
    }
    
    /**
     * Delegate to a subcommand form depending on number of positional arguments.
     * 
     * @param subcommand The subcommand bean
     * @param pargs The list of (remaining) positional arguments
     * @param options The map of option arguments
     */
    private void run(SubCommand subcommand, List<String> pargs, Map<String, List<String>> options)
    {
        int n = pargs.size();
        if (n == 0)
            subcommand.run(options);
        else if (n == 1)
            subcommand.run(pargs.get(0), options);
        else if (n == 2)
            subcommand.run(pargs.get(0), pargs.get(1), options);
        else if (n == 3)
            subcommand.run(pargs.get(0), pargs.get(1), pargs.get(2), options);
        else {
            throw new IllegalArgumentException(
                "Too many (>3) positional arguments for subcommand");
        }
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
        
        // Prepare optional arguments to be forwarded to subcommand
        
        HashMap<String, List<String>> options = new HashMap<>();
        for (String key: args.getOptionNames())
            options.put(key, args.getOptionValues(key));
        
        // Delegate to subcommand
        
        try {
            run(subcommand, pargs, options);
        } catch (ApplicationException e) {
            // Format this top-level application exception
            ApplicationException e1 = 
                e.withFormattedMessage(messageSource, Locale.getDefault());
            logger.error("The subcommand has failed: {}", e1.getMessage());
        }
    } 

}
