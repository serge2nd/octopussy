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

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static ru.serge2nd.octopussy.support.DataEnvironmentDefinitionTest.DEF;
import static ru.serge2nd.octopussy.support.DataEnvironmentDefinitionTest.ID;

@ExtendWith(MockitoExtension.class)
@TestInstance(Lifecycle.PER_CLASS)
public class DataEnvironmentProxyTest {
    @Mock                              DataEnvironmentProxy dataEnvMock;
    @Mock(answer = RETURNS_DEEP_STUBS) DataEnvService serviceMock;
    @Mock(answer = RETURNS_DEEP_STUBS) DataEnvFactory factoryMock;
    DataEnvironmentProxy proxy;

    @BeforeEach void setUp() { proxy = new DataEnvironmentProxy(DEF, serviceMock, factoryMock); }

    @Test
    void testGetTargetCheckOpen() {
        // GIVEN
        setField(proxy, "closed", true);

        // WHEN
        Throwable error = catchThrowableOfType(proxy::getTarget, DataEnvironmentException.Closed.class);

        /* THEN */ assertAll(() ->
        assertNotNull(error, "target must die when closed"), () ->
        assertNull(getField(proxy, "target"), "must not set target"), () ->
        verifyNoInteractions(factoryMock));
    }

    @Test
    void testGetTargetSecondCheckOpen() {
        // GIVEN
        enableWorker(() -> setField(proxy, "closed", true), proxy);

        // WHEN
        Throwable error = catchThrowableOfType(proxy::getTarget, DataEnvironmentException.Closed.class);

        /* THEN */ assertAll(() ->
        assertNotNull(error, "target must die when closed"), () ->
        assertNull(getField(proxy, "target"), "must not set target"));
    }

    @Test
    void testGetTargetCheckProxyActive() {
        // GIVEN
        enableWorker(() -> {}, dataEnvMock);

        // WHEN
        Throwable error = catchThrowableOfType(proxy::getTarget, IllegalStateException.class);

        /* THEN */ assertAll(() ->
        assertNotNull(error, "inactive proxy cannot work"), () ->
        assertNull(getField(proxy, "target"), "must not set target"), () ->
        assertFalse(proxy.isClosed(), "closed unexpectedly"));
    }

    @Test
    void testGetTargetNotNull() {
        // GIVEN
        setField(proxy, "target", dataEnvMock);

        // WHEN
        DataEnvironment target = proxy.getTarget();

        /* THEN */ assertAll(() ->
        assertSame(dataEnvMock, target, "expected proxy target"), () ->
        assertFalse(proxy.isClosed(), "closed unexpectedly"));
    }

    @Test
    void testGetTargetNull() {
        // GIVEN
        enableWorker(() -> {}, proxy);
        when(factoryMock.apply(same(proxy.getDefinition()))).thenReturn(dataEnvMock);

        // WHEN
        DataEnvironment target = proxy.getTarget();

        /* THEN */ assertAll(() ->
        assertSame(dataEnvMock, target, "expected proxy target"), () ->
        assertFalse(proxy.isClosed(), "closed unexpectedly"));
    }

    @Test
    void testClose() {
        // GIVEN
        enableWorker(() -> {}, proxy);
        setField(proxy, "target", dataEnvMock);

        // WHEN
        proxy.close();

        /* THEN */ assertAll(() ->
        assertTrue(proxy.isClosed(), "not closed"), () ->
        verify(dataEnvMock, times(1)).close());
    }

    @Test
    void testCloseNullTarget() {
        // GIVEN
        enableWorker(() -> {}, proxy);

        // WHEN
        proxy.close();

        /* THEN */ assertAll(() ->
        assertTrue(proxy.isClosed(), "not closed"), () ->
        assertNull(getField(proxy, "target"), "must not set target"));
    }

    @Test
    void testCloseCheckProxyActive() {
        // GIVEN
        enableWorker(() -> {}, dataEnvMock);
        setField(proxy, "target", dataEnvMock);

        // WHEN
        Throwable error = catchThrowableOfType(proxy::close, IllegalStateException.class);

        /* THEN */ assertAll(() ->
        assertNotNull(error, "inactive proxy cannot be closed"), () ->
        assertFalse(proxy.isClosed(), "closed unexpectedly"), () ->
        verifyNoInteractions(dataEnvMock));
    }

    @Test
    void testAlreadyClosed() {
        // GIVEN
        setField(proxy, "target", dataEnvMock);
        setField(proxy, "closed", true);

        // WHEN
        proxy.close();

        /* THEN */ assertAll(() ->
        assertTrue(proxy.isClosed(), "closing status was reset"), () ->
        verifyNoInteractions(dataEnvMock, factoryMock));
    }

    @Test
    void testClosedInMiddle() {
        // GIVEN
        enableWorker(() -> setField(proxy, "closed", true), proxy);
        setField(proxy, "target", dataEnvMock);

        // WHEN
        proxy.close();

        /* THEN */ assertAll(() ->
        assertTrue(proxy.isClosed(), "closing status was reset"), () ->
        verifyNoInteractions(dataEnvMock));
    }

    @Test
    void testUnwrapTarget() {
        // GIVEN
        Integer expected = 9;
        setField(proxy, "target", dataEnvMock);
        when(dataEnvMock.unwrap(expected.getClass())).thenAnswer($->expected);

        // WHEN
        Integer result = proxy.unwrap(expected.getClass());

        // THEN
        assertEquals(expected, result, "expected result from target");
    }

    @Test
    void testUnwrapDeadTarget() {
        // GIVEN
        RuntimeException expected = new ArrayStoreException();
        mockWorker(i -> {throw expected;});

        // WHEN
        Throwable error = catchThrowable(() -> proxy.unwrap(null));

        /* THEN */ assertAll(() ->
        assertSame(expected, error, "expected error while getting target"), () ->
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
