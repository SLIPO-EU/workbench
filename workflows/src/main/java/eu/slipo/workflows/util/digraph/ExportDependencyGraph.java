package eu.slipo.workflows.util.digraph;

import java.util.function.IntFunction;

public class ExportDependencyGraph
{
    public static final float NODE_WIDTH = 2.0f;
    
    public static final String NODE_COLOR = "black";
    
    public static final String NODE_FILL_COLOR = "white";
    
    public enum Direction { LR, RL, TB, BT };
    
    public enum Shape 
    {
        RECT("rect"), 
        ELLIPSE("ellipse"), 
        SQUARE("square"), 
        CIRCLE("circle"), 
        POINT("point"),
        TRIANGLE("triangle"),
        INVTRIANGLE("invtriangle"),
        PARALLELOGRAM("parallelogram"),
        NOTE("note");
        
        private final String value;
        
        private Shape(String value)
        {
            this.value = value;
        }
    }

    public enum Style
    {
        FILLED("filled"),
        SOLID("solid"),
        BOLD("bold"),
        DASHED("dashed"),
        DOTTED("dotted"),
        ROUNDED("rounded"),
        STRIPED("striped");
        
        private final String value;
        
        private Style(String value)
        {
            this.value = value;
        }
    }
    
    /**
     * Represent a subset of styling options understood by DOT.
     */
    public static class NodeAttributes
    {                
        private String color = NODE_COLOR;
        
        private String fillColor = NODE_FILL_COLOR;
        
        private Shape shape;
        
        private Style style;
        
        public NodeAttributes()
        {
        }

        public String getColor()
        {
            return color;
        }

        public String getFillColor()
        {
            return fillColor;
        }

        public void setColor(String color)
        {
            this.color = color;
        }

        public void setFillColor(String fillColor)
        {
            this.fillColor = fillColor;
        }

        public Shape getShape()
        {
            return shape;
        }

        public void setShape(Shape shape)
        {
            this.shape = shape;
        }

        public Style getStyle()
        {
            return style;
        }

        public void setStyle(Style style)
        {
            this.style = style;
        }  
        
        @Override
        public String toString()
        {
            StringBuilder stringbuilder = new StringBuilder();
            stringbuilder.append(
                String.format("color=<%s>,fillcolor=<%s>", color, fillColor));
            
            if (shape != null)
                stringbuilder.append(",shape=" + shape.value);
            
            if (style != null)
                stringbuilder.append(",style=" + style.value);
            
            return stringbuilder.toString();
        }
    }
    
    public static NodeAttributes DEFAULT_NODE_ATTRS = new NodeAttributes();
    
    /**
     * Dump a dependency graph using DOT format
     * 
     * @param graph
     * @param nameMapper
     */
    public static String toString(
        DependencyGraph graph, 
        IntFunction<String> nameMapper, 
        IntFunction<NodeAttributes> styleMapper,
        Direction direction)
    {        
        // Export as a DOT graph
        
        final int n = graph.size();
        
        StringBuilder stringbuilder = new StringBuilder();
        
        stringbuilder
            .append("digraph g {")
            .append(String.format(
                "rankdir=%s;", direction == null? Direction.TB : direction.name()))
            .append(String.format(
                "node [" +
                    "style=filled," +
                    "shape=rect," +
                    "color=%s," +
                    "fixedsize=true," +
                    "fontname=arial," +
                    "width=%.1f];", 
                NODE_COLOR, NODE_WIDTH));
        
        for (int u = 0; u < n; ++u) {
            String name = nameMapper.apply(u);
            NodeAttributes style = styleMapper.apply(u);
            stringbuilder.append(
                String.format("%d [label=<%s>,%s];", u, name, style)); 
        }
        
        for (int u = 0; u < n; ++u)
            for (int v: graph.dependencies(u))
                stringbuilder.append(String.format("%d -> %d;", u, v));
        
        stringbuilder.append("}");
        return stringbuilder.toString();
    }
}
