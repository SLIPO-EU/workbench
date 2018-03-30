package eu.slipo.workbench.rpc.jobs;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermissions;

import javax.annotation.PostConstruct;
import javax.validation.Validator;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.spotify.docker.client.DockerClient;

import eu.slipo.workbench.common.model.tool.LimesConfiguration;
import eu.slipo.workbench.common.service.util.PropertiesConverterService;
import eu.slipo.workbench.rpc.jobs.listener.ExecutionContextPromotionListeners;
import eu.slipo.workbench.rpc.jobs.tasklet.ReadConfigurationTasklet;
import eu.slipo.workbench.rpc.jobs.tasklet.ValidateConfigurationTasklet;

@Component
public class LimesJobConfiguration
{
    private static final String JOB_NAME = "limes";

    private static final FileAttribute<?> DIRECTORY_ATTRIBUTE =
        PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxr-xr-x"));

    /**
     * The default timeout (milliseconds) for a container run
     */
    public static final long DEFAULT_RUN_TIMEOUT = 30 * 1000L;

    /**
     * The default interval (milliseconds) for polling a container
     */
    public static final long DEFAULT_CHECK_INTERVAL = 1000L;

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private DockerClient docker;

    @Autowired
    private PropertiesConverterService propertiesConverter;

    @Autowired
    private Validator validator;

    @Autowired
    private Path jobDataDirectory;

    /**
     * The root directory on the docker host, under which we bind-mount volumes.
     */
    private Path dataDir;

    /**
     * The root directory on a container, under which directories/files will be bind-mounted
     * (eg. <tt>/var/local/foo</tt>).
     */
    private Path containerDataDir;

    @Autowired
    private void setContainerDataDirectory(
        @Value("${slipo.rpc-server.tools.limes.docker.container-data-dir}") String dir)
    {
        Path dirPath = Paths.get(dir);
        Assert.isTrue(dirPath.isAbsolute(), "Expected an absolute path (inside a container)");
        this.containerDataDir = dirPath;
    }

    @PostConstruct
    private void setupDataDirectory() throws IOException
    {
        this.dataDir = jobDataDirectory.resolve("limes");
        try {
            Files.createDirectory(dataDir, DIRECTORY_ATTRIBUTE);
        } catch (FileAlreadyExistsException e) {}
    }

    /**
     * A tasklet that reads job parameters to a configuration bean into execution-context.
     */
    @Bean("limes.readConfigurationTasklet")
    public Tasklet readConfigurationTasklet()
    {
        ReadConfigurationTasklet<LimesConfiguration> tasklet =
            new ReadConfigurationTasklet<>(LimesConfiguration.class, propertiesConverter);
        tasklet.setValidator(validator);
        return tasklet;
    }

    @Bean("limes.readConfigurationStep")
    public Step readConfigurationStep(
        @Qualifier("limes.readConfigurationTasklet") Tasklet tasklet)
    {
        return stepBuilderFactory.get("limes.readConfiguration")
            .tasklet(tasklet)
            .listener(ExecutionContextPromotionListeners.fromKeys("config"))
            .build();
    }

    @Bean("limes.validateConfigurationTasklet")
    public Tasklet validateConfigurationTasklet()
    {
        return new ValidateConfigurationTasklet<>(LimesConfiguration.class, validator);
    }

    @Bean("limes.validateConfigurationStep")
    public Step validateConfigurationStep(
        @Qualifier("limes.validateConfigurationTasklet") Tasklet tasklet)
    {
        return stepBuilderFactory.get("limes.validateConfiguration")
            .tasklet(tasklet)
            .build();
    }


}
