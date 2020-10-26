package ru.serge2nd.octopussy.service;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import ru.serge2nd.octopussy.service.ex.DataEnvironmentException;
import ru.serge2nd.octopussy.spi.DataEnvironment;
import ru.serge2nd.octopussy.support.DataEnvironmentDefinition;
import ru.serge2nd.test.util.ToRun;

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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.*;
import static org.mockito.internal.util.collections.Sets.newSet;
import static ru.serge2nd.stream.CommonCollectors.toList;
import static ru.serge2nd.stream.util.Collecting.collect;
import static ru.serge2nd.test.Asserting.assertEach;
import static ru.serge2nd.test.matcher.AssertThat.assertThat;
import static ru.serge2nd.test.matcher.CommonMatch.absent;
import static ru.serge2nd.test.matcher.CommonMatch.equalTo;
import static ru.serge2nd.test.matcher.CommonMatch.fails;
import static ru.serge2nd.test.matcher.CommonMatch.presentedSame;
import static ru.serge2nd.test.matcher.CommonMatch.sameAs;

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

        // THEN
        assertThat(result, sameAs(EXISTING), () ->
        assertEquals(INITIAL, repository, "repository should remain unchanged"));
    }

    @Test
    void testGetNotFound() {
        assertThat(()->dataEnvService.get(ID1), fails(DataEnvironmentException.NotFound.class), () ->
        assertEquals(INITIAL, repository, "repository should remain unchanged"));
    }

    @Test
    void testFind() {
        // GIVEN
        repository.put(ID1, DATA_ENV);

        // WHEN
        Optional<DataEnvironment> result = dataEnvService.find(ID1);

        // THEN
        assertThat(result, presentedSame(DATA_ENV), () ->
        assertEquals(new HashMap<String, DataEnvironment>() {{
            put(ID1, DATA_ENV);
            put(ID2, EXISTING);
        }}, repository, "repository should keep all elements"));
    }

    @Test
    void testFindNotFound() {
        // WHEN
        Optional<DataEnvironment> result = dataEnvService.find(ID1);

        // THEN
        assertThat(result, absent(), () ->
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

        // THEN
        assertThat(result, equalTo(expected), () ->
        assertEquals(INITIAL, repository, "repository should remain unchanged"));
    }

    @Test
    void testDoWithNotFound() {
        assertThat(()->dataEnvService.doWith(ID1, $ -> $), fails(DataEnvironmentException.NotFound.class));
    }

    @Test
    void testCreate() {
        // GIVEN
        DataEnvironment expected = mock(DataEnvironment.class);
        when(prototypeMock.toBuilder().definition(same(DATA_ENV.getDefinition())).build()).thenReturn(expected);

        // WHEN
        DataEnvironment created = dataEnvService.create(DATA_ENV);

        // THEN
        assertThat(created, sameAs(expected), () ->
        assertEquals(new HashMap<String, DataEnvironment>() {{
            put(ID1, created);
            put(ID2, EXISTING);
        }}, repository, "repository should keep all elements"));
    }

    @Test
    void testCreateAlreadyExists() {
        assertThat(()->dataEnvService.create(dataEnv(ID2)), fails(DataEnvironmentException.Exists.class), () ->
        assertEquals(INITIAL, repository, "repository should remain unchanged"));
    }

    @Test
    void testDelete() {
        // GIVEN
        repository.put(ID1, DATA_ENV);

        // WHEN
        dataEnvService.delete(ID2);

        // THEN
        InOrder inOrder = inOrder(preClose, EXISTING); assertEach(() ->
        assertEquals(singletonMap(ID1, DATA_ENV), repository, "expected one element left"), () ->
        inOrder.verify(preClose, times(1)).apply(same(EXISTING)), () ->
        inOrder.verify(EXISTING, times(1)).close());
    }

    @Test
    void testDeleteNotFound() {
        assertThat(()->dataEnvService.delete(ID1), fails(DataEnvironmentException.NotFound.class), () ->
        assertEquals(INITIAL, repository, "repository should remain unchanged"));
    }

    @Test
    void testLockOnCreate() {
        CountDownLatch freeLocks = new CountDownLatch(1);
        doAnswer(i -> waitOn(dataEnvService, freeLocks)).when(prototypeMock).toBuilder();
        runAsyncAndDelayOn(dataEnvService, () -> dataEnvService.create(DATA_ENV), freeLocks);

        assertTimeoutAndRelease(() ->
            supplyAsync(() -> dataEnvService
            .doWith(ID1, e -> e))
            .get(TIMEOUT, MILLISECONDS), dataEnvService);
    }

    @Test
    void testLockOnDoWith() {
        CountDownLatch freeLocks = new CountDownLatch(1);
        runAsyncAndDelayOn(dataEnvService, () ->
                dataEnvService.doWith(ID2, $ -> waitOn(dataEnvService, freeLocks)), freeLocks);

        assertTimeoutAndRelease(() ->
            runAsync(() -> dataEnvService
            .delete(ID2))
            .get(TIMEOUT, MILLISECONDS), dataEnvService);
    }

    @Test
    void testLockOnDelete() {
        CountDownLatch freeLocks = new CountDownLatch(1);
        doAnswer(i -> waitOn(dataEnvService, freeLocks)).when(EXISTING).close();
        runAsyncAndDelayOn(dataEnvService, () -> dataEnvService.delete(ID2), freeLocks);

        assertTimeoutAndRelease(() ->
            supplyAsync(() -> dataEnvService
            .create(EXISTING))
            .get(TIMEOUT, MILLISECONDS), dataEnvService);
    }

    static void runAsyncAndDelayOn(Object lock, Runnable task, CountDownLatch start) {
        runAsync(task); try {
            start.await(10*TIMEOUT, MILLISECONDS);
            synchronized (lock) { sleep(1); }
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

    static void assertTimeoutAndRelease(ToRun e, Object lock) {
        try {
            assertThat(e, fails(TimeoutException.class));
        } finally {
            synchronized (lock) { lock.notifyAll(); }
        }
    }

    static DataEnvironment dataEnv(String envId) {
        return new JpaEnvironmentImpl(
                DataEnvironmentDefinition.builder()
                        .envId(envId)
                        .property("database", "H2")
                        .build(),
                null, null);
    }

    static class JpaEnvironmentImpl extends ru.serge2nd.octopussy.support.JpaEnvironmentImpl {
        JpaEnvironmentImpl(DataEnvironmentDefinition definition, DataSource dataSource, EntityManagerFactory entityManagerFactory) {
            super(definition, dataSource, entityManagerFactory);
        }
    }
}
