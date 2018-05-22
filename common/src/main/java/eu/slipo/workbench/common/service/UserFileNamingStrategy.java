package eu.slipo.workbench.common.service;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;

public interface UserFileNamingStrategy 
{
    /**
     * Get the URI scheme used for representing (as URIs) paths under this hierarchy. 
     * 
     * @return a URI scheme
     */
    String getScheme();
    
    /**
     * Resolve a user's home directory as an absolute path. 
     * 
     * <p>This method will not interact in any way with the underlying filesystem; will
     * simply map a user id to a home directory.  
     * 
     * @param userId
     */
    Path getUserDir(int userId);
    
    /**
     * Resolve a user's home directory as an absolute path. If told so, attempt to create an
     * empty home directory (if it doesn't already exist).
     * 
     * @param userId
     * @param createIfNotExists
     * @throws IOException if an attempt to create the directory fails
     */
    Path getUserDir(int userId, boolean createIfNotExists) throws IOException;
    
    /**
     * Resolve a path against a user's home directory
     * 
     * @param userId
     * @param relativePath A relative path to be resolved
     * @return an absolute path
     */
    Path resolvePath(int userId, String relativePath);
    
    /**
     * Resolve a path against a user's home directory
     * @see UserFileNamingStrategy#resolvePath(int, String)  
     */
    Path resolvePath(int userId, Path relativePath);
    
    /**
     * Convert a relative path (under a user's data directory) to a pseudo-URI.
     * 
     * <p>The result URI will have the scheme returned by {@link UserFileNamingStrategy#getScheme()}.
     * 
     * @param userId
     * @param relativePath
     * @return a URI representing the given location
     */
    URI convertToUri(int userId, Path relativePath);
    
    /**
     * Convert an absolute path to a pseudo-URI. 
     * 
     * <p>The result URI will have the scheme returned by {@link UserFileNamingStrategy#getScheme()}.
     * 
     * @param path A path to be converted
     * @return a URI representing the given location
     * 
     * @throws IllegalArgumentException if given path cannot be represented as a URI (e.g. when
     *   not inside central data directory)
     */
    URI convertToUri(Path path);
    
    /**
     * Resolve a URI to an absolute path
     * 
     * @param uri A URI under the scheme returned by {@link UserFileNamingStrategy#getScheme()}
     * @return an absolute path
     * 
     * @throws IllegalArgumentException if given URI does not represent a user-scoped file (e.g 
     * having an unknown scheme).
     */
    Path resolveUri(URI uri);
}
