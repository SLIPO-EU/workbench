package eu.slipo.workbench.common.config;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

@Configuration
public class CatalogDataConfiguration
{
    private Path tempDir;
    
    private Path catalogDataDir;
    
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
    
    @PostConstruct
    private void initialize() throws IOException
    {
        // Create directory hierarchy, if not already exist
        
        try {
            Files.createDirectories(tempDir);
        } catch (FileAlreadyExistsException ex) {}
        
        try {
            Files.createDirectories(catalogDataDir);
        } catch (FileAlreadyExistsException ex) {}
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
}