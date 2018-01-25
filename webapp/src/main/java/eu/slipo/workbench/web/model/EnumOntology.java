package eu.slipo.workbench.web.model;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public enum EnumOntology {
    UNDEFINED(0),
    GEOSPARQL(1),
    VIRTUOSO(2),
    WGS84(3),
    ;

    private final int value;

    private EnumOntology(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public String getKey() {
        return (this.getClass().getSimpleName() + '.' + name());
    }

    public static EnumOntology fromString(String value) {
        if (StringUtils.isBlank(value)) {
            return EnumOntology.UNDEFINED;
        }

        for (EnumOntology item : EnumOntology.values()) {
            if (item.name().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return EnumOntology.UNDEFINED;
    }

    public static class Deserializer extends JsonDeserializer<EnumOntology> {

        @Override
        public EnumOntology deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
            return EnumOntology.fromString(parser.getValueAsString());
        }
    }
}
