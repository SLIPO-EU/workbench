package eu.slipo.workbench.web.controller.action;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.slipo.workbench.common.model.BasicErrorCode;
import eu.slipo.workbench.common.model.FileSystemErrorCode;
import eu.slipo.workbench.common.model.RestResponse;
import eu.slipo.workbench.common.service.FileNamingStrategy;
import eu.slipo.workbench.web.model.CreateDirectoryRequest;
import eu.slipo.workbench.web.service.IAuthenticationFacade;


@RestController
@Secured({ "ROLE_USER", "ROLE_ADMIN" })
@RequestMapping(produces = "application/json")
public class FileSytemController {

    @Autowired
    private IAuthenticationFacade authenticationFacade;

    @Autowired
    private FileNamingStrategy fileNamingStrategy;

    /**
     * Enumerates files and folders for the specified path
     *
     * @param authentication the authenticated principal
     * @param path the path to search
     * @return all files and folders
     */
    @RequestMapping(value = "/action/file-system",  method = RequestMethod.GET)
    public RestResponse<?> browserDirectory() {
        try {
            return RestResponse.result(fileNamingStrategy.getUserFileSystem(authenticationFacade.getCurrentUserId()));
        } catch (IOException ex) {
            return RestResponse.error(BasicErrorCode.IO_ERROR, "An unknown error has occurred");
        }
    }

    /**
     * Creates a new folder
     *
     * @param request a request with the new folder's name
     * @return the updated file system
     */
    @RequestMapping(value = "/action/file-system", method = RequestMethod.POST)
    public RestResponse<?> createFolder(@RequestBody CreateDirectoryRequest request) {
        try {
            if (StringUtils.isEmpty(request.getPath())) {
                return RestResponse.error(FileSystemErrorCode.PATH_IS_EMPTY, "Path is not set");
            }
            final String filename = fileNamingStrategy.resolveFileName(authenticationFacade.getCurrentUserId(), request.getPath(), false);
            if (StringUtils.isAllBlank(filename)) {
                return RestResponse.error(
                    FileSystemErrorCode.CANNOT_RESOLVE_PATH,
                    String.format("Cannot resolve path [%s]", request.getPath()));
            }
            final Path path = Paths.get(filename);
            if (Files.exists(path)) {
                return RestResponse.error(
                    FileSystemErrorCode.PATH_ALREADY_EXISTS,
                    String.format("Path [%s] already exists", request.getPath()));
            }
            Files.createDirectories(path);

            return RestResponse.success();
        } catch (IOException ex) {
            return RestResponse.error(BasicErrorCode.IO_ERROR, "An unknown error has occurred");
        }
    }

}
