package eu.slipo.workbench.command.subcommand;

import java.net.URI;
import java.text.DateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import eu.slipo.workbench.common.model.jobs.JobExecutionInfo;
import eu.slipo.workbench.common.model.jobs.JobInstanceInfo;
import eu.slipo.workbench.common.model.ErrorCode;
import eu.slipo.workbench.common.model.ApplicationException;
import eu.slipo.workbench.common.model.BasicErrorCode;
import eu.slipo.workbench.common.model.Error;
import eu.slipo.workbench.common.model.RestResponse;
import eu.slipo.workbench.command.SubCommand;

/**
 * A client that interacts with jobs running on the RPC server.
 */
@Component("job")
public class JobCommand implements SubCommand
{
    private static Logger logger = LoggerFactory.getLogger(JobCommand.class);
    
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
     * Build a {@link RequestEntity} for a POST request around a payload (body).
     * Set path and basic request headers.
     * 
     * @param path
     * @param body
     */
    private <T> RequestEntity<T> buildPostEntity(String path, T body)
    {
        return RequestEntity
            .post(URI.create(rootUrl + "/" + path))
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .body(body);
    }
    
    /**
     * Build a {@link RequestEntity} for a GET request.
     * Set path and basic request headers.
     * 
     * @param path
     * @param body
     */
    private RequestEntity<Void> buildGetEntity(String path)
    {
        return RequestEntity
            .get(URI.create(rootUrl + "/" + path))
            .accept(MediaType.APPLICATION_JSON)
            .build();
    }
    
    /**
     * The set of supported job-related operations.   
     */
    private enum Operation {
        UNKNOWN,
        
        LIST,    // List registered job names or instances for a specific job
        START,   // Start a job by name, passing a map of parameters
        STOP,    // Stop a running job by identified by an  instance-id
        STATUS,  // Poll status of a job (by instance-id)
        CLEAR;   // Clear interrupted job executions (marking them as abandoned).
    }
    
    @Override
    public String getSummary()
    {
        return 
            "job list [<job-name> [<instance-id>]]\n" +
            "job start <job-name> [PARAMETERS]\n" +
            "job status <job-name> [<instance-id>]\n" +
            "job stop <job-name> <instance-id>\n" +
            "job clear <job-name> <instance-id>";
    }

    @Override
    public String getDescription()
    {
        return "Interact with jobs on slipo.eu RPC service";
    }

    @Override
    public void run(Map<String, String> options)
    {
        run("list", null, options);
    }

    @Override
    public void run(String operationName, Map<String, String> options)
    {
        run(operationName, null, null, options); 
    }

    @Override
    public void run(String operationName, String jobName, Map<String, String> options)
    {
        run(operationName, jobName, null, options);
    }
    
    @Override
    public void run(
        String operationName, String jobName, String jobId, Map<String, String> options)
    {      
        Operation op;
        try {
            op = Operation.valueOf(operationName.toUpperCase());
        } catch (IllegalArgumentException e) {
            op = Operation.UNKNOWN;
        }
        
        switch (op) {
        case LIST:
            if (jobName == null) {
                // List job names
                System.out.printf("%s%n", "job-name");
                for (String s: getJobNames())
                    System.out.printf("%s%n", s);
            } else if (jobId == null) {
                // List instances for given job
                System.out.printf("%-12.10s%-10.8s%n", "job-name", "job-id");
                for (JobInstanceInfo r: getJobInstances(jobName)) {
                    System.out.printf("%-12.10s%-10.8s%n", jobName, r.getId().toString());
                }
            } else {
                // List executions for given job instance (identified by jobId)
                System.out.printf("%-12.10s%-10.8s%-10.8s%-24.24s%-24.24s%n", 
                    "job-name", "job-id", "job-xid", "started", "finished");
                for (JobExecutionInfo r: getJobExecutions(jobName, Long.valueOf(jobId))) {
                    System.out.printf("%-12.10s%-10.8s%-10.8s%-24.22s%-24.22s%n",
                        jobName, r.getId(), r.getExecutionId(), 
                        r.getStarted(), r.getFinished());
                }
            }
            break;
        case START:
            // Todo START job
            break;
        case STOP:
            // Todo STOP job running execution of instance
            break;
        case STATUS:
            // Todo STATUS of job instance
            break;
        case CLEAR:
            // Todo CLEAR
            break;
        case UNKNOWN:
        default:
            System.err.printf(
                "Unknown operation (%s). Try `help job`%n", operationName);
            break;
        }
    }

    /**
     * Check the given REST response for errors.
     * 
     * @throws ApplicationException if response reports errors
     */
    private <R> void checkErrors(RestResponse<R> response)
    {
        List<Error> errors = response.getErrors();
        if (!errors.isEmpty()) {
            logger.error("Received response with errors: {}", errors);
            throw ApplicationException.fromPattern(BasicErrorCode.REST_RESPONSE_WITH_ERRORS, errors);
        }
    }
    
    /**
     * Carry out the given HTTP REST request with the expected response-type.
     * 
     * @throws ApplicationException if client fails to perform request
     */
    private <B, R> ResponseEntity<RestResponse<R>> fetch(
        RequestEntity<B> requestEntity, ParameterizedTypeReference<RestResponse<R>> responseType)
    {        
        ResponseEntity<RestResponse<R>> responseEntity = null;
        
        try {
            responseEntity = rest.exchange(requestEntity, responseType);   
        } catch (RestClientException e) {
            logger.error("Cannot exchange via REST: {}", e.getMessage());
            throw ApplicationException.fromPattern(
                e, 
                BasicErrorCode.REST_CLIENT_EXCEPTION, 
                Collections.singletonList(requestEntity));
        }
        
        return responseEntity;
    }
    
    private Collection<String> getJobNames()
    {
        final String path = "/api/jobs";
        
        ParameterizedTypeReference<RestResponse<Collection<String>>> responseType = 
            new ParameterizedTypeReference<RestResponse<Collection<String>>>() {};
        RequestEntity<Void> requestEntity = buildGetEntity(path);
        ResponseEntity<RestResponse<Collection<String>>> responseEntity = 
            fetch(requestEntity, responseType);
               
        RestResponse<Collection<String>> response = responseEntity.getBody();
        checkErrors(response);
        
        return response.getResult();
    }
    
    private List<JobInstanceInfo> getJobInstances(String jobName)
    {
        final String path = String.format("/api/jobs/%s/instances", jobName);
        
        ParameterizedTypeReference<RestResponse<List<JobInstanceInfo>>> responseType = 
            new ParameterizedTypeReference<RestResponse<List<JobInstanceInfo>>>() {};
        RequestEntity<Void> requestEntity = buildGetEntity(path);
        ResponseEntity<RestResponse<List<JobInstanceInfo>>> responseEntity = 
            fetch(requestEntity, responseType);
        
        RestResponse<List<JobInstanceInfo>> response = responseEntity.getBody();
        checkErrors(response);
               
        return response.getResult();
    }
    
    private List<JobExecutionInfo> getJobExecutions(String jobName, long jobId)
    {
        final String path = String.format("/api/jobs/%s/executions/%d", jobName, jobId);
        
        ParameterizedTypeReference<RestResponse<List<JobExecutionInfo>>> responseType = 
            new ParameterizedTypeReference<RestResponse<List<JobExecutionInfo>>>() {};
        RequestEntity<Void> requestEntity = buildGetEntity(path);
        ResponseEntity<RestResponse<List<JobExecutionInfo>>> responseEntity = 
            fetch(requestEntity, responseType);
        
        RestResponse<List<JobExecutionInfo>> response = responseEntity.getBody();
        checkErrors(response);
               
        return response.getResult(); 
    }
}
