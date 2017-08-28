package eu.slipo.workbench.command.subcommand;

import java.net.URI;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import eu.slipo.workbench.command.SubCommand;

/**
 * A simple client for slipo.eu RPC server
 */
@Component("rpc")
public class RpcCommand implements SubCommand
{
    @Value("${slipo.rpc-server.url:http://localhost:8080}")
    private String rootUrl;
    
    @Autowired
    private RestTemplate rest;
    
    @PostConstruct
    private void normalizeUrls()
    {
        rootUrl = rootUrl.replaceAll("[/]+$", ""); // remove trailing "/"
    }
    
    /**
     * Build a {@link RequestEntity} object around a request payload (body).
     * Set path and basic request headers.
     * 
     * @param path
     * @param body
     */
    private <T> RequestEntity<T> buildRequestEntity(String path, T body)
    {
        return RequestEntity
            .post(URI.create(rootUrl + "/" + path))
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .body(body);
    }
    
    private enum Operation {
        LIST, // List registered job names or instances for a specific job
        START, // Start a job by name, passing a map of parameters
        STOP, // Stop a running job by identified by an  instance-id
        STATUS, // Poll status of a job by either its instance-id.
        CLEAR; // Clear interrupted or aged job executions (marking them as FAILED).
    }
    
    @Override
    public String getSummary()
    {
        return 
            "rpc list [<job-name>]\n" +
            "rpc start <job-name> [PARAMETERS]\n" +
            "rpc status <job-name> --id=<instance-id>\n" +
            "rpc stop <job-name> --id=<instance-id>\n" +
            "rpc clear <job-name> [--age=<num-seconds>]";
    }

    @Override
    public String getDescription()
    {
        return "Interact with slipo RPC service";
    }

    @Override
    public void run(Map<String, String> options)
    {
        run("list", null, options);
    }

    @Override
    public void run(String a1, Map<String, String> options)
    {
        run(a1, null, options); 
    }

    @Override
    public void run(String operationName, String jobName, Map<String, String> options)
    {
        Operation op;
        try {
            op = Operation.valueOf(operationName.toUpperCase());
        } catch (IllegalArgumentException e) {
            op = null;
            System.err.printf(
                "Unknown RPC operation (%s). Try `help rpc`%n", operationName);
        }
        
        // Todo invoke RPC api
    }

}
