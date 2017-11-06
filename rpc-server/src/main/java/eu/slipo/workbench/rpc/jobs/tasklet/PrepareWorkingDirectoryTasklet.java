package eu.slipo.workbench.rpc.jobs.tasklet;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.data.util.Pair;
import org.springframework.util.Assert;

import com.google.common.base.Functions;

import eu.slipo.workbench.common.model.ApplicationException;
import eu.slipo.workbench.common.model.BasicErrorCode;
import eu.slipo.workbench.common.model.poi.EnumDataFormat;

/**
 * A tasklet that prepares a working directory.
 */
public class PrepareWorkingDirectoryTasklet implements Tasklet
{
    private static final Set<PosixFilePermission> DIRECTORY_PERMISSIONS = 
        PosixFilePermissions.fromString("rwxr-xr-x");
    
    private static final FileAttribute<?> DIRECTORY_ATTRIBUTE = 
        PosixFilePermissions.asFileAttribute(DIRECTORY_PERMISSIONS);
    
    public static class Builder 
    {
        private Path workDir;
        
        private List<Path> input = Collections.emptyList();
        
        private EnumDataFormat inputFormat;
        
        private Map<String, Pair<Path, Supplier<String>>> config = new LinkedHashMap<>();
        
        /**
         * Set working directory
         * @param workDir
         */
        public Builder workingDirectory(Path workDir)
        {
            Assert.notNull(workDir, 
                "Expected a non-null directory");
            Assert.isTrue(workDir.isAbsolute(), 
                "Expected an absolute path as working directory");
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
        
        /**
         * Add a configuration file (of properties) under this working directory.
         * 
         * @param key The key for this (part of) configuration.
         * @param name The relative name under which this configuration should be stored
         *    under working directory. 
         * @param properties The actual configuration as a map of properties.
         */
        public Builder config(String key, String name, Properties properties)
        {
            Assert.notNull(key, 
                "A key is required for a configuration");
            Assert.isTrue(key.matches("[a-zA-Z][-_0-9a-zA-Z]*"), 
                "The key contains invalid characters");
            Assert.notNull(name, 
                "A name is required for a configuration");
            Assert.notNull(properties, 
                "A non-null map of properties is required");
            Assert.isTrue(!this.config.containsKey(key), 
                "The configuration key of [" + key +"] is already present");
            
            Supplier<String> configDataSupplier = new Supplier<String>()
            {
                @Override
                public String get()
                {
                    String result = null;
                    try (StringWriter writer = new StringWriter()) {
                        properties.store(writer, "This configuration is auto-generated");
                        result = writer.toString();
                    } catch (IOException e) {
                        throw ApplicationException.fromMessage(
                            e, BasicErrorCode.IO_ERROR, "Failed to write properties configuration");
                    }
                    return result;
                }
            };
            
            this.config.put(key, Pair.of(Paths.get(name), configDataSupplier));
            return this;
        }
        
        /**
         * Add a configuration file (of XML) under this working directory.
         * 
         * @param key The key for this (part of) configuration.
         * @param name The relative name under which this configuration should be stored
         *    under working directory.
         * @param configData The actual configuration data which should be XML serializable
         */
        public Builder config(String key, Path name, Object configData)
        {
            Assert.notNull(key, 
                "A key is required for a configuration");
            Assert.isTrue(key.matches("[a-zA-Z][-_0-9a-zA-Z]*"), 
                "The key contains invalid characters");
            Assert.notNull(name, 
                "A name is required for a configuration");
            Assert.notNull(configData, 
                "A non-null configuration object is required");
            Assert.isTrue(!this.config.containsKey(key), 
                "The configuration key of [" + key +"] is already present");
            
            Supplier<String> configDataSupplier = null;
            
            // Todo Use a Supplier<String> that serializes configData into XML
            
            return null;
        }
        
        public PrepareWorkingDirectoryTasklet build()
        {
            Assert.state(workDir != null, "A working directory is required");
            
            PrepareWorkingDirectoryTasklet tasklet = 
                new PrepareWorkingDirectoryTasklet(workDir, input, config);
            
            if (inputFormat != null)
                tasklet.setInputFormat(inputFormat);
            
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
        
        public static final String CONFIG = "configByName";
    }
    
    private final Path workDir;
    
    private final Path inputDir;
    
    private final Path outputDir;
    
    private final List<Path> input;
    
    private final Map<String, Pair<Path, Supplier<String>>> config;
    
    private EnumDataFormat inputFormat;
    
    private FileAttribute<?> directoryAttribute = DIRECTORY_ATTRIBUTE;
    
    private PrepareWorkingDirectoryTasklet(
        Path workDir, List<Path> input, Map<String, Pair<Path, Supplier<String>>> config)
    {
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
        
        // Create working directory hierarchy
        
        Files.createDirectory(workDir, directoryAttribute);
        Files.createDirectory(inputDir, directoryAttribute);
        Files.createDirectory(outputDir, directoryAttribute);
        
        // Link to each input from inside input directory
        
        // Todo Handle the case of a single archive input (with 1 level of nesting)
        // Unpack contents in input directory, expand names to unpacked filenames
        
        List<String> inputFiles = new ArrayList<>(input.size());
        for (Path inputPath: input) {
            String name = inputPath.getFileName().toString();
            createLinkFromInputDirectory(inputPath, name);
            inputFiles.add(name);
        }
       
        // Generate configuration files inside working directory
        
        for (Pair<Path, Supplier<String>> pair: config.values()) {
            Supplier<String> configDataSupplier = pair.getSecond();
            String configData = configDataSupplier.get();
            Files.write(
                workDir.resolve(pair.getFirst()),
                configData.getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE_NEW);
        }
        
        // Update execution context
        
        executionContext.putString(Keys.WORK_DIR, workDir.toString());
        executionContext.putString(Keys.INPUT_DIR, inputDir.toString());
        executionContext.putString(Keys.INPUT_FORMAT, inputFormat.name());
        executionContext.put(Keys.INPUT_FILES, inputFiles);
        
        executionContext.putString(Keys.OUTPUT_DIR, outputDir.toString());
        
        Map<String,String> configByName = new LinkedHashMap<>(config.size());
        for (String key: config.keySet())
            configByName.put(key, config.get(key).getFirst().toString());
        executionContext.put(Keys.CONFIG, configByName);

        return RepeatStatus.FINISHED;
    }

    /**
     * Link to the given input file from inside our input directory.
     * 
     * <p>
     * A shallow link will be created, i.e. there is no attempt to create a nested structure 
     * inside input directory. If a hard link cannot be created (because of file-system limitations),
     * we reside to a symbolic link. 
     * Note that in the later case (symbolic link), we assume that no input will be moved/deleted 
     * throughout the whole job execution! 
     * 
     * @param inputPath The input path to link to
     * @param name The link name
     * @throws IOException 
     */
    private void createLinkFromInputDirectory(Path inputPath, String name) 
        throws IOException
    {
        Path dst = inputDir.resolve(name);
        Path link = null;
        try {
            link = Files.createLink(dst, inputPath);
        } catch (FileSystemException e) {
            link = null;
        }
        
        if (link == null) {
            link = Files.createSymbolicLink(dst, inputPath);
        }
    }
}
