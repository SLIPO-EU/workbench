package eu.slipo.workflows.tasklet;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.util.Assert;


/**
 * A tasklet to copy sources from a job's output directory/archive to a target directory. 
 * 
 * <p>A job's output directory/archive is something completely job-specific and is 
 * only known at runtime. This tasklet expects to find this information into job's 
 * execution context (by examining certain entries with predefined semantics).
 * 
 * <p>The following entries are examined:<ul>
 *  <li><tt>{@value #OUTPUT_ARCHIVE_KEY}</tt>: 
 *    The output is an archive in ZIP format</li>
 *  <li><tt>{@value #OUTPUT_DIR_KEY}</tt>: 
 *    The output is the entire subtree of the specified directory</li>
 * </ul>
 */
public class CopyOutputTasklet implements Tasklet
{
    private static final Logger logger = LoggerFactory.getLogger(CopyOutputTasklet.class);
    
    public static final String OUTPUT_ARCHIVE_KEY = "output";
    
    public static final String OUTPUT_DIR_KEY = "outputDir";
    
    private static final FileAttribute<?> directoryAttribute = PosixFilePermissions
        .asFileAttribute(PosixFilePermissions.fromString("rwxr-xr-x"));
    
    /**
     * The target directory to which output should be copied 
     */
    private final Path targetDir;
    
    /**
     * List source paths to copy to target directory. A source path will be resolved relative
     * to this job's output directory/archive.
     */
    private final List<Path> sources;
    
    /**
     * Should we create nested directories (under target directory) for copying sources?
     */
    private final boolean createNestedDirectories = true;
    
    public CopyOutputTasklet(Path targetDir, List<Path> sources)
    {
        Assert.isTrue(targetDir != null && targetDir.isAbsolute() , 
            "Expected a non-null absolute target directory");
        this.targetDir = targetDir;
        this.sources = sources;
    }
    
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext context)
        throws Exception
    {
        StepContext stepContext = context.getStepContext();
        Map<String, Object> jobExecutionContext = stepContext.getJobExecutionContext();
        
        logger.info("Copying to staging directory: target={}, sources={}", targetDir, sources);
        
        // Create target directory, if needed
        
        try {
            Files.createDirectories(targetDir, directoryAttribute);
            logger.info("Created target directory at {}", targetDir);
        } catch (FileAlreadyExistsException e) {
            // no-op: The directory already exists
        }
        
        // Copy from output to target directory
        
        if (jobExecutionContext.containsKey(OUTPUT_ARCHIVE_KEY)) {
            // Extract source entries from archive into target directory
            Path outputPath = Paths.get(jobExecutionContext.get(OUTPUT_ARCHIVE_KEY).toString());
            Assert.state(outputPath.isAbsolute(), 
                "The output archive should be given as an absolute file path");
            Assert.state(
                Files.isRegularFile(outputPath) && Files.isReadable(outputPath),
                "The output archive is not readable");
            try (ZipFile zip = new ZipFile(outputPath.toFile())) {
                for (Path source: sources)
                    extractToTargetDirectory(zip, source);
            }
        } else if (jobExecutionContext.containsKey(OUTPUT_DIR_KEY)) {
            // Copy sources from output directory to target directory
            Path outputDir = Paths.get(jobExecutionContext.get(OUTPUT_DIR_KEY).toString());
            Assert.state(outputDir.isAbsolute(), 
                "The output directory should be given as an absolute file path");
            Assert.state(Files.isDirectory(outputDir) && Files.isReadable(outputDir),
                "The output directory is not readable");
            for (Path source: sources) 
                copyToTargetDirectory(outputDir, source);
        } else {
            Assert.state(false, "The execution-context contains no relevant entry");
        }
        
        return RepeatStatus.FINISHED;
    }
    
    private void extractToTargetDirectory(ZipFile zip, Path source) throws IOException
    {
        ZipEntry entry = zip.getEntry(source.toString());
        Assert.state(entry != null, "The entry [" + source + "] does not exist");
        
        Path target = targetDir.resolve(source);
        
        // Create nested directory structure for target (if needed)
        
        if (createNestedDirectories && source.getNameCount() > 1) {
            Files.createDirectories(target.getParent(), directoryAttribute);
        }
        
        // Extract
        
        try (InputStream in = zip.getInputStream(entry)) {
            Files.copy(in, target);
        }
    }
    
    private void copyToTargetDirectory(Path outputDir, Path source) throws IOException
    {
        Path target = targetDir.resolve(source);
        Path path = outputDir.resolve(source);
     
        // Create nested directory structure for target (if needed)
        
        if (createNestedDirectories && source.getNameCount() > 1) {
            Files.createDirectories(target.getParent(), directoryAttribute);
        }
        
        // First, attempt to create a hard link
    
        Path link = null;
        try {
            link = Files.createLink(target, path);
        } catch (FileSystemException e) {
            link = null;
        }
        
        // Fallback to copying
        
        if (link == null)
            Files.copy(path, target);
    }
}
