package ru.serge2nd.octopussy.service;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.transaction.PlatformTransactionManager;
import ru.serge2nd.octopussy.App;
import ru.serge2nd.octopussy.service.ex.DataEnvironmentException;
import ru.serge2nd.octopussy.spi.DataEnvironment;
import ru.serge2nd.octopussy.support.DataEnvironmentDefinition;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static java.lang.Thread.sleep;
import static java.util.Collections.singletonMap;
import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.*;
import static org.mockito.internal.util.collections.Sets.newSet;
import static ru.serge2nd.stream.CommonCollectors.toList;
import static ru.serge2nd.stream.util.Collecting.collect;

@SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
class InMemoryDataEnvironmentServiceTest {
    static final String ID1 = "5010";
    static final String ID2 = "7010";
    static final int TIMEOUT = 300;

    final DataEnvironment DATA_ENV = dataEnv(ID1);
    final DataEnvironment EXISTING = spy(dataEnv(ID2));
    final Map<String, DataEnvironment> INITIAL = singletonMap(ID2, EXISTING);

    final Map<String, DataEnvironment> repository = new ConcurrentHashMap<>(INITIAL);
    @SuppressWarnings("unchecked")
    final UnaryOperator<DataEnvironment> preClose = mock(UnaryOperator.class, i -> i.getArgument(0));

    final DataEnvironment prototypeMock = mock(DataEnvironment.class, RETURNS_DEEP_STUBS);
    InMemoryDataEnvironmentService dataEnvService = new InMemoryDataEnvironmentService(repository, prototypeMock, preClose);

    @Test
    void testGet() {
        // WHEN
        DataEnvironment result = dataEnvService.get(ID2);

        /* THEN */ assertAll(() ->
        assertSame(EXISTING, result, "expected one from repository"), () ->
        assertEquals(INITIAL, repository, "repository should remain unchanged"));
    }

    @Test
    void testGetNotFound() {
        // WHEN
        Throwable error = catchThrowableOfType(() -> dataEnvService.get(ID1), DataEnvironmentException.NotFound.class);

        /* THEN */ assertAll(() ->
        assertNotNull(error, "expected error due to absence"), () ->
        assertEquals(INITIAL, repository, "repository should remain unchanged"));
    }

    @Test
    void testFind() {
        // GIVEN
        repository.put(ID1, DATA_ENV);

        // WHEN
        Optional<DataEnvironment> result = dataEnvService.find(ID1);

        /* THEN */ assertAll(() ->
        assertEquals(new HashMap<String, DataEnvironment>() {{
            put(ID1, DATA_ENV);
            put(ID2, EXISTING);
        }}, repository, "repository should keep all elements"), () ->
        // AND
        assertTrue(result.isPresent(), "should be presented"));
        assertSame(DATA_ENV, result.get(), "expected one from repository");
    }

    @Test
    void testFindNotFound() {
        // WHEN
        Optional<DataEnvironment> result = dataEnvService.find(ID1);

        /* THEN */ assertAll(() ->
        assertFalse(result.isPresent(), "should be empty if not found"), () ->
        assertEquals(INITIAL, repository, "repository should remain unchanged"));
    }

    @Test
    void testGetAll() {
        // GIVEN
        repository.put(ID1, DATA_ENV);

        // WHEN
        List<DataEnvironment> result = collect(dataEnvService.getAll(), toList(0));

        // THEN
        assertEquals(newSet(DATA_ENV, EXISTING), new HashSet<>(result), "expected all from repository");
    }

    @Test
    void testDoWith() {
        // GIVEN
        Function<DataEnvironment, String> action = dataEnv -> dataEnv.getDefinition().toString();
        String expected = EXISTING.getDefinition().toString();

        // WHEN
        String result = dataEnvService.doWith(ID2, action);

        /* THEN */ assertAll(() ->
        assertEquals(expected, result, "expected action result"), () ->
        assertEquals(INITIAL, repository, "repository should remain unchanged"));
    }

    @Test
    void testDoWithNotFound() {
        // WHEN
        Throwable error = catchThrowableOfType(() -> dataEnvService.doWith(ID1, $ -> $), DataEnvironmentException.NotFound.class);

        // THEN
        assertNotNull(error, "expected error due to absence");
    }

    @Test
    void testCreate() {
        // GIVEN
        DataEnvironment expected = mock(DataEnvironment.class);
        when(prototypeMock.toBuilder().definition(same(DATA_ENV.getDefinition())).build()).thenReturn(expected);

        // WHEN
        DataEnvironment created = dataEnvService.create(DATA_ENV);

        /* THEN */ assertAll(() ->
        assertSame(expected, created, "expected builder result"), () ->
        // AND
        assertEquals(new HashMap<String, DataEnvironment>() {{
            put(ID1, created);
            put(ID2, EXISTING);
        }}, repository, "repository should keep all elements"));
    }

    @Test
    void testCreateAlreadyExists() {
        // WHEN
        Throwable error = catchThrowableOfType(() -> dataEnvService.create(dataEnv(ID2)), DataEnvironmentException.Exists.class);

        /* THEN */ assertAll(() ->
        assertNotNull(error, "expected already exists"), () ->
        assertEquals(INITIAL, repository, "repository should remain unchanged"));
    }

    @Test
    void testDelete() {
        // GIVEN
        repository.put(ID1, DATA_ENV);

        // WHEN
        dataEnvService.delete(ID2);

        // THEN
        InOrder inOrder = inOrder(preClose, EXISTING); assertAll(() ->
        assertEquals(singletonMap(ID1, DATA_ENV), repository, "expected one element left"), () ->
        inOrder.verify(preClose, times(1)).apply(same(EXISTING)), () ->
        inOrder.verify(EXISTING, times(1)).close());
    }

    @Test
    void testDeleteNotFound() {
        // WHEN
        Throwable error = catchThrowableOfType(() -> dataEnvService.delete(ID1), DataEnvironmentException.NotFound.class);

        /* THEN */ assertAll(() ->
        assertNotNull(error, "expected not found"), () ->
        assertEquals(INITIAL, repository, "repository should remain unchanged"));
    }

    @Test
    void testLockOnCreate() {
        // GIVEN
        CountDownLatch freeLocks = new CountDownLatch(1);
        doAnswer(i -> waitOn(dataEnvService, freeLocks)).when(prototypeMock).toBuilder();
        runAsyncAndDelayOn(dataEnvService, () -> dataEnvService.create(DATA_ENV), freeLocks);

        // WHEN
        Throwable error = catchTimeoutAndRelease(() ->
                supplyAsync(() -> dataEnvService
                .doWith(ID1, e -> e))
                .get(TIMEOUT, MILLISECONDS), dataEnvService);

        // THEN
        assertNotNull(error, "expected deadlock");
    }

    @Test
    void testLockOnDoWith() {
        CountDownLatch freeLocks = new CountDownLatch(1);
        runAsyncAndDelayOn(dataEnvService, () ->
                dataEnvService.doWith(ID2, $ -> waitOn(dataEnvService, freeLocks)), freeLocks);

        // WHEN
        Throwable error = catchTimeoutAndRelease(() ->
                runAsync(() -> dataEnvService
                .delete(ID2))
                .get(TIMEOUT, MILLISECONDS), dataEnvService);

        // THEN
        assertNotNull(error, "expected deadlock");
    }

    @Test
    void testLockOnDelete() {
        // GIVEN
        CountDownLatch freeLocks = new CountDownLatch(1);
        doAnswer(i -> waitOn(dataEnvService, freeLocks)).when(EXISTING).close();
        runAsyncAndDelayOn(dataEnvService, () -> dataEnvService.delete(ID2), freeLocks);

        // WHEN
        Throwable error = catchTimeoutAndRelease(() ->
                supplyAsync(() -> dataEnvService
                .create(EXISTING))
                .get(TIMEOUT, MILLISECONDS), dataEnvService);

        // THEN
        assertNotNull(error, "expected deadlock");
    }

    static void runAsyncAndDelayOn(Object lock, Runnable task, CountDownLatch start) {
        runAsync(task); try {
            start.await(TIMEOUT, MILLISECONDS);
            synchronized (lock) { sleep(100); }
        } catch (InterruptedException e) {
            fail("unexpected interruption", e);
        }
    }

    static <T> T waitOn(Object lock, CountDownLatch freeLocks) {
        synchronized (lock) { try {
            freeLocks.countDown();
            lock.wait(TIMEOUT + 200);
        } catch (InterruptedException e) {
            fail("unexpected interruption", e);
        }} return null;
    }

    static Throwable catchTimeoutAndRelease(ThrowingCallable c, Object lock) {
        Throwable error; try {
            error = catchThrowableOfType(c, TimeoutException.class);
        } finally {
            synchronized (lock) { lock.notifyAll(); }
        }
        return error;
    }

    static DataEnvironment dataEnv(String envId) {
        return new JpaEnvironmentImpl(
                DataEnvironmentDefinition.builder()
                        .envId(envId)
                        .property(App.DATA_ENV_DB, "H2")
                        .build(),
                null, null, null);
    }

    static class JpaEnvironmentImpl extends ru.serge2nd.octopussy.support.JpaEnvironmentImpl {
        JpaEnvironmentImpl(DataEnvironmentDefinition definition, DataSource dataSource, EntityManagerFactory entityManagerFactory, PlatformTransactionManager transactionManager) {
            super(definition, dataSource, entityManagerFactory, transactionManager);
        }
    }
}
