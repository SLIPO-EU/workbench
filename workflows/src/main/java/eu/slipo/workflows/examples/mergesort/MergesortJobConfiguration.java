package eu.slipo.workflows.examples.mergesort;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.listener.ExecutionContextPromotionListener;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

@Configuration("mergesort.jobConfiguration")
public class MergesortJobConfiguration
{
    @Autowired
    private StepBuilderFactory stepBuilderFactory;
   
    @Autowired
    private Path jobDataDirectory;
        
    @PostConstruct
    private void setupDataDirectories() throws IOException
    {
        for (String dirName: Arrays.asList(
            "mergesort.mergeFiles", "mergesort.sortFile", "mergesort.splitFile")) 
        {
            try {
                Files.createDirectory(jobDataDirectory.resolve(dirName));
            } catch (FileAlreadyExistsException ex) {}
        }
    }
    
    @Bean("mergesort.contextListener")
    public ExecutionContextPromotionListener contextListener()
    {
        ExecutionContextPromotionListener listener = new ExecutionContextPromotionListener();
        listener.setKeys(new String[] { "outputDir" });
        listener.setStrict(true);
        return listener;
    }
      
    @Bean("mergesort.mergeFiles.tasklet")
    @JobScope
    public Tasklet mergeFilesTasklet(
        @Value("#{jobParameters['input']}") String input,
        @Value("#{jobParameters['outputName']}") String outputName,
        @Value("#{jobExecution.jobInstance.id}") Long jobId)
    {
        Path[] inputPaths = Arrays.stream(input.split(File.pathSeparator))
            .filter(p -> !p.isEmpty())
            .map(Paths::get)
            .toArray(Path[]::new);
        Path outputDir = jobDataDirectory.resolve(
            Paths.get("mergesort.mergeFiles", String.valueOf(jobId)));
        return new MergeFilesTasklet(outputDir, outputName, inputPaths);
    }
    
    @Bean("mergesort.mergeFiles.step")
    public Step mergeFilesStep(
        @Qualifier("mergesort.mergeFiles.tasklet") Tasklet tasklet,
        @Qualifier("mergesort.contextListener") ExecutionContextPromotionListener contextListener)
        throws Exception
    {
        return stepBuilderFactory.get("mergesort.mergeFiles")
            .tasklet(tasklet).listener(contextListener)
            .build();
    }
    
    @Bean("mergesort.mergeFiles.flow")
    public Flow mergeFilesFlow(@Qualifier("mergesort.mergeFiles.step") Step step)
    {
        return new FlowBuilder<Flow>("mergesort.mergeFiles").start(step).end();
    }
    
    @Bean("mergesort.sortFile.tasklet")
    @JobScope
    public Tasklet sortFileTasklet(
        @Value("#{jobParameters['input']}") String input,
        @Value("#{jobParameters['outputName']}") String outputName,
        @Value("#{jobExecution.jobInstance.id}") Long jobId)
    {
        Path inputPath = Paths.get(input);
        Path outputDir = jobDataDirectory.resolve(
            Paths.get("mergesort.sortFile", String.valueOf(jobId)));
        return new SortFileTasklet(inputPath, outputDir, outputName);
    }
    
    @Bean("mergesort.sortFile.step")
    public Step sortFileStep(
        @Qualifier("mergesort.sortFile.tasklet") Tasklet tasklet, 
        @Qualifier("mergesort.contextListener") ExecutionContextPromotionListener contextListener) 
        throws Exception
    {
        return stepBuilderFactory.get("mergesort.sortFile")
            .tasklet(tasklet).listener(contextListener)
            .build();
    }
    
    @Bean("mergesort.sortFile.flow")
    public Flow sortFileFlow(@Qualifier("mergesort.sortFile.step") Step step)
    {
        return new FlowBuilder<Flow>("mergesort.sortFile").start(step).end();
    }
 
    @Bean("mergesort.splitFile.tasklet")
    @JobScope
    public Tasklet splitFileTasklet(
        @Value("#{jobParameters['input']}") String input,
        @Value("#{jobParameters['outputPrefix']?:'p'}") String outputPrefix,
        @Value("#{jobParameters['outputSuffix']?:''}") String outputSuffix,
        @Value("#{jobParameters['numParts']}") Number numParts,
        @Value("#{jobExecution.jobInstance.id}") Long jobId)
    {
        Path inputPath = Paths.get(input);
        Assert.isTrue(Files.exists(inputPath), "The input file does not exist");
        Path outputDir = jobDataDirectory.resolve(
            Paths.get("mergesort.splitFile", String.valueOf(jobId)));
        return new SplitFileTasklet(
            inputPath, outputDir, numParts.intValue(), outputPrefix, outputSuffix);
    }
    
    @Bean("mergesort.splitFile.step")
    public Step splitFileStep(
        @Qualifier("mergesort.splitFile.tasklet") Tasklet tasklet,
        @Qualifier("mergesort.contextListener") ExecutionContextPromotionListener contextListener) 
        throws Exception
    {
        return stepBuilderFactory.get("mergesort.splitFile")
            .tasklet(tasklet).listener(contextListener)
            .build();
    }
    
    @Bean("mergesort.splitFile.flow")
    public Flow splitFileFlow(@Qualifier("mergesort.splitFile.step") Step step)
    {
        return new FlowBuilder<Flow>("mergesort.splitFile").start(step).end();
    }
    
    @Bean("mergesort.statFiles.tasklet")
    @JobScope
    public Tasklet statFilesTasklet(@Value("#{jobParameters['input']}") String input)
    {
        List<Path> inputPaths = Arrays.stream(input.split(File.pathSeparator))
            .map(Paths::get)
            .collect(Collectors.toList());
        return new StatFilesTasklet(inputPaths);
    }
    
    @Bean("mergesort.statFiles.step")
    public Step statFilesStep(@Qualifier("mergesort.statFiles.tasklet") Tasklet tasklet)
        throws Exception
    {
        return stepBuilderFactory.get("mergesort.statFiles").tasklet(tasklet).build();
    }
}
