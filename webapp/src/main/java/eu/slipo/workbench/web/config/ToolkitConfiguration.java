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

    private TripleGeoConfiguration triplegeo;

    public TripleGeoConfiguration getTriplegeo() {
        return triplegeo;
    }

    public void setTriplegeo(TripleGeoConfiguration triplegeo) {
        this.triplegeo = triplegeo;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.triplegeo, "TripleGeo configuration is required");
        Assert.isTrue(!StringUtils.isBlank(this.triplegeo.getVersion()), "TripleGeo version is required");
        Assert.isTrue(!StringUtils.isBlank(this.triplegeo.getBaselineVersion()), "TripleGeo baseline version is required");
        Assert.isTrue(this.triplegeo.getSupportedVersions().contains(this.triplegeo.getVersion()), "TripleGeo version is not supported");
        Assert.isTrue(this.triplegeo.getSupportedVersions().contains(this.triplegeo.getBaselineVersion()), "TripleGeo version is not supported");
    }

}
