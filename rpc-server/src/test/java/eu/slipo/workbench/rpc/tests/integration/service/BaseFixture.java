package eu.slipo.workbench.rpc.tests.integration.service;

import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.annotation.Nullable;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import eu.slipo.workbench.common.model.resource.DataSource;
import eu.slipo.workbench.common.model.resource.FileSystemDataSource;
import eu.slipo.workbench.common.model.resource.UrlDataSource;
import eu.slipo.workbench.common.service.ProcessOperator;
import eu.slipo.workbench.common.service.util.PropertiesConverterService;

/**
 * A basic fixture for testing {@link ProcessOperator}
 */
abstract class BaseFixture
{
    /**
     * A name for this test fixture
     */
    abstract String name();

    /**
     * The application user directory (where input sources are staged)
     */
    abstract @Nullable Path stagingDir();

    /**
     * The absolute path for the expected result of the transformation applied on given input
     */
    abstract Path expectedResult();

    DataSource inputAsDataSource(URI inputUri) throws MalformedURLException
    {
        final String scheme = inputUri.getScheme();
        final Path stagingDir = stagingDir();

        if (scheme.equals("file")) {
            return new FileSystemDataSource(stagingDir.relativize(Paths.get(inputUri)));
        } else if (scheme.equals("http") || scheme.equals("https") || scheme.equals("ftp")) {
            return new UrlDataSource(inputUri.toURL());
        } else {
             throw new IllegalStateException(
                 "Did not expect input with scheme of  [" + scheme + "]");
        }
    }

    Path inputAsAbsolutePath(URI inputUri)
    {
        return inputUri.getScheme().equals("file")? Paths.get(inputUri) : null;
    }

    void checkState()
    {
        final String name = name();
        Assert.state(!StringUtils.isEmpty(name), "A non-empty name is required");

        final Path path = expectedResult();
        Assert.isTrue(path.isAbsolute() && Files.isReadable(path),
            "The expected result should be given as an absolute file path");

        final Path stagingDir = stagingDir();
        Assert.state(stagingDir == null ||
                (stagingDir.isAbsolute() && Files.isDirectory(stagingDir) && Files.isReadable(stagingDir)),
            "The staging directory (if given) should be an absolute path for an existing readable directory");
    }

    /**
     * A base builder for fixtures
     *
     * @param <B> A builder which is a subclass this one
     * @param <F> The fixture type built
     */
    abstract static class BaseBuilder <B extends BaseBuilder<B, ?>, F extends BaseFixture>
    {
        protected PropertiesConverterService propertiesConverter;

        void setPropertiesConverter(PropertiesConverterService propertiesConverter)
        {
            this.propertiesConverter = propertiesConverter;
        }

        // Define basic getters/setters

        abstract B name(String name);

        abstract String name();

        abstract B stagingDir(Path dir);

        abstract Path stagingDir();

        abstract B expectedResult(Path path);

        abstract Path expectedResult();

        abstract F autoBuild();

        F build()
        {
            F f = autoBuild();

            f.checkState();
            return f;
        }
    }
}
