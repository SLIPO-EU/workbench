package eu.slipo.workbench.web.controller.action;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.annotation.Secured;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import eu.slipo.workbench.common.model.BasicErrorCode;
import eu.slipo.workbench.common.model.DirectoryInfo;
import eu.slipo.workbench.common.model.FileSystemErrorCode;
import eu.slipo.workbench.common.model.RestResponse;
import eu.slipo.workbench.common.model.process.InvalidProcessDefinitionException;
import eu.slipo.workbench.web.model.FileSystemPathRequest;
import eu.slipo.workbench.web.model.UploadRequest;


@RestController
@Secured({ "ROLE_USER", "ROLE_AUTHOR", "ROLE_ADMIN" })
@RequestMapping(produces = "application/json")
public class FileSytemController extends BaseController {

    private long maxUserSpace;

    @Value("${slipo.user.max-space:20971520}")
    public void setDefaultLocale(String maxUserSpace) {
        this.maxUserSpace = this.parseSize(maxUserSpace);
    }

    /**
     * List (recursively) files and folders for user's directory
     *
     * @return information on all files and folders
     */
    @RequestMapping(value = "/action/file-system",  method = RequestMethod.GET)
    public RestResponse<?> browseDirectory() {
        try {
            Path userDir = fileNamingStrategy.getUserDir(currentUserId(), true);
            return RestResponse.result(directoryTraverse.getDirectoryInfo(userDir));
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
    public RestResponse<?> createFolder(@RequestBody FileSystemPathRequest request) {
        try {
            if (StringUtils.isEmpty(request.getPath())) {
                return RestResponse.error(FileSystemErrorCode.PATH_IS_EMPTY, "A path is required");
            }

            final int userId = currentUserId();
            final Path userDir = fileNamingStrategy.getUserDir(userId);

            final Path dir = userDir.resolve(request.getPath());
            if (Files.exists(dir)) {
                return RestResponse.error(
                    FileSystemErrorCode.PATH_ALREADY_EXISTS,
                    String.format("The directory already exists: %s", request.getPath()));
            }

            Files.createDirectories(dir);

            return RestResponse.result(directoryTraverse.getDirectoryInfo(userDir));
        } catch (IOException ex) {
            return RestResponse.error(BasicErrorCode.IO_ERROR, "An unknown error has occurred");
        }
    }

    /**
     * Deletes a file or an empty folder
     *
     * @param request a request with file or folder to delete
     * @return the updated file system
     */
    @RequestMapping(value = "/action/file-system", params = { "path" }, method = RequestMethod.DELETE)
    public RestResponse<?> deletePath(@RequestParam("path") String relativePath) {
        try {
            if (StringUtils.isEmpty(relativePath)) {
                return RestResponse.error(FileSystemErrorCode.PATH_IS_EMPTY, "A path is required");
            }

            final int userId = currentUserId();
            final Path userDir = fileNamingStrategy.getUserDir(userId);

            final Path absolutePath = userDir.resolve(relativePath);
            final File file = absolutePath.toFile();

            if (!file.exists()) {
                return RestResponse.error(FileSystemErrorCode.PATH_NOT_FOUND, "Path does not exist");
            }
            if ((file.isDirectory()) && (file.listFiles().length != 0)) {
                return RestResponse.error(FileSystemErrorCode.PATH_NOT_EMPTY, "Path is not empty");
            }
            Files.delete(absolutePath);

            return RestResponse.result(directoryTraverse.getDirectoryInfo(userDir));
        } catch (IOException ex) {
            return RestResponse.error(BasicErrorCode.IO_ERROR, "Failed to delete path");
        } catch (Exception ex) {
            return RestResponse.error(BasicErrorCode.UNKNOWN, "An unknown error has occurred");
        }
    }

    /**
     * Uploads a file to the given path
     *
     * @param file uploaded resource file
     * @param request request with the file name
     * @throws InvalidProcessDefinitionException
     */
    @RequestMapping(value = "/action/file-system/upload", method = RequestMethod.POST)
    public RestResponse<?> upload(@RequestPart("file") MultipartFile file, @RequestPart("data") UploadRequest request) {

        try {
            final int userId = currentUserId();
            final Path userDir = fileNamingStrategy.getUserDir(userId);

            final DirectoryInfo userDirInfo = directoryTraverse.getDirectoryInfo(userDir);

            final long size = userDirInfo.getSize();
            if (size + file.getSize() > maxUserSpace) {
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

            if (Files.exists(absolutePath)) {
                return RestResponse.error(FileSystemErrorCode.PATH_ALREADY_EXISTS, "File with the same name already exists");
            }

            InputStream in = new ByteArrayInputStream(file.getBytes());
            Files.copy(in, absolutePath, StandardCopyOption.REPLACE_EXISTING);

            return RestResponse.result(directoryTraverse.getDirectoryInfo(userDir));
        } catch (IOException ex) {
            return RestResponse.error(BasicErrorCode.IO_ERROR, "Failed to create file");
        } catch (Exception ex) {
            return RestResponse.error(BasicErrorCode.UNKNOWN, "An unknown error has occurred");
        }
    }

    private long parseSize(String size) {
        Assert.hasLength(size, "Size must not be empty");

        size = size.toUpperCase(Locale.ENGLISH);
        if (size.endsWith("KB")) {
            return Long.valueOf(size.substring(0, size.length() - 2)) * 1024;
        }
        if (size.endsWith("MB")) {
            return Long.valueOf(size.substring(0, size.length() - 2)) * 1024 * 1024;
        }
        if (size.endsWith("GB")) {
            return Long.valueOf(size.substring(0, size.length() - 2)) * 1024 * 1024 * 1024;
        }
        return Long.valueOf(size);
    }

}
