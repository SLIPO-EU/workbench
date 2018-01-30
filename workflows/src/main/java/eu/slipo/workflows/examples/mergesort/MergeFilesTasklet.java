package eu.slipo.workflows.examples.mergesort;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Scanner;

import org.apache.commons.collections4.iterators.PeekingIterator;
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

/**
 * A tasklet that performs k-way merging on a bunch of input files.
 */
public class MergeFilesTasklet implements Tasklet
{
    private static Logger logger = LoggerFactory.getLogger(MergeFilesTasklet.class);
    
    private final List<Path> inputPaths;
    
    private final Path outputDir;
    
    private final String outputName;
    
    public MergeFilesTasklet(Path outputDir, String outputName, Path... inputPaths)
    {
        Assert.isTrue(outputDir != null && outputDir.isAbsolute(), 
            "Expected an absolute path for output directory");
        Assert.isTrue(!StringUtils.isEmpty(outputName), 
            "An output name is required");
        Assert.isTrue(inputPaths.length >= 2, "Expected at least 2 files to merge");
        Assert.isTrue(Arrays.stream(inputPaths).allMatch(Files::isReadable), 
            "Expected a list of readable files");
        this.inputPaths = new ArrayList<>(Arrays.asList(inputPaths));
        this.outputDir = outputDir;
        this.outputName = outputName;
    }

    private static PeekingIterator<Long> inputIterator(Scanner in)
    {            
        Iterator<Long> iter = new Iterator<Long>()
        {
            @Override
            public Long next()
            {
                return in.nextLong();
            }
            
            @Override
            public boolean hasNext()
            {
                return in.hasNextLong();
            }
        };
        
        return new PeekingIterator<>(iter);
    }

    private static class Entry
    {
        private final int index;
        
        private final Long value;

        public Entry(int index, long value)
        {
            this.index = index;
            this.value = value;
        }
        
        public static Entry of(int index, long value)
        {
            return new Entry(index, value);
        }
        
        public int index()
        {
            return index;
        }
        
        public Long value()
        {
            return value;
        }
    }
      
    /**
     * Perform a basic (2-way) merge for the pair of input files
     * 
     * @param outPath
     * @param inputPath1
     * @param inputPath2
     * @throws IOException
     */
    private static void merge2(Path outPath, Path inputPath1, Path inputPath2) 
        throws IOException
    {
        try (
            Scanner scanner1 = new Scanner(Files.newInputStream(inputPath1));
            Scanner scanner2 = new Scanner(Files.newInputStream(inputPath2));
            BufferedWriter writer = Files.newBufferedWriter(outPath)) 
        {
            PeekingIterator<Long> in1 = inputIterator(scanner1);
            PeekingIterator<Long> in2 = inputIterator(scanner2);
            
            Long x1, x2;
            while ((x1 = in1.peek()) != null && (x2 = in2.peek()) != null) {
                if (x1 <= x2) {
                    writer.write(x1.toString());
                    in1.next();
                } else {
                    writer.write(x2.toString());
                    in2.next();
                }
                writer.newLine();
            }
            
            while ((x1 = in1.peek()) != null) {
                writer.write(x1.toString());
                in1.next();
                writer.newLine();
            }
            
            while ((x2 = in2.peek()) != null) {
                writer.write(x2.toString());
                in2.next();
                writer.newLine();
            }
        }
    }
    
    /**
     * Perform a k-way merge for the list of input files
     * @param outPath
     * @param inputPaths
     * @throws IOException
     */
    private static void merge(Path outputPath, List<Path> inputPaths) 
        throws IOException
    {
        final int k = inputPaths.size(); 
        
        PriorityQueue<Entry> pq = 
            new PriorityQueue<>(Comparator.comparing(Entry::value));
        List<Scanner> scanners = new ArrayList<>();
        BufferedWriter writer = null;
        try {
            // Open readers and writer
            for (Path path: inputPaths) {
                Scanner scanner = new Scanner(Files.newBufferedReader(path)); 
                scanners.add(scanner);
            }
            writer = Files.newBufferedWriter(outputPath);
            
            // Populate heap with initial values from each stream
            for (int index = 0; index < k; ++index) {
                Scanner scanner = scanners.get(index);
                if (scanner.hasNextLong())
                    pq.add(Entry.of(index, scanner.nextLong()));
            }
            
            // Merge 
            while (!pq.isEmpty()) {
                Entry e = pq.remove();
                int index = e.index();
                // Write value to output
                writer.write(String.valueOf(e.value()));
                writer.newLine();
                // Advance scanner
                Scanner scanner = scanners.get(index);
                if (scanner.hasNextLong())
                    pq.add(Entry.of(index, scanner.nextLong()));
            }
        } finally {
            for (Scanner scanner: scanners)
                scanner.close();
            if (writer != null) 
                writer.close();
        } 
    }
    
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
        throws Exception
    {
        StepExecution stepExecution = chunkContext.getStepContext().getStepExecution();
        ExecutionContext executionContext = stepExecution.getExecutionContext();
        
        final Path outputPath = outputDir.resolve(outputName);
        
        // Create output directory
        try {
            Files.createDirectory(outputDir);
            logger.debug("Created output directory at {}", outputDir);
        } catch (IOException ex) {
            // no-op
        }
        
        // Merge
        if (inputPaths.size() == 2) {
            merge2(outputPath, inputPaths.get(0), inputPaths.get(1));
        } else {
            merge(outputPath, inputPaths);
        }
        
        // Update execution context
        executionContext.put("outputDir", outputDir.toString());
        
        return null;
    }
}
