package eu.slipo.workbench.web.controller.action;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.slipo.workbench.web.config.MapConfiguration;

@RestController
@Secured({ "ROLE_USER", "ROLE_ADMIN" })
@RequestMapping(produces = "application/json")
public class ProxyController {

    private static final Logger logger = LoggerFactory.getLogger(ResourceController.class);

    private final String TRANSFER_ENCONDING_HEADER = "Transfer-Encoding";

    private final String TRANSFER_ENCONDING_CHUNKED = "chunked";

    @Autowired
    private MapConfiguration mapConfiguration;

    @RequestMapping(value = "/action/proxy/service/wfs", method = RequestMethod.GET)
    public void geoserverProxy(HttpServletRequest request, HttpServletResponse response) {
        try {
            proxyRequest(request, response, mapConfiguration.getGeoServer().getServices().getWfs());
        } catch (IOException ex) {
            logger.error("Send Response Error:" + ex.getMessage(), ex);
        }
    }

    private void proxyRequest(HttpServletRequest request, HttpServletResponse response, String targetUrl) throws IOException {
        HttpClient httpClient = HttpClients.createDefault();

        try {
            HttpUriRequest proxiedRequest = createHttpUriRequest(request, targetUrl);
            HttpResponse proxiedResponse = httpClient.execute(proxiedRequest);

            writeToResponse(proxiedResponse, response);
        } catch (Exception ex) {
            handleError(ex, response);
        }
    }

    private HttpUriRequest createHttpUriRequest(HttpServletRequest request, String targetUrl) throws URISyntaxException {
        URI uri = new URI(targetUrl);
        Map<String, String[]> parameterMap = request.getParameterMap();

        RequestBuilder rb = RequestBuilder.create(request.getMethod());
        rb.setUri(uri);

        parameterMap.keySet().forEach(key -> {
            if (parameterMap.get(key).length == 1) {
                rb.addParameter(key, parameterMap.get(key)[0]);
            }
        });

        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            rb.addHeader(headerName, headerValue);
        }

        return rb.build();
    }

    private void writeToResponse(HttpResponse proxiedResponse, HttpServletResponse response) throws IOException {
        for (Header header : proxiedResponse.getAllHeaders()) {
            if ((!header.getName().equals(TRANSFER_ENCONDING_HEADER)) || (!header.getValue().equals(TRANSFER_ENCONDING_CHUNKED))) {
                response.addHeader(header.getName(), header.getValue());
            }
        }

        OutputStream output = null;
        InputStream input = null;

        try {
            input = proxiedResponse.getEntity().getContent();
            output = response.getOutputStream();
            IOUtils.copy(input, output);
        } finally {
            IOUtils.closeQuietly(output);
            IOUtils.closeQuietly(input);
        }
    }

    private void handleError(Exception ex, HttpServletResponse response) throws IOException {
        logger.error("Proxy Error:" + ex.getMessage(), ex);

        response.sendError(HttpServletResponse.SC_BAD_GATEWAY);
    }

}
