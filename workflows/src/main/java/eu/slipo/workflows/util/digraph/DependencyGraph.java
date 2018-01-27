package eu.slipo.workflows.util.digraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A dependency graph for items represented as integers.
 */
public interface DependencyGraph
{
    /**
     * Add a dependency of <tt>u</tt> on <tt>v</tt>
     * @param u The dependent
     * @param v The dependency
     */
    void addDependency(int u, int v);

    /**
     * Test if a given item (vertex) <tt>u</tt> has a dependency on item <tt>v</tt>.
     * @param u
     */
    boolean depends(int u, int v);

    /**
     * The number of items (i.e. number of vertices).
     * The items under examination are represented as vertices in <tt>0..size-1</tt>    
     */
    int size();

    /**
     * The number of declared dependencies (i.e number of edges)
     * @return
     */
    int numberOfDependencies();

    /**
     * Get dependencies of u
     * @param u
     */
    Iterable<Integer> dependencies(int u);

    /**
     * Get dependents on v
     * @param v
     */
    Iterable<Integer> dependents(int v);

    /**
     * Get dependencies of u as a list
     * @param u
     */
    default List<Integer> dependencyList(int u)
    {
        ArrayList<Integer> deps = new ArrayList<>(size());
        for (Integer v: dependencies(u))
            deps.add(v);
        return Collections.unmodifiableList(deps);
    }
}