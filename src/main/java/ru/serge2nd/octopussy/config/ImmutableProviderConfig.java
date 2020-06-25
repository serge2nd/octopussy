package ru.serge2nd.octopussy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.serge2nd.util.HardProperties;
import ru.serge2nd.util.immutable.DelegatingImmutableProvider;
import ru.serge2nd.util.immutable.ImmutableProvider;

import java.util.*;

import static java.util.Optional.of;

@Configuration
public class ImmutableProviderConfig {

    @Bean
    public ImmutableProvider<?> delegatingImmutableProvider() {
        DelegatingImmutableProvider<Object> provider = new DelegatingImmutableProvider<>();
        provider.registerDelegate(Properties.class, $ -> of(HardProperties::of));
        provider.registerDelegate(NavigableMap.class, $ -> of(Collections::<Object, Object>unmodifiableNavigableMap));
        provider.registerDelegate(SortedMap.class, $ -> of(Collections::<Object, Object>unmodifiableSortedMap));
        provider.registerDelegate(Map.class, $ -> of(Collections::<Object, Object>unmodifiableMap));
        return provider;
    }
}
