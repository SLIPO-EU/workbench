package eu.slipo.workbench.rpc.jobs.listener;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Function;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.listener.StepExecutionListenerSupport;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.support.PatternMatcher;
import org.springframework.util.Assert;

import com.google.common.collect.Iterables;

/**
 * Provide static builders for listeners ({@link StepExecutionListener}) that map and promote
 * a part of step-level execution context to job-level execution context.
 */
public class ExecutionContextPromotionListeners
{
    public static class Builder
    {
        private List<String> keys;

        private List<String> keyPatterns;

        private Boolean strict;

        private List<String> statuses;

        private Function<String,String> keyMapper;

        private Builder() {}

        public Builder keys(List<String> keys)
        {
            Assert.isNull(this.keyPatterns, "A set of key patterns is already specified!");
            Assert.notEmpty(keys, "Expected a non empty list of keys");
            this.keys = keys;
            return this;
        }

        public Builder keys(String ...keys)
        {
            return this.keys(Arrays.asList(keys));
        }

        public Builder keysMatching(List<String> keyPatterns)
        {
            Assert.isNull(this.keys, "A set of keys is already given!");
            Assert.notEmpty(keyPatterns, "Expected a non empty list of key patterns");
            this.keyPatterns = keyPatterns;
            return this;
        }

        public Builder keysMatching(String ...keyPatterns)
        {
            return this.keysMatching(Arrays.asList(keyPatterns));
        }

        /**
         * Specify a common prefix to be applied to keys of promoted entries.
         * @param prefix
         */
        public Builder prefix(String prefix)
        {
            Assert.notNull(prefix, "Expected a non-null prefix");
            Assert.state(this.keyMapper == null, "The key mapper is already set");
            this.keyMapper = key -> prefix + "." + key;
            return this;
        }

        /**
         * Set whether we should fail if a given key is not found in step context (this
         * is meaningful only when a set of keys is given).
         * @param flag
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
        public Builder onStatus(List<String> statuses)
        {
            Assert.notEmpty(statuses, "Expected a non empty list of exit statuses");
            this.statuses = statuses;
            return this;
        }

        public Builder onStatus(String ...statuses)
        {
            return this.onStatus(Arrays.asList(statuses));
        }

        public StepExecutionListener build()
        {
            Assert.state(this.keys != null || this.keyPatterns != null,
                "A set of keys (or a key patterns) must be provided!");

            KeyMappingPromotionListener listener = new KeyMappingPromotionListener();

            if (this.keys != null) {
                listener.keys = new LinkedList<>(this.keys);
            } else {
                listener.keyPatterns = new LinkedList<>(this.keyPatterns);
            }

            if (this.keyMapper != null)
                listener.keyMapper = this.keyMapper;

            if (this.statuses != null)
                listener.statuses = new LinkedList<>(this.statuses);

            if (this.strict != null && this.keys != null)
                listener.strict = this.strict.booleanValue();

            return listener;
        }
    }

    public static Builder builder()
    {
        return new Builder();
    }

    public static StepExecutionListener fromKeys(String ...keys)
    {
        return (new Builder()).keys(keys).strict(true).build();
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
        private List<String> keyPatterns;

        private List<String> keys;

        private Function<String, String> keyMapper;

        private List<String> statuses = Collections.singletonList(ExitStatus.COMPLETED.getExitCode());

        private boolean strict = true;

        private KeyMappingPromotionListener() {}

        @Override
        public ExitStatus afterStep(StepExecution stepExecution)
        {
            if (!matchStatus(stepExecution.getExitStatus()))
                return null; // The status is not matching: promotion is skipped

            final ExecutionContext stepExecutionContext = stepExecution.getExecutionContext();
            final ExecutionContext jobExecutionContext = stepExecution.getJobExecution().getExecutionContext();

            if (keys != null) {
                // Promote given keys (if found in execution context)
                for (String key : keys) {
                    if (stepExecutionContext.containsKey(key)) {
                        String key1 = keyMapper != null ? keyMapper.apply(key) : key;
                        if (key1 != null && !key1.isEmpty())
                            jobExecutionContext.put(key1, stepExecutionContext.get(key));
                    } else if (strict) {
                        throw new IllegalStateException(
                            "The key [" + key + "] was not found into step context");
                    }
                }
            } else {
                // Promote all keys matching a pattern
                for (Entry<String,Object> entry: stepExecutionContext.entrySet()) {
                    final String key = entry.getKey();
                    if (key.startsWith(".batch"))
                        continue; // ignore keys used internally by Batch
                    if (Iterables.any(keyPatterns, keyPattern -> PatternMatcher.match(keyPattern, key))) {
                        String key1 = keyMapper != null ? keyMapper.apply(key) : key;
                        if (key1 != null && !key1.isEmpty())
                            jobExecutionContext.put(key1, entry.getValue());
                    }
                }
            }

            return null;
        }

        private boolean matchStatus(ExitStatus exitStatus)
        {
            final String exitCode = exitStatus.getExitCode();
            return Iterables.any(statuses, status -> PatternMatcher.match(status, exitCode));
        }
    }
}
