package eu.slipo.workbench.rpc.tests.integration.service;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.google.auto.value.AutoValue;

import eu.slipo.workbench.common.model.resource.DataSource;
import eu.slipo.workbench.common.model.tool.TriplegeoConfiguration;
import eu.slipo.workbench.common.service.util.PropertiesConverterService;
import eu.slipo.workbench.common.service.util.PropertiesConverterService.ConversionFailedException;

/**
 * A test fixture that represents a basic to-RDF operation (using Triplegeo).
 */
@AutoValue
abstract class TransformFixture extends BaseFixture
{
    abstract URI inputUri();

    abstract TriplegeoConfiguration configuration();

    DataSource inputAsDataSource() throws MalformedURLException
    {
        return inputAsDataSource(inputUri());
    }

    @Override
    void checkState()
    {
        super.checkState();

        URI inputUri = inputUri();
        Path stagingDir = stagingDir();
        Assert.state(stagingDir == null || inputUri.getScheme().equals("file"),
            "If no staging directory given, input is expected as an absolute file URI");
        Assert.state(!inputUri.getScheme().equals("file") || Paths.get(inputUri).startsWith(stagingDir),
            "If input is a local file (a file URI), must be located under staging directory");
    }

    static Builder newBuilder()
    {
        return new AutoValue_TransformFixture.Builder();
    }

    static Builder newBuilder(PropertiesConverterService propertiesConverter)
    {
        Builder b = new AutoValue_TransformFixture.Builder();
        b.setPropertiesConverter(propertiesConverter);
        return b;
    }

    @AutoValue.Builder
    static abstract class Builder extends BaseFixture.BaseBuilder<Builder, TransformFixture>
    {
        abstract Builder inputUri(URI uri);

        Builder inputUri(URL url) throws URISyntaxException
        {
            return inputUri(url.toURI());
        }

        abstract Builder configuration(TriplegeoConfiguration configuration);

        Builder configuration(Properties opts, Optional<String> mappingSpec, Optional<String> classificationSpec)
            throws ConversionFailedException
        {
            Assert.state(propertiesConverter != null, "A properties converter is required");
            TriplegeoConfiguration configuration =
                propertiesConverter.propertiesToValue(opts, TriplegeoConfiguration.class);
            configuration.setMappingSpec(mappingSpec.orElse(null));
            configuration.setClassificationSpec(classificationSpec.orElse(null));
            return configuration(configuration);
        }

        abstract TriplegeoConfiguration configuration();
    }
}
