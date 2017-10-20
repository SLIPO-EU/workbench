package eu.slipo.workbench.web.controller.action;

import java.util.Arrays;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.slipo.workbench.common.model.RestResponse;
import eu.slipo.workbench.web.model.Configuration;
import eu.slipo.workbench.web.model.EnumDataFormat;
import eu.slipo.workbench.web.model.EnumDataSource;
import eu.slipo.workbench.web.model.EnumOperation;
import eu.slipo.workbench.web.model.EnumResourceType;
import eu.slipo.workbench.web.model.ValueListItem;

@RestController
public class ConfigurationController {

    private final String defaultLang = "en";

    @Autowired
    private MessageSource messageSource;

    @RequestMapping(value = "/action/configuration", method = RequestMethod.GET, produces = "application/json")
    public RestResponse<Configuration> getConfiguration() {
        return RestResponse.result(this.createConfiguration(defaultLang));
    }

    @RequestMapping(value = "/action/configuration/{lang}", method = RequestMethod.GET, produces = "application/json")
    public RestResponse<Configuration> getConfiguration(@PathVariable String lang) {
        return RestResponse.result(this.createConfiguration(lang));
    }

    private Configuration createConfiguration(String lang) {
        Configuration config = new Configuration();

        Locale locale = Locale.forLanguageTag(lang);

        Arrays.stream(EnumDataSource.values()).forEach(value -> {
            config.getValues().addDataSource(
                new ValueListItem<EnumDataSource>(value, messageSource.getMessage(value.getKey(), null, locale))
            );
        });

        Arrays.stream(EnumDataFormat.values()).forEach(value -> {
            config.getValues().addDataFormat(
                new ValueListItem<EnumDataFormat>(value, messageSource.getMessage(value.getKey(), null, locale))
            );
        });

        Arrays.stream(EnumOperation.values()).forEach(value -> {
            config.getValues().addOperation(
                new ValueListItem<EnumOperation>(value, messageSource.getMessage(value.getKey(), null, locale))
            );
        });

        Arrays.stream(EnumResourceType.values()).forEach(value -> {
            config.getValues().addResourceType(
                new ValueListItem<EnumResourceType>(value, messageSource.getMessage(value.getKey(), null, locale))
            );
        });

        return config;
    }

}
