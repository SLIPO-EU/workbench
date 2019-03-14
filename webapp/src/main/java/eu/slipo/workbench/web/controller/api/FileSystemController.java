package eu.slipo.workbench.web.controller.api;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import eu.slipo.workbench.common.model.DirectoryInfo;
import eu.slipo.workbench.common.model.Error;
import eu.slipo.workbench.common.model.FileSystemErrorCode;
import eu.slipo.workbench.common.model.RestResponse;

@Secured({ "ROLE_API" })
@RestController("ApiFileSystemController")
@RequestMapping(produces = "application/json")
public class FileSystemController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(FileSystemController.class);

    /**
     * Enumerates all user files and folders
     *
     * @return An instance of {@link DirectoryInfo} for the user root directory
     */
    @GetMapping(value = "/api/v1/file-system")
    public RestResponse<?> browse() {
        try {
            Path userDir = fileNamingStrategy.getUserDir(currentUserId(), true);
            return RestResponse.result(directoryTraverse.getDirectoryInfo(userDir));
        } catch (Exception ex) {
            return this.exceptionToResponse(ex);
        }
    }

    /**
     * Downloads a file
     *
     * @param filePath the path of the file to download
     * @return the requested file
     */
    @RequestMapping(value = "/api/v1/file-system", params = { "path" }, method = RequestMethod.GET)
    public FileSystemResource download(@RequestParam("path") String filePath, HttpServletResponse response) throws IOException {
        try {
            if (StringUtils.isEmpty(filePath)) {
                createErrorResponse(
                    HttpServletResponse.SC_BAD_REQUEST, response, FileSystemErrorCode.PATH_IS_EMPTY, "A path to the file is required"
                );
                return null;
            }

            final int userId = currentUserId();
            final Path userDir = fileNamingStrategy.getUserDir(userId);

            final Path absolutePath = userDir.resolve(filePath);
            final File file = absolutePath.toFile();

            if (!file.exists()) {
                createErrorResponse(
                    HttpServletResponse.SC_NOT_FOUND, response, FileSystemErrorCode.PATH_NOT_FOUND, "File does not exist"
                );
            } else if (file.isDirectory()) {
                createErrorResponse(
                    HttpServletResponse.SC_NOT_FOUND, response, FileSystemErrorCode.PATH_NOT_FOUND, "File path is a directory"
                );
            } else {
                logger.info("User {} ({}) has downloaded file {}",
                    this.currentUserName(),
                    this.currentUserId(),
                    absolutePath.toString());

                response.setHeader("Content-Disposition", String.format("attachment; filename=%s", file.getName()));
                return new FileSystemResource(file);
            }
        } catch (Exception ex) {
            logger.warn("Failed to download file [{}] for user {} ({})",
                filePath,
                this.currentUserName(),
                this.currentUserId());

            createErrorResponse(
                HttpServletResponse.SC_GONE, response, FileSystemErrorCode.PATH_NOT_FOUND, "File has been removed"
            );
        }

        return null;
    }

    private RestResponse<?> exceptionToResponse(Exception ex) {
        logger.error(ex.getMessage(), ex);
        return exceptionToResponse(ex, Error.EnumLevel.ERROR);
    }

}
