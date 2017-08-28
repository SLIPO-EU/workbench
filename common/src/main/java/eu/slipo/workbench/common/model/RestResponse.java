package eu.slipo.workbench.common.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RestResponse<Result>
{
    private final List<Error> errors = new ArrayList<Error>();

    private final Result result;
    
    protected RestResponse() 
    {
        this.result = null;
    }
    
    protected RestResponse(Result r)
    {
        this.result = r;
    }
    
    protected RestResponse(Result r, List<Error> errors) 
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
    public Result getResult()
    {
        return result;
    }
    
    public static <R> RestResponse<R> result(R r)
    {
        return new RestResponse<>(r);
    }
    
    public static <R> RestResponse<R> error(ErrorCode code, String description)
    {
        return RestResponse.<R>error(new Error(code, description));
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
