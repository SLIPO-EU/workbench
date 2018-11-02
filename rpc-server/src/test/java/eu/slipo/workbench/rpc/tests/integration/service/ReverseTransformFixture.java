package eu.slipo.workbench.rpc.tests.integration.service;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import org.springframework.util.Assert;

import com.google.auto.value.AutoValue;
import com.google.common.collect.Lists;

import eu.slipo.workbench.common.model.tool.ReverseTriplegeoConfiguration;
import eu.slipo.workbench.common.service.util.PropertiesConverterService;
import eu.slipo.workbench.common.service.util.PropertiesConverterService.ConversionFailedException;

/**
 * A test fixture that represents a reverse transformation (from RDF) operation
 * (using Triplegeo).
 */
@AutoValue
abstract class ReverseTransformFixture extends BaseFixture
{
    abstract List<URI> inputList();

    abstract ReverseTriplegeoConfiguration configuration();

    @Override
    void checkState()
    {
        super.checkState();

        List<URI> input = inputList();
        Assert.state(!input.isEmpty(), "Expected a non-empty list of inputs");
        Path stagingDir = stagingDir();

        for (URI inputUri: inputList()) {
            Assert.state(stagingDir == null || inputUri.getScheme().equals("file"),
                "If no staging directory given, input is expected as an absolute file URI");
            Assert.state(!inputUri.getScheme().equals("file") || Paths.get(inputUri).startsWith(stagingDir),
                "If input is a local file (a file URI), must be located under staging directory");
        }
    }

    static Builder newBuilder()
    {
        return new AutoValue_ReverseTransformFixture.Builder();
    }

    static Builder newBuilder(PropertiesConverterService propertiesConverter)
    {
        Builder b = new AutoValue_ReverseTransformFixture.Builder();
        b.setPropertiesConverter(propertiesConverter);
        return b;
    }

    @AutoValue.Builder
    static abstract class Builder extends BaseFixture.BaseBuilder<Builder, ReverseTransformFixture>
    {
        abstract Builder inputList(List<URI> input);

        Builder inputList(Iterable<URI> input)
        {
            return inputList(Lists.newArrayList(input));
        }

        Builder inputList(URI uri1)
        {
            return inputList(Collections.singletonList(uri1));
        }

        Builder inputList(URI uri1, URI uri2)
        {
            return inputList(Arrays.asList(uri1, uri2));
        }

        abstract Builder configuration(ReverseTriplegeoConfiguration configuration);

        Builder configuration(Properties opts, Optional<String> sparqlLocation)
            throws ConversionFailedException
        {
            Assert.state(propertiesConverter != null, "A properties converter is required");
            ReverseTriplegeoConfiguration configuration =
                propertiesConverter.propertiesToValue(opts, ReverseTriplegeoConfiguration.class);
            configuration.setSparqlFile(sparqlLocation.orElse(null));
            return configuration(configuration);
        }
    }
}
