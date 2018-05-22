package eu.slipo.workbench.rpc.tests.integration.service;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;

import org.springframework.data.util.Pair;

import eu.slipo.workbench.common.model.resource.DataSource;
import eu.slipo.workbench.common.model.tool.FagiConfiguration;
import eu.slipo.workbench.common.model.tool.LimesConfiguration;

/**
 * A test fixture that represents a basic fusion operation (using Limes+Fagi).
 */
class FuseFixture extends BaseFixture
{
    /**
     * A pair of RDF inputs (no transformation takes place)
     */
    final Pair<URI, URI> inputPair;

    /**
     * The configuration for interlinking operation
     */
    final LimesConfiguration linkConfiguration;

    /**
     * The expected (intermediate) result for interlinking operation
     */
    final Path expectedLinkResult;

    /**
     * The configuration for fusion operation
     */
    final FagiConfiguration configuration;

    FuseFixture(
        String name,
        Path stagingDir,
        Pair<URI, URI> inputPair,
        LimesConfiguration linkConfiguration,
        Path expectedLinkResult,
        Path expectedResult, FagiConfiguration configuration)
    {
        super(name, stagingDir, expectedResult);
        this.inputPair = inputPair;
        this.linkConfiguration = linkConfiguration;
        this.expectedLinkResult = expectedLinkResult;
        this.configuration = configuration;
    }

    public FagiConfiguration getConfiguration()
    {
        return configuration;
    }

    public LimesConfiguration getLinkConfiguration()
    {
        return linkConfiguration;
    }

    public Pair<DataSource, DataSource> getInputAsDataSource() throws MalformedURLException
    {
        return Pair.of(
            convertInputAsDataSource(inputPair.getFirst()),
            convertInputAsDataSource(inputPair.getSecond()));
    }

    public Pair<URL, URL> getInputAsUrl() throws MalformedURLException
    {
        return Pair.of(inputPair.getFirst().toURL(), inputPair.getSecond().toURL());
    }

    @Override
    public String toString()
    {
        return String.format("FuseFixture [name=%s, input=%s]", name, inputPair);
    }
}
