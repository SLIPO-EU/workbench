package eu.slipo.workbench.rpc.tests.integration.service;

import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Properties;

import org.springframework.data.util.Pair;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.google.auto.value.AutoValue;

import eu.slipo.workbench.common.model.resource.DataSource;
import eu.slipo.workbench.common.model.tool.FagiConfiguration;
import eu.slipo.workbench.common.model.tool.LimesConfiguration;
import eu.slipo.workbench.common.service.util.PropertiesConverterService;
import eu.slipo.workbench.common.service.util.PropertiesConverterService.ConversionFailedException;

/**
 * A test fixture that represents a basic fusion operation (using Limes+Fagi).
 */
@AutoValue
abstract class FuseFixture extends BaseFixture
{
    /**
     * A pair of RDF inputs (no transformation takes place)
     */
    abstract Pair<URI, URI> inputPair();

    /**
     * The configuration for interlinking operation
     */
    abstract LimesConfiguration configurationForLinking();

    /**
     * The expected (intermediate) result for interlinking operation
     */
    abstract Path expectedLinkResult();

    /**
     * The configuration for fusion operation
     */
    abstract FagiConfiguration configuration();

    abstract Path expectedRemainingResult();

    public Pair<DataSource, DataSource> inputAsDataSource() throws MalformedURLException
    {
        Pair<URI, URI> p = inputPair();
        return Pair.of(inputAsDataSource(p.getFirst()), inputAsDataSource(p.getSecond()));
    }

    @Override
    void checkState()
    {
        super.checkState();

        for (Path p: Arrays.asList(expectedLinkResult(), expectedResult(), expectedRemainingResult())) {
            Assert.isTrue(p != null && p.isAbsolute() && Files.isReadable(p),
                "An expected result should be given as an absolute file path");
        }

        final Pair<URI, URI> inputPair = inputPair();
        final Path stagingDir = stagingDir();
        for (URI inputUri: Arrays.asList(inputPair.getFirst(), inputPair.getSecond())) {
            Assert.state(stagingDir == null || inputUri.getScheme().equals("file"),
                "If no staging directory given, input is expected as an absolute file URI");
            Assert.state(!inputUri.getScheme().equals("file") || Paths.get(inputUri).startsWith(stagingDir),
                "If input is a local file (a file URI), must be located under staging directory");
        }
    }

    static Builder newBuilder()
    {
        return new AutoValue_FuseFixture.Builder();
    }

    static Builder newBuilder(PropertiesConverterService propertiesConverter)
    {
        Builder b = new AutoValue_FuseFixture.Builder();
        b.setPropertiesConverter(propertiesConverter);
        return b;
    }

    @AutoValue.Builder
    abstract static class Builder extends BaseFixture.BaseBuilder<Builder, FuseFixture>
    {
        abstract Builder inputPair(Pair<URI, URI> p);

        abstract Pair<URI, URI> inputPair();

        abstract Builder configurationForLinking(LimesConfiguration c);

        Builder configurationForLinking(Properties p)
            throws ConversionFailedException
        {
            final LimesConfiguration configuration =
                propertiesConverter.propertiesToValue(p, LimesConfiguration.class);
            return configurationForLinking(configuration);
        }

        abstract Builder expectedLinkResult(Path p);

        abstract Builder configuration(FagiConfiguration c);

        abstract Builder expectedRemainingResult(Path p);

        Builder configuration(Properties p, String rulesSpec)
            throws ConversionFailedException
        {
            Assert.notNull(p, "Expected a map of properties");
            Assert.isTrue(!StringUtils.isEmpty(rulesSpec),
                "Expected a non-empty location for Fagi rules configuration file");
            final FagiConfiguration configuration =
                propertiesConverter.propertiesToValue(p, FagiConfiguration.class);
            configuration.setRulesSpec(rulesSpec);
            return configuration(configuration);
        }
    }
}
