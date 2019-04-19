package eu.slipo.workbench.common.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.slipo.workbench.common.model.DirectoryInfo;
import eu.slipo.workbench.common.model.FileInfo;

@Service
public class DefaultDirectoryTraverse implements DirectoryTraverse
{
    private static final int MAX_DEPTH = 8;

    @Override
    public DirectoryInfo getDirectoryInfo(Path rootDir) throws IOException
    {
        return getDirectoryInfo(rootDir, MAX_DEPTH);
    }

    @Override
    public DirectoryInfo getDirectoryInfo(Path rootDir, int maxDepth) throws IOException
    {
        Assert.notNull(rootDir, "A path is required");
        Assert.isTrue(rootDir.isAbsolute(), "The directory is expected as an absolute path");
        Assert.isTrue(Files.isDirectory(rootDir), "The given path is not a directory");
        Assert.isTrue(maxDepth > 0, "The maximum depth must be a positive number");
        return createDirectoryInfo("/", rootDir, Paths.get(""), maxDepth, new ArrayList<String>());
    }


    @Override
    public DirectoryInfo getDirectoryInfo(Path rootDir, List<String> exclude) throws IOException {
        Assert.notNull(rootDir, "A path is required");
        Assert.isTrue(rootDir.isAbsolute(), "The directory is expected as an absolute path");
        Assert.isTrue(Files.isDirectory(rootDir), "The given path is not a directory");
        Assert.notNull(exclude, "A exclude is required");
        return createDirectoryInfo("/", rootDir, Paths.get(""), MAX_DEPTH, exclude);
    }

    private DirectoryInfo createDirectoryInfo(String name, Path dir, Path relativePath, int depth, List<String> exclude)
    {
        if(exclude.contains(name)) {
            return null;
        }

        final File dirAsFile = dir.toFile();
        final DirectoryInfo di = new DirectoryInfo(name, relativePath.toString(), dirAsFile.lastModified());

        for (File entry: dirAsFile.listFiles()) {
            final Path relativeEntryPath = relativePath.resolve(entry.getName());
            if ((entry.isDirectory()) && (!exclude.contains(entry.getName()))) {
                if (depth > 0) {
                    // Descend
                    di.addDirectory(
                        createDirectoryInfo(entry.getName(), entry.toPath(), relativeEntryPath, depth - 1, exclude));
                } else {
                    // No more recursion is allowed: simply report a directory entry
                    di.addDirectory(
                        new DirectoryInfo(entry.getName(), entry.getPath(), entry.lastModified()));
                }
            } else if (entry.isFile()) {
                di.addFile(
                    new FileInfo(
                        entry.getName(), relativeEntryPath.toString(), entry.length(), entry.lastModified()));
            }
        }

        return di;
    }
}
