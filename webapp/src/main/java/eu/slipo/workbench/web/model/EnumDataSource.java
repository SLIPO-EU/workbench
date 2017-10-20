package eu.slipo.workbench.web.model;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

/**
 * Supported data sources
 */
public enum EnumDataSource {
    /**
     * Invalid data source
     */
    UNDEFINED(0),
    /**
     * File uploaded using the workbench web application
     */
    UPLOAD(1),
    /**
     * File loaded from the NFS distributed file system
     */
    FILESYSTEM(2),
    /**
     * Data obtained using a harvester implementation
     */
    HARVESTER(3),
    /**
     * Data loaded from a public external URL
     */
    EXTERNAL_URL(4),
    /**
     * Resource is the result of a data integration process e.g. FAGI fusion operation
     */
    COMPUTED(5);

    private final int value;

    private EnumDataSource(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public String getKey() {
        return (this.getClass().getSimpleName() + '.' + name());
    }

    public static EnumDataSource fromString(String value) {
        for (EnumDataSource item : EnumDataSource.values()) {
            if (item.name().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return EnumDataSource.UNDEFINED;
    }

    public static class Deserializer extends JsonDeserializer<EnumDataSource> {

        @Override
        public EnumDataSource deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
            return EnumDataSource.fromString(parser.getValueAsString());
        }
    }
}
