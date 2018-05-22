package eu.slipo.workbench.rpc.jobs.tasklet;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.core.io.Resource;
import org.springframework.data.util.Pair;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.google.common.collect.Maps;

import eu.slipo.workbench.common.model.ApplicationException;
import eu.slipo.workbench.common.model.BasicErrorCode;
import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.common.model.tool.EnumConfigurationFormat;
import eu.slipo.workbench.common.model.tool.ToolConfiguration;
import eu.slipo.workbench.rpc.service.ConfigurationGeneratorService;
import jersey.repackaged.com.google.common.collect.Iterables;

/**
 * A tasklet that prepares a working directory.
 */
public class PrepareWorkingDirectoryTasklet implements Tasklet
{
    private static final Logger logger = LoggerFactory.getLogger(PrepareWorkingDirectoryTasklet.class);

    private static final Set<PosixFilePermission> DIRECTORY_PERMISSIONS =
        PosixFilePermissions.fromString("rwxr-xr-x");

    private static final FileAttribute<?> DIRECTORY_ATTRIBUTE =
        PosixFilePermissions.asFileAttribute(DIRECTORY_PERMISSIONS);

    /**
     * A pattern for a key identifying a configuration entry
     */
    private static final String CONFIG_KEY_PATTERN = "^[a-zA-Z][-_0-9a-zA-Z]*$";

    /**
     * A flag indicating the default behavior on whether a input given as a ZIP archive
     * should be unpacked into our input directory.
     */
    private static final boolean UNPACK_ZIP_ARCHIVE = true;

    /**
     * Describe a configuration entry.
     */
    private static class ConfigurationSpec
    {
        /** Path (relative to working directory) where this configuration should be placed */
        private final Path path;

        /** The source for this configuration */
        private final Object source;

        /** The desired configuration format for which configuration should be generated */
        private final EnumConfigurationFormat format;

        private ConfigurationSpec(Path path, Object source, EnumConfigurationFormat format)
        {
            this.path = path;
            this.source = source;
            this.format = format;
        }

        public EnumConfigurationFormat format()
        {
            return format;
        }

        public Object source()
        {
            return source;
        }

        public Path path()
        {
            return path;
        }
    }

    /**
     * A builder for the enclosing class
     */
    public static class Builder
    {
        private ConfigurationGeneratorService configurationGenerator;

        private Path workDir;

        private List<Path> input = Collections.emptyList();

        private EnumDataFormat inputFormat;

        private EnumDataFormat outputFormat;

        private Map<String, ConfigurationSpec> config = new LinkedHashMap<>();

        private Boolean unzip;

        /**
         * Set the configuration-generator service to be used
         * @param service
         */
        public Builder configurationGeneratorService(ConfigurationGeneratorService service)
        {
            Assert.notNull(service, "Expected an non-null service instance");
            this.configurationGenerator = service;
            return this;
        }

        /**
         * Set working directory
         * @param workDir
         */
        public Builder workingDirectory(Path workDir)
        {
            Assert.notNull(workDir, "Expected a non-null directory");
            Assert.isTrue(workDir.isAbsolute(), "Expected an absolute path as working directory");
            this.workDir = workDir;
            return this;
        }

        /**
         * Specify input files to be copied inside input directory
         *
         * <p>Note: The referenced files will be actually copied during tasklet's execution.
         * @param paths
         */
        public Builder input(List<Path> paths)
        {
            Assert.notEmpty(paths, "Expected an non-empty list of paths");
            this.input = new ArrayList<>(paths);
            return this;
        }

        /**
         * Specify input files to be copied inside input directory
         * @param paths
         */
        public Builder input(String ...paths)
        {
            return input(Arrays.stream(paths).map(Paths::get).collect(Collectors.toList()));
        }

        /**
         * Specify input files to be copied inside input directory
         * @param paths
         */
        public Builder input(Path ...paths)
        {
            return input(Arrays.asList(paths));
        }

        public Builder inputFormat(EnumDataFormat format)
        {
            this.inputFormat = format;
            return this;
        }

        public Builder outputFormat(EnumDataFormat format)
        {
            this.outputFormat = format;
            return this;
        }

        /**
         * Set whether a single input given as a ZIP archive should be extracted into
         * our input directory. If so, all ZIP entries will be extracted discarding their
         * directory prefix (if any).
         * @param flag
         */
        public Builder unzipIfArchive(boolean flag)
        {
            this.unzip = flag;
            return this;
        }

        /**
         * Add a configuration file under this working directory. The configuration is copied verbatim
         * from the given resource (no conversion taking place).
         *
         * @param key The key for this (part of) configuration.
         * @param name The filename for this configuration (resolved against working directory)
         * @param resource A resource to read configuration from
         */
        public Builder config(String key, String name, Resource resource)
        {
            Assert.notNull(key, "A key is required for a configuration");
            Assert.isTrue(key.matches(CONFIG_KEY_PATTERN), "The key contains invalid characters");
            Assert.isTrue(!StringUtils.isEmpty(name), "A name is required for a configuration");
            Path namePath = Paths.get(name);
            Assert.isTrue(namePath.getNameCount() == 1, "The name cannot be nested in directories");

            Assert.notNull(resource, "A resource is required");

            config.put(key, new ConfigurationSpec(namePath, resource, null));
            return this;
        }

        /**
         * Add a configuration file (of properties) under this working directory.
         *
         * @param key The key for this (part of) configuration.
         * @param name The filename for this configuration (resolved against working directory)
         * @param properties The actual configuration as a map of properties.
         */
        public Builder config(String key, String name, Properties properties)
        {
            Assert.notNull(key, "A key is required for a configuration");
            Assert.isTrue(key.matches(CONFIG_KEY_PATTERN), "The key contains invalid characters");
            Assert.isTrue(!StringUtils.isEmpty(name), "A name is required for a configuration");
            Path namePath = Paths.get(name);
            Assert.isTrue(namePath.getNameCount() == 1, "The name cannot be nested in directories");

            Assert.notNull(properties, "A non-null map of properties is required");

            config.put(key, new ConfigurationSpec(namePath, properties, EnumConfigurationFormat.PROPERTIES));
            return this;
        }

        /**
         * Add a tool-specific configuration file under this working directory.
         *
         * @param key The key for this (part of) configuration.
         * @param name The filename for this configuration (resolved against working directory).
         * @param source The actual configuration object from which configuration will be derived
         * @param configFormat The desired configuration format
         */
        public Builder config(
            String key, String name, ToolConfiguration source, EnumConfigurationFormat configFormat)
        {
            Assert.notNull(key, "A key is required for a configuration");
            Assert.isTrue(key.matches(CONFIG_KEY_PATTERN), "The key contains invalid characters");
            Assert.isTrue(!StringUtils.isEmpty(name), "A name is required for a configuration");
            Path namePath = Paths.get(name);
            Assert.isTrue(namePath.getNameCount() == 1, "The name cannot be nested in directories");

            Assert.notNull(source, "A non-null configuration object is required");
            Assert.notNull(configFormat, "A non-null configuration format is required");

            config.put(key, new ConfigurationSpec(namePath, source, configFormat));
            return this;
        }

        public PrepareWorkingDirectoryTasklet build()
        {
            Assert.state(workDir != null, "A working directory is required");

            Assert.state(configurationGenerator != null ||
                    (Iterables.all(config.values(), s -> s.source instanceof Resource)),
                "A configuration-generator service is needed to generate a textual representation " +
                "for several tool-specific configuration sources");

            PrepareWorkingDirectoryTasklet tasklet = new PrepareWorkingDirectoryTasklet(
                workDir, input, config, configurationGenerator);

            if (inputFormat != null)
                tasklet.setInputFormat(inputFormat);

            if (outputFormat != null)
                tasklet.setOutputFormat(outputFormat);

            if (unzip != null)
                tasklet.setUnzip(unzip);

            return tasklet;
        }
    }

    public static Builder builder()
    {
        return new Builder();
    }

    /**
     * The collection of keys used inside our execution context
     */
    public static class Keys
    {
        public static final String WORK_DIR = "workDir";

        public static final String INPUT_DIR = "inputDir";

        public static final String INPUT_FILES = "inputFiles";

        public static final String INPUT_FORMAT = "inputFormat";

        public static final String OUTPUT_DIR = "outputDir";

        public static final String OUTPUT_FORMAT = "outputFormat";

        public static final String CONFIG_FILE_BY_NAME = "configFileByName";
    }

    private final ConfigurationGeneratorService configurationGenerator;

    private final Path workDir;

    private final Path inputDir;

    private final Path outputDir;

    private final List<Path> input;

    private final Map<String, ConfigurationSpec> config;

    private EnumDataFormat inputFormat;

    private EnumDataFormat outputFormat;

    private FileAttribute<?> directoryAttribute = DIRECTORY_ATTRIBUTE;

    private boolean unzip = UNPACK_ZIP_ARCHIVE;

    private PrepareWorkingDirectoryTasklet(
        Path workDir, List<Path> input, Map<String, ConfigurationSpec> config,
        ConfigurationGeneratorService configurationGeneratorService)
    {
        this.configurationGenerator = configurationGeneratorService;
        this.workDir = workDir.toAbsolutePath();
        this.input = input;
        this.config = config;
        this.inputDir = this.workDir.resolve("input");
        this.outputDir = this.workDir.resolve("output");
    }

    private void setInputFormat(EnumDataFormat format)
    {
        this.inputFormat = format;
    }

    private void setOutputFormat(EnumDataFormat format)
    {
        this.outputFormat = format;
    }

    private void setUnzip(boolean flag)
    {
        this.unzip = flag;
    }

    public Path workDir()
    {
        return inputDir;
    }

    public Path inputDir()
    {
        return inputDir;
    }

    public Path outputDir()
    {
        return outputDir;
    }

    public EnumDataFormat inputFormat()
    {
        return inputFormat;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext context)
        throws Exception
    {
        StepExecution stepExecution = context.getStepContext().getStepExecution();
        ExecutionContext executionContext = stepExecution.getExecutionContext();

        //
        // Create working directory hierarchy
        //

        for (Path p: Arrays.asList(workDir, inputDir, outputDir)) {
            try {
                Files.createDirectory(p, directoryAttribute);
            } catch (FileAlreadyExistsException ex) {}
        }

        //
        // Put (extract or link) each input into our input directory
        //

        List<String> inputFiles = new ArrayList<>();
        if (!input.isEmpty()) {
            if (unzip && (input.size() == 1) &&
                    "application/zip".equals(Files.probeContentType(input.get(0))))
            {
                // The input archive should be extracted to input directory
                try (ZipFile zipfile = new ZipFile(input.get(0).toString())) {
                    List<String> entryNames = zipfile.stream()
                        .filter(e -> !e.isDirectory())
                        .map(e -> e.getName())
                        .collect(Collectors.toList());
                    for (String entryName: entryNames) {
                        String name = Paths.get(entryName).getFileName().toString();
                        extractToInputDirectory(zipfile, entryName, name);
                        inputFiles.add(name);
                    }
                }
            } else {
                // Copy each input to input directory
                for (Path inputPath: input) {
                    String name = inputPath.getFileName().toString();
                    copyToInputDirectory(inputPath, name);
                    inputFiles.add(name);
                }
            }
        }

        //
        // Generate configuration files inside working directory
        //

        for (ConfigurationSpec u: config.values()) {
            Path path = workDir.resolve(u.path());
            Object source = u.source();
            if (source instanceof Resource) {
                // Copy configuration from source
                try (InputStream in = ((Resource) source).getInputStream()) {
                    Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
                }
            } else {
                // Generate configuration from source, then write to destination
                String configData = configurationGenerator.generate(source, u.format());
                Files.write(path, configData.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            }
        }

        //
        // Update execution context
        //

        executionContext.putString(Keys.WORK_DIR, workDir.toString());
        executionContext.putString(Keys.INPUT_DIR, inputDir.toString());
        executionContext.putString(Keys.INPUT_FORMAT, inputFormat.name());
        executionContext.put(Keys.INPUT_FILES, inputFiles);

        executionContext.putString(Keys.OUTPUT_DIR, outputDir.toString());
        executionContext.putString(Keys.OUTPUT_FORMAT, outputFormat.name());

        Map<String, String> configFileByName = new HashMap<>(
            Maps.transformValues(config, u -> u.path().toString()));
        executionContext.put(Keys.CONFIG_FILE_BY_NAME, configFileByName);

        return RepeatStatus.FINISHED;
    }

    /**
     * Extract a named ZIP entry inside our input directory.
     *
     * @param zipfile An opened ZipFile handle
     * @param entryName The entry name
     * @param name The name of the extracted file (relative to input directory)
     * @throws IOException
     */
    private void extractToInputDirectory(ZipFile zipfile, String entryName, String name)
        throws IOException
    {
        ZipEntry e = zipfile.getEntry(entryName);
        try (InputStream in = new BufferedInputStream(zipfile.getInputStream(e))) {
            Files.copy(in, inputDir.resolve(name));
        }
    }

    /**
     * Copy given input file to our input directory.
     *
     * <p>
     * A shallow hard link will be created, i.e. there is no attempt to create a nested structure
     * inside input directory. If a hard link cannot be created (because of file-system limitations),
     * we fallback to a plain copying.
     *
     * @param source The input path to link to
     * @param filename The link name (relative to input directory)
     * @throws IOException
     */
    private void copyToInputDirectory(Path source, String filename)
        throws IOException
    {
        Path destination = inputDir.resolve(filename);
        Files.deleteIfExists(destination);

        // Try to link

        Path link = null;
        try {
            link = Files.createLink(destination, source);
        } catch (FileSystemException e) {
            link = null;
        }

        // If no link was created, fallback to copying

        if (link == null) {
            Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
