package eu.slipo.workbench.common.model.resource;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

/**
 * Enumerate supported types of data sources
 */
public enum EnumDataSourceType {
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
    ;

    private final int value;

    private EnumDataSourceType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public String getKey() {
        return (this.getClass().getSimpleName() + '.' + name());
    }

    public static EnumDataSourceType fromString(String value) {
        for (EnumDataSourceType item : EnumDataSourceType.values()) {
            if (item.name().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return EnumDataSourceType.UNDEFINED;
    }

    public static class Deserializer extends JsonDeserializer<EnumDataSourceType> {

        @Override
        public EnumDataSourceType deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
            return EnumDataSourceType.fromString(parser.getValueAsString());
        }
    }
}
