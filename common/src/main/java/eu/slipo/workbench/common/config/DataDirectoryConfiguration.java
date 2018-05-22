package eu.slipo.workbench.common.config;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

@Configuration
public class DataDirectoryConfiguration
{
    private Path tempDir;
    
    private Path catalogDataDir;
    
    private Path userDataDir;
    
    @Autowired
    private void setTempDir(@Value("${slipo.temp-dir}") String d)
    {
        Path path = Paths.get(d);
        Assert.isTrue(path.isAbsolute(), "Expected an absolute directory path");
        this.tempDir = path;
    }
    
    @Autowired
    private void setCatalogDataDir(@Value("${slipo.catalog.data-dir}") String d)
    {
        Path path = Paths.get(d);
        Assert.isTrue(path.isAbsolute(), "Expected an absolute directory path");
        this.catalogDataDir = path;
    }
    
    @Autowired
    private void setUserDataDir(@Value("${slipo.users.data-dir}") String d)
    {
        Path path = Paths.get(d);
        Assert.isTrue(path.isAbsolute(), "Expected an absolute directory path");
        this.userDataDir = path;
    }
    
    @PostConstruct
    private void initialize() throws IOException
    {
        // Prepare directory hierarchy
        
        for (Path dataDir: Arrays.asList(tempDir, catalogDataDir, userDataDir)) {
            try {
                Files.createDirectories(dataDir);
            } catch (FileAlreadyExistsException ex) {
                // no-op: already exists
            }
        }
    }
    
    @Bean
    Path tempDataDirectory()
    {
        return tempDir;
    }
    
    @Bean
    Path catalogDataDirectory()
    {
        return catalogDataDir;
    }
    
    @Bean
    Path userDataDirectory()
    {
        return userDataDir;
    }
    
}