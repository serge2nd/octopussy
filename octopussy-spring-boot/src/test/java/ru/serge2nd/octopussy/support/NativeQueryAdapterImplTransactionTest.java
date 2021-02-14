package ru.serge2nd.octopussy.support;

import org.hibernate.query.Query;
import org.hibernate.transform.ResultTransformer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.Cache;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import ru.serge2nd.octopussy.BaseContextTest;
import ru.serge2nd.octopussy.SpringBootSoftTest;
import ru.serge2nd.octopussy.TestCommonConfig;
import ru.serge2nd.octopussy.spi.DataKitExecutor;
import ru.serge2nd.octopussy.spi.DataKitService;
import ru.serge2nd.octopussy.spi.NativeQueryAdapter;
import ru.serge2nd.octopussy.spi.NativeQueryAdapterProvider;
import ru.serge2nd.octopussy.util.QueryWithParams;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManagerFactory;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Collections.emptyMap;
import static javax.persistence.SynchronizationType.SYNCHRONIZED;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Answers.RETURNS_SELF;
import static org.mockito.Mockito.*;
import static org.springframework.transaction.TransactionDefinition.*;
import static ru.serge2nd.octopussy.App.QUERY_ADAPTERS_CACHE;
import static ru.serge2nd.octopussy.support.DataKitDefinitionTest.DEF;
import static ru.serge2nd.octopussy.support.DataKitDefinitionTest.ID;
import static ru.serge2nd.octopussy.util.Queries.queries;
import static ru.serge2nd.test.Asserting.assertEach;

@SpringBootSoftTest
@ContextHierarchy({
    @ContextConfiguration(classes = TestCommonConfig.class)
})
@TestInstance(Lifecycle.PER_CLASS)
class NativeQueryAdapterImplTransactionTest implements BaseContextTest {
    @SuppressWarnings("SqlDialectInspection,SqlNoDataSourceInspection")
    static final String Q = "not executed";

    @Autowired NativeQueryAdapterProvider adapterProvider;
    @MockBean(name = "dataKitService") DataKitServiceMock serviceMock;
    @MockBean Function<EntityManagerFactory, PlatformTransactionManager> tmProviderMock;
    @Value("#{cacheManager.getCache('"+QUERY_ADAPTERS_CACHE+"')}") Cache queryAdaptersCache;

    @Mock(answer = RETURNS_DEEP_STUBS) EntityManagerFactory emfMock;
    @Mock(answer = RETURNS_DEEP_STUBS) PlatformTransactionManager tmMock;
    @Mock(answer = RETURNS_SELF) Query<Object> queryMock;

    JpaKitImpl jpaKit;

    @PostConstruct void init() { jpaKit = new JpaKitImpl(DEF, null, emfMock); }
    @BeforeEach void setUp() { queryAdaptersCache.clear(); reset(tmProviderMock, tmMock); }

    @Test
    void testGetQueryAdapter() {
        // GIVEN
        mockDataKitService(); mockTransactionManager();

        // WHEN
        NativeQueryAdapter queryAdapter = adapterProvider.getQueryAdapter(ID);

        // THEN
        assertSame(queryAdapter, queryAdaptersCache.get(ID, NativeQueryAdapter.class), "expected to be cached");
    }

    @Test
    void testGetQueryAdapterCached() {
        // GIVEN
        mockDataKitService(); mockTransactionManager();
        NativeQueryAdapter queryAdapterMock = mock(NativeQueryAdapter.class);
        queryAdaptersCache.put(ID, queryAdapterMock);

        // WHEN
        NativeQueryAdapter queryAdapter = adapterProvider.getQueryAdapter(ID);

        // THEN
        assertSame(queryAdapterMock, queryAdapter, "expected cached object");
    }

    @Test
    void testExecuteTransactional() {
        // GIVEN
        mockQuery(Query::getResultStream, Stream.empty());

        // WHEN
        List<?> result = adapterProvider.getQueryAdapter(ID).execute(Q, emptyMap());

        // THEN
        capture(tx -> assertEach(() ->
            assertTrue(result.isEmpty(), "wrong result"), () ->
            assertEquals(ISOLATION_DEFAULT, tx.getIsolationLevel(), "wrong isolation"), () ->
            assertEquals(PROPAGATION_SUPPORTS, tx.getPropagationBehavior(), "wrong propagation"), () ->
            assertTrue(tx.isReadOnly(), "not read-only")
        ));
    }

    @Test
    void testExecuteUpdateTransactional() {
        // GIVEN
        int[] expected = new int[] {64};
        mockQuery(Query::executeUpdate, expected[0]);

        // WHEN
        int[] result = adapterProvider.getQueryAdapter(ID).executeUpdate(queries(new QueryWithParams(Q, emptyMap())));

        // THEN
        capture(tx -> assertEach(() ->
            assertArrayEquals(expected, result, "wrong result"), () ->
            assertEquals(ISOLATION_DEFAULT, tx.getIsolationLevel(), "wrong isolation"), () ->
            assertEquals(PROPAGATION_REQUIRED, tx.getPropagationBehavior(), "wrong propagation"), () ->
            assertFalse(tx.isReadOnly(), "read-only")
        ));
    }

    @SuppressWarnings("deprecation")
    void mockQuery(Function<Query<?>, Object> toMock, Object toReturn) {
        mockDataKitService();
        mockTransactionManager();

        when(emfMock.isOpen()).thenReturn(true);
        when(emfMock.createEntityManager(SYNCHRONIZED).createNativeQuery(Q)).thenReturn(queryMock);
        when(toMock.apply(queryMock
                .unwrap(same(Query.class))
                .setResultTransformer(any(ResultTransformer.class))))
                .thenReturn(toReturn);
    }

    @SuppressWarnings("unchecked")
    void mockDataKitService() {
        when(serviceMock.apply(eq(ID), same(EntityManagerFactory.class), any(Function.class)))
                .thenAnswer(i -> i.getArgument(2, Function.class).apply(emfMock));
    }
    void mockTransactionManager() { when(tmProviderMock.apply(same(emfMock))).thenReturn(tmMock); }

    void capture(Consumer<TransactionDefinition> consumer) {
        ArgumentCaptor<TransactionDefinition> tx = ArgumentCaptor.forClass(TransactionDefinition.class);
        verify(tmMock, times(1)).getTransaction(tx.capture());
        consumer.accept(tx.getValue());
    }

    interface DataKitServiceMock extends DataKitService, DataKitExecutor {}
}