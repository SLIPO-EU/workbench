package eu.slipo.workbench.common.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.slipo.workbench.common.model.DirectoryInfo;
import eu.slipo.workbench.common.model.FileInfo;

@Service
public class DefaultFileNamingStrategy implements FileNamingStrategy {

    @Autowired
    private Path catalogDataDirectory;

    @Override
    public DirectoryInfo getUserFileSystem(int userId) throws IOException {
        final Path userPath = Paths.get(catalogDataDirectory.toString(), Integer.toString(userId));

        try {
            Files.createDirectories(userPath);
        } catch (FileAlreadyExistsException ex) {
        }

        return createFolder("/", userPath, "/");

    }

    @Override
    public String resolveFileName(int userId, String relativePath) throws IOException {
        return resolveFileName(userId, relativePath, true);
    }

    @Override
    public String resolveFileName(int userId, String relativePath, boolean throwIfNotExists) throws IOException {
        final Path path = Paths.get(catalogDataDirectory.toString(), Integer.toString(userId), relativePath);

        if (!Files.exists(path)) {
            throw new IOException(String.format("File [%s] does not exist", path.toString()));
        }

        return path.toString();

    }

    private DirectoryInfo createFolder(String name, Path path, String relativePath) {
        final File file = path.toFile();

        final DirectoryInfo di = new DirectoryInfo(name, relativePath, toZonedDateTime(file.lastModified()));

        for (File f : file.listFiles()) {
            if (f.isDirectory()) {
                di.addFolder(createFolder(f.getName(), f.toPath(), relativePath + f.getName() + "/"));
            }
            if (f.isFile()) {
                di.addFile(createFile(f, relativePath));
            }
        }

        return di;
    }

    private FileInfo createFile(File file, String path) {
        return new FileInfo(file.length(), file.getName(), path + file.getName(), toZonedDateTime(file.lastModified()));
    }

    private ZonedDateTime toZonedDateTime(long millis) {
        Instant i = Instant.ofEpochMilli(millis);
        return ZonedDateTime.ofInstant(i, ZoneOffset.UTC);
    }

}
