package eu.slipo.workflows.util.digraph;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;

public class TopologicalSort
{
    @SuppressWarnings("serial")
    public static class CycleDetected extends Exception
    {
        public final int root;
        
        private CycleDetected(int root)
        {
            this.root = root;
        }
        
        public int root()
        {
            return root;
        }
        
        @Override
        public String getMessage()
        {
            return String.format("cycle detected at vertex %s", root);
        } 
    }
    
    private enum Color { 
        WHITE, // not visited  
        GREY,  // visited, not finished
        BLACK; // visited, finished with all accessible vertices 
    };
    
    private final DependencyGraph graph;
    
    private final int n;
    
    private Color[] color = null;
    
    private Deque<Integer> result = null;
    
    TopologicalSort(DependencyGraph g) throws CycleDetected
    {
        this.graph = g;
        this.n = g.size();
        
        // Perform a DFS scan 
        dfs();
    }
    
    private void dfs() throws CycleDetected
    {
        result = new ArrayDeque<>(n);
        color = new Color[n];
        Arrays.fill(color, Color.WHITE); // mark as not visited
        
        for (int r = 0; r < n; ++r)
            if (color[r] == Color.WHITE)
                dfs(r);
    }
    
    private void dfs(int u) throws CycleDetected
    {
        // Mark current vertex to be in discovery path
        color[u] = Color.GREY;
        
        for (int v: graph.dependencies(u)) {
            switch (color[v]) {
            case BLACK:
                // already discovered: no-op
                break;
            case GREY:
                // detected a cycle with an ancestor in path
                throw new CycleDetected(v);
            case WHITE:
            default:
                // discovered an unvisited node: dive in
                dfs(v);
                break;
            }
        }
        
        // Mark as finished
        color[u] = Color.BLACK;
        
        // Add this vertex into result; all dependencies have preceded
        result.addLast(u);
    }
    
    Iterable<Integer> result()
    {
        return Collections.unmodifiableCollection(result);
    }
}
