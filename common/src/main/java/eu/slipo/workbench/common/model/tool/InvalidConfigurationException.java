package eu.slipo.workbench.common.model.tool;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;

import org.springframework.util.Assert;

/**
 * An exception thrown to indicate that invalid configuration passed for a tool. 
 */
@SuppressWarnings("serial")
public class InvalidConfigurationException extends RuntimeException
{
    private static class Detail 
    {
        private final javax.validation.Path propertyPath;
        
        private final Object invalidValue;
        
        private final String message;

        private Detail(javax.validation.Path propertyPath, Object invalidValue, String message)
        {
            this.propertyPath = propertyPath;
            this.invalidValue = invalidValue;
            this.message = message;
        }
        
        public static <T> Detail create(ConstraintViolation<T> constraintViolation)
        {
            return new Detail(
                constraintViolation.getPropertyPath(), 
                constraintViolation.getInvalidValue(),
                constraintViolation.getMessage());
        }
    }
    
    private List<Detail> details;
    
    private InvalidConfigurationException(List<Detail> details)
    {
        super("The configuration is invalid"); 
        this.details = details;
    }
    
    public static <T> InvalidConfigurationException fromErrors(
        Set<ConstraintViolation<T>> constraintViolations)
    {
        Assert.notEmpty(constraintViolations, "Expected a non-empty set of violations");
        
        List<Detail> details = constraintViolations.stream()
            .collect(Collectors.mapping(Detail::create, Collectors.toList()));
        
        return new InvalidConfigurationException(Collections.unmodifiableList(details));
    }
    
    public List<Detail> getErrorDetails()
    {
        return details;
    }
    
    /**
     * Generate a detailed message (invalid properties with property-specific messages)
     */
    @Override
    public String getMessage()
    {
        StringBuilder sb = new StringBuilder(super.getMessage() + ": ");
        for (Detail detail: details) {
            sb.append(String.format("%n - [%s]: %s", 
                detail.propertyPath, detail.message));
        }
        sb.append(String.format("%n"));
        return sb.toString();
    }
}
