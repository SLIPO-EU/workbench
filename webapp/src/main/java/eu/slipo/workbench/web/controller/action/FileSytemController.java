package eu.slipo.workbench.web.controller.action;

import java.time.ZonedDateTime;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import eu.slipo.workbench.common.model.RestResponse;
import eu.slipo.workbench.web.model.DirectoryInfo;
import eu.slipo.workbench.web.model.FileInfo;

@RestController
public class FileSytemController {

    /**
     * Enumerates files and folders for the specified path
     *
     * @param authentication  the authenticated principal
     * @param path the path to search
     * @return all files and folders in the specified path
     */
    @RequestMapping(value = "/action/file-system", params = { "path" }, method = RequestMethod.GET, produces = "application/json")
    public RestResponse<DirectoryInfo> browserDirectory(Authentication authentication, @RequestParam(value = "path", required = false) String path) {
        if (StringUtils.isBlank(path)) {
            path = "/";
        } else if (!path.endsWith("/")) {
            path += "/";
        }
        String[] tokens = StringUtils.split(path.trim(), '/');

        return RestResponse.result(this.createFolder((tokens.length == 0 ? "" : tokens[tokens.length - 1]), path));
    }

    private DirectoryInfo createFolder(String name, String path) {
        DirectoryInfo di = new DirectoryInfo(name, path, ZonedDateTime.now());

        // Add sub folders
        for (int i = 0; i < 10; i++) {
            String subFolderName = String.format("Folder %d", i + 1);
            di.addFolder(new DirectoryInfo(subFolderName, path + subFolderName + "/", ZonedDateTime.now()));
        }
        // Add files
        for (int i = 0; i < 10; i++) {
            String fileName = String.format("File %d", i + 1);
            di.addFile(this.createFile(fileName, path + fileName));
        }

        return di;
    }

    private FileInfo createFile(String name, String path) {
        return new FileInfo((int) (Math.random() * 1024 * 1024) + 100, name, path, ZonedDateTime.now());
    }

}
