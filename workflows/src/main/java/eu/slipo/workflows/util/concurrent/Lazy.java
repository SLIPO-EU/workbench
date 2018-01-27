package eu.slipo.workflows.util.concurrent;

import java.util.function.Supplier;

import org.apache.commons.lang3.Validate;

/**
 * A wrapper class for thread-safe lazy initialization of a value.
 *
 * <p>Based on the double-check initialization idiom
 * @param <V>
 */
public class Lazy<V>
{    
    private final Supplier<V> supplier;
    
    private volatile V value;

    public Lazy(Supplier<V> supplier)
    {
        Validate.notNull(supplier, "A supplier function is required");
        this.supplier = supplier;
    }
    
    public V get()
    {
        V result = value;
        if (result == null)
            result = computeIfAbsent();
        
        Validate.validState(result != null, "Did not expect a null result");
        return result;
    }

    private synchronized V computeIfAbsent()
    {
        if (value == null)
            value = supplier.get();
        return value;
    }
}