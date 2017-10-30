package eu.slipo.workbench.rpc.config;

import javax.annotation.PostConstruct;

import org.springframework.batch.core.configuration.DuplicateJobException;
import org.springframework.batch.core.configuration.JobFactory;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;

/**
 * Register basic jobs to enable name-based lookup.
 */
@Configuration
public class JobRegistrar
{
    @Autowired
    JobRegistry registry;
    
    @Autowired
    @Qualifier("job1.jobFactory") 
    JobFactory job1JobFactory;
    
    @Autowired
    @Qualifier("triplegeo.jobFactory") 
    JobFactory triplegeoJobFactory;
    
    /**
     * Register basic job factories ({@link JobFactory}).
     * <p>
     * Note that only those jobs created from factories registered here can be later
     * retrieved by name (i.e. are the only ones known to {@link JobRegistry} bean).
     * 
     * @throws DuplicateJobException 
     */
    @PostConstruct
    private void registerFactories() throws DuplicateJobException
    {
        registry.register(job1JobFactory);
        registry.register(triplegeoJobFactory);
    }
}
