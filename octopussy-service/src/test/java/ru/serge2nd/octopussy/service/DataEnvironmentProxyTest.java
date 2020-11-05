package ru.serge2nd.octopussy.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.serge2nd.octopussy.service.ex.DataEnvironmentException;
import ru.serge2nd.octopussy.spi.DataEnvironment;
import ru.serge2nd.octopussy.spi.DataEnvironmentService;
import ru.serge2nd.octopussy.support.DataEnvironmentDefinition;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertSame;
import static ru.serge2nd.octopussy.service.Matchers.extractsTarget;
import static ru.serge2nd.octopussy.service.Matchers.noTarget;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.*;
import static ru.serge2nd.octopussy.service.Matchers.isClosed;
import static ru.serge2nd.octopussy.service.Matchers.isOpen;
import static ru.serge2nd.octopussy.support.DataEnvironmentDefinitionTest.DEF;
import static ru.serge2nd.octopussy.support.DataEnvironmentDefinitionTest.ID;
import static ru.serge2nd.test.matcher.AssertThat.assertThat;
import static ru.serge2nd.test.matcher.CommonMatch.fails;
import static ru.serge2nd.test.matcher.CommonMatch.illegalState;

@ExtendWith(MockitoExtension.class)
@TestInstance(Lifecycle.PER_CLASS)
public class DataEnvironmentProxyTest {
    @Mock                              DataEnvironmentProxy dataEnvMock;
    @Mock(answer = RETURNS_DEEP_STUBS) DataEnvService serviceMock;
    @Mock(answer = RETURNS_DEEP_STUBS) DataEnvFactory factoryMock;
    DataEnvironmentProxy proxy;

    @BeforeEach void setUp() { proxy = new DataEnvironmentProxy(DEF, serviceMock, factoryMock); }

    @Test void testGetTargetCheckOpen() {
        proxy.closed = true;

        assertThat(
        proxy::getTarget, fails(DataEnvironmentException.Closed.class),
        proxy           , noTarget()                                  , () ->
        verifyNoInteractions(factoryMock));
    }

    @Test void testGetTargetSecondCheckOpen() {
        enableWorker(() -> proxy.closed = true, proxy);

        assertThat(
        proxy::getTarget, fails(DataEnvironmentException.Closed.class),
        proxy           , noTarget());
    }

    @Test void testGetTargetCheckProxyActive() {
        enableWorker(() -> {}, dataEnvMock);

        assertThat(
        proxy::getTarget, illegalState(),
        proxy           , isOpen(),
        proxy           , noTarget());
    }

    @Test void testGetTargetNotNull() {
        proxy.target = dataEnvMock;
        assertThat(proxy, extractsTarget(dataEnvMock), isOpen());
    }

    @Test void testGetTargetNull() {
        enableWorker(() -> {}, proxy);
        when(factoryMock.apply(same(proxy.getDefinition()))).thenReturn(dataEnvMock);
        assertThat(proxy, extractsTarget(dataEnvMock), isOpen());
    }

    @Test void testClose() {
        // GIVEN
        enableWorker(() -> {}, proxy);
        proxy.target = dataEnvMock;

        // WHEN
        proxy.close();

        /* THEN */
        assertThat(proxy, isClosed(), () ->
        verify(dataEnvMock, times(1)).close());
    }

    @Test void testCloseNullTarget() {
        // GIVEN
        enableWorker(() -> {}, proxy);

        // WHEN
        proxy.close();

        // THEN
        assertThat(proxy, isClosed(), noTarget());
    }

    @Test void testCloseCheckProxyActive() {
        enableWorker(() -> {}, dataEnvMock);
        proxy.target = dataEnvMock;

        assertThat(
        proxy::close, illegalState(),
        proxy       , isOpen(), () ->
        verifyNoInteractions(dataEnvMock));
    }

    @Test void testAlreadyClosed() {
        // GIVEN
        proxy.target = dataEnvMock;
        proxy.closed = true;

        // WHEN
        proxy.close();

        // THEN
        assertThat(proxy, isClosed(), () ->
        verifyNoInteractions(dataEnvMock, factoryMock));
    }

    @Test void testClosedInMiddle() {
        // GIVEN
        enableWorker(() -> proxy.closed = true, proxy);
        proxy.target = dataEnvMock;

        // WHEN
        proxy.close();

        // THEN
        assertThat(proxy, isClosed(), () ->
        verifyNoInteractions(dataEnvMock));
    }

    @Test void testUnwrapSelf() {
        assertSame(proxy, proxy.unwrap(DataEnvironmentProxy.class), "expected proxy itself");
    }

    @Test void testUnwrapTarget() {
        // GIVEN
        Double expected = Math.PI;
        proxy.target = dataEnvMock;
        when(dataEnvMock.unwrap(expected.getClass())).thenAnswer($->expected);

        // WHEN
        Double result = proxy.unwrap(expected.getClass());

        // THEN
        assertSame(expected, result, "expected result from target");
    }

    @Test void testUnwrapDeadTarget() {
        // GIVEN
        RuntimeException expected = new ArrayStoreException();
        mockWorker(i -> {throw expected;});

        /* THEN */
        assertThat(()->proxy.unwrap(null), fails(expected), () ->
        verifyNoInteractions(dataEnvMock));
    }

    @SuppressWarnings("unchecked")
    void mockWorker(Consumer<InvocationOnMock> invocationConsumer) {
        when(serviceMock.get().doWith(eq(ID), same(DataEnvironmentProxy.class), any(Function.class)))
        .thenAnswer(i -> {invocationConsumer.accept(i); return null;});
    }
    @SuppressWarnings("unchecked")
    void enableWorker(Runnable pre, DataEnvironment arg) {
        when(serviceMock.get().doWith(eq(ID), same(DataEnvironmentProxy.class), any(Function.class)))
        .thenAnswer(i -> {
            pre.run();
            return i.getArgument(2, Function.class).apply(arg);
        });
    }

    interface DataEnvService extends Supplier<DataEnvironmentService> {}
    interface DataEnvFactory extends Function<DataEnvironmentDefinition, DataEnvironment> {}
}
