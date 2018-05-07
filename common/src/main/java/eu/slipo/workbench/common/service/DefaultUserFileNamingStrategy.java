package eu.slipo.workbench.common.service;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import eu.slipo.workbench.common.model.DirectoryInfo;
import eu.slipo.workbench.common.model.FileInfo;

@Service
public class DefaultUserFileNamingStrategy implements UserFileNamingStrategy
{
    @Autowired
    private Path userDataDirectory;

    @Override
    public DirectoryInfo getUserDirectoryInfo(int userId) throws IOException
    {
        final Path userDir = getUserDir(userId, true);
        return createDirectoryInfo("/", userDir, "");
    }

    @Override
    public Path getUserDir(int userId)
    {
        Assert.isTrue(userId > 0, "Expected a valid (> 0) user id");
        return userDataDirectory.resolve(Integer.toString(userId));
    }

    @Override
    public Path getUserDir(int userId, boolean createIfNotExists)
        throws IOException
    {
        Assert.isTrue(userId > 0, "Expected a valid (> 0) user id");
        Path userDir = getUserDir(userId);

        if (createIfNotExists) {
            try {
                Files.createDirectory(userDir);
            } catch (FileAlreadyExistsException ex) {}
        }

        return userDir;
    }

    @Override
    public Path resolvePath(int userId, String relativePath)
    {
        Assert.isTrue(!StringUtils.isEmpty(relativePath), "Expected a non-empty path");
        return resolvePath(userId, Paths.get(relativePath));
    }

    @Override
    public Path resolvePath(int userId, Path relativePath)
    {
        Assert.isTrue(userId > 0, "Expected a valid (> 0) user id");
        Assert.notNull(relativePath, "Expected a non-null path");
        Assert.isTrue(!relativePath.isAbsolute(), "Expected a relative path to be resolved");
        Path userDir = getUserDir(userId);
        return userDir.resolve(relativePath);
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
        Assert.isTrue(path.startsWith(userDataDirectory), "The path is outside user-data directory");
        
        Path userPath = userDataDirectory.relativize(path);
        int userPathLength = userPath.getNameCount();
        Assert.isTrue(userPathLength > 1, "The relative path is too short");
        int userId = Integer.parseInt(userPath.getName(0).toString());
        
        return convertToUri(userId, userPath.subpath(1, userPathLength));
    }
    
    @Override
    public Path resolveUri(URI uri)
    {
        Assert.notNull(uri, "A user-data URI is required");
        Assert.isTrue(uri.getScheme().equals(SCHEME), "The given URI is malformed (unknown scheme)");
        
        int userId = Integer.parseInt(uri.getHost());
        
        String path = uri.getPath();
        Assert.isTrue(path.startsWith("/"), "The URI path was expected as an absolute path");
        
        return resolvePath(userId, Paths.get(path.substring(1)));
    }
    
    private DirectoryInfo createDirectoryInfo(String name, Path path, String relativePath)
    {
        final File file = path.toFile();

        final DirectoryInfo di = new DirectoryInfo(name, relativePath, toZonedDateTime(file.lastModified()));

        for (File f : file.listFiles()) {
            if (f.isDirectory()) {
                di.addFolder(createDirectoryInfo(f.getName(), f.toPath(), relativePath + f.getName() + "/"));
            }
            if (f.isFile()) {
                di.addFile(createFileInfo(f, relativePath));
            }
        }

        return di;
    }

    private FileInfo createFileInfo(File file, String path)
    {
        return new FileInfo(
            file.length(), file.getName(), path + file.getName(), toZonedDateTime(file.lastModified()));
    }

    private ZonedDateTime toZonedDateTime(long millis)
    {
        Instant i = Instant.ofEpochMilli(millis);
        return ZonedDateTime.ofInstant(i, ZoneOffset.UTC);
    }
}
