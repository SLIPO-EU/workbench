package eu.slipo.workbench.web.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.util.Assert;

import eu.slipo.workbench.web.model.configuration.AbstractToolConfiguration;
import eu.slipo.workbench.web.model.configuration.LimesConfiguration;
import eu.slipo.workbench.web.model.configuration.TripleGeoConfiguration;

@Configuration
@PropertySource("classpath:config/slipo-toolkit.properties")
@ConfigurationProperties(prefix = "slipo-toolkit")
public class ToolkitConfiguration implements InitializingBean {

    private TripleGeoConfiguration triplegeo;

    private LimesConfiguration limes;

    public TripleGeoConfiguration getTriplegeo() {
        return triplegeo;
    }

    public void setTriplegeo(TripleGeoConfiguration triplegeo) {
        this.triplegeo = triplegeo;
    }

    public LimesConfiguration getLimes() {
        return limes;
    }

    public void setLimes(LimesConfiguration limes) {
        this.limes = limes;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.CheckConfiguration("TripleGeo", this.triplegeo);
        this.CheckConfiguration("LIMES", this.limes);
    }

    public void CheckConfiguration(String name, AbstractToolConfiguration configuration) throws Exception {
        Assert.notNull(configuration,
                       String.format("%s configuration is required", name));
        Assert.isTrue(!StringUtils.isBlank(configuration.getVersion()),
                      String.format("%s version is required", name));
        Assert.isTrue(!StringUtils.isBlank(configuration.getBaselineVersion()),
                      String.format("%s baseline version is required", name));
        Assert.isTrue(configuration.getSupportedVersions().contains(configuration.getVersion()),
                      String.format("%s version is not supported", name));
        Assert.isTrue(configuration.getSupportedVersions().contains(configuration.getBaselineVersion()),
                      String.format("%s version is not supported", name));
    }

}
