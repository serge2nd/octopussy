package ru.serge2nd.octopussy.config.support.bean;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import ru.serge2nd.octopussy.config.ImmutableProviderConfig;
import ru.serge2nd.util.HardProperties;
import ru.serge2nd.util.TypeWrap;
import ru.serge2nd.util.bean.Immutable;
import ru.serge2nd.util.bean.ImmutableBeanPostProcessor;
import ru.serge2nd.util.immutable.DelegatingImmutableProvider;

import java.util.*;

import static java.util.Collections.*;
import static java.util.Optional.of;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

@SpringBootTest(classes = {
        ImmutableProviderConfig.class,
        ImmutableBeanPostProcessor.class,
        ImmutableBeanPostProcessorTest.Config.class},
        webEnvironment = NONE)
@ActiveProfiles({ImmutableBeanPostProcessorTest.PROFILE, "test"})
class ImmutableBeanPostProcessorTest {
    @Qualifier("immutableCollection") @Autowired Collection<String> immutableCollection;
    @Qualifier("immutableList") @Autowired List<String> immutableList;
    @Qualifier("immutableSet") @Autowired Set<String> immutableSet;
    @Qualifier("immutableSortedSet") @Autowired SortedSet<String> immutableSortedSet;
    @Qualifier("immutableNavigableSet") @Autowired NavigableSet<String> immutableNavigableSet;
    @Qualifier("immutableMap") @Autowired Map<String, Integer> immutableMap;
    @Qualifier("immutableSortedMap") @Autowired SortedMap<String, Integer> immutableSortedMap;
    @Qualifier("immutableNavigableMap") @Autowired NavigableMap<String, Integer> immutableNavigableMap;
    @Qualifier("immutableStringToStringMap") @Autowired Map<String, String> immutableStringToStringMap;
    @Qualifier("immutableProperties") @Autowired Properties immutableProperties;
    @Autowired StringBuilder notImmutableBean;

    static final String PROFILE = "ImmutableBeanPostProcessorTest";
    static final Collection<String> EXPECTED = singleton("z");
    static final Map<String, Integer> EXPECTEDM = singletonMap("z", "z".codePointAt(0));
    static final Map<String, String> EXPECTED_STRINGS = singletonMap("z", "z");

    @Test
    void testImmutableCollection() {
        assertSame(unmodifiableCollection(emptySet()).getClass(), immutableCollection.getClass());
        assertEquals(EXPECTED, new HashSet<>(immutableCollection));
    }

    @Test
    void testImmutableList() {
        assertSame(unmodifiableList(emptyList()).getClass(), immutableList.getClass());
        assertEquals(EXPECTED, new HashSet<>(immutableList));
    }

    @Test
    void testImmutableSet() {
        assertSame(unmodifiableSet(emptySet()).getClass(), immutableSet.getClass());
        assertEquals(EXPECTED, new HashSet<>(immutableSet));
    }

    @Test
    void testImmutableSortedSet() {
        assertSame(unmodifiableSortedSet(emptySortedSet()).getClass(), immutableSortedSet.getClass());
        assertEquals(EXPECTED, new HashSet<>(immutableSortedSet));
    }

    @Test
    void testImmutableNavigableSet() {
        assertSame(unmodifiableNavigableSet(emptyNavigableSet()).getClass(), immutableNavigableSet.getClass());
        assertEquals(EXPECTED, new HashSet<>(immutableNavigableSet));
    }

    @Test
    void testImmutableMap() {
        assertSame(unmodifiableMap(emptyMap()).getClass(), immutableMap.getClass());
        assertEquals(EXPECTEDM, new HashMap<>(immutableMap));
    }

    @Test
    void testImmutableSortedMap() {
        assertSame(unmodifiableSortedMap(emptySortedMap()).getClass(), immutableSortedMap.getClass());
        assertEquals(EXPECTEDM, new HashMap<>(immutableSortedMap));
    }

    @Test
    void testImmutableNavigableMap() {
        assertSame(unmodifiableNavigableMap(emptyNavigableMap()).getClass(), immutableNavigableMap.getClass());
        assertEquals(EXPECTEDM, new HashMap<>(immutableNavigableMap));
    }

    @Test
    void testImmutableStringToStringMap() {
        assertSame(HardProperties.class, immutableStringToStringMap.getClass());
        assertEquals(EXPECTED_STRINGS, new HashMap<>(immutableStringToStringMap));
    }

    @Test
    void testImmutableProperties() {
        assertSame(HardProperties.class, immutableProperties.getClass());
        assertEquals(EXPECTED_STRINGS, new HashMap<>(immutableProperties));
    }

    @Test
    void testNotImmutableBean() {
        assertSame(StringBuilder.class, notImmutableBean.getClass());
    }

    @Configuration
    @Profile(ImmutableBeanPostProcessorTest.PROFILE)
    static class Config {
        Config(DelegatingImmutableProvider<Object> immutableProvider) {
            immutableProvider.registerPrimaryDelegate(
                    TypeWrap.of(Map.class, String.class, String.class),
                    $ -> of(HardProperties::of));
            immutableProvider.registerDelegate(NavigableSet.class, $ -> of(Collections::<Object>unmodifiableNavigableSet));
            immutableProvider.registerDelegate(SortedSet.class, $ -> of(Collections::<Object>unmodifiableSortedSet));
            immutableProvider.registerDelegate(Set.class, $ -> of(Collections::<Object>unmodifiableSet));
            immutableProvider.registerDelegate(List.class, $ -> of(Collections::<Object>unmodifiableList));
            immutableProvider.registerDelegate(Collection.class, $ -> of(Collections::<Object>unmodifiableCollection));
        }

        @Immutable @Bean
        Collection<String> immutableCollection() {
            return new Collection<String>() {
                public int size() { return EXPECTED.size(); }
                public boolean isEmpty() { return EXPECTED.isEmpty(); }
                public Iterator<String> iterator() { return EXPECTED.iterator(); }
                public boolean contains(Object o) { throw new UnsupportedOperationException(); }
                public Object[] toArray() { throw new UnsupportedOperationException(); }
                public <T> T[] toArray(T[] ts) { throw new UnsupportedOperationException(); }
                public boolean add(String s) { throw new UnsupportedOperationException(); }
                public boolean remove(Object o) { throw new UnsupportedOperationException(); }
                public boolean containsAll(Collection<?> collection) { throw new UnsupportedOperationException(); }
                public boolean addAll(Collection<? extends String> collection) { throw new UnsupportedOperationException(); }
                public boolean removeAll(Collection<?> collection) { throw new UnsupportedOperationException(); }
                public boolean retainAll(Collection<?> collection) { throw new UnsupportedOperationException(); }
                public void clear() { throw new UnsupportedOperationException(); }
            };
        }
        @Immutable @Bean List<String> immutableList() { return new ArrayList<>(EXPECTED); }
        @Immutable @Bean Set<String> immutableSet() { return new HashSet<>(EXPECTED); }
        @Immutable @Bean SortedSet<String> immutableSortedSet() { return new TreeSet<>(EXPECTED); }
        @Immutable @Bean NavigableSet<String> immutableNavigableSet() { return new TreeSet<>(EXPECTED); }
        @Immutable @Bean Map<String, Integer> immutableMap() { return new HashMap<>(EXPECTEDM); }
        @Immutable @Bean SortedMap<String, Integer> immutableSortedMap() { return new TreeMap<>(EXPECTEDM); }
        @Immutable @Bean NavigableMap<String, Integer> immutableNavigableMap() { return new TreeMap<>(EXPECTEDM); }
        @Immutable @Bean Map<String, String> immutableStringToStringMap() { return new HashMap<>(EXPECTED_STRINGS); }
        @Immutable @Bean Properties immutableProperties() { return new Properties(){{putAll(EXPECTED_STRINGS);}}; }
        @Immutable @Bean StringBuilder notImmutableBean() { return new StringBuilder(); }
    }
}