package eu.slipo.workflows.jobs.examples.mergesort;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * Configure a job that computes statistics on a group of input files 
 */
@Component("mergesort.statFiles.jobConfiguration")
public class StatFileJobConfiguration
{
    @Autowired
    private StepBuilderFactory stepBuilderFactory;
    
    private static class StatFilesTasklet implements Tasklet
    {
        private static final Logger logger = LoggerFactory.getLogger(StatFilesTasklet.class);
        
        private final List<Path> inputList;
        
        public StatFilesTasklet(List<Path> input)
        {
            Assert.notEmpty(input, "Expected a non empty collection of inputs");
            this.inputList = input;
        }

        private long countLines(Path inputPath) throws IOException
        {
            long count = 0;
            try (BufferedReader reader = Files.newBufferedReader(inputPath)) {
                String line = null;
                while ((line = reader.readLine()) != null)
                    count++;
            }
            return count;
        }
        
        @Override
        public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
            throws Exception
        {
            for (Path input: this.inputList) {
                long size = Files.size(input);
                long lineCount = countLines(input);
                logger.info("Got input {}: {} lines, {} bytes", input, lineCount, size);
            }
            
            return null;
        }
    }
    
    @Bean("mergesort.statFiles.tasklet")
    @JobScope
    public Tasklet tasklet(@Value("#{jobParameters['input']}") String input)
    {
        List<Path> inputPaths = Arrays.stream(input.split(File.pathSeparator))
            .map(Paths::get)
            .collect(Collectors.toList());
        return new StatFilesTasklet(inputPaths);
    }
    
    @Bean("mergesort.statFiles.step")
    public Step step(@Qualifier("mergesort.statFiles.tasklet") Tasklet tasklet)
        throws Exception
    {
        return stepBuilderFactory.get("mergesort.statFiles")
            .tasklet(tasklet)
            .build();
    }
}
