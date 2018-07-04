package eu.slipo.workbench.web.controller.action;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.slipo.workbench.web.config.MapConfiguration;

@RestController
@Secured({ "ROLE_USER", "ROLE_AUTHOR", "ROLE_ADMIN" })
@RequestMapping(produces = "application/json")
public class ProxyController implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(ResourceController.class);

    private final String TRANSFER_ENCONDING_HEADER = "Transfer-Encoding";

    private final String TRANSFER_ENCONDING_CHUNKED = "chunked";

    private final String PARAMETER_BBOX = "bbox";

    private final String PARAMETER_TYPE_NAMES = "typeNames";

    private final String PARAMETER_TYPE_NAME = "typeName";

    private final String CONTENT_TYPE_HEADER = "Content-Type";

    @Value("${vector-data.default.geometry-column:the_geom}")
    private String defaultGeometryColumn;

    private final Map<String, String> tableColumns = new HashMap<String, String>();

    @Autowired
    private MapConfiguration mapConfiguration;

    @Autowired
    private DataSource dataSource;

    private JdbcTemplate jdbcTemplate;

    @Override
    public void afterPropertiesSet() throws Exception {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @RequestMapping(value = "/action/proxy/service/wfs", method = RequestMethod.GET)
    public void geoserverProxy(HttpServletRequest request, HttpServletResponse response) {
        try {
            if(mapConfiguration.getGeoServer().isEnabled()) {
                proxyRequest(request, response, mapConfiguration.getGeoServer().getServices().getWfs());
            } else {
                loadFeatures(request, response);
            }
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

    private void loadFeatures(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Get type name and bounding box
        Map<String, String[]> parameterMap = request.getParameterMap();

        String boundingBox = parameterMap.keySet().stream()
            .filter(p -> p.equalsIgnoreCase(PARAMETER_BBOX))
            .map(p -> parameterMap.get(p)[0])
            .findFirst()
            .orElse(null);

        String typeName = parameterMap.keySet().stream()
            .filter(p -> p.equalsIgnoreCase(PARAMETER_TYPE_NAMES) || p.equalsIgnoreCase(PARAMETER_TYPE_NAME))
            .map(p -> parameterMap.get(p)[0])
            .findFirst()
            .orElse(null);

        if ((boundingBox != null) && (typeName != null)) {
            String[] typeNameParts = StringUtils.split(typeName, ":");
            String tableName = (typeNameParts.length == 2) ? typeNameParts[1] : typeName;

            String[] boundingBoxParts = StringUtils.split(boundingBox, ",");
            if (boundingBoxParts.length == 5) {
                boundingBox = String.join(",", ArrayUtils.remove(boundingBoxParts, 4));
            }

            // Get table schema
            String columns = getColumns(tableName);

            // Get data as GeoJson
            String dataQuery =
                "select row_to_json(fc) " +
                "from   ( " +
                "    select 'FeatureCollection' As type, array_to_json(array_agg(f)) As features " +
                "    from   (" +
                "               select 'Feature' As type, " +
                "                      ST_AsGeoJSON(dt.the_geom)::json As geometry," +
                "                      row_to_json((select columns FROM (SELECT %1$s) As columns)) As properties " +
                "               from   spatial.\"%2$s\" As dt" +
                "               where   ST_Intersects(ST_Transform(ST_MakeEnvelope(%3$s, 3857), 4326), the_geom) = true " +
                "    ) As f " +
                ")  As fc";

            dataQuery = String.format(dataQuery, columns, tableName, boundingBox);

            String output = jdbcTemplate.queryForObject(dataQuery, String.class);

            response.addHeader(CONTENT_TYPE_HEADER,"application/json; charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(output);
        }

        response.setStatus(HttpServletResponse.SC_OK);
    }

    private String getColumns(String tableName) {
        if (tableColumns.containsKey(tableName)) {
            return tableColumns.get(tableName);
        }
        synchronized (tableColumns) {
            if (tableColumns.containsKey(tableName)) {
                return tableColumns.get(tableName);
            }

            String columnQuery = String.format(
                "select column_name from information_schema.columns where table_name='%s'", tableName);

            List<Map<String, Object>> rows = jdbcTemplate.queryForList(columnQuery);
            List<String> columns = rows.stream()
                .map(r -> (String) r.get("column_name"))
                .filter(c -> !c.equalsIgnoreCase(defaultGeometryColumn))
                .collect(Collectors.toList());
            String result =  String.join(",", columns);

            tableColumns.put(tableName, result);
            return result;

        }
    }

    private void handleError(Exception ex, HttpServletResponse response) throws IOException {
        logger.error("Proxy Error:" + ex.getMessage(), ex);

        response.sendError(HttpServletResponse.SC_BAD_GATEWAY);
    }

}
