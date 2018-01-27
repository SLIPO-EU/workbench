package eu.slipo.workflows.jobs.examples.mergesort;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import javax.annotation.PostConstruct;

import org.apache.commons.collections4.bag.TreeBag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.listener.ExecutionContextPromotionListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Sort a file of space-delimited integers.
 */
@Component("mergesort.sortFile.jobConfiguration")
public class SortFileJobConfiguration
{
    @Autowired
    private StepBuilderFactory stepBuilderFactory;
   
    @Autowired
    private Path jobDataDirectory;
    
    private Path dataDir;
    
    @PostConstruct
    private void createDataDirectory() throws IOException
    {
        this.dataDir = jobDataDirectory.resolve("mergesort.sortFile");
        try {
            Files.createDirectory(dataDir);
        } catch (FileAlreadyExistsException ex) {
            // no-op: already exists
        }
    }
    
    private static class SortFileTasklet implements Tasklet
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
    
    @Bean("mergesort.sortFile.tasklet")
    @JobScope
    public Tasklet tasklet(
        @Value("#{jobParameters['input']}") String input,
        @Value("#{jobParameters['outputName']}") String outputName,
        @Value("#{jobExecution.jobInstance.id}") Long jobId)
    {
        Path inputPath = Paths.get(input);
        Path outputDir = dataDir.resolve(String.valueOf(jobId));
        return new SortFileTasklet(inputPath, outputDir, outputName);
    }
    
    @Bean("mergesort.sortFile.step")
    public Step step(@Qualifier("mergesort.sortFile.tasklet") Tasklet tasklet) throws Exception
    {
        ExecutionContextPromotionListener contextListener = new ExecutionContextPromotionListener();
        contextListener.setKeys(new String[] { "outputDir" });
        contextListener.setStrict(true);
        contextListener.afterPropertiesSet();
        
        return stepBuilderFactory.get("mergesort.sortFile")
            .tasklet(tasklet)
            .listener(contextListener)
            .build();
    }
    
    @Bean("mergesort.sortFile.flow")
    public Flow flow(@Qualifier("mergesort.sortFile.step") Step step)
    {
        return new FlowBuilder<Flow>("mergesort.sortFile")
            .start(step)
            .end();
    }
}
