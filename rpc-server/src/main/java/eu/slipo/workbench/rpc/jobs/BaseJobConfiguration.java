package eu.slipo.workbench.rpc.jobs;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermissions;

import javax.validation.Validator;

import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import eu.slipo.workbench.common.service.util.PropertiesConverterService;
import eu.slipo.workbench.rpc.service.ConfigurationGeneratorService;

class BaseJobConfiguration
{
    protected static final FileAttribute<?> DEFAULT_DIRECTORY_ATTRIBUTE =
        PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxr-xr-x"));

    @Autowired
    protected JobBuilderFactory jobBuilderFactory;

    @Autowired
    protected StepBuilderFactory stepBuilderFactory;

    @Autowired
    protected PropertiesConverterService propertiesConverter;

    @Autowired
    protected ConfigurationGeneratorService configurationGenerator;

    @Autowired
    protected Validator validator;

    @Autowired
    protected Path jobDataDirectory;

    /**
     * The root directory under which a job-specific data are stored
     */
    protected Path dataDir;

    /**
     * Setup the job-specific data directory
     *
     * @param dirName A directory name
     * @throws IOException
     */
    protected void setupDataDirectory(String dirName) throws IOException
    {
        Assert.isTrue(!StringUtils.isEmpty(dirName),
            "A non-empty directory name is expected");
        Assert.isTrue(Paths.get(dirName).getNameCount() == 1,
            "A plain directory name is expected (no nested directories are allowed)");

        this.dataDir = jobDataDirectory.resolve(dirName);
        try {
            Files.createDirectory(dataDir, DEFAULT_DIRECTORY_ATTRIBUTE);
        } catch (FileAlreadyExistsException e) {
            // no-op
        }
    }
}
