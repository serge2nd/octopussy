package ru.serge2nd.octopussy.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import ru.serge2nd.octopussy.service.ex.DataEnvironmentClosedException;
import ru.serge2nd.octopussy.spi.DataEnvironment;
import ru.serge2nd.octopussy.spi.DataSourceProvider;
import ru.serge2nd.octopussy.support.DataEnvironmentDefinition;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
public class DataEnvironmentProxyTest {
    static final String ID = "5000";
    static final DataEnvironmentDefinition DEF = DataEnvironmentDefinition.builder().envId(ID).build();

    @Mock DataEnvironment dataEnvMock;
    @Mock(answer = RETURNS_DEEP_STUBS) DataSourceProvider providerMock;
    DataEnvironmentProxy proxy;

    @BeforeEach void setUp() { proxy = new DataEnvironmentProxy(DEF, providerMock); }

    @Test
    void testGetTargetCheckOpen() {
        // GIVEN
        setField(proxy, "closed", true);

        // WHEN
        Throwable thrown = catchThrowable(proxy::getTarget);

        // THEN
        assertTrue(thrown instanceof DataEnvironmentClosedException, "target should die when closed");
        assertNull(getField(proxy, "target"), "should not set target");
        verifyNoInteractions(providerMock);
    }

    @Test
    void testGetTargetSecondCheckOpen() {
        // GIVEN
        enableWorker(() -> setField(proxy, "closed", true), proxy);

        // WHEN
        Throwable thrown = catchThrowable(proxy::getTarget);

        // THEN
        assertTrue(thrown instanceof DataEnvironmentClosedException, "target should die when closed");
        assertNull(getField(proxy, "target"), "should not set target");
    }

    @Test
    void testGetTargetCheckProxyActive() {
        // GIVEN
        enableWorker(() -> {}, dataEnvMock);

        // WHEN
        Throwable thrown = catchThrowable(proxy::getTarget);

        // THEN
        assertTrue(thrown instanceof IllegalStateException, "inactive proxy cannot work");
        assertNull(getField(proxy, "target"), "should not set target");
        assertFalse(proxy.isClosed(), "closed unexpectedly");
    }

    @Test
    void testGetTargetNotNull() {
        // GIVEN
        enableWorker(() -> {}, proxy);
        setField(proxy, "target", dataEnvMock);

        // WHEN
        DataEnvironment target = proxy.getTarget();

        // THEN
        assertSame(dataEnvMock, target, "expected proxy target");
        assertFalse(proxy.isClosed(), "closed unexpectedly");
    }

    @Test
    void testGetTargetNull() {
        // GIVEN
        enableWorker(() -> {}, proxy);
        when(providerMock.getDataEnvironment(same(proxy.getDefinition()))).thenReturn(dataEnvMock);

        // WHEN
        DataEnvironment target = proxy.getTarget();

        // THEN
        assertSame(dataEnvMock, target, "expected proxy target");
        assertFalse(proxy.isClosed(), "closed unexpectedly");
    }

    @Test
    void testClose() {
        // GIVEN
        enableWorker(() -> {}, proxy);
        setField(proxy, "target", dataEnvMock);

        // WHEN
        proxy.close();

        // THEN
        assertTrue(proxy.isClosed(), "not closed");
        verify(dataEnvMock, times(1)).close();
    }

    @Test
    void testCloseNullTarget() {
        // GIVEN
        enableWorker(() -> {}, proxy);

        // WHEN
        proxy.close();

        // THEN
        assertTrue(proxy.isClosed(), "not closed");
        assertNull(getField(proxy, "target"), "should not set target");
    }

    @Test
    void testCloseCheckProxyActive() {
        // GIVEN
        enableWorker(() -> {}, dataEnvMock);
        setField(proxy, "target", dataEnvMock);

        // WHEN
        Throwable thrown = catchThrowable(() -> proxy.close());

        // THEN
        assertTrue(thrown instanceof IllegalStateException, "inactive proxy cannot be closed");
        assertFalse(proxy.isClosed(), "closed unexpectedly");
        verifyNoInteractions(dataEnvMock);
    }

    @Test
    void testAlreadyClosed() {
        // GIVEN
        setField(proxy, "target", dataEnvMock);
        setField(proxy, "closed", true);

        // WHEN
        proxy.close();

        // THEN
        assertTrue(proxy.isClosed(), "closing status was reset");
        verifyNoInteractions(dataEnvMock, providerMock);
    }

    @Test
    void testClosedInMiddle() {
        // GIVEN
        enableWorker(() -> setField(proxy, "closed", true), proxy);
        setField(proxy, "target", dataEnvMock);

        // WHEN
        proxy.close();

        // THEN
        assertTrue(proxy.isClosed(), "closing status was reset");
        verifyNoInteractions(dataEnvMock);
    }

    static Stream<Arguments> getterFromTargetProvider() {
        return Stream.of(
                arguments(DataSource.class.getSimpleName(), DataSource.class, f(DataEnvironment::getDataSource)),
                arguments(EntityManagerFactory.class.getSimpleName(), EntityManagerFactory.class, f(DataEnvironment::getEntityManagerFactory)),
                arguments(PlatformTransactionManager.class.getSimpleName(), PlatformTransactionManager.class, f(DataEnvironment::getTransactionManager)));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("getterFromTargetProvider")
    void testGetFromTarget(String title, Class<?> clazz, Function<DataEnvironment, Object> getter) {
        // GIVEN
        enableWorker(() -> {}, proxy);
        setField(proxy, "target", dataEnvMock);
        when(getter.apply(dataEnvMock)).thenReturn(mock(clazz));

        // WHEN
        Object result = getter.apply(proxy);

        // THEN
        assertNotNull(clazz.cast(result), "expected result from target");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("getterFromTargetProvider")
    void testGetFromDeadTarget(String title, Class<?> clazz, Function<DataEnvironment, Object> getter) {
        // GIVEN
        RuntimeException expected = new ArrayStoreException();
        mockWorker(i -> {throw expected;});
        setField(proxy, "target", dataEnvMock);

        // WHEN
        Throwable thrown = catchThrowable(() -> getter.apply(proxy));

        // THEN
        assertSame(expected, thrown, "expected error while getting target");
        verifyNoInteractions(dataEnvMock);
    }

    @SuppressWarnings("unchecked")
    void mockWorker(Consumer<InvocationOnMock> invocationConsumer) {
        when(providerMock.getDataEnvironmentService().doWith(eq(ID), any(Function.class)))
                .thenAnswer(i -> {invocationConsumer.accept(i); return null;});
    }
    @SuppressWarnings("unchecked")
    void enableWorker(Runnable pre, DataEnvironment arg) {
        when(providerMock.getDataEnvironmentService().doWith(eq(ID), any(Function.class)))
                .thenAnswer(i -> {
                    pre.run();
                    return i.getArgument(1, Function.class).apply(arg);
                });
    }

    static Function<DataEnvironment, Object> f(Function<DataEnvironment, Object> src) { return src; }
}
