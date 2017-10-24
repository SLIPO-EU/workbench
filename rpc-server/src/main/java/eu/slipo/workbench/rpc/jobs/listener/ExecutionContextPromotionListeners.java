package eu.slipo.workbench.rpc.jobs.listener;

import java.util.function.Function;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.listener.StepExecutionListenerSupport;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.support.PatternMatcher;
import org.springframework.util.Assert;

/**
 * Provide static builders for listeners ({@link StepExecutionListener}) that map and promote 
 * a part of step-execution context to job-execution context. 
 */
public class ExecutionContextPromotionListeners
{
    public static class Builder
    {
        private final String[] keys;
        
        private Boolean strict;
        
        private String[] statuses;
        
        private Function<String,String> keyMapper;
        
        private Builder(String[] keys)
        {
            this.keys = keys;
        }
        
        /**
         * Specify a common prefix to be applied to keys of promoted entries.
         * @param prefix
         */
        public Builder prefix(String prefix)
        {
            Assert.notNull(prefix, "Expected a non-null prefix");
            Assert.state(keyMapper == null, "The key mapper is already set");
            this.keyMapper = key -> prefix + "." + key; 
            return this;
        }
        
        /**
         * Set whether we should fail if a given key is not found in step context.
         * @return
         */
        public Builder strict(boolean flag)
        {
            this.strict = flag;
            return this;
        }
        
        /**
         * Provide a mapping function to be applied to keys of promoted entries
         * @param keyMapper
         */
        public Builder map(Function<String,String> keyMapper)
        {
            Assert.notNull(keyMapper, "Expected a non-null key mapper");
            Assert.state(keyMapper == null, "The key mapper is already set");
            this.keyMapper = keyMapper;
            return this;
        }
        
        /**
         * Specify a list of statuses for which promotion will occur.
         */
        public Builder onStatus(String ...statuses)
        {
            Assert.notEmpty(statuses, "Expected a non empty array of exit statuses");
            this.statuses = statuses;
            return this;
        }
        
        public StepExecutionListener build()
        {   
            KeyMappingPromotionListener listener = new KeyMappingPromotionListener(keys, keyMapper);
            
            if (statuses != null)
                listener.setStatuses(statuses);
            if (strict != null)
                listener.setStrict(strict);
            
            return listener;
        }
    }
    
    public static Builder fromKeys(String ...keys)
    {
        return new Builder(keys);
    }
    
    /**
     * A listener that maps a part of step execution context to job execution context.
     * <p>
     * This behavior is useful basically when we want to avoid key conflicts on entries from
     * different steps. Note that if no mapping is needed (no conflict is possible), then
     * one could use {@link ExecutionContextPromotionListener} that simply copies entries.  
     */
    private static class KeyMappingPromotionListener extends StepExecutionListenerSupport
    {
        private final String[] keys;
        
        private final Function<String, String> keyMapper;

        private String[] statuses = new String[] { ExitStatus.COMPLETED.getExitCode() };

        private boolean strict = false;
        
        public KeyMappingPromotionListener(String[] keys, Function<String, String> keyMapper)
        {
            Assert.notNull(keys, "Expected a non null array of keys");
            Assert.notEmpty(keys, "Expected a non empty array of keys");
            this.keys = keys;
            this.keyMapper = keyMapper;
        }
        
        public KeyMappingPromotionListener(String[] keys)
        {
            this(keys, null);
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
        public ExitStatus afterStep(StepExecution stepExecution)
        {
            ExecutionContext stepContext = stepExecution.getExecutionContext();
            ExecutionContext jobContext = stepExecution.getJobExecution().getExecutionContext();
            
            String exitCode = stepExecution.getExitStatus().getExitCode();
            for (String statusPattern : statuses) {
                if (PatternMatcher.match(statusPattern, exitCode)) {
                    for (String key : keys) {
                        if (stepContext.containsKey(key)) {
                            String key1 = keyMapper != null? keyMapper.apply(key) : key;
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
    
}
