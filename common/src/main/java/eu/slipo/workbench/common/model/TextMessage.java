package eu.slipo.workbench.common.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TextMessage implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final String text;
    
    private final String comment;
    
    private final Integer id;
    
    @JsonCreator
    public TextMessage(
        @JsonProperty("id") Integer id, 
        @JsonProperty("text") String text, 
        @JsonProperty("comment") String comment)
    {
        this.id = id;
        this.text = text;
        this.comment = comment;
    }

    public TextMessage(String text)
    {
        this(null, text, null);
    }

    @JsonProperty("text")
    public String text()
    {
        return text;
    }
    
    @JsonProperty("comment")
    public String comment()
    {
        return comment;
    }
    
    @JsonProperty("id")
    public Integer id()
    {
        return id;
    }

    @Override
    public String toString()
    {
        return String.format(
            "TextMessage [text=%s, comment=%s, id=%s]", text, comment, id);
    }
}
