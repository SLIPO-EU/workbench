package eu.slipo.workbench.rpc.jobs.listener;

import java.util.function.Function;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.listener.ExecutionContextPromotionListener;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.support.PatternMatcher;
import org.springframework.util.Assert;

/**
 * A listener that maps a part of step execution context to job execution context.
 * <p>
 * This behavior is useful basically when we want to avoid key conflicts on entries from
 * different steps. Note that if no mapping is needed (no conflict is possible), then
 * one could use {@link ExecutionContextPromotionListener} that simply copies entries.  
 */
public class ExecutionContextKeyMappingPromotionListener implements StepExecutionListener
{
    private final String[] keys;
    
    private final Function<String, String> keyMapper;

    private String[] statuses = new String[] { ExitStatus.COMPLETED.getExitCode() };

    private boolean strict = false;
    
    public ExecutionContextKeyMappingPromotionListener(String[] keys, Function<String, String> keyMapper)
    {
        Assert.notNull(keys, "Expected a non null array of keys");
        Assert.notEmpty(keys, "Expected a non empty array of keys");
        Assert.notNull(keyMapper, "Expected a non null key mapper (for context keys)");
        
        this.keys = keys;
        this.keyMapper = keyMapper;
    }
    
    /**
     * A convenience factory for creating a listener that just adds a prefix to target keys.
     */
    public static ExecutionContextKeyMappingPromotionListener createWithPrefix(String[] keys, String keyPrefix)
    {
        return new ExecutionContextKeyMappingPromotionListener(keys, key -> keyPrefix + "." + key);
    }
    
    /**
     * Set if an exception should be thrown if a key is missing
     */
    public void setStrict(boolean strict)
    {
        this.strict = strict;
    }
    
    /**
     * Set a list of statuses for which the promotion should occur
     */
    public void setStatuses(String[] statuses)
    {
        this.statuses = statuses;
    }
    
    @Override
    public void beforeStep(StepExecution stepExecution) 
    {
        // no-op
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution)
    {
        ExecutionContext stepContext = stepExecution.getExecutionContext();
        ExecutionContext jobContext = stepExecution.getJobExecution().getExecutionContext();
        
        String exitCode = stepExecution.getExitStatus().getExitCode();
        for (String statusPattern : statuses) {
            if (PatternMatcher.match(statusPattern, exitCode)) {
                for (String key : keys) {
                    if (stepContext.containsKey(key)) {
                        String key1 = keyMapper.apply(key);
                        jobContext.put(key1, stepContext.get(key));
                    } else if (strict) {
                        throw new IllegalStateException(
                            "The key [" + key +"] was not found into step context");
                    }
                }
            }
        }
        return null;
    }
}