package eu.slipo.workflows.util.concurrent;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections4.IteratorUtils;

/**
 * Provide static utility methods for lazy-initialized objects  
 */
public class LazyUtils
{
    private LazyUtils() {}
    
    public static <V> Iterable<V> lazyIterable(Iterator<V> iterator)
    {
        Lazy<List<V>> list = 
            new Lazy<List<V>>(() -> IteratorUtils.toList(iterator));
        
        return () -> list.get().iterator();
    }
}