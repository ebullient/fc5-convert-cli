package dev.ebullient.fc5.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.ebullient.fc5.Log;

@SuppressWarnings("unchecked")
public class ParsingContext {
    private static final List<String> EMPTY_STRING_LIST = Collections.emptyList();

    final String owner;
    final Map<String, Object> elements = new HashMap<>();

    public ParsingContext(String owner) {
        this.owner = owner;
    }

    public void put(String key, Object value) {
        Object obj = elements.get(key);
        if (obj == null) {
            elements.put(key, value);
        } else {
            List<Object> multiple = (List<Object>) elements.computeIfPresent(key, this::convertToList);
            multiple.add(value);
        }
    }

    public <T> T getOrDefault(String owner, String key, T defaultValue) {
        if (defaultValue instanceof Text) {
            List<String> list = getOrDefault(owner, key, EMPTY_STRING_LIST);
            return (T) new Text(list);
        }
        return convertObject(owner, elements.get(key), defaultValue);
    }

    public <T> T getOrFail(String parentElement, String key, Class<T> typeClass) {
        Object o = elements.get(key);
        if (o == null) {
            String errorMsg = parentElement + " is missing a value for required key " + key;
            Log.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }

        if (Text.class.isAssignableFrom(typeClass)) {
            List<String> list = getOrFail(parentElement, key, EMPTY_STRING_LIST.getClass());
            return (T) new Text(list);
        }
        if (Double.class.isAssignableFrom(typeClass)) {
            return (T) Double.valueOf((String) o);
        }
        if (Integer.class.isAssignableFrom(typeClass)) {
            return (T) Integer.valueOf((String) o);
        }
        if (List.class.isAssignableFrom(typeClass) && !(o instanceof List)) {
            return (T) Collections.singletonList(o);
        }

        try {
            return (T) o;
        } catch (Exception ex) {
            String errorMsg = String.format("Error parsing %s: Unable to cast %s to type %s",
                    owner, o, typeClass);
            Log.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
    }

    public <T> T convertObject(String owner, Object o, T defaultValue) {
        if (o == null) {
            return defaultValue;
        }

        if (defaultValue instanceof Double) {
            if (o instanceof String && ((String) o).isBlank()) {
                return defaultValue;
            }
            return (T) Double.valueOf((String) o);
        }
        if (defaultValue instanceof Integer) {
            if (o instanceof String && ((String) o).isBlank()) {
                return defaultValue;
            }
            return (T) Integer.valueOf((String) o);
        }
        if (defaultValue instanceof List && !(o instanceof List)) {
            if (o instanceof String && ((String) o).isBlank()) {
                return (T) Collections.emptyList();
            }
            return (T) Collections.singletonList(o);
        }

        try {
            return (T) o;
        } catch (Exception ex) {
            String errorMsg = String.format("Error parsing %s: unable to cast %s with default value %s",
                    owner, o, defaultValue);
            Log.error(ex, errorMsg);
            throw new IllegalArgumentException(errorMsg, ex);
        }
    }

    private List<Object> convertToList(String key, Object existing) {
        if (existing instanceof List) {
            return (List<Object>) existing;
        } else {
            List<Object> newList = new ArrayList<>();
            newList.add(existing);
            return newList;
        }
    }

    @Override
    public String toString() {
        return "ParsingContext[owner=" + owner + ", elements=" + elements.keySet() + "]";
    }

    /**
     * Features end up with multiple (section) names. Smash subsequent
     * names into the regular text (prepending a heading ##)
     */
    public static class FeatureParsingContext extends ParsingContext {
        boolean foundName;

        public FeatureParsingContext(String owner) {
            super(owner);
        }

        @Override
        public void put(String key, Object value) {
            if ("name".equals(key)) {
                if (foundName) {
                    super.put("text", "## " + value);
                } else {
                    super.put(key, value);
                    foundName = true;
                }
            } else {
                super.put(key, value);
            }
        }
    }
}
