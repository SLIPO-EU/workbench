package eu.slipo.workbench.web.service;

import java.nio.file.Path;

import eu.slipo.workbench.common.service.UserFileNamingStrategy;

public interface WebFileNamingStrategy extends UserFileNamingStrategy {

    Path resolveExecutionPath(String relativePath);

}
