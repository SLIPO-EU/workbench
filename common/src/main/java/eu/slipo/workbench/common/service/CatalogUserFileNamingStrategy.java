package eu.slipo.workbench.common.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class CatalogUserFileNamingStrategy extends AbstractUserFileNamingStrategy
{
    public static final String SCHEME = "catalog-data";
    
    @Autowired
    private Path catalogDataDirectory;
    
    @Override
    public String getScheme()
    {
        return SCHEME;
    }

    @Override
    public Path getUserDir(int userId)
    {
        Assert.isTrue(userId > 0, "Expected a valid (> 0) user id");
        return catalogDataDirectory.resolve(Integer.toString(userId));
    }

    @Override
    public URI convertToUri(int userId, Path relativePath)
    {
        Assert.isTrue(userId > 0, "Expected a valid (>0) user id");
        Assert.notNull(relativePath, "A path is required");
        Assert.isTrue(!relativePath.isAbsolute(), "Expected a relative path");
        
        URI uri = null; 
        try {
            uri = new URI(SCHEME, String.valueOf(userId), "/" + relativePath.toString(), null);
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException(ex);
        }
        return uri;
    }

    @Override
    public URI convertToUri(Path path)
    {
        Assert.notNull(path, "A path is required");
        Assert.isTrue(path.isAbsolute(), "Expected an absolute path");
        Assert.isTrue(path.startsWith(catalogDataDirectory), "The path is outside catalog-data directory");
        
        Path userPath = catalogDataDirectory.relativize(path);
        int userPathLength = userPath.getNameCount();
        Assert.isTrue(userPathLength > 1, "The relative path is too short");
        int userId = Integer.parseInt(userPath.getName(0).toString());
        
        return convertToUri(userId, userPath.subpath(1, userPathLength));
    }

    @Override
    public Path resolveUri(URI uri)
    {
        Assert.notNull(uri, "A catalog-data URI is required");
        Assert.isTrue(SCHEME.equals(uri.getScheme()), "The URI has an unexpected scheme");
        
        int userId = Integer.parseInt(uri.getHost());
        String path = uri.getPath();
        Assert.isTrue(path.startsWith("/"), "The URI path was expected as an absolute path");
        
        return resolvePath(userId, Paths.get(path.substring(1)));
    }
}
