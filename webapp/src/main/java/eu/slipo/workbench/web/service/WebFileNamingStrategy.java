package eu.slipo.workbench.web.service;

import java.nio.file.Path;

import eu.slipo.workbench.common.service.FileNamingStrategy;

public interface WebFileNamingStrategy extends FileNamingStrategy {

    Path resolveExecutionPath(String relativePath);

}
