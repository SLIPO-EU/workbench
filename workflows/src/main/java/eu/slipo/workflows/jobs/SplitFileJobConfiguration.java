package eu.slipo.workflows.jobs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import javax.annotation.PostConstruct;

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
 * Split a line-oriented text file to a fixed number of parts.
 */
@Component("splitFile.jobConfiguration")
public class SplitFileJobConfiguration
{
    @Autowired
    private StepBuilderFactory stepBuilderFactory;
   
    @Autowired
    private Path jobDataDirectory;
    
    private Path dataDir;
    
    @PostConstruct
    private void setupDataDirectory() throws IOException
    {
        this.dataDir = jobDataDirectory.resolve("splitFile");
        try {
            Files.createDirectory(dataDir);
        } catch (FileAlreadyExistsException ex) {
            // no-op: already exists
        }
    }
    
    private static class SplitFileTasklet implements Tasklet
    {        
        private static Logger logger = LoggerFactory.getLogger(SplitFileTasklet.class);
        
        private final Path inputPath;
        
        private final String outputPrefix;
        
        private final String outputSuffix;
        
        private final Path outputDir;
        
        private final int numParts;
        
        public SplitFileTasklet(
            Path inputPath, Path outputDir, int numParts, String outputPrefix, String outputSuffix)
        {
            Assert.isTrue(numParts > 1, 
                "The number of parts must be > 1");
            Assert.isTrue(!StringUtils.isEmpty(outputPrefix), 
                "A non-empty output prefix is required");
            Assert.isTrue(outputDir != null && outputDir.isAbsolute(), 
                "The output directory must be given as an absolute path");
            Assert.isTrue(!StringUtils.isEmpty(inputPath.toString()), 
                "The input path is required");
            Assert.isTrue(Files.isRegularFile(inputPath) && Files.isReadable(inputPath), 
                "The input path must point to a readable file");
            this.inputPath = inputPath;
            this.outputDir = outputDir;
            this.numParts = numParts;
            this.outputPrefix = outputPrefix;
            this.outputSuffix = outputSuffix;
        }
        
        private Path outputPath(int partNumber)
        {
            return outputDir.resolve(
                outputPrefix + String.valueOf(partNumber + 1) + outputSuffix);
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
            
            // Split input
            
            final long inputSize = Files.size(inputPath);
            final long partSize = 1 + ((inputSize - 1) / numParts);
            
            final byte[] newline = String.format("%n").getBytes();
            int partNumber = 0;
            int writeCount = 0;
            OutputStream out = null;
            BufferedReader reader = Files.newBufferedReader(inputPath);
            try {
                String line = null;
                while ((line = reader.readLine()) != null) {
                    if (out == null) {
                        Path outPath = outputPath(partNumber);
                        out = Files.newOutputStream(outPath, 
                            StandardOpenOption.TRUNCATE_EXISTING, 
                            StandardOpenOption.CREATE);
                        writeCount = 0;
                    }
                    // Append this line to output
                    byte[] buf = line.getBytes();
                    out.write(buf);
                    out.write(newline);
                    writeCount += buf.length + newline.length;
                    // Check if part-size was exceeded
                    if (writeCount > partSize) {
                        out.close();
                        out = null;
                        partNumber++;
                    }
                }
            } finally {
                reader.close();
                if (out != null) 
                    out.close();
            }
            
            Assert.state(numParts == partNumber + 1, "Expected a different number of parts!");
            logger.info("Split {} to {} parts inside {}", inputPath, numParts, outputDir);
            
            // Update execution context
            
            executionContext.put("outputDir", outputDir.toString());
            executionContext.putInt("partNumber", partNumber);
            
            return RepeatStatus.FINISHED;
        }   
    }
    
    @Bean("splitFile.tasklet")
    @JobScope
    public Tasklet tasklet(
        @Value("#{jobParameters['input']}") String input,
        @Value("#{jobParameters['outputPrefix']?:'p'}") String outputPrefix,
        @Value("#{jobParameters['outputSuffix']?:''}") String outputSuffix,
        @Value("#{jobParameters['numParts']}") Number numParts,
        @Value("#{jobExecution.jobInstance.id}") Long jobId)
    {
        Path inputPath = Paths.get(input);
        Assert.isTrue(Files.exists(inputPath), "The input file does not exist");
        Path outputDir = dataDir.resolve(jobId.toString());
        return new SplitFileTasklet(
            inputPath, outputDir, numParts.intValue(), outputPrefix, outputSuffix);
    }
    
    
    @Bean("splitFile.step")
    public Step step(@Qualifier("splitFile.tasklet") Tasklet tasklet) throws Exception
    {
        ExecutionContextPromotionListener contextListener = new ExecutionContextPromotionListener();
        contextListener.setKeys(new String[] { "outputDir" });
        contextListener.setStrict(true);
        contextListener.afterPropertiesSet();
        
        return stepBuilderFactory.get("splitFile")
            .tasklet(tasklet)
            .listener(contextListener)
            .build();
    }
    
    @Bean("splitFile.flow")
    public Flow flow(@Qualifier("splitFile.step") Step step)
    {
        return new FlowBuilder<Flow>("splitFile")
            .start(step)
            .end();
    }
}
