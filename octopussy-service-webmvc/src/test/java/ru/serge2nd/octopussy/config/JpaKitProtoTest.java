package ru.serge2nd.octopussy.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.cache.Cache;
import ru.serge2nd.octopussy.spi.JpaKit;
import ru.serge2nd.octopussy.support.DataKitDefinition;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import java.util.Map;

import static java.util.Arrays.copyOfRange;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;
import static ru.serge2nd.collection.HardProperties.properties;
import static ru.serge2nd.octopussy.App.*;
import static ru.serge2nd.octopussy.App.DATA_KIT_PASSWORD;
import static ru.serge2nd.octopussy.support.DataKitDefinitionTest.ID;
import static ru.serge2nd.stream.MapCollectors.toMap;
import static ru.serge2nd.stream.util.Collecting.collect;
import static ru.serge2nd.test.match.AssertThat.assertThat;
import static ru.serge2nd.test.match.CoreMatch.sameAs;

@TestInstance(Lifecycle.PER_CLASS)
class JpaKitProtoTest {
    static final String[] PROP_NAMES = {DATA_KIT_DRIVER_CLASS, DATA_KIT_URL, DATA_KIT_LOGIN, DATA_KIT_PASSWORD, DATA_KIT_DB, DATA_KIT_ID};
    static final String[] DS_PROP_NAMES = copyOfRange(PROP_NAMES, 0, 4);

    static final Map<String, String> KEYS = collect(toMap(k -> k, 0), DS_PROP_NAMES);
    static final Map<String, Object> VALS = collect(toMap(k -> k + "_val", 0), DS_PROP_NAMES);
    static final DataKitDefinition DEF = new DataKitDefinition(ID,
            collect(PROP_NAMES, toMap(k -> DATA_KIT_ID.equals(k) ? ID : (k + "_val"), 0)));

    final PersistenceUnitProvider pUnitsMock = mock(PersistenceUnitProvider.class, RETURNS_DEEP_STUBS);
    final JpaKitConfig cfg = spy(new JpaKitConfig(pUnitsMock, mock(Cache.class)));
    final JpaKitProto proto = new JpaKitProto(cfg);

    @Test
    void testNewJpaKit() {
        // GIVEN
        DataSource ds = mock(DataSource.class);
        EntityManagerFactory emf = mock(EntityManagerFactory.class);
        // AND
        when(cfg.propertyMappings()).thenReturn(KEYS);
        when(pUnitsMock.getDataSource(VALS)).thenReturn(ds);
        when(pUnitsMock.getEntityManagerFactory(same(ds), eq(properties(
                DATA_KIT_DB, DATA_KIT_DB + "_val",
                DATA_KIT_ID, ID
        ).toMap()))).thenReturn(emf);

        // WHEN
        JpaKit result = proto.newJpaKit(DEF);

        /* THEN */ assertThat(
        result.getDefinition()          , sameAs(DEF),
        result.getDataSource()          , sameAs(ds),
        result.getEntityManagerFactory(), sameAs(emf));
    }
}