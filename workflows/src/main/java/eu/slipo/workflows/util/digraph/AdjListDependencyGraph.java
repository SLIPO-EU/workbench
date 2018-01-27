package eu.slipo.workflows.util.digraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A dependency graph.
 * 
 * <p>This is implemented as a directed graph with an adjacency list.
 */
public class AdjListDependencyGraph implements DependencyGraph
{
    /**
     * The number of vertices (i.e. n = |V|)
     */
    private final int n;
    
    /** 
     * adj[v] is the set of vertices adjacent to v.
     */
    private final List<Set<Integer>> adj;
    
    /** 
     * adjTo[v] is the set of vertices which arr adjacent to v.
     * 
     * <p>This information is redundant (it can be computed from adj), and is only
     * useful to speedup iteration on dependent vertices. 
     */
    private final List<Set<Integer>> adjTo;
    
    /**
     * Create a dependency graph for <tt>n</tt> items
     * @param n
     */
    public AdjListDependencyGraph(int n)
    {
        if (n < 0)
            throw new IllegalArgumentException("Expected a non-negative size");
        this.n = n;
        this.adj = new ArrayList<>(Collections.nCopies(n, null));
        this.adjTo = new ArrayList<>(Collections.nCopies(n, null));
    }
    
    private void checkVertex(int u)
    {
        if (u < 0 || u >= n)
            throw new IllegalArgumentException("The vertex number is out of bounds");
    }
    
    @Override
    public void addDependency(int u, int v)
    {
        checkVertex(u);
        checkVertex(v);
        
        Set<Integer> a = adj.get(u);
        if (a == null) {
            a = new HashSet<>(); 
            adj.set(u, a);
        }
        a.add(v);
        
        Set<Integer> b = adjTo.get(v);
        if (b == null) {
            b = new HashSet<>(); 
            adjTo.set(v, b);
        }
        b.add(u);
    }

    @Override
    public boolean depends(int u, int v)
    {
        checkVertex(u);
        checkVertex(v);
        
        Set<Integer> a = adj.get(u);
        return a == null? false : a.contains(v);
    }

    @Override
    public int size()
    {
        return n;
    }

    @Override
    public int numberOfDependencies()
    {
        return adj.stream()
            .mapToInt(a -> a == null? 0 : a.size())
            .sum();
    }

    @Override
    public Iterable<Integer> dependencies(int u)
    {
        checkVertex(u);
        final Set<Integer> a = adj.get(u);
        return a == null? Collections.emptyList(): a; 
    }

    @Override
    public Iterable<Integer> dependents(int v)
    {
        checkVertex(v);
        final Set<Integer> b = adjTo.get(v);
        return b == null? Collections.emptyList(): b; 
    }

}
