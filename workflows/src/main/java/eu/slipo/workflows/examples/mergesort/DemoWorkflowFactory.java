package eu.slipo.workflows.examples.mergesort;

import java.nio.file.Path;
import java.util.UUID;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import eu.slipo.workflows.Workflow;


@Component("mergesort.demoWorkflowFactory")
public class DemoWorkflowFactory
{
    @Autowired
    private eu.slipo.workflows.WorkflowBuilderFactory workflowBuilderFactory;
    
    @Autowired
    @Qualifier("splitFile.flow")
    private Flow splitFileFlow;
    
    @Autowired
    @Qualifier("mergesort.mergeFiles.flow")
    private Flow mergeFilesFlow;
    
    @Autowired
    @Qualifier("mergesort.sortFile.flow")
    private Flow sortFileFlow;
    
    @Autowired
    @Qualifier("mergesort.statFiles.step")
    private Step statFilesStep;
    
    public Workflow buildWorkflow(UUID workflowId, Path inputPath)
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
