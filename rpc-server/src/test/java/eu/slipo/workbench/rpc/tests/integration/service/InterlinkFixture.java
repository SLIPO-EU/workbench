package eu.slipo.workbench.rpc.tests.integration.service;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;

import org.springframework.data.util.Pair;

import eu.slipo.workbench.common.model.resource.DataSource;
import eu.slipo.workbench.common.model.tool.LimesConfiguration;
import eu.slipo.workbench.common.model.tool.TriplegeoConfiguration;

/**
 * A test fixture that represents a transformation+interlinking operation (using Triplegeo+Limes).
 */
class InterlinkFixture extends BaseFixture
{
    /**
     * A pair of tabular inputs (to be transformed to RDF)
     */
    final Pair<URI, URI> inputPair;

    /**
     * A pair of transform configurations (to transform respective input)
     */
    final Pair<TriplegeoConfiguration, TriplegeoConfiguration> transformConfigurationPair;

    /**
     * A pair of expected transformed results
     */
    final Pair<Path, Path> expectedTransformedPair;

    /**
     * The configuration for interlinking operation
     */
    final LimesConfiguration configuration;

    InterlinkFixture(
        String name,
        Path stagingDir,
        Pair<URI, URI> inputPair,
        Pair<TriplegeoConfiguration, TriplegeoConfiguration> transformConfigurationPair,
        Pair<Path, Path> expectedTransformedPair,
        Path expectedResult, LimesConfiguration configuration)
    {
        super(name, stagingDir, expectedResult);
        this.inputPair = inputPair;
        this.transformConfigurationPair = transformConfigurationPair;
        this.expectedTransformedPair = expectedTransformedPair;
        this.configuration = configuration;
    }

    public LimesConfiguration getConfiguration()
    {
        return configuration;
    }

    public Pair<TriplegeoConfiguration, TriplegeoConfiguration> getTransformConfiguration()
    {
        return transformConfigurationPair;
    }

    public Pair<DataSource, DataSource> getInputAsDataSource() throws MalformedURLException
    {
        return Pair.of(
            convertInputAsDataSource(inputPair.getFirst()),
            convertInputAsDataSource(inputPair.getSecond()));
    }

    public Pair<TransformFixture, TransformFixture> getTransformFixtures()
    {
        TransformFixture first = new TransformFixture(
            name + ".tr-1",
            inputPair.getFirst(),
            stagingDir,
            expectedTransformedPair.getFirst(),
            transformConfigurationPair.getFirst());

        TransformFixture second = new TransformFixture(
            name + ".tr-2",
            inputPair.getSecond(),
            stagingDir,
            expectedTransformedPair.getSecond(),
            transformConfigurationPair.getSecond());

        return Pair.of(first, second);
    }

    public Pair<URL, URL> getInputAsUrl() throws MalformedURLException
    {
        return Pair.of(inputPair.getFirst().toURL(), inputPair.getSecond().toURL());
    }

    @Override
    public String toString()
    {
        return String.format("InterlinkFixture [name=%s, input=%s]", name, inputPair);
    }
}
