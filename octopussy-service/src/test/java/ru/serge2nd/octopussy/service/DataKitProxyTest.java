package ru.serge2nd.octopussy.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.serge2nd.octopussy.service.ex.DataKitException;
import ru.serge2nd.octopussy.spi.DataKit;
import ru.serge2nd.octopussy.spi.DataKitService;
import ru.serge2nd.octopussy.support.DataKitDefinition;

import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertSame;
import static ru.serge2nd.octopussy.service.Matchers.extractsTarget;
import static ru.serge2nd.octopussy.service.Matchers.noTarget;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.*;
import static ru.serge2nd.octopussy.service.Matchers.isClosed;
import static ru.serge2nd.octopussy.service.Matchers.isOpen;
import static ru.serge2nd.octopussy.support.DataKitDefinitionTest.DEF;
import static ru.serge2nd.octopussy.support.DataKitDefinitionTest.ID;
import static ru.serge2nd.test.match.AssertThat.assertThat;
import static ru.serge2nd.test.match.CoreMatch.fails;
import static ru.serge2nd.test.match.CoreMatch.illegalState;

@ExtendWith(MockitoExtension.class)
@TestInstance(Lifecycle.PER_CLASS)
public class DataKitProxyTest {
    @Mock                              DataKitProxy dataKitMock;
    @Mock                              DataKitService serviceMock;
    @Mock(answer = RETURNS_DEEP_STUBS) DataKitFactory factoryMock;

    DataKitProxy proxy;

    @BeforeEach void setUp() { proxy = new DataKitProxy(DEF, serviceMock, factoryMock); }

    @Test void testGetTargetCheckOpen() {
        proxy.closed = true;

        assertThat(
        proxy::getTarget, fails(DataKitException.Closed.class),
        proxy           , noTarget()                          , () ->
        verifyNoInteractions(factoryMock));
    }

    @Test void testGetTargetSecondCheckOpen() {
        enableWorker(() -> proxy.closed = true, proxy);

        assertThat(
        proxy::getTarget, fails(DataKitException.Closed.class),
        proxy           , noTarget());
    }

    @Test void testGetTargetCheckProxyActive() {
        enableWorker(() -> {}, dataKitMock);

        assertThat(
        proxy::getTarget, illegalState(),
        proxy           , isOpen(),
        proxy           , noTarget());
    }

    @Test void testGetTargetNotNull() {
        proxy.target = dataKitMock;
        assertThat(proxy, extractsTarget(dataKitMock), isOpen());
    }

    @Test void testGetTargetNull() {
        enableWorker(() -> {}, proxy);
        when(factoryMock.apply(same(proxy.getDefinition()))).thenReturn(dataKitMock);
        assertThat(proxy, extractsTarget(dataKitMock), isOpen());
    }

    @Test void testClose() {
        // GIVEN
        enableWorker(() -> {}, proxy);
        proxy.target = dataKitMock;

        // WHEN
        proxy.close();

        // THEN
        assertThat(proxy, isClosed(), () ->
        verify(dataKitMock, times(1)).close());
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
        enableWorker(() -> {}, dataKitMock);
        proxy.target = dataKitMock;

        assertThat(
        proxy::close, illegalState(),
        proxy       , isOpen(), () ->
        verifyNoInteractions(dataKitMock));
    }

    @Test void testAlreadyClosed() {
        // GIVEN
        proxy.target = dataKitMock;
        proxy.closed = true;

        // WHEN
        proxy.close();

        // THEN
        assertThat(proxy, isClosed(), () ->
        verifyNoInteractions(dataKitMock, factoryMock));
    }

    @Test void testClosedInMiddle() {
        // GIVEN
        enableWorker(() -> proxy.closed = true, proxy);
        proxy.target = dataKitMock;

        // WHEN
        proxy.close();

        // THEN
        assertThat(proxy, isClosed(), () ->
        verifyNoInteractions(dataKitMock));
    }

    @Test void testUnwrapSelf() {
        assertSame(proxy, proxy.unwrap(DataKitProxy.class), "expected proxy itself");
    }

    @Test void testUnwrapTarget() {
        // GIVEN
        Double expected = Math.PI;
        proxy.target = dataKitMock;
        when(dataKitMock.unwrap(expected.getClass())).thenAnswer($->expected);

        // WHEN
        Double result = proxy.unwrap(expected.getClass());

        // THEN
        assertSame(expected, result, "expected result from target");
    }

    @Test void testUnwrapDeadTarget() {
        // GIVEN
        RuntimeException expected = new ArrayStoreException();
        mockWorker(i -> {throw expected;});

        // THEN
        assertThat(()->proxy.unwrap(DataKit.class), fails(expected), () ->
        verifyNoInteractions(dataKitMock));
    }

    @SuppressWarnings("unchecked")
    void mockWorker(Consumer<InvocationOnMock> invocationConsumer) {
        when(serviceMock.doWith(eq(ID), same(DataKitProxy.class), any(Function.class)))
        .thenAnswer(i -> {invocationConsumer.accept(i); return null;});
    }
    @SuppressWarnings("unchecked")
    void enableWorker(Runnable pre, DataKit arg) {
        when(serviceMock.doWith(eq(ID), same(DataKitProxy.class), any(Function.class)))
        .thenAnswer(i -> {
            pre.run();
            return i.getArgument(2, Function.class).apply(arg);
        });
    }

    interface DataKitFactory extends Function<DataKitDefinition, DataKit> {}
}
