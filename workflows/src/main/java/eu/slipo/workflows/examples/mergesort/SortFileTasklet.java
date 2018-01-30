package eu.slipo.workflows.examples.mergesort;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

import org.apache.commons.collections4.bag.TreeBag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public class SortFileTasklet implements Tasklet
{
    private static Logger logger = LoggerFactory.getLogger(SortFileTasklet.class);
    
    private final Path inputPath;
    
    private final Path outputDir;
    
    private final String outputName;
    
    public SortFileTasklet(Path inputPath, Path outputDir, String outputName)
    {
        Assert.isTrue(Files.isRegularFile(inputPath) && Files.isReadable(inputPath), 
            "The input path must point to a readable file");
        Assert.isTrue(outputDir != null && outputDir.isAbsolute(), 
            "The output directory must be given as an absolute path");
        Assert.isTrue(!StringUtils.isEmpty(outputName), 
            "An output name is required");
        this.inputPath = inputPath;
        this.outputDir = outputDir;
        this.outputName = outputName;
    }
    
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
        throws Exception
    {
        StepExecution stepExecution = chunkContext.getStepContext().getStepExecution();
        ExecutionContext executionContext = stepExecution.getExecutionContext();
        
        // Create output directory
        try {
            Files.createDirectory(outputDir);
            logger.debug("Created output directory at {}", outputDir);
        } catch (IOException ex) {
            // no-op
        }
        
        // Load in-memory in a tree structure 
        final TreeBag<Long> bag = new TreeBag<>();
        try (Scanner in = new Scanner(Files.newInputStream(inputPath))) {
            while (in.hasNextLong()) {
                bag.add(in.nextLong());
            }
        }
        
        // write to output
        final Path outPath = outputDir.resolve(outputName);
        try (BufferedWriter writer = Files.newBufferedWriter(outPath)) {
            for (Long x: bag) {
                writer.write(x.toString());
                writer.newLine();
            }
        }
        
        logger.info("Wrote {} integers into {}", bag.size(), outPath);
        
        // Update execution context
        executionContext.put("outputDir", outputDir.toString());
        
        return null;
    } 
}