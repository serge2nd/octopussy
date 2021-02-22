package ru.serge2nd.octopussy.config;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.aop.framework.Advised;
import org.springframework.cache.Cache;
import org.springframework.transaction.interceptor.TransactionalProxy;
import ru.serge2nd.octopussy.spi.NativeQueryAdapter;
import ru.serge2nd.octopussy.support.NativeQueryAdapterImpl;

import javax.persistence.EntityManagerFactory;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;
import static org.springframework.aop.support.AopUtils.isJdkDynamicProxy;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static ru.serge2nd.octopussy.support.DataKitDefinitionTest.ID;
import static ru.serge2nd.test.Asserting.assertEach;

@TestInstance(Lifecycle.PER_CLASS)
@SuppressWarnings("unchecked")
class JpaKitConfigNoCtxTest {
    final PersistenceUnitProvider pUnitsMock = mock(PersistenceUnitProvider.class);
    final EntityManagerFactory    emfMock    = mock(EntityManagerFactory.class);
    final Cache                   cacheMock  = mock(Cache.class);

    final JpaKitConfig cfg = mock(JpaKitConfig.class, withSettings()
            .spiedInstance(new JpaKitConfig(pUnitsMock, cacheMock))
            .defaultAnswer(RETURNS_DEEP_STUBS));

    @BeforeAll void beforeAll() {
        when(cfg.dataKitService().on(eq(ID), same(EntityManagerFactory.class), any(Function.class)))
                .thenAnswer(i -> i.getArgument(2, Function.class).apply(emfMock));
        when(cfg.getQueryAdapter(ID)).thenCallRealMethod();
    }
    @BeforeEach void setUp() { reset(cacheMock); }

    @Test
    @SuppressWarnings("ConstantConditions")
    void testGetQueryAdapter() throws Exception {
        // WHEN
        NativeQueryAdapter result = cfg.getQueryAdapter(ID);

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
        assertSame(pUnitsMock.getResultTransformer(), getField(target, "transformer"), "wrong result transformer"), () ->
        verify(cacheMock, times(1)).putIfAbsent(eq(ID), same(result)));
    }

    @Test
    void testGetQueryAdapterCache() {
        // GIVEN
        NativeQueryAdapter cached = mock(NativeQueryAdapter.class);
        when(cacheMock.get(ID, NativeQueryAdapter.class)).thenReturn(cached);

        // WHEN
        NativeQueryAdapter result = cfg.getQueryAdapter(ID);

        // THEN
        assertSame(cached, result, "expected cached");
    }
}