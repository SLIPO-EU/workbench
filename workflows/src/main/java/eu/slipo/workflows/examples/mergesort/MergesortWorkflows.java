package eu.slipo.workflows.examples.mergesort;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.slipo.workflows.Workflow;
import eu.slipo.workflows.WorkflowBuilderFactory;
import eu.slipo.workflows.WorkflowExecutionEventListener;

@Configuration("mergesort.workflows")
public class MergesortWorkflows
{
    @Autowired
    private WorkflowBuilderFactory workflowBuilderFactory;
    
    @Autowired
    @Qualifier("mergesort.splitFile.flow")
    private Flow splitFileFlow;
    
    @Autowired
    @Qualifier("mergesort.statFiles.step")
    private Step statFilesStep;
    
    @Autowired
    @Qualifier("mergesort.mergeFiles.flow")
    private Flow mergeFilesFlow;
    
    @Autowired
    @Qualifier("mergesort.sortFile.flow")
    private Flow sortFileFlow;
    
    public static final String RESULT_FILENAME = "r.txt";
    
    private static final long DEFAULT_PART_SIZE = 10 * 1024 * 1024;
    
    public Builder getBuilder(UUID workflowId, Path inputPath)
    {
        return this.new Builder(workflowId, inputPath);
    }
    
    public class Builder
    {
        private final UUID workflowId;
        
        private final Path inputPath;
        
        private long partSize = DEFAULT_PART_SIZE;
        
        private int numParts = -1;
        
        private int k = 5;
        
        private WorkflowExecutionEventListener listener;
        
        private Builder(UUID workflowId, Path inputPath)
        {
            Assert.notNull(workflowId, "Expected a non-null workflow id");
            Assert.notNull(inputPath, "Expected a non-null input path");
            Assert.isTrue(Files.isReadable(inputPath), "Expected a readable input file");
            this.workflowId = workflowId;
            this.inputPath = inputPath;
        }
        
        public Builder listener(WorkflowExecutionEventListener listener)
        {
            Assert.notNull(listener, "Expected a non-null listener");
            this.listener = listener;
            return this;
        }
        
        /**
         * Specify the number of parts to split the input file. 
         * 
         * This option is mutually exclusive with {@link Builder#partSize(long)}.
         * @param numParts
         * @return
         */
        public Builder numParts(int numParts)
        {
            Assert.isTrue(numParts > 1, "Expected number of parts to be > 1");
            this.numParts = numParts;
            this.partSize = -1;
            return this;
        }
        
        /**
         * Specify a desired file size for each part of the input.
         * 
         * This option is mutually exclusive with {@link Builder#numParts(long)}.
         * @param size A file size (in bytes)
         */
        public Builder partSize(long size)
        {
            Assert.isTrue(size > 1024 * 1024, "The minimum size of a part is 1MB");
            this.partSize = size;
            this.numParts = -1;
            return this;
        }
        
        /**
         * The number of files to be handled by a single k-way merge job
         * 
         * @param k
         */
        public Builder mergeIn(int k)
        {
            Assert.isTrue(k > 1, "Expected k > 1");
            this.k = k;
            return this;
        }
        
        /**
         * Build a k-way merging workflow according to the prescription.
         * 
         * @throws IOException if an attempt to determine the size of the input fails 
         */
        public Workflow build() throws IOException
        {            
            int n;
            
            if (numParts < 0) {
                final long inputSize = Files.size(inputPath);
                n = Long.valueOf(inputSize / partSize).intValue();
            } else {
                n = numParts;
            }
            
            n = ((n - 1) / k + 1) * k; // closest multiple of k
            return buildWorkflow(n, k);
        }
        
        private Workflow buildWorkflow(final int n, final int k)
        {
            final String mergerName = "merger-%d-h%d";
            final String sorterName = "sorter-%d";
            
            Workflow.Builder workflowBuilder = workflowBuilderFactory.get(workflowId);

            // Define splitter 
            
            List<Path> splitterParts = IntStream.rangeClosed(1, n)
                .mapToObj(i -> Paths.get(String.format("part%d.txt", i)))
                .collect(Collectors.toList());
            workflowBuilder
                .job(b -> b
                    .name("splitter")
                    .flow(splitFileFlow)
                    .input(inputPath)
                    .parameters(p -> p
                        .addString("outputPrefix", "part")
                        .addString("outputSuffix", ".txt")
                        .addLong("numParts", Long.valueOf(n)))
                    .output(splitterParts))
                .job(b -> b
                    .name("stat-parts")
                    .flow(statFilesStep)
                    .input("splitter", "part*.txt"));
            
            // Define sorters
            
            for (int i = 0; i < n; ++i) {
                final int s = i + 1;
                workflowBuilder
                    .job(b -> b
                        .name(String.format(sorterName, s))
                        .flow(sortFileFlow)
                        .parameters(p -> p.addString("outputName", "r.txt"))
                        .input("splitter", String.format("part%d.txt", s))
                        .output("r.txt"));
            }
            
            // Define mergers
            
            Set<Pair<Integer,Integer>> span = new HashSet<>();
            int h = -1;
            for (h = 1; h < n; h *= k) {
                for (int i = 0; i + h < n; i += k * h) {
                    final List<String> dependencyNames = new ArrayList<>();
                    final int s = i + 1;
                    final int d = k * h;
                    for (int j = s, r = 0; r < k && j <= n; r++, j += h) {
                         int h1 = h;
                         while (h1 > 1 && !span.contains(Pair.of(j, h1)))
                             h1 = h1 / k;
                         dependencyNames.add(
                             h1 == 1? String.format(sorterName, j) : String.format(mergerName, j, h1));
                    }
                    span.add(Pair.of(s, d));
                    workflowBuilder
                        .job(b -> b
                            .name(String.format(mergerName, s, d))
                            .flow(mergeFilesFlow)
                            .parameters(p -> p.addString("outputName", "r.txt"))
                            .input(dependencyNames, "r.txt")
                            .output("r.txt"));
                    
                }
            }
            
            workflowBuilder.output(String.format(mergerName, 1, h), "r.txt", RESULT_FILENAME);
         
            if (listener != null)
                workflowBuilder.listener(listener);
            
            return workflowBuilder.build();
        }
    }
    
    public Workflow demoWorkflow(UUID workflowId, Path inputPath)
    {
        Workflow workflow = workflowBuilderFactory.get(workflowId)
            .output("merger-1-2-3-4-5-6", "r.txt")
            .job(b -> b
                .name("splitter")
                .flow(splitFileFlow)
                .input(inputPath)
                .parameters(p -> p
                    .addString("outputPrefix", "p")
                    .addString("outputSuffix", ".txt")
                    .addLong("numParts", 6L))
                .output("p1.txt", "p2.txt", "p3.txt", "p4.txt", "p5.txt", "p6.txt"))
            .job(b -> b
                .name("sorter-1")
                .flow(sortFileFlow)
                .input("splitter", "p1.txt")
                .parameters(p -> p.addString("outputName", "r.txt"))
                .output("r.txt"))
            .job(b -> b
                .name("sorter-2")
                .flow(sortFileFlow)
                .input("splitter", "p2.txt")
                .parameters(p -> p.addString("outputName", "r.txt"))
                .output("r.txt"))
            .job(c -> c
                .name("sorter-3")
                .flow(sortFileFlow)
                .input("splitter", "p3.txt")
                .parameters(p -> p.addString("outputName", "r.txt"))
                .output("r.txt"))
            .job(b -> b
                .name("sorter-4")
                .flow(sortFileFlow)
                .input("splitter", "p4.txt")
                .parameters(p -> p.addString("outputName", "r.txt"))
                .output("r.txt"))
            .job(b -> b
                .name("sorter-5")
                .flow(sortFileFlow)
                .input("splitter", "p5.txt")
                .parameters(p -> p.addString("outputName", "r.txt"))
                .output("r.txt"))
            .job(b -> b
                .name("sorter-6")
                .flow(sortFileFlow)
                .input("splitter", "p6.txt")
                .parameters(p -> p.addString("outputName", "r.txt"))
                .output("r.txt"))
            .job(b -> b
                .name("merger-1-2")
                .flow(mergeFilesFlow)
                .input("sorter-1", "r.txt")
                .input("sorter-2", "r.txt")
                .parameters(p -> p.addString("outputName", "r.txt"))
                .output("r.txt"))
            .job(b -> b
                .name("merger-3-4")
                .flow(mergeFilesFlow)
                .input("sorter-3", "r.txt")
                .input("sorter-4", "r.txt")
                .parameters(p -> p.addString("outputName", "r.txt"))
                .output("r.txt"))
            .job(b -> b
                .name("merger-5-6")
                .flow(mergeFilesFlow)
                .input("sorter-5", "r.txt")
                .input("sorter-6", "r.txt")
                .parameters(p -> p.addString("outputName", "r.txt"))
                .output("r.txt"))
            .job(b -> b
                .name("merger-1-2-3-4")
                .flow(mergeFilesFlow)
                .input("merger-1-2", "r.txt")
                .input("merger-3-4", "r.txt")
                .parameters(p -> p.addString("outputName", "r.txt"))
                .output("r.txt"))
            .job(b -> b
                .name("merger-1-2-3-4-5-6")
                .flow(mergeFilesFlow)
                .input("merger-1-2-3-4", "r.txt")
                .input("merger-5-6", "r.txt")
                .parameters(p -> p.addString("outputName", "r.txt"))
                .output("r.txt"))
            .job(b -> b
                .name("stat-parts")
                .flow(statFilesStep)
                .input("splitter", "p*.txt"))
            .build();
        
        return workflow;
    }
    
}
