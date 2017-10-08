package eu.slipo.workbench.command.subcommand;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
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

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.slipo.workbench.common.model.ApplicationException;
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
    
    private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    
    @Value("${slipo.rpc-server.url:http://localhost:8080}")
    private String rootUrl;
    
    @Autowired
    private RestTemplate rest;
    
    @PostConstruct
    private void normalizeUrls()
    {
        rootUrl = rootUrl.replaceAll("[/]+$", ""); // remove trailing "/"
    }
    
    private interface PrettyPrinter
    {
        void printNames(List<String> names, PrintStream out);
        
        void printInstanceInfo(String jobName, JobInstanceInfo info, PrintStream out);
        
        void printInstanceInfo(String jobName, List<JobInstanceInfo> info, PrintStream out);
        
        void printExecutionInfo(String jobName, JobExecutionInfo info, PrintStream out);
        
        void printExecutionInfo(String jobName, List<JobExecutionInfo> info, PrintStream out);
    }
    
    /**
     * Print info in a tabular CSV-like format
     */
    private static class DefaultPrinter implements PrettyPrinter
    {
        @Override
        public void printNames(List<String> names, PrintStream out)
        {
            out.printf("%s%n", "job-name");
            for (String s: names)
                out.printf("%s%n", s);
        }

        @Override
        public void printInstanceInfo(String jobName, JobInstanceInfo info, PrintStream out)
        {
            out.printf("job-name: %s%n", jobName);
            out.printf("id: %d%n", info.getId());
        }

        @Override
        public void printInstanceInfo(String jobName, List<JobInstanceInfo> instancesInfo, PrintStream out)
        {
            out.printf("%-12.10s%-10.8s%n", "job-name", "id");
            for (JobInstanceInfo r: instancesInfo) {
                out.printf("%-12.10s%-10.8s%n", jobName, r.getId());
            }
        }

        @Override
        public void printExecutionInfo(String jobName, JobExecutionInfo info, PrintStream out)
        {
            String startedDatestamp = (info.getStarted() != null)?
                dateFormat.format(info.getStarted()) : "";
            String finishedDatestamp = (info.getFinished() != null)?
                dateFormat.format(info.getFinished()) : "";
            
            out.printf("job-name: %s%n", jobName);
            out.printf("id: %d%n", info.getId());
            out.printf("execution-id: %d%n", info.getExecutionId());
            out.printf("started: %s%n", startedDatestamp);
            out.printf("finished: %s%n", finishedDatestamp);
            out.printf("status: %s%n", info.getStatus());
            out.printf("exit-status: %s%n", info.getExitStatus());
            out.printf("exit-description: %s%n", info.getExitDescription());
        }

        @Override
        public void printExecutionInfo(String jobName, List<JobExecutionInfo> executionsInfo, PrintStream out)
        {
            out.printf(
                "%-12.12s %-7.7s %-12.12s %-24.24s %-24.24s %-12.12s %-14.14s %s%n", 
                "job-name", 
                "id", 
                "execution-id", 
                "started", 
                "finished", 
                "status", 
                "exit-status",
                "exit-description");
            
            for (JobExecutionInfo r: executionsInfo) {
                String startedDatestamp = (r.getStarted() != null)?
                    dateFormat.format(r.getStarted()) : "";
                String finishedDatestamp = (r.getFinished() != null)?
                    dateFormat.format(r.getFinished()) : "";
                String statusText = r.getStatus();
                String exitStatusText = r.getExitStatus();
                String exitDescription = r.getExitDescription();
                exitDescription = exitDescription.split("\\R")[0]; // only 1st line
                out.printf(
                    "%-12.12s %-7.7s %-12.12s %-24.24s %-24.24s %-12.12s %-14.14s %s%n",
                    jobName, 
                    r.getId(), 
                    r.getExecutionId(), 
                    startedDatestamp, 
                    finishedDatestamp,
                    statusText, 
                    exitStatusText, 
                    exitDescription);
            }
        }
    }
    
    /**
     * Print info in JSON format
     */
    private static class JsonPrinter implements PrettyPrinter
    {
        private static ObjectMapper jsonMapper = new ObjectMapper();
        
        private <R> void print(R object, PrintStream out)
        {
            try {
                jsonMapper.writeValue(out, object);
            } catch (IOException e) {
                e.printStackTrace();
                throw ApplicationException.fromMessage(e, "Failed to write JSON output");
            }
        }
        
        @Override
        public void printNames(List<String> names, PrintStream out)
        {
            print(names, out);
        }

        @Override
        public void printInstanceInfo(String jobName, JobInstanceInfo info, PrintStream out)
        {
            print(info, out);
        }

        @Override
        public void printInstanceInfo(String jobName, List<JobInstanceInfo> info, PrintStream out)
        {
            print(info, out);
        }

        @Override
        public void printExecutionInfo(String jobName, JobExecutionInfo info, PrintStream out)
        {
            print(info, out);
        }

        @Override
        public void printExecutionInfo(String jobName, List<JobExecutionInfo> info, PrintStream out)
        {
            print(info, out);
        }
    }
    
    private static DefaultPrinter defaultPrinter = new DefaultPrinter();
    
    private static JsonPrinter jsonPrinter = new JsonPrinter();
    
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
     * Build a {@link RequestEntity} for a POST request with an empty payload (body).
     * Set path and basic request headers.
     * 
     * @param path
     * @param body
     */
    private RequestEntity<Void> buildPostEntity(String path)
    {
        return RequestEntity
            .post(URI.create(rootUrl + "/" + path))
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .build();
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
        
        LIST,    // List registered job names
        START,   // Start a job by name, passing a map of parameters
        STOP,    // Stop a running job identified by an  instance-id
        STATUS,  // Poll status of a job (by instance-id)
        CLEAR;   // Clear interrupted job executions (marking them as abandoned).
    }
    
    @Override
    public String getSummary()
    {
        return 
            "job list [--json]\n" +
                "\tList registered job names\n" +
            "job start <job-name> [--json] [[--parameter=NAME=VALUE]*]\n" +
                "\tStart a job by name passing a map of parameters\n" +
            "job status <job-name> [<instance-id>] [--json]\n" +
                "\tPoll status of a job (by instance-id)\n" +
            "job stop <job-name> <instance-id> [--json]\n" +
                "\tStop a running job (by instance-id)\n" +
            "job clear <job-name> <instance-id> [--json]\n" +
                "\tClear interrupted job executions marking them as abandoned\n";
    }

    @Override
    public String getDescription()
    {
        return "Interact with jobs on slipo.eu RPC service";
    }

    @Override
    public void run(Map<String, List<String>> options)
    {
        run("list", null, options);
    }

    @Override
    public void run(String operationName, Map<String, List<String>> options)
    {
        run(operationName, null, null, options); 
    }

    @Override
    public void run(String operationName, String jobName, Map<String, List<String>> options)
    {
        run(operationName, jobName, null, options);
    }
    
    @Override
    public void run(
        String operationName, String jobName, String jobId, Map<String, List<String>> options)
    {      
        Operation op;
        try {
            op = Operation.valueOf(operationName.toUpperCase());
        } catch (IllegalArgumentException e) {
            op = Operation.UNKNOWN;
        }
        
        boolean printAsJson = options.containsKey("json");
        PrettyPrinter printer = printAsJson? jsonPrinter : defaultPrinter;
        
        switch (op) {
        case LIST:
            // List job names
            printer.printNames(getJobNames(), System.out);
            break;
        case START:
            // Start job
            {
                // Collect parameters passed as name=value pairs from command-line options
                Map<String, Object> parametersMap = new LinkedHashMap<>();
                for (String p: options.getOrDefault("parameter", Collections.emptyList())) {
                    if (p.isEmpty())
                        continue;
                    int i = p.indexOf('=');
                    String key = i < 0? p : p.substring(0, i);
                    if (key.isEmpty()) {
                        logger.warn("Found a parameter with empty name ({}): skipping", p);
                        continue;
                    }
                    String value = i < 0? null : p.substring(i + 1);
                    parametersMap.put(key,
                        (value == null || value.isEmpty())? Boolean.TRUE : value);
                }
                logger.info("About to submit job {} with parameters: {}", jobName, parametersMap);
                // Start job
                JobExecutionInfo info = startJob(jobName, parametersMap);
                printer.printExecutionInfo(jobName, info, System.out);
            }
            break;
        case STOP:
            // Stop the running execution of a job instance
            {
                if (jobId == null) {
                    throw new IllegalArgumentException("The instance id must be specified");
                } else {
                    JobExecutionInfo info = stopJob(jobName, Long.valueOf(jobId));
                    printer.printExecutionInfo(jobName, info, System.out);
                }
            }
            break;
        case STATUS:
            {
                if (jobId == null) {
                    // List instances for given job
                    List<JobInstanceInfo> r = getJobInstances(jobName);
                    printer.printInstanceInfo(jobName, r, System.out);
                } else {
                    // List executions for given job instance (identified by jobId)
                    List<JobExecutionInfo> r = getJobExecutions(jobName, Long.valueOf(jobId));
                    printer.printExecutionInfo(jobName, r, System.out);
                }
            }
            break;
        case CLEAR:
            // Clear running execution of job instance
            {
                if (jobId == null) {
                    throw new IllegalArgumentException("The instance id must be specified");
                } else {
                    JobExecutionInfo info = clearJob(jobName, Long.valueOf(jobId));
                    printer.printExecutionInfo(jobName, info, System.out);
                }
            }
            break;
        case UNKNOWN:
        default:
            throw new IllegalArgumentException("Unknown operation: " + operationName); 
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
            logger.error("Received response with ({}) errors: -", errors.size());
            for (Error error: errors) {
                logger.error("  * {}", error);
            }
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
    
    private List<String> getJobNames()
    {
        final String path = "/api/jobs";
        
        ParameterizedTypeReference<RestResponse<List<String>>> responseType = 
            new ParameterizedTypeReference<RestResponse<List<String>>>() {};
        RequestEntity<Void> requestEntity = buildGetEntity(path);
        ResponseEntity<RestResponse<List<String>>> responseEntity = 
            fetch(requestEntity, responseType);
               
        RestResponse<List<String>> response = responseEntity.getBody();
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
    
    private JobExecutionInfo startJob(String jobName, Map<String, Object> parametersMap)
    {
        final String path = String.format("/api/jobs/%s/submit", jobName);
        
        ParameterizedTypeReference<RestResponse<JobExecutionInfo>> responseType = 
            new ParameterizedTypeReference<RestResponse<JobExecutionInfo>>() {};
        RequestEntity<Map<String,Object>> requestEntity = 
            buildPostEntity(path, parametersMap);
        ResponseEntity<RestResponse<JobExecutionInfo>> responseEntity = 
            fetch(requestEntity, responseType); 
        
        RestResponse<JobExecutionInfo> response = responseEntity.getBody();
        checkErrors(response);
        
        return response.getResult();
    }
    
    private JobExecutionInfo stopJob(String jobName, long jobId)
    {
        final String path = String.format("/api/jobs/%s/stop/%d", jobName, jobId);
        
        ParameterizedTypeReference<RestResponse<JobExecutionInfo>> responseType = 
            new ParameterizedTypeReference<RestResponse<JobExecutionInfo>>() {};
        RequestEntity<Void> requestEntity = buildPostEntity(path);
        ResponseEntity<RestResponse<JobExecutionInfo>> responseEntity = 
            fetch(requestEntity, responseType); 
        
        RestResponse<JobExecutionInfo> response = responseEntity.getBody();
        checkErrors(response);
        
        return response.getResult();
    }
    
    private JobExecutionInfo clearJob(String jobName, Long jobId)
    {
        final String path = String.format("/api/jobs/%s/clear-running-execution/%d", jobName, jobId);
        
        ParameterizedTypeReference<RestResponse<JobExecutionInfo>> responseType = 
            new ParameterizedTypeReference<RestResponse<JobExecutionInfo>>() {};
        RequestEntity<Void> requestEntity = buildPostEntity(path);
        ResponseEntity<RestResponse<JobExecutionInfo>> responseEntity = 
            fetch(requestEntity, responseType); 
        
        RestResponse<JobExecutionInfo> response = responseEntity.getBody();
        checkErrors(response);
        
        return response.getResult();
    }
}
