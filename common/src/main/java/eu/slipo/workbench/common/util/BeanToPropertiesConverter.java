package eu.slipo.workbench.common.util;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import static java.util.Collections.singleton;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;

import static org.apache.commons.collections4.IterableUtils.chainedIterable;

/**
 * Convert a bean to a (flat) map of properties ({@link Properties}).  
 * <p>
 * This converter is based on Jackson's {@link ObjectMapper}) for performing JSON to/from 
 * object mappings.
 */
public class BeanToPropertiesConverter
{
    private static final ObjectMapper objectMapper = new ObjectMapper();
        
    private static class TreeVisitor 
    {    
        private final Properties properties;
        
        public TreeVisitor(JsonNode root)
        {
            this.properties = new Properties();
            objectToProperties(root, null);
        }
        
        public Properties getProperties()
        {
            return properties;
        }
        
        private void arrayToProperties(JsonNode node, String prefix)
        {
            for (int i = 0, n = node.size(); i < n; ++i) {
                String key = prefix + "[" + String.valueOf(i) + "]";
                JsonNode child = node.get(i);
                switch (child.getNodeType()) {
                case ARRAY:
                    arrayToProperties(child, key);
                    break;
                case OBJECT:
                    objectToProperties(child, key);
                    break;
                default:
                    properties.setProperty(key, child.asText(""));
                    break;
                }
            }
        }
        
        private void objectToProperties(JsonNode node, String prefix)
        {        
            Iterator<String> fieldNames = node.fieldNames(); 
            while (fieldNames.hasNext()) {
                String field = fieldNames.next();
                String key = prefix == null? field : (prefix + "." + field);
                if (node.has(field)) {
                    JsonNode child = node.get(field);
                    switch (child.getNodeType()) {
                    case ARRAY:
                        arrayToProperties(child, key);
                        break;
                    case OBJECT:
                        objectToProperties(child, key);
                        break;
                    default:
                        properties.put(key, child.asText(""));
                        break;
                    }
                }
            }
        }
    }
    
    private static class TreeBuilder
    {
        private static enum FieldType { LEAF, OBJECT, ARRAY };
      
        private static class FieldToken { 
            
            private final FieldType type;
            
            private final String name;
            
            private final Integer index;
            
            private FieldToken(FieldType type, String name)
            {
                this.type = type;
                this.name = name;
                this.index = null;
            }
            
            private FieldToken(FieldType type, String name, int index)
            {
                this.type = type;
                this.name = name;
                this.index = index;
            }
            
            @Override
            public int hashCode()
            {                
                return name == null? 0 : name.hashCode();
            }

            @Override
            public boolean equals(Object obj)
            {
                if (this == obj)
                    return true;
                if (obj == null || getClass() != obj.getClass())
                    return false;
                
                FieldToken other = (FieldToken) obj;
                if (name == null) {
                    if (other.name != null)
                        return false;
                } else if (!name.equals(other.name))
                    return false;
                return true;
            }

            private static FieldToken of(FieldType type, String name, int index)
            {
                return new FieldToken(type, name, index);
            }
            
            private static FieldToken of(FieldType type, String name)
            {
                return new FieldToken(type, name);
            }
        };
     
        private static final String SENTINEL_KEY = String.valueOf(Character.MAX_VALUE);
        
        private final JsonNode root;
        
        private final Properties properties;
        
        public TreeBuilder(Properties properties)
        {
            this.properties = properties;
            this.root = buildTree();
        }
        
        private JsonNode buildTree()
        {
            TreeSet<String> keys = properties.keySet().stream()
                .map(k -> (String) k)
                .collect(Collectors.toCollection(TreeSet::new));
            return propertiesToObject(0, keys);
        }
        
        private ObjectNode propertiesToObject(int prefixLen, SortedSet<String> keys)
        {
            ObjectNode node = objectMapper.createObjectNode();
            
            String groupKey = null; // a group starting on this key
            FieldToken token1 = null; // the previous token (for the previous group)
            for (String key: chainedIterable(keys, singleton(SENTINEL_KEY))) {
                FieldToken token = null;
                // Parse field token for this key
                if (key != SENTINEL_KEY) { 
                    int p = key.indexOf('[', prefixLen);
                    if (p >= 0)
                        token = FieldToken.of(FieldType.ARRAY, key.substring(prefixLen, p));
                    else if ((p = key.indexOf('.', prefixLen)) >= 0)
                        token = FieldToken.of(FieldType.OBJECT, key.substring(prefixLen, p));
                    else 
                        token = FieldToken.of(FieldType.LEAF, key.substring(prefixLen));
                }
                // Check if field token has changed
                if (token == null || !token.equals(token1)) {
                    // A group ends here: Set field on target object node
                    if (token1 != null) {
                        String fieldName = token1.name;
                        SortedSet<String> fieldKeys = key.compareTo(keys.last()) <= 0?
                            keys.subSet(groupKey, key) : keys.tailSet(groupKey);
                        switch (token1.type) {
                        case ARRAY:
                            {
                                ArrayNode child = propertiesToArray(
                                    prefixLen + fieldName.length() + 1, // +1 for "[" 
                                    fieldKeys);
                                node.set(fieldName, child);
                            }
                            break;
                        case OBJECT:
                            {
                                ObjectNode child = propertiesToObject(
                                    prefixLen + fieldName.length() + 1, // +1 for "."
                                    fieldKeys);
                                node.set(fieldName, child);
                            }
                            break;
                        case LEAF:
                        default:
                            {
                                String value = properties.getProperty(groupKey);
                                if (value != null && !value.isEmpty()) 
                                    node.put(fieldName, value);
                            }
                            break;
                        }
                    }
                    // Move to next group (described by token)
                    token1 = token;
                    groupKey = key;
                }
            }
            
            return node;
        }
        
        private ArrayNode propertiesToArray(int prefixLen, SortedSet<String> keys)
        {
            final Pattern fieldPattern = Pattern.compile("((\\d+)\\])(.)?");
            
            ArrayNode node = objectMapper.createArrayNode();
            
            String groupKey = null; // a group starting on this key
            FieldToken token1 = null; // the previous token (for the previous group)
            for (String key: chainedIterable(keys, singleton(SENTINEL_KEY))) {
                FieldToken token = null;
                // Parse field token for this key
                if (key != SENTINEL_KEY) { 
                    Matcher m = fieldPattern.matcher(key);
                    if (m.find(prefixLen)) {
                        int index = Integer.valueOf(m.group(2));
                        String name = m.group(1);
                        char a1 = m.start(3) < 0? '\0' : m.group(3).charAt(0); // look-ahead of 1 char
                        if (a1 == '.')
                            token = FieldToken.of(FieldType.OBJECT, name, index);
                        else if (a1 == '[')
                            token = FieldToken.of(FieldType.ARRAY, name, index);
                        else if (a1 == '\0')
                            token = FieldToken.of(FieldType.LEAF, name, index);
                        else 
                            throw new IllegalArgumentException(
                                "The property key is invalid: " + key.substring(prefixLen));
                    } else 
                        throw new IllegalArgumentException(
                            "Expected an indexed item at: " + key.substring(prefixLen));
                }
                // Check if field token has changed
                if (token == null || !token.equals(token1)) {
                    // A group ends here: Add item on target array node
                    if (token1 != null) {
                        if (token != null && (token.index - token1.index != 1))
                            throw new IllegalArgumentException(
                                "Expected a consecutive range of indices!");
                        String fieldName = token1.name;
                        SortedSet<String> fieldKeys = key.compareTo(keys.last()) <= 0?
                            keys.subSet(groupKey, key) : keys.tailSet(groupKey);
                        switch (token1.type) {
                        case ARRAY:
                            {
                                ArrayNode child = propertiesToArray(
                                    prefixLen + fieldName.length() + 1, // +1 for "[",
                                    fieldKeys);
                                node.add(child);
                            }
                            break;
                        case OBJECT:
                            {
                                ObjectNode child = propertiesToObject(
                                    prefixLen + fieldName.length() + 1, // +1 for "."
                                    fieldKeys);
                                node.add(child);
                            }
                            break;
                        case LEAF:
                        default:
                            {
                                String value = properties.getProperty(groupKey);
                                if (value != null && !value.isEmpty()) 
                                    node.add(value);
                            }
                            break;
                        }
                    } else {
                        if (token != null && token.index != 0)
                            throw new IllegalArgumentException("Expected 0-based indices");
                    }
                    // Move to next group (described by token)
                    token1 = token;
                    groupKey = key;
                }
            }
            
            return node;
        }
        
        public JsonNode getRoot()
        {
            return root;
        }
    }
    
    /**
     * Convert a bean to a map of properties by using an intermediate JSON serialization.
     *  
     * @param value The bean to be converted
     */
    public static Properties valueToProperties(Object value)
    {
        JsonNode root = objectMapper.valueToTree(value);
        return (new TreeVisitor(root)).getProperties();
    }
    
    /**
     * Create a bean from a map of properties.
     * 
     * @param props The given map of properties
     * @param valueType The target type
     * @throws JsonProcessingException
     */
    public static <T extends Serializable> T propertiesToValue(Properties props, Class<T> valueType) 
        throws JsonProcessingException
    {
        JsonNode root = (new TreeBuilder(props)).getRoot();
        return objectMapper.treeToValue(root, valueType);
    }
    
    /**
     * Create a bean from a map of properties.
     * <p>
     * Note that this method will not examine nested maps/arrays into given map, i.e. it will
     * consider the map as a flat map of properties (see {@link Properties}). If nested structure
     * should be examined, then an ordinary JSON deserialization is preferable.  
     * 
     * @param map A map with property-like keys (e.g. "foo.baz") mapping to arbitrary values. 
     * @param valueType The target type
     * @throws JsonProcessingException
     */
    public static <T extends Serializable> T propertiesToValue(Map<String,Object> map, Class<T> valueType) 
        throws JsonProcessingException
    {
        Properties props = new Properties();
        props.putAll(map);
        return propertiesToValue(props, valueType);
    }
    
    /**
     * Create a bean from a map of properties lying under a certain root property.
     * 
     * @param props The given map of properties
     * @param rootPropertyName The name of the root property (without the trailing dot) for 
     *   properties we are interested into.  
     * @param valueType The target type
     * @throws JsonProcessingException
     */
    public static <T extends Serializable> T propertiesToValue(
            Properties props, String rootPropertyName, Class<T> valueType) 
        throws JsonProcessingException
    {
        Assert.isTrue(!StringUtils.isEmpty(rootPropertyName), 
            "Expected a non-empty root property name");

        final String prefix = rootPropertyName + '.';
        final int prefixLen = prefix.length();
        
        Properties p1 = new Properties();
        for (Object key: props.keySet()) {
            String k = (String) key;
            if (k.startsWith(prefix))
                p1.put(k.substring(prefixLen), props.get(k));
        }
        
        return propertiesToValue(p1, valueType);
    }
    
    /**
     * @see BeanToPropertiesConverter#propertiesToValue(Properties, String, Class)
     */
    public static <T extends Serializable> T propertiesToValue(
            Map<String,Object> map, String rootPropertyName, Class<T> valueType) 
        throws JsonProcessingException
    {
        Assert.isTrue(!StringUtils.isEmpty(rootPropertyName), 
            "Expected a non-empty root property name");

        final String prefix = rootPropertyName + '.';
        final int prefixLen = prefix.length();
        
        Properties p1 = new Properties();
        for (String k: map.keySet())
            if (k.startsWith(prefix))
                p1.put(k.substring(prefixLen), map.get(k));
        
        return propertiesToValue(p1, valueType);
    }
    
    /**
     * @see BeanToPropertiesConverter#propertiesToValue(Properties, String, Class)
     */
    public static <T extends Serializable> T propertiesToValue(
            SortedMap<String,Object> map, String rootPropertyName, Class<T> valueType) 
        throws JsonProcessingException
    {
        Assert.isTrue(!StringUtils.isEmpty(rootPropertyName), 
            "Expected a non-empty root property name");

        final String prefix = rootPropertyName + '.';
        final String startKey = prefix;
        final String endKey = rootPropertyName + '/';
        final int prefixLen = prefix.length();
        
        Properties p1 = new Properties();
        for (String k: map.subMap(startKey, endKey).keySet())
            p1.put(k.substring(prefixLen), map.get(k));
        
        return propertiesToValue(p1, valueType);
    }
}