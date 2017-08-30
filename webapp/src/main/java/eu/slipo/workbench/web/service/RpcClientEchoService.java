package eu.slipo.workbench.web.service;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import eu.slipo.workbench.common.model.ApplicationException;
import eu.slipo.workbench.common.model.BasicErrorCode;
import eu.slipo.workbench.common.model.RestResponse;
import eu.slipo.workbench.common.model.TextMessage;
import eu.slipo.workbench.common.model.Error;

@Component
public class RpcClientEchoService implements EchoService
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
     * Build a POST request ({@link RequestEntity}) around a payload (body).
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
    
    @Override
    public TextMessage echo(String text)
    {        
        final String path = "/api/echo";
        
        TextMessage message = new TextMessage(text);
        
        ParameterizedTypeReference<RestResponse<TextMessage>> responseType = 
            new ParameterizedTypeReference<RestResponse<TextMessage>>() {};
        
        ResponseEntity<RestResponse<TextMessage>> responseEntity = 
            rest.exchange(buildPostEntity(path, message), responseType);   
            
        RestResponse<TextMessage> response = responseEntity.getBody();
        
        // If server reported an error on this response, raise an ApplicationException
        List<Error> errors = response.getErrors();
        if (!errors.isEmpty())
            throw ApplicationException.fromPattern(BasicErrorCode.REST_RESPONSE_WITH_ERRORS, errors);
        
        return response.getResult();
    }

}
