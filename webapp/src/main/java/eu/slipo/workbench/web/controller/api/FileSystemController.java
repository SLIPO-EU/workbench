package eu.slipo.workbench.web.controller.api;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.core.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import eu.slipo.workbench.common.model.BasicErrorCode;
import eu.slipo.workbench.common.model.DirectoryInfo;
import eu.slipo.workbench.common.model.Error;
import eu.slipo.workbench.common.model.FileSystemErrorCode;
import eu.slipo.workbench.common.model.RestResponse;
import eu.slipo.workbench.common.model.process.InvalidProcessDefinitionException;
import eu.slipo.workbench.web.model.UploadRequest;

@Secured({ "ROLE_API" })
@RestController("ApiFileSystemController")
@RequestMapping(produces = "application/json")
public class FileSystemController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(FileSystemController.class);

    private long maxUserSpace;

    private static int PATH_MAX_DEPTH = 5;

    @Value("${slipo.user.max-space:20971520}")
    public void setMaxUserSpace(String maxUserSpace) {
        this.maxUserSpace = this.parseSize(maxUserSpace);
    }

    @Value("${slipo-toolkit.triplegeo.ml-mappings-folder:triplegeo-ml-mappings}")
    private String tripleGeoMappingFolder;

    /**
     * Enumerates all user files and folders
     *
     * @return An instance of {@link DirectoryInfo} for the user root directory
     */
    @GetMapping(value = "/api/v1/file-system")
    public RestResponse<?> browse() {
        try {
            Path userDir = fileNamingStrategy.getUserDir(currentUserId(), true);
            return RestResponse.result(directoryTraverse.getDirectoryInfo(userDir, Arrays.asList(tripleGeoMappingFolder)));
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
    @GetMapping(value = "/api/v1/file-system", params = { "path" })
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

    /**
     * Uploads a file to the given path
     *
     * @param remoteFile uploaded resource file
     * @param request request with the file name
     * @throws InvalidProcessDefinitionException
     */
    @PostMapping(value = "/api/v1/file-system/upload")
    public RestResponse<?> upload(@RequestPart("file") MultipartFile remoteFile, @RequestPart("data") UploadRequest request) {

        try {
            final int userId = currentUserId();
            final Path userDir = fileNamingStrategy.getUserDir(userId);

            final DirectoryInfo userDirInfo = directoryTraverse.getDirectoryInfo(userDir);

            final long size = userDirInfo.getSize();
            if (size + remoteFile.getSize() > maxUserSpace) {
                return RestResponse.error(FileSystemErrorCode.NOT_ENOUGH_SPACE, "Insufficient storage space");
            }

            if (StringUtils.isEmpty(request.getPath())) {
                request.setPath("");
            }
            if (StringUtils.isEmpty(request.getFilename())) {
                return RestResponse.error(FileSystemErrorCode.PATH_IS_EMPTY, "File name is not set");
            }

            final Path relativePath = Paths.get(request.getPath(), request.getFilename());
            final Path absolutePath = fileNamingStrategy.resolvePath(userId, relativePath);

            // Validate file
            File localFile = absolutePath.toFile();

            if (relativePath.getNameCount() > PATH_MAX_DEPTH) {
                return RestResponse.error(FileSystemErrorCode.PATH_MAX_DEPTH, "File name exceeds max path nesting");
            }
            if(localFile.isDirectory()) {
                return RestResponse.error(FileSystemErrorCode.PATH_IS_DIRECTORY, "File is a directory");
            }
            if ((localFile.exists()) && (!request.isOverwrite())) {
                return RestResponse.error(FileSystemErrorCode.PATH_ALREADY_EXISTS, "File with the same name already exists");
            }

            // Create folder if not already exists
            String resolvedPath = FilenameUtils.getFullPath(absolutePath.toString());
            File localPath = new File(resolvedPath);
            FileUtils.mkdir(localPath, true);

            InputStream in = new ByteArrayInputStream(remoteFile.getBytes());
            Files.copy(in, absolutePath, StandardCopyOption.REPLACE_EXISTING);

            return RestResponse.result(directoryTraverse.getDirectoryInfo(userDir));
        } catch (IOException ex) {
            return RestResponse.error(BasicErrorCode.IO_ERROR, "Failed to create file");
        } catch (Exception ex) {
            return RestResponse.error(BasicErrorCode.UNKNOWN, "An unknown error has occurred");
        }
    }

    private RestResponse<?> exceptionToResponse(Exception ex) {
        logger.error(ex.getMessage(), ex);
        return exceptionToResponse(ex, Error.EnumLevel.ERROR);
    }

}
