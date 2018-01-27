package eu.slipo.workflows.util.digraph;

import java.util.BitSet;
import java.util.Iterator;


/**
 * A dependency graph.
 * 
 * This is implemented as a directed graph with an adjacency matrix.
 *  
 * <p>If number of vertices V is expected to be < 10^3, the O(V^2) space cost (for the
 * matrix itself), and the O(V) time cost (for iterating on adjacent vertices) is acceptable.
 */
public class AdjMatrixDependencyGraph implements DependencyGraph
{
    /** The number of vertices (V) */
    private final int n;
   
    /** 
     * The adjacency matrix as a V x V boolean matrix 
     */
    private final BitSet adj;
    
    private class DependencyIterator implements Iterator<Integer>
    {
        private final int startIndex;
        private final int endIndex;
        private int index;
        
        public DependencyIterator(int u)
        {
            startIndex = index(u, 0);
            endIndex = startIndex + n;
            index = adj.nextSetBit(startIndex);
        }
        
        @Override
        public boolean hasNext()
        {
            return (index >= 0 && index < endIndex);
        }

        @Override
        public Integer next()
        {
            int v = column(index);
            index = adj.nextSetBit(index + 1);
            return v;
        }   
    }
    
    private class DependentIterator implements Iterator<Integer>
    {
        private final int startIndex;
        private final int endIndex;
        private int index;
        
        public DependentIterator(int v)
        {
            startIndex = index(0, v);    
            endIndex = adj.size();
            index = next(startIndex, endIndex);
        }
            
        @Override
        public boolean hasNext()
        {
            return index < endIndex;
        }

        @Override
        public Integer next()
        {
            int u = row(index);
            index = next(index + n, endIndex);
            return u;
        }
        
        private int next(int fromIndex, int toIndex) 
        {
            int i = fromIndex;
            while (!adj.get(i) && i < toIndex)
                i += n;
            return i;
        }
    }
    
    /**
     * Create a dependency graph for <tt>n</tt> items
     * @param n
     */
    public AdjMatrixDependencyGraph(int n)
    {
        if (n < 0)
            throw new IllegalArgumentException("Expected a non-negative size");
        this.n = n;
        this.adj = new BitSet(n * n);
    }
    
    @Override
    public void addDependency(int u, int v)
    {
        checkVertex(u);
        checkVertex(v);
        adj.set(index(u, v));
    }
    
    @Override
    public boolean depends(int u, int v)
    {
        checkVertex(u);
        checkVertex(v);
        return adj.get(index(u, v));
    }
    
    @Override
    public int size()
    {
        return n;
    }
    
    @Override
    public int numberOfDependencies()
    {
        return adj.cardinality();
    }
    
    @Override
    public Iterable<Integer> dependencies(final int u)
    {
        checkVertex(u);
        return () -> new DependencyIterator(u);
    }
    
    @Override
    public Iterable<Integer> dependents(final int v)
    {
        checkVertex(v);
        return () -> new DependentIterator(v);
    }
    
    private void checkVertex(int u)
    {
        if (u < 0 || u >= n)
            throw new IllegalArgumentException("The vertex number is out of bounds");
    }
    
    private int index(int u, int v)
    {
        return u * n + v;
    }
    
    private int row(int index)
    {
        return index / n;
    }
    
    private int column(int index)
    {
        return index % n;
    }
}