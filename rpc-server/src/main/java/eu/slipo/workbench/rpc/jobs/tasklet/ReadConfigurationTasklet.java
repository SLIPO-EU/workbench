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
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.util.Assert;

import eu.slipo.workbench.common.model.tool.InvalidConfigurationException;
import eu.slipo.workbench.common.service.util.PropertiesConverterService;

/**
 * A tasklet reading job parameters into a configuration bean
 */
public class ReadConfigurationTasklet <T extends Serializable> implements Tasklet
{
    private final PropertiesConverterService propertiesConverter;

    private final Class<T> targetType;

    private Validator validator;

    public ReadConfigurationTasklet(Class<T> targetType, PropertiesConverterService converter)
    {
        Assert.notNull(targetType,
            "The class descriptor for target configuration is required");
        Assert.notNull(converter,
            "A properties converter is required (to read job parameters)");
        this.targetType = targetType;
        this.propertiesConverter = converter;
    }

    /**
     * Provide a bean validator to validate target configuration
     */
    public void setValidator(Validator validator)
    {
        this.validator = validator;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
        throws Exception
    {
        StepContext stepContext = chunkContext.getStepContext();
        ExecutionContext executionContext = stepContext.getStepExecution().getExecutionContext();
        Map<String, Object> jobParameters = stepContext.getJobParameters();

        // Read properties for given target type

        T config = propertiesConverter.propertiesToValue(jobParameters, targetType);

        if (validator != null) {
            Set<ConstraintViolation<T>> constraintViolations = validator.validate(config);
            if (!constraintViolations.isEmpty()) {
                throw InvalidConfigurationException.fromErrors(constraintViolations);
            }
        }

        // Update execution context

        executionContext.put("config", config);

        return RepeatStatus.FINISHED;
    }
}
