package eu.slipo.workbench.config;

import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(
    basePackageClasses = { eu.slipo.workbench.repository._Marker.class })
@EnableTransactionManagement(mode = AdviceMode.PROXY)
public class DatabaseConfig 
{
}
