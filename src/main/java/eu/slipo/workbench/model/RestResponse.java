package eu.slipo.workbench.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RestResponse<R>
{
    private final List<Error> errors = new ArrayList<Error>();

    private final R result;
    
    protected RestResponse() 
    {
        this.result = null;
    }
    
    protected RestResponse(R r)
    {
        this.result = r;
    }
    
    protected RestResponse(R r, List<Error> errors) 
    {
        this.result = r;
        this.errors.addAll(errors);
    }

    public boolean hasErrors() 
    {        
        return errors.size() > 0;
    }

    @JsonProperty("errors")
    public List<Error> getErrors() 
    {
        return Collections.unmodifiableList(errors);
    }
    
    @JsonProperty("result")
    public R getResult()
    {
        return result;
    }
    
    public static <R> RestResponse<R> result(R r)
    {
        return new RestResponse<>(r);
    }
    
    public static <R> RestResponse<R> error(Error e)
    {
        return new RestResponse<R>(null, Collections.singletonList(e));
    }
    
    public static <R> RestResponse<R> error(List<Error> errors)
    {
        return new RestResponse<R>(null, errors);
    }
}
