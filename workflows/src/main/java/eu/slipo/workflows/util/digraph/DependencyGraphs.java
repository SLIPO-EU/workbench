package eu.slipo.workflows.util.digraph;

import java.util.function.IntFunction;

import eu.slipo.workflows.util.digraph.ExportDependencyGraph.Direction;
import eu.slipo.workflows.util.digraph.ExportDependencyGraph.NodeAttributes;
import eu.slipo.workflows.util.digraph.TopologicalSort.CycleDetected;


public class DependencyGraphs
{
    private DependencyGraphs() {}
    
    /**
     * A convenience static factory for creating a new {@link DependencyGraph}.
     * 
     * @param n The size of the graph, i.e. the number of vertices
     */
    public static DependencyGraph create(int n)
    {
        return new AdjListDependencyGraph(n);
    }
    
    /**
     * Sort vertices of a dependency graph in topological order.
     * 
     * @param graph
     * @return
     * @throws CycleDetected if the graph contains cycles
     */
    public static Iterable<Integer> topologicalSort(DependencyGraph graph) 
        throws CycleDetected
    {
        return (new TopologicalSort(graph)).result();
    }
    
    /**
     * Check if a graph is acyclic (DAG).
     * 
     * @param graph
     * @throws CycleDetected
     */
    public static void check(DependencyGraph graph) 
        throws CycleDetected
    {
        new TopologicalSort(graph);
    }
    
    public static String toString(
        DependencyGraph graph, 
        IntFunction<String> nameMapper, 
        IntFunction<NodeAttributes> styleMapper,
        Direction direction)
    {
        return ExportDependencyGraph.toString(graph, nameMapper, styleMapper, direction);
    }
    
    public static String toString(
        DependencyGraph graph, IntFunction<String> nameMapper, IntFunction<NodeAttributes> styleMapper)
    {
        return ExportDependencyGraph.toString(graph, nameMapper, styleMapper, null);
    }
    
    public static String toString(
        DependencyGraph graph, IntFunction<String> nameMapper)
    {
        final NodeAttributes defaultStyle = ExportDependencyGraph.DEFAULT_NODE_ATTRS;
        return ExportDependencyGraph.toString(graph, nameMapper, u -> defaultStyle, null);
    }
    
    public static String toString(
        DependencyGraph graph, IntFunction<String> nameMapper, Direction direction)
    {
        final NodeAttributes defaultStyle = ExportDependencyGraph.DEFAULT_NODE_ATTRS;
        return ExportDependencyGraph.toString(graph, nameMapper, u -> defaultStyle, direction);
    }
}
