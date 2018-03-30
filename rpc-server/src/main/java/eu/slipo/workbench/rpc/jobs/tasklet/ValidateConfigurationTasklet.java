package eu.slipo.workbench.rpc.jobs.tasklet;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.util.Assert;

import eu.slipo.workbench.common.model.tool.InvalidConfigurationException;

/**
 * A tasklet that validates a configuration bean from job execution context
 */
public class ValidateConfigurationTasklet <T extends Serializable> implements Tasklet
{
    private final Class<T> targetType;

    private final String contextKey;

    private final Validator validator;

    public ValidateConfigurationTasklet(Class<T> targetType, String key, Validator validator)
    {
        Assert.notNull(targetType, "The class descriptor for target configuration is required");
        Assert.notNull(key, "A key (for the context entry) is required");
        Assert.notNull(validator, "A bean validator is required");

        this.targetType = targetType;
        this.contextKey = key;
        this.validator = validator;
    }

    public ValidateConfigurationTasklet(Class<T> targetType, Validator validator)
    {
        this(targetType, "config", validator);
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
        throws Exception
    {
        StepContext stepContext = chunkContext.getStepContext();
        Map<String, Object> jobExecutionContext = stepContext.getJobExecutionContext();

        // Load value from context; cast to target type

        if (!jobExecutionContext.containsKey(contextKey)) {
            throw new IllegalStateException(
                "The key [" + contextKey + "] is not found into execution context");
        }

        T config = targetType.cast(jobExecutionContext.get(contextKey));

        // Validate

        Set<ConstraintViolation<T>> constraintViolations = validator.validate(config);
        if (!constraintViolations.isEmpty()) {
            throw InvalidConfigurationException.fromErrors(constraintViolations);
        }

        return RepeatStatus.FINISHED;
    }

}
