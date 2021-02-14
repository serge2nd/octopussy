package ru.serge2nd.octopussy.service;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import ru.serge2nd.octopussy.service.ex.DataKitException;
import ru.serge2nd.octopussy.spi.DataKit;
import ru.serge2nd.octopussy.support.DataKitDefinition;
import ru.serge2nd.test.util.ToRun;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import static java.util.Collections.singletonMap;
import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.*;
import static org.mockito.internal.util.collections.Sets.newSet;
import static ru.serge2nd.collection.HardProperties.properties;
import static ru.serge2nd.stream.CommonCollectors.toList;
import static ru.serge2nd.stream.util.Collecting.collect;
import static ru.serge2nd.test.Asserting.assertEach;
import static ru.serge2nd.test.match.AssertThat.assertThat;
import static ru.serge2nd.test.match.CoreMatch.absent;
import static ru.serge2nd.test.match.CoreMatch.equalTo;
import static ru.serge2nd.test.match.CoreMatch.fails;
import static ru.serge2nd.test.match.CoreMatch.presentedSame;
import static ru.serge2nd.test.match.CoreMatch.sameAs;

@SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
@TestInstance(Lifecycle.PER_CLASS)
class InMemoryDataKitsTest {
    static final String ID1 = "5010";
    static final String ID2 = "7010";
    static final int TIMEOUT = 300;

    final DataKit DATA_KIT = dataKit(ID1);
    final DataKit EXISTING = spy(dataKit(ID2));
    final Map<String, DataKit> INITIAL = singletonMap(ID2, EXISTING);
    final Map<String, DataKit> REPO    = new ConcurrentHashMap<>(INITIAL);

    final DataKit          prototypeMock  = mock(DataKit.class, RETURNS_DEEP_STUBS);
    final InMemoryDataKits dataKitService = new InMemoryDataKits(REPO, prototypeMock);

    @BeforeEach void setUp() {
        REPO.clear(); REPO.putAll(INITIAL);
        reset(EXISTING, prototypeMock);
    }

    @Test
    void testGet() {
        // WHEN
        DataKit result = dataKitService.get(ID2);

        /* THEN */ assertThat(
        result, sameAs(EXISTING),
        REPO  , equalTo(INITIAL));
    }

    @Test
    void testGetNotFound() {
        assertThat(
        ()-> dataKitService.get(ID1), fails(DataKitException.NotFound.class),
        REPO                        , equalTo(INITIAL));
    }

    @Test
    void testFind() {
        // GIVEN
        REPO.put(ID1, DATA_KIT);

        // WHEN
        Optional<DataKit> result = dataKitService.find(ID1);

        /* THEN */ assertThat(
        result, presentedSame(DATA_KIT),
        REPO  , equalTo(properties(ID1, DATA_KIT, ID2, EXISTING)));
    }

    @Test
    void testFindNotFound() {
        // WHEN
        Optional<DataKit> result = dataKitService.find(ID1);

        /* THEN */ assertThat(
        result, absent(),
        REPO  , equalTo(INITIAL));
    }

    @Test
    void testGetAll() {
        // GIVEN
        REPO.put(ID1, DATA_KIT);

        // WHEN
        List<DataKit> result = collect(dataKitService.getAll(), toList(0));

        // THEN
        assertEquals(newSet(DATA_KIT, EXISTING), new HashSet<>(result), "expected all from repository");
    }

    @Test
    void testDoWith() {
        // GIVEN
        Function<DataKit, String> action = dataKit -> dataKit.getDefinition().toString();
        String expected = EXISTING.getDefinition().toString();

        // WHEN
        String result = dataKitService.apply(ID2, action);

        /* THEN */ assertThat(
        result, equalTo(expected),
        REPO  , equalTo(INITIAL));
    }

    @Test
    void testDoWithNotFound() {
        assertThat(()->dataKitService.apply(ID1, $->$), fails(DataKitException.NotFound.class));
    }

    @Test
    void testCreate() {
        // GIVEN
        DataKit expected = mock(DataKit.class);
        when(prototypeMock.toBuilder()
                .definition(same(DATA_KIT.getDefinition())).build())
                .thenReturn(expected);

        // WHEN
        DataKit created = dataKitService.create(DATA_KIT);

        /* THEN */ assertThat(
        created, sameAs(expected),
        REPO   , equalTo(properties(ID1, created, ID2, EXISTING)));
    }

    @Test
    void testCreateAlreadyExists() {
        assertThat(
        ()->dataKitService.create(dataKit(ID2)), fails(DataKitException.Exists.class),
        REPO                                   , equalTo(INITIAL));
    }

    @Test
    void testDelete() {
        // GIVEN
        REPO.put(ID1, DATA_KIT);

        // WHEN
        dataKitService.delete(ID2);

        /* THEN */ assertEach(() ->
        assertEquals(singletonMap(ID1, DATA_KIT), REPO, "expected one element left"), () ->
        verify(EXISTING, times(1)).close());
    }

    @Test
    void testDeleteNotFound() {
        assertThat(
        ()->dataKitService.delete(ID1), fails(DataKitException.NotFound.class),
        REPO                          , equalTo(INITIAL));
    }

    @Test
    void testLockOnCreate() {
        CountDownLatch freeLocks = new CountDownLatch(1);
        doAnswer(i -> waitOn(dataKitService, freeLocks)).when(prototypeMock).toBuilder();
        runAsyncAndDelayOn(dataKitService, () -> dataKitService.create(DATA_KIT), freeLocks);

        assertTimeoutAndRelease(() ->
            supplyAsync(() -> dataKitService
            .apply(ID1, e -> e))
            .get(TIMEOUT, MILLISECONDS), dataKitService);
    }

    @Test
    void testLockOnDoWith() {
        CountDownLatch freeLocks = new CountDownLatch(1);
        runAsyncAndDelayOn(dataKitService, () ->
            dataKitService.apply(ID2, $ -> waitOn(dataKitService, freeLocks)), freeLocks);

        assertTimeoutAndRelease(() ->
            runAsync(() -> dataKitService
            .delete(ID2))
            .get(TIMEOUT, MILLISECONDS), dataKitService);
    }

    @Test
    void testLockOnDelete() {
        CountDownLatch freeLocks = new CountDownLatch(1);
        doAnswer(i -> waitOn(dataKitService, freeLocks)).when(EXISTING).close();
        runAsyncAndDelayOn(dataKitService, () -> dataKitService.delete(ID2), freeLocks);

        assertTimeoutAndRelease(() ->
            supplyAsync(() -> dataKitService
            .create(EXISTING))
            .get(TIMEOUT, MILLISECONDS), dataKitService);
    }

    @SneakyThrows
    static void runAsyncAndDelayOn(Object lock, Runnable task, CountDownLatch start) {
        runAsync(task);
        assertTrue(start.await(10*TIMEOUT, MILLISECONDS), "unexpected timeout on start");
        synchronized (lock) {}
    }
    @SneakyThrows
    static <T> T waitOn(Object lock, CountDownLatch freeLocks) {
        synchronized (lock) {
            freeLocks.countDown();
            lock.wait(TIMEOUT + 200);
        } return null;
    }
    static void assertTimeoutAndRelease(ToRun e, Object lock) {
        try     { assertThat(e, fails(TimeoutException.class)); }
        finally { synchronized(lock) {lock.notifyAll();} }
    }

    static DataKit dataKit(String kitId) {
        return new JpaKitImpl(
            DataKitDefinition.builder()
                .kitId(kitId)
                .property("database", "H2")
                .build(),
        null, null);
    }

    static class JpaKitImpl extends ru.serge2nd.octopussy.support.JpaKitImpl {
        JpaKitImpl(DataKitDefinition definition, DataSource dataSource, EntityManagerFactory entityManagerFactory) {
            super(definition, dataSource, entityManagerFactory);
        }
    }
}
