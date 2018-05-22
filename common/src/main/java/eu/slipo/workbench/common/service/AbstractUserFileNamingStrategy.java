package eu.slipo.workbench.common.service;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public abstract class AbstractUserFileNamingStrategy implements UserFileNamingStrategy
{
    @Override
    public Path getUserDir(int userId, boolean createIfNotExists)
        throws IOException
    {
        Assert.isTrue(userId > 0, "Expected a valid (> 0) user id");
        Path userDir = getUserDir(userId);

        if (createIfNotExists && !Files.exists(userDir)) {
            try {
                Files.createDirectory(userDir);
            } catch (FileAlreadyExistsException ex) {
                // no-op: another thread may have created this entry
            }
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
}
