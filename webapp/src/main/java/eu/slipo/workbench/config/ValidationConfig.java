package eu.slipo.workbench.config;

import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Configuration
public class ValidationConfig
{
    @Bean(name = "defaultBeanValidator")
    Validator validator()
    {
        LocalValidatorFactoryBean b = new LocalValidatorFactoryBean();
        b.afterPropertiesSet();
        return b.getValidator();
    }
}