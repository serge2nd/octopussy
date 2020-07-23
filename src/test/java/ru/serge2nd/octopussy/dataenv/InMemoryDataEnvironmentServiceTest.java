package ru.serge2nd.octopussy.dataenv;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.serge2nd.octopussy.dataenv.InMemoryDataEnvironmentService.DataEnvironmentProxy;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.UnaryOperator;

import static java.lang.Thread.sleep;
import static java.util.Collections.singletonMap;
import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.internal.util.collections.Sets.newSet;

@ExtendWith(MockitoExtension.class)
class InMemoryDataEnvironmentServiceTest {
    static final String ID1 = "5010";
    static final String ID2 = "7010";

    InMemoryDataEnvironmentService dataEnvService;
    Map<String, DataEnvironment> repository;
    final DataEnvironment existing = spy(dataEnv(ID2));

    @BeforeEach
    void setUp() {
        repository = new ConcurrentHashMap<>(singletonMap(ID2, existing));
        dataEnvService = new InMemoryDataEnvironmentService(null, () -> repository);
    }

    @Test
    void testGet() {
        // WHEN
        DataEnvironment result = dataEnvService.get(ID2);

        // THEN
        assertSame(existing, result, "expected one from repository");
        assertEquals(singletonMap(ID2, existing), repository, "repository should remain unchanged");
    }

    @Test
    void testGetNotFound() {
        // WHEN
        Throwable thrown = catchThrowable(() -> dataEnvService.get(ID1));

        // THEN
        assertTrue(thrown instanceof DataEnvironmentNotFoundException, "expected error due to absence");
        assertEquals(singletonMap(ID2, existing), repository, "repository should remain unchanged");
    }

    @Test
    void testFind() {
        // GIVEN
        DataEnvironment dataEnv = dataEnv(ID1);
        repository.put(ID1, dataEnv);

        // WHEN
        Optional<DataEnvironment> result = dataEnvService.find(ID1);

        // THEN
        assertTrue(result.isPresent(), "should be presented");
        assertSame(dataEnv, result.get(), "expected one from repository");
        // AND
        assertEquals(new HashMap<String, DataEnvironment>() {{
            put(ID1, dataEnv);
            put(ID2, existing);
        }}, repository, "repository should keep all elements");
    }

    @Test
    void testFindNotFound() {
        // WHEN
        Optional<DataEnvironment> result = dataEnvService.find(ID1);

        // THEN
        assertFalse(result.isPresent(), "should be empty if not found");
        assertEquals(singletonMap(ID2, existing), repository, "repository should remain unchanged");
    }

    @Test
    void testGetAll() {
        // GIVEN
        DataEnvironment dataEnv = dataEnv(ID1);
        repository.put(ID1, dataEnv);

        // WHEN
        Collection<DataEnvironment> result = dataEnvService.getAll();

        // THEN
        assertEquals(newSet(dataEnv, existing), new HashSet<>(result), "expected all from repository");
    }

    @Test
    void testCreate() {
        // GIVEN
        DataEnvironment existing = repository.get(ID2);
        DataEnvironment toCreate = dataEnv(ID1);

        // WHEN
        DataEnvironment created = dataEnvService.create(toCreate);

        // THEN
        assertTrue(created instanceof DataEnvironmentProxy, "expected a proxy");
        assertSame(toCreate.getDefinition(), created.getDefinition(), "expected same definition");
        // AND
        assertEquals(new HashMap<String, DataEnvironment>() {{
            put(ID1, created);
            put(ID2, existing);
        }}, repository, "repository should keep all elements");
    }

    @Test
    void testCreateAlreadyExists() {
        // WHEN
        Throwable thrown = catchThrowable(() -> dataEnvService.create(dataEnv(ID2)));

        // THEN
        assertTrue(thrown instanceof DataEnvironmentExistsException, "expected already exists");
        assertEquals(singletonMap(ID2, existing), repository, "repository should remain unchanged");
    }

    @Test
    void testDelete() {
        // GIVEN
        DataEnvironment dataEnv = dataEnv(ID1);
        repository.put(ID1, dataEnv);

        // WHEN
        dataEnvService.delete(ID2);

        // THEN
        assertEquals(singletonMap(ID1, dataEnv), repository, "expected one element left");
        verify(existing, times(1)).close();
    }

    @Test
    void testDeleteNotFound() {
        // WHEN
        Throwable thrown = catchThrowable(() -> dataEnvService.delete(ID1));

        // THEN
        assertTrue(thrown instanceof DataEnvironmentNotFoundException, "expected not found");
        assertEquals(singletonMap(ID2, existing), repository, "repository should remain unchanged");
    }

    @Test
    @SuppressWarnings("unchecked")
    void testLockOnCreate() throws InterruptedException, ExecutionException {
        // GIVEN
        CountDownLatch freeLocks = new CountDownLatch(1);
        InMemoryDataEnvironmentService dataEnvService = spy(this.dataEnvService);
        // AND
        doAnswer(i -> this.dataEnvService.compute(ID1, $ -> waitOn(dataEnvService, freeLocks)))
                .when(dataEnvService).compute(anyString(), any(UnaryOperator.class));
        // AND
        runAsync(() -> dataEnvService.create(dataEnv(ID1)));
        delayOn(dataEnvService, freeLocks);

        // WHEN
        Future<Void> result = runAsync(
                () -> dataEnvService.delete(ID1));

        // THEN
        try {
            result.get(2, SECONDS);
            fail("lack of lock");
        } catch (TimeoutException e) { // NOOP
        } finally { notifyOn(dataEnvService);}
    }

    @Test
    void testLockOnDelete() throws InterruptedException, ExecutionException {
        // GIVEN
        CountDownLatch freeLocks = new CountDownLatch(1);
        DataEnvironment dataEnvMock = mock(DataEnvironment.class);
        repository.put(ID1, dataEnvMock);
        // AND
        doAnswer(i -> waitOn(dataEnvMock, freeLocks)).when(dataEnvMock).close();
        // AND
        runAsync(() -> dataEnvService.delete(ID1));
        delayOn(dataEnvMock, freeLocks);

        // WHEN
        Future<DataEnvironment> result = supplyAsync(
                () -> dataEnvService.create(dataEnv(ID1)));

        // THEN
        try {
            result.get(2, SECONDS);
            fail("lack of lock");
        } catch (TimeoutException e) { // NOOP
        } finally { notifyOn(dataEnvMock); }
    }

    static void delayOn(Object lock, CountDownLatch start) {
        try {
            start.await();
            synchronized (lock) { sleep(100); }
        } catch (InterruptedException e) {
            fail("unexpected interruption", e);
        }
    }

    static <T> T waitOn(Object lock, CountDownLatch freeLocks) {
        synchronized (lock) { try {
            freeLocks.countDown();
            lock.wait();
        } catch (InterruptedException e) {
            fail("unexpected interruption", e);
        }} return null;
    }

    static void notifyOn(Object lock) {
        synchronized (lock) { lock.notifyAll(); }
    }

    static DataEnvironment dataEnv(String envId) {
        return new DataEnvironmentImpl(
                DataEnvironmentDefinition.builder().envId(envId).build(),
                null, null, null);
    }
}
