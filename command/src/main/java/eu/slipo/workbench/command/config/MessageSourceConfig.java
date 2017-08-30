package eu.slipo.workbench.command.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

@Configuration
public class MessageSourceConfig
{
    @Bean
    MessageSource messageSource()
    {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        
        source.setBasenames(
            "common-messages", 
            "messages", 
            "i18n/common-messages",
            "i18n/messages");
                
        source.setAlwaysUseMessageFormat(true);
        source.setUseCodeAsDefaultMessage(true);
        source.setDefaultEncoding("utf-8");
        
        return source;
    }
}
