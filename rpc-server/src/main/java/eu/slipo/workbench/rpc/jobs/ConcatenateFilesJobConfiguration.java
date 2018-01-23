package eu.slipo.workbench.rpc.jobs;

import java.io.File;
import java.io.OutputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
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

@Component("concatenateFiles.jobConfiguration")
public class ConcatenateFilesJobConfiguration
{
    private static Logger logger = LoggerFactory.getLogger(ConcatenateFilesJobConfiguration.class);
    
    @Autowired
    private StepBuilderFactory stepBuilderFactory;
    
    @Autowired
    private JobBuilderFactory jobBuilderFactory;
   
    private Path dataDir;
   
    @Autowired
    private void setDataDir(@Value("${slipo.rpc-server.jobs.data-dir}") String dir)
    {
        Path parentDir = Paths.get(dir);
        Assert.isTrue(parentDir.isAbsolute(), "Expected an absolute directory");
        Assert.isTrue(Files.isDirectory(parentDir) && Files.isWritable(parentDir), 
            "Expected a writable directory as a parent data directory");
        this.dataDir = parentDir.resolve("concatenateFiles");
    }
    
    public class ConcatenateFilesTasklet implements Tasklet
    {
        private final List<Path> input;
        
        private final Path outputDir;
        
        private final String outputName;
        
        public ConcatenateFilesTasklet(List<Path> input, Path outputDir, String outputName)
        {
            Assert.notNull(outputDir, "An output directory is required");
            Assert.isTrue(outputDir.isAbsolute(), 
                "The output directory is expected as an absolute path");
            Assert.notNull(outputName, "An output name is required");
            Assert.notEmpty(input, "A non-empty input list is required");
            this.input = input;
            this.outputDir = outputDir;
            this.outputName = outputName;
        }
        
        @Override
        public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
            throws Exception
        {
            StepExecution stepExecution = chunkContext.getStepContext().getStepExecution();
            ExecutionContext executionContext = stepExecution.getExecutionContext();
            
            // Create parent directory if needed
            
            try {
                Files.createDirectories(outputDir);
            } catch (FileAlreadyExistsException ex) {
                // no-op
            }
            
            Assert.state(Files.isDirectory(outputDir) && Files.isWritable(outputDir),
                "Expected outputDir to be a writable directory");
            
            // Concatenate input into target
            
            Path output = outputDir.resolve(outputName);
            
            OpenOption[] outputOptions = 
                new StandardOpenOption[] { StandardOpenOption.WRITE, StandardOpenOption.CREATE };
            
            try (OutputStream out = Files.newOutputStream(output, outputOptions)) {
                for (Path inputPath: input)
                    Files.copy(inputPath, out);
            }
            
            // Update execution context
            
            executionContext.put("outputDir", outputDir.toString());
            executionContext.put("outputName", outputName);
            
            return RepeatStatus.FINISHED;
        }
    }
    
    @Bean("concatenateFiles.tasklet")
    @JobScope
    public ConcatenateFilesTasklet  concatenateFilesTasklet(
        @Value("#{jobParameters['input']}") String input,
        @Value("#{jobParameters['outputName']}") String outputName,
        @Value("#{jobExecution.jobInstance.id}") Long jobId)
    {
        List<Path> inputPaths = Arrays.stream(input.split(File.pathSeparator))
            .map(Paths::get)
            .collect(Collectors.toList());
        Path outputDir = dataDir.resolve(String.valueOf(jobId));
        return new ConcatenateFilesTasklet(inputPaths, outputDir, outputName);
    }
    
    @Bean("concatenateFiles.step")
    Step step(@Qualifier("concatenateFiles.tasklet") ConcatenateFilesTasklet tasklet)
        throws Exception
    { 
        ExecutionContextPromotionListener contextListener = new ExecutionContextPromotionListener();
        contextListener.setKeys(new String[] { "outputDir" });
        contextListener.setStrict(true);
        contextListener.afterPropertiesSet();
        
        return stepBuilderFactory.get("concatenateFiles")
            .tasklet(tasklet)
            .listener(contextListener)
            .build();
    }
    
    @Bean("concatenateFiles.flow")
    Flow flow(@Qualifier("concatenateFiles.step") Step step)
    {
        return new FlowBuilder<Flow>("concatenateFiles")
            .start(step)
            .end();
    }
    
}
