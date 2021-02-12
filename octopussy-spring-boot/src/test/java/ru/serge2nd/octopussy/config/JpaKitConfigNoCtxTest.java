package ru.serge2nd.octopussy.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.aop.framework.Advised;
import org.springframework.transaction.interceptor.TransactionalProxy;
import ru.serge2nd.octopussy.spi.JpaKit;
import ru.serge2nd.octopussy.spi.NativeQueryAdapter;
import ru.serge2nd.octopussy.support.DataKitDefinition;
import ru.serge2nd.octopussy.support.NativeQueryAdapterImpl;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Map;
import java.util.function.Function;

import static java.util.Arrays.copyOfRange;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;
import static org.springframework.aop.support.AopUtils.isJdkDynamicProxy;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static ru.serge2nd.collection.HardProperties.properties;
import static ru.serge2nd.octopussy.App.*;
import static ru.serge2nd.octopussy.support.DataKitDefinitionTest.ID;
import static ru.serge2nd.stream.MapCollectors.toMap;
import static ru.serge2nd.stream.util.Collecting.collect;
import static ru.serge2nd.test.Asserting.assertEach;
import static ru.serge2nd.test.match.AssertThat.assertThat;
import static ru.serge2nd.test.match.CoreMatch.sameAs;

@TestInstance(Lifecycle.PER_CLASS)
class JpaKitConfigNoCtxTest {
    static final String[] PROP_NAMES = {DATA_KIT_DRIVER_CLASS, DATA_KIT_URL, DATA_KIT_LOGIN, DATA_KIT_PASSWORD, DATA_KIT_DB, DATA_KIT_ID};
    static final String[] DS_PROP_NAMES = copyOfRange(PROP_NAMES, 0, 4);

    static final Map<String, String> KEYS = collect(toMap(k -> k, 0), DS_PROP_NAMES);
    static final Map<String, Object> VALS = collect(toMap(k -> k + "_val", 0), DS_PROP_NAMES);
    static final DataKitDefinition DEF = new DataKitDefinition(ID,
            collect(toMap(k -> DATA_KIT_ID.equals(k) ? ID : (k + "_val"), 0), PROP_NAMES));

    final PersistenceUnitProvider pUnitMock = mock(PersistenceUnitProvider.class, RETURNS_DEEP_STUBS);
    final JpaKitConfig instance = mock(JpaKitConfig.class, withSettings()
            .spiedInstance(new JpaKitConfig(pUnitMock))
            .defaultAnswer(RETURNS_DEEP_STUBS));

    @Test
    void testNewJpaKit() {
        // GIVEN
        DataSource ds = mock(DataSource.class);
        EntityManagerFactory emf = mock(EntityManagerFactory.class);
        // AND
        when(pUnitMock.getDataSource(VALS)).thenReturn(ds);
        when(pUnitMock.getEntityManagerFactory(same(ds), eq(properties(
                DATA_KIT_DB, DATA_KIT_DB + "_val",
                DATA_KIT_ID, ID
        ).toMap()))).thenReturn(emf);
        // AND
        when(instance.getPropertyMappings()).thenReturn(KEYS);
        when(instance.newJpaKit(same(DEF))).thenCallRealMethod();

        // WHEN
        JpaKit result = instance.newJpaKit(DEF);

        /* THEN */ assertThat(
        result.getDefinition()          , sameAs(DEF),
        result.getDataSource()          , sameAs(ds),
        result.getEntityManagerFactory(), sameAs(emf));
    }

    @Test
    @SuppressWarnings("unchecked,ConstantConditions")
    void testGetQueryAdapter() throws Exception {
        // GIVEN
        EntityManagerFactory emfMock = mock(EntityManagerFactory.class);
        when(instance.dataKitService().doWith(eq(ID), same(EntityManagerFactory.class), any(Function.class)))
                .thenAnswer(i -> i.getArgument(2, Function.class).apply(emfMock));
        when(instance.getQueryAdapter(ID)).thenCallRealMethod();

        // WHEN
        NativeQueryAdapter result = instance.getQueryAdapter(ID);

        /* THEN */ assertEach("Check proxy interfaces", () ->
        assertTrue(isJdkDynamicProxy(result), "expected a JDK proxy"), () ->
        assertTrue(result instanceof TransactionalProxy, "expected a transactional proxy"), () ->
        assertTrue(result instanceof Advised, "expected an advised"));
        // AND
        Advised advised = (Advised)result;
        assertEach("Check proxy contents", () ->
        assertFalse(advised.isProxyTargetClass(), "must not proxy target class"), () ->
        assertTrue(advised.isFrozen(), "must be frozen"), () ->
        assertSame(NativeQueryAdapterImpl.class, ((Advised)result).getTargetSource().getTargetClass(), "wrong target"));
        // AND
        Object target = advised.getTargetSource().getTarget();
        assertEach("Check target", () ->
        assertSame(emfMock, getField(target, "emf"), "wrong entity manager factory"), () ->
        assertSame(pUnitMock.getResultTransformer(), getField(target, "transformer"), "wrong result transformer"));
    }
}