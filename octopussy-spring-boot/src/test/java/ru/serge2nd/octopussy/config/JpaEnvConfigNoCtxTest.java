package ru.serge2nd.octopussy.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.aop.framework.Advised;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.interceptor.TransactionalProxy;
import ru.serge2nd.octopussy.spi.JpaEnvironment;
import ru.serge2nd.octopussy.spi.NativeQueryAdapter;
import ru.serge2nd.octopussy.spi.PersistenceUnitProvider;
import ru.serge2nd.octopussy.support.DataEnvironmentDefinition;
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
import static ru.serge2nd.octopussy.support.DataEnvironmentDefinitionTest.ID;
import static ru.serge2nd.stream.MapCollectors.toMap;
import static ru.serge2nd.stream.util.Collecting.collect;
import static ru.serge2nd.test.Asserting.assertEach;
import static ru.serge2nd.test.match.AssertThat.assertThat;
import static ru.serge2nd.test.match.CommonMatch.sameAs;

@TestInstance(Lifecycle.PER_CLASS)
class JpaEnvConfigNoCtxTest {
    static final String[] PROP_NAMES = {DATA_ENV_DRIVER_CLASS, DATA_ENV_URL, DATA_ENV_LOGIN, DATA_ENV_PASSWORD, DATA_ENV_DB, DATA_ENV_ID};
    static final String[] DS_PROP_NAMES = copyOfRange(PROP_NAMES, 0, 4);

    static final Map<String, String>       KEYS = collect(toMap(k -> k, 0), DS_PROP_NAMES);
    static final Map<String, Object>       VALS = collect(toMap(k -> k + "_val", 0), DS_PROP_NAMES);
    static final DataEnvironmentDefinition DEF = new DataEnvironmentDefinition(ID,
            collect(toMap(k -> DATA_ENV_ID.equals(k) ? ID : (k + "_val"), 0), PROP_NAMES));

    final PersistenceUnitProvider providerMock = mock(PersistenceUnitProvider.class);
    final JpaEnvConfig instance = mock(JpaEnvConfig.class, withSettings()
            .spiedInstance(new JpaEnvConfig(providerMock))
            .defaultAnswer(RETURNS_DEEP_STUBS));

    @Test
    void testNewJpaEnvironment() {
        // GIVEN
        DataSource ds = mock(DataSource.class);
        EntityManagerFactory emf = mock(EntityManagerFactory.class);
        PlatformTransactionManager tm = mock(PlatformTransactionManager.class);
        // AND
        when(providerMock.getDataSource(VALS)).thenReturn(ds);
        when(providerMock.getEntityManagerFactory(same(ds), eq(properties(
                DATA_ENV_DB, DATA_ENV_DB + "_val",
                DATA_ENV_ID, ID
        ).toMap()))).thenReturn(emf);
        // AND
        when(instance.getPropertyMappings()).thenReturn(KEYS);
        when(instance.newJpaEnvironment(same(DEF))).thenCallRealMethod();

        // WHEN
        JpaEnvironment result = instance.newJpaEnvironment(DEF);

        /* THEN */ assertThat(
        result.getDefinition()          , sameAs(DEF),
        result.getDataSource()          , sameAs(ds),
        result.getEntityManagerFactory(), sameAs(emf));
    }

    @Test
    @SuppressWarnings("unchecked,ConstantConditions")
    void testGetQueryAdapter() throws Exception {
        // GIVEN
        when(instance.dataEnvironmentService().doWith(eq(ID), same(JpaEnvironment.class), any(Function.class)))
                .thenAnswer(i -> i.getArgument(2, Function.class).apply(instance.newJpaEnvironment(DEF)));
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
        Object target = advised.getTargetSource().getTarget(); Object[] th = new Object[1];
        assertEach("Check target", () ->
        assertNotNull(getField(target, "jpaEnv"), "no JPA environment"), () ->
        assertNotNull(th[0] = getField(target, "transformer"), "no result transformer"), () ->
        assertSame(instance.resultTransformer(), getField(th[0], "delegate"), "wrong result transformer"));
    }
}