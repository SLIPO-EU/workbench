package eu.slipo.workbench.common.service;

import java.io.IOException;

import eu.slipo.workbench.common.model.DirectoryInfo;

public interface FileNamingStrategy {

    DirectoryInfo getUserFileSystem(int userId) throws IOException;

    String resolveFileName(int userId, String relativePath) throws IOException;

    String resolveFileName(int userId, String relativePath, boolean throwIfNotExists) throws IOException;

}
