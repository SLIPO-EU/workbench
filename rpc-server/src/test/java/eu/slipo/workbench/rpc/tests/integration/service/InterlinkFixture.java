package eu.slipo.workbench.rpc.tests.integration.service;

import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;

import org.springframework.data.util.Pair;
import org.springframework.util.Assert;

import com.google.auto.value.AutoValue;

import eu.slipo.workbench.common.model.resource.DataSource;
import eu.slipo.workbench.common.model.tool.LimesConfiguration;
import eu.slipo.workbench.common.model.tool.TriplegeoConfiguration;
import eu.slipo.workbench.common.service.util.PropertiesConverterService;
import eu.slipo.workbench.common.service.util.PropertiesConverterService.ConversionFailedException;

/**
 * A test fixture that represents a transformation+interlinking operation (using Triplegeo+Limes).
 */
@AutoValue
abstract class InterlinkFixture extends BaseFixture
{
    /**
     * A pair of tabular inputs (to be transformed to RDF)
     */
    abstract Pair<URI, URI> inputPair();

    /**
     * A pair of transform configurations (to transform respective input)
     */
    abstract Pair<TriplegeoConfiguration, TriplegeoConfiguration> configurationForTransformation();

    /**
     * A pair of expected transformed results
     */
    abstract Pair<Path, Path> expectedTransformedResults();

    /**
     * The configuration for interlinking operation
     */
    abstract LimesConfiguration configuration();

    public Pair<DataSource, DataSource> inputAsDataSource() throws MalformedURLException
    {
        Pair<URI, URI> p = inputPair();
        return Pair.of(inputAsDataSource(p.getFirst()), inputAsDataSource(p.getSecond()));
    }

    public Pair<TransformFixture, TransformFixture> getTransformFixtures()
    {
        final String name = name();
        final Pair<URI, URI> inputPair = inputPair();
        final Path stagingDir = stagingDir();
        final Pair<Path, Path> expectedTransformedResults =
            expectedTransformedResults();
        final Pair<TriplegeoConfiguration, TriplegeoConfiguration> configurationForTransformation =
            configurationForTransformation();

        TransformFixture first = TransformFixture.newBuilder()
            .name(name + ".tr-1")
            .inputUri(inputPair.getFirst())
            .stagingDir(stagingDir)
            .expectedResult(expectedTransformedResults.getFirst())
            .configuration(configurationForTransformation.getFirst())
            .build();

        TransformFixture second = TransformFixture.newBuilder()
            .name(name + ".tr-2")
            .inputUri(inputPair.getSecond())
            .stagingDir(stagingDir)
            .expectedResult(expectedTransformedResults.getSecond())
            .configuration(configurationForTransformation.getSecond())
            .build();

        return Pair.of(first, second);
    }

    @Override
    void checkState()
    {
        super.checkState();

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
        return new AutoValue_InterlinkFixture.Builder();
    }

    static Builder newBuilder(PropertiesConverterService propertiesConverter)
    {
        Builder b = new AutoValue_InterlinkFixture.Builder();
        b.setPropertiesConverter(propertiesConverter);
        return b;
    }

    @AutoValue.Builder
    abstract static class Builder extends BaseFixture.BaseBuilder<Builder, InterlinkFixture>
    {
        abstract Builder inputPair(Pair<URI, URI> p);

        abstract Builder configurationForTransformation(Pair<TriplegeoConfiguration, TriplegeoConfiguration> p);

        abstract Builder expectedTransformedResults(Pair<Path, Path> p);

        abstract Builder configuration(LimesConfiguration configuration);

        Builder configurationForTransformation(
            Properties p1, Properties p2, String mappingSpec, String classificationSpec)
            throws ConversionFailedException
        {
            final TriplegeoConfiguration config1 =
                propertiesConverter.propertiesToValue(p1, TriplegeoConfiguration.class);
            config1.setMappingSpec(mappingSpec);
            config1.setClassificationSpec(classificationSpec);

            final TriplegeoConfiguration config2 =
                propertiesConverter.propertiesToValue(p2, TriplegeoConfiguration.class);
            config2.setMappingSpec(mappingSpec);
            config2.setClassificationSpec(classificationSpec);

            return configurationForTransformation(Pair.of(config1, config2));
        }

        Builder configurationForTransformation(
            Properties p1, Properties p2, Optional<String> mappingSpec, Optional<String> classificationSpec)
            throws ConversionFailedException
        {
            return configurationForTransformation(p1, p2, mappingSpec.orElse(null), classificationSpec.orElse(null));
        }

        Builder configuration(Properties p) throws ConversionFailedException
        {
            final LimesConfiguration configuration =
                propertiesConverter.propertiesToValue(p, LimesConfiguration.class);
            return configuration(configuration);
        }
    }
}
