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

    public <T> T getOrDefault(String key, T defaultValue) {
        if (defaultValue instanceof Text) {
            List<String> list = getOrDefault(key, EMPTY_STRING_LIST);
            return (T) new Text(list);
        }
        return convertObject(elements.get(key), defaultValue);
    }

    public <T> T getOrFail(String parentElement, String key, Class<T> typeClass) {
        Object o = elements.get(key);
        if (o == null) {
            throw new IllegalStateException(parentElement + " is missing a value for required key " + key);
        }

        try {
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
            return (T) o;
        } catch (ClassCastException ex) {
            Log.err().println("Error casting " + o + " to type " + typeClass);
            throw ex;
        }
    }

    public <T> T convertObject(Object o, T defaultValue) {
        if (o == null) {
            return defaultValue;
        }
        try {
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
                return (T) Collections.singletonList(o);
            }
            return (T) o;
        } catch (ClassCastException ex) {
            Log.err().println("Error casting " + o + " with defaultValue " + defaultValue);
            throw ex;
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
