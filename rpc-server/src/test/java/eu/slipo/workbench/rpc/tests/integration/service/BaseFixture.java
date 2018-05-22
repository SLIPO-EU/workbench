package eu.slipo.workbench.rpc.tests.integration.service;

import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

import eu.slipo.workbench.common.model.resource.DataSource;
import eu.slipo.workbench.common.model.resource.FileSystemDataSource;
import eu.slipo.workbench.common.model.resource.UrlDataSource;

class BaseFixture
{
    final String name;

    /**
     * The application user directory (where input sources are staged)
     */
    final Path stagingDir;

    /**
     * The absolute path for the expected result of the transformation applied on given input
     */
    final Path expectedResult;

    BaseFixture(String name, Path stagingDir, Path expectedResult)
    {
        this.name = name;
        this.stagingDir = stagingDir;
        this.expectedResult = expectedResult;
    }

    public String getName()
    {
        return name;
    }

    public Path getExpectedResultPath()
    {
        return expectedResult;
    }

    DataSource convertInputAsDataSource(URI inputUri) throws MalformedURLException
    {
        final String scheme = inputUri.getScheme();
        if (scheme.equals("file")) {
            return new FileSystemDataSource(stagingDir.relativize(Paths.get(inputUri)));
        } else if (scheme.equals("http") || scheme.equals("https") || scheme.equals("ftp")) {
            return new UrlDataSource(inputUri.toURL());
        } else {
             throw new IllegalStateException(
                 "Did not expect input with scheme of  [" + scheme + "]");
        }
    }

    Path convertInputToAbsolutePath(URI inputUri)
    {
        return inputUri.getScheme().equals("file")? Paths.get(inputUri) : null;
    }
}
