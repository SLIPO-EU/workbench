package eu.slipo.workbench.web.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import eu.slipo.workbench.common.service.DefaultUserFileNamingStrategy;
import eu.slipo.workbench.common.service.UserFileNamingStrategy;

@Service("defaultWebFileNamingStrategry")
public class DefaultWebFileNamingStrategry extends DefaultUserFileNamingStrategy implements WebFileNamingStrategy {

    @Value("#{T(java.nio.file.Paths).get('${slipo.rpc-server.workflows.data-dir}')}")
    private Path workflowDataDir;

    @Autowired
    @Qualifier("catalogUserFileNamingStrategy")
    private UserFileNamingStrategy catalogUserFileNamingStrategy;

    @Autowired
    @Qualifier("defaultUserFileNamingStrategy")
    private UserFileNamingStrategy defaultUserFileNamingStrategy;

    @Override
    public Path resolveExecutionPath(String relativePath) throws URISyntaxException {
        if (relativePath.startsWith(defaultUserFileNamingStrategy.getScheme())) {
            return defaultUserFileNamingStrategy.resolveUri(new URI(relativePath));
        }
        if (relativePath.startsWith(catalogUserFileNamingStrategy.getScheme())) {
            return catalogUserFileNamingStrategy.resolveUri(new URI(relativePath));
        }
        return workflowDataDir.resolve(relativePath);
    }

}
