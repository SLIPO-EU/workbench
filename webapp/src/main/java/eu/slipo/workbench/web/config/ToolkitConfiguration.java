package eu.slipo.workbench.web.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.util.Assert;

import eu.slipo.workbench.web.model.configuration.TripleGeoConfiguration;

@Configuration
@PropertySource("classpath:config/slipo-toolkit.properties")
@ConfigurationProperties(prefix = "slipo-toolkit")
public class ToolkitConfiguration implements InitializingBean {

    private TripleGeoConfiguration tripleGeo;

    public TripleGeoConfiguration getTripleGeo() {
        return tripleGeo;
    }

    public void setTripleGeo(TripleGeoConfiguration tripleGeo) {
        this.tripleGeo = tripleGeo;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.tripleGeo, "TripleGeo configuration is required");
        Assert.isTrue(!StringUtils.isBlank(this.tripleGeo.getVersion()), "TripleGeo version is required");
        Assert.isTrue(!StringUtils.isBlank(this.tripleGeo.getBaselineVersion()), "TripleGeo baseline version is required");
        Assert.isTrue(this.tripleGeo.getSupportedVersions().contains(this.tripleGeo.getVersion()), "TripleGeo version is not supported");
        Assert.isTrue(this.tripleGeo.getSupportedVersions().contains(this.tripleGeo.getBaselineVersion()), "TripleGeo version is not supported");
    }

}
