package eu.slipo.workbench.rpc.tests.integration.service;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;

import eu.slipo.workbench.common.model.resource.DataSource;
import eu.slipo.workbench.common.model.tool.TriplegeoConfiguration;

/**
 * A test fixture that represents a basic to-RDF operation (using Triplegeo).
 */
class TransformFixture extends BaseFixture
{
    final URI input;

    final TriplegeoConfiguration configuration;

    TransformFixture(
        String name, URI input, Path stagingDir, Path expectedResult, TriplegeoConfiguration configuration)
    {
        super(name, stagingDir, expectedResult);
        this.input = input;
        this.configuration = configuration;
    }

    public TriplegeoConfiguration getConfiguration()
    {
        return configuration;
    }

    public DataSource getInputAsDataSource() throws MalformedURLException
    {
        return convertInputAsDataSource(input);
    }

    public URL getInputAsUrl() throws MalformedURLException
    {
        return input.toURL();
    }

    @Override
    public String toString()
    {
        return String.format("TransformFixture [name=%s, input=%s]", name, input);
    }
}
