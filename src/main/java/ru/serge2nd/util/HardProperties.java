package ru.serge2nd.util;

import java.io.*;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collector;

import static java.util.Arrays.stream;
import static java.util.Collections.*;

public final class HardProperties extends Properties {
    public static final HardProperties EMPTY = new HardProperties(null, false);

    private final Map<String, String> map;

    public static HardProperties from(Map<?, ?>... sources) {
        if (stream(sources).allMatch(Map::isEmpty))
            return EMPTY;
        if (sources.length == 1)
            return new HardProperties(sources[0], true);

        return new HardProperties(stream(sources)
                .flatMap(m -> m.entrySet().stream())
                .collect(Collector.of(
                        HashMap::new,
                        (m, e) -> m.put(e.getKey(), e.getValue()),
                        (m1, m2) -> { m1.putAll(m2); return m1; })),
                false);
    }

    public static HardProperties of(Map<?, ?> src) {
        return new HardProperties(src, false);
    }

    public Map<String, String> toMap() { return map; }

    @SuppressWarnings("unchecked,rawtypes")
    private HardProperties(Map<?, ?> src, boolean copy) {
        if (src == null) {
            this.map = emptyMap();
            return;
        }

        src.forEach((k, v) -> {
            if (!(k instanceof String))
                throw new IllegalArgumentException("expected String as key");
            if (!(v == null || v instanceof String))
                throw new IllegalArgumentException("expected String or null as value");
        });
        this.map = unmodifiableMap(copy ? new HashMap(src) : src);
    }

    @Override
    public int size() { return map.size(); }
    @Override
    public boolean isEmpty() { return map.isEmpty(); }
    @Override
    public String getProperty(String key) { return map.get(key); }
    @Override
    public String getProperty(String key, String defaultValue) { return map.getOrDefault(key, defaultValue); }
    @Override
    public Object get(Object key) { return this.getProperty((String)key); }
    @Override
    public Object getOrDefault(Object key, Object defaultValue) { return this.getProperty((String)key, (String)defaultValue); }
    @Override
    public boolean contains(Object value) { return this.containsValue(value); }
    @Override
    public boolean containsValue(Object value) { return map.containsValue(value); }
    @Override
    public boolean containsKey(Object key) { return map.containsKey(key); }
    @Override
    public void forEach(BiConsumer<? super Object, ? super Object> action) { map.forEach(action); }
    @Override
    public Enumeration<?> propertyNames() { return this.keys(); }
    @Override
    public Set<String> stringPropertyNames() { return map.keySet(); }
    @Override
    public String toString() { return map.toString(); }
    @Override
    public int hashCode() { return map.hashCode(); }
    @Override @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    public boolean equals(Object o) { return map.equals(o); }
    @Override @SuppressWarnings("MethodDoesntCallSuperMethod")
    public Object clone() { return HardProperties.from(map); }

    @Override @SuppressWarnings("unchecked,rawtypes")
    public Set<Object> keySet() { return (Set) map.keySet(); }
    @Override @SuppressWarnings("unchecked,rawtypes")
    public Collection<Object> values() { return (Set) map.values(); }
    @Override @SuppressWarnings("unchecked,rawtypes")
    public Set<Map.Entry<Object, Object>> entrySet() { return (Set) map.entrySet(); }
    @Override
    public Enumeration<Object> keys() { return enumeration(this.keySet()); }
    @Override
    public Enumeration<Object> elements() { return enumeration(this.values()); }

    //
    // *** Unsupported operations ***

    @Override
    public Object setProperty(String key, String value) { throw new UnsupportedOperationException(IMMUTABLE_MSG); }
    @Override
    public Object put(Object key, Object value) { throw new UnsupportedOperationException(IMMUTABLE_MSG); }
    @Override
    public Object putIfAbsent(Object key, Object value) { throw new UnsupportedOperationException(IMMUTABLE_MSG); }
    @Override
    public Object replace(Object key, Object value) { throw new UnsupportedOperationException(IMMUTABLE_MSG); }
    @Override
    public boolean replace(Object key, Object oldValue, Object newValue) { throw new UnsupportedOperationException(IMMUTABLE_MSG); }
    @Override
    public void replaceAll(BiFunction<? super Object, ? super Object, ?> function) { throw new UnsupportedOperationException(IMMUTABLE_MSG); }
    @Override
    public Object compute(Object key, BiFunction<? super Object, ? super Object, ?> remappingFunction) { throw new UnsupportedOperationException(IMMUTABLE_MSG); }
    @Override
    public Object computeIfAbsent(Object key, Function<? super Object, ?> mappingFunction) { throw new UnsupportedOperationException(IMMUTABLE_MSG); }
    @Override
    public Object computeIfPresent(Object key, BiFunction<? super Object, ? super Object, ?> remappingFunction) { throw new UnsupportedOperationException(IMMUTABLE_MSG); }
    @Override
    public Object merge(Object key, Object value, BiFunction<? super Object, ? super Object, ?> remappingFunction) { throw new UnsupportedOperationException(IMMUTABLE_MSG); }
    @Override
    public Object remove(Object key) { throw new UnsupportedOperationException(IMMUTABLE_MSG); }
    @Override
    public boolean remove(Object key, Object value) { throw new UnsupportedOperationException(IMMUTABLE_MSG); }
    @Override
    public void putAll(Map<?, ?> t) { throw new UnsupportedOperationException(IMMUTABLE_MSG); }
    @Override
    public void clear() { throw new UnsupportedOperationException(IMMUTABLE_MSG); }
    @Override
    public void load(Reader reader) { throw new UnsupportedOperationException(IMMUTABLE_MSG); }
    @Override
    public void load(InputStream inStream) { throw new UnsupportedOperationException(IMMUTABLE_MSG); }
    @Override
    public void loadFromXML(InputStream in) { throw new UnsupportedOperationException(IMMUTABLE_MSG); }
    @Override @SuppressWarnings("deprecation")
    public void save(OutputStream out, String comments) { throw new UnsupportedOperationException(LIST_MSG); }
    @Override
    public void store(Writer writer, String comments) { throw new UnsupportedOperationException(LIST_MSG); }
    @Override
    public void store(OutputStream out, String comments) { throw new UnsupportedOperationException(LIST_MSG); }
    @Override
    public void storeToXML(OutputStream os, String comment) { throw new UnsupportedOperationException(LIST_MSG); }
    @Override
    public void storeToXML(OutputStream os, String comment, String encoding) { throw new UnsupportedOperationException(LIST_MSG); }
    @Override
    public void list(PrintStream out) { throw new UnsupportedOperationException(LIST_MSG); }
    @Override
    public void list(PrintWriter out) { throw new UnsupportedOperationException(LIST_MSG); }

    private static final String IMMUTABLE_MSG = "This properties are immutable";
    private static final String LIST_MSG = "This properties cannot be listed";
}
