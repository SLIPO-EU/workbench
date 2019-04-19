package eu.slipo.workbench.common.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import eu.slipo.workbench.common.model.DirectoryInfo;

public interface DirectoryTraverse
{
    /**
     * @see {@link DirectoryTraverse#getDirectoryInfo(Path, int)}
     */
    DirectoryInfo getDirectoryInfo(Path rootDir) throws IOException;

    /**
     * Traverse directory entries (recursively) and collect detailed information on
     * file-system entries (files and nested directories).
     *
     * @param rootDir The root directory of this traversal
     * @param maxDepth A maximum depth to descend
     * @throws IOException
     */
    DirectoryInfo getDirectoryInfo(Path rootDir, int maxDepth) throws IOException;

    /**
     * Traverse directory entries (recursively) and collect detailed information on
     * file-system entries (files and nested directories).
     *
     * @param rootDir The root directory of this traversal
     * @param exclude List of folder names to exclude
     * @throws IOException
     */
    DirectoryInfo getDirectoryInfo(Path rootDir, List<String> exclude) throws IOException;

}
