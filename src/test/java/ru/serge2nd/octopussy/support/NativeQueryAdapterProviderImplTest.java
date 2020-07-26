package ru.serge2nd.octopussy.support;

import org.hibernate.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.Cache;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import ru.serge2nd.octopussy.config.WebConfig;
import ru.serge2nd.octopussy.spi.DataEnvironmentService;
import ru.serge2nd.octopussy.spi.NativeQueryAdapter;
import ru.serge2nd.octopussy.spi.NativeQueryAdapterProvider;

import javax.persistence.EntityManagerFactory;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static javax.persistence.SynchronizationType.SYNCHRONIZED;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;
import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;
import static org.springframework.transaction.TransactionDefinition.*;

@SpringBootTest(classes = {
        NativeQueryAdapterProviderImplTest.Config.class,
        NativeQueryAdapterProviderImpl.class},
        webEnvironment = NONE)
@ActiveProfiles("test")
class NativeQueryAdapterProviderImplTest {
    static final String ID = "5000";
    static final String Q = "not executed";

    @Autowired NativeQueryAdapterProvider adapterProvider;
    @MockBean DataEnvironmentService envServiceMock;
    @Value("#{cacheManager.getCache('nativeQueryAdapters')}") Cache queryAdaptersCache;

    final DataEnvironmentDefinition definition = DataEnvironmentDefinition.builder().envId(ID).build();
    DataEnvironmentImpl dataEnv;

    @Mock EntityManagerFactory emfMock;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS) Session emMock;
    @Mock PlatformTransactionManager tmMock;

    @BeforeEach
    void setUp() {
        queryAdaptersCache.clear();
        dataEnv = new DataEnvironmentImpl(definition, null, emfMock, tmMock);
    }

    @Test
    void testGetQueryAdapter() {
        // GIVEN
        when(envServiceMock.get(ID)).thenReturn(dataEnv);

        // WHEN
        NativeQueryAdapter queryAdapter = adapterProvider.getQueryAdapter(ID);

        // THEN
        assertSame(queryAdapter, queryAdaptersCache.get(ID, NativeQueryAdapter.class), "expected to be cached");
    }

    @Test
    void testGetQueryAdapterCached() {
        // GIVEN
        NativeQueryAdapter queryAdapterMock = mock(NativeQueryAdapter.class);
        queryAdaptersCache.put(ID, queryAdapterMock);

        // WHEN
        NativeQueryAdapter queryAdapter = adapterProvider.getQueryAdapter(ID);

        // THEN
        assertSame(queryAdapterMock, queryAdapter, "expected cached object");
        verifyNoInteractions(envServiceMock);
    }

    @Test
    void testExecuteTransactional() {
        // GIVEN
        mockExecute();

        // WHEN
        List<?> result = adapterProvider.getQueryAdapter(ID).execute(Q);

        // THEN
        assertTrue(result.isEmpty(), "wrong result");
        captureTransaction(tx -> {
            assertEquals(ISOLATION_DEFAULT, tx.getIsolationLevel(), "wrong isolation");
            assertEquals(PROPAGATION_SUPPORTS, tx.getPropagationBehavior(), "wrong propagation");
            assertTrue(tx.isReadOnly(), "not read-only");
        });
    }

    @Test
    void testExecuteUpdateTransactional() {
        // GIVEN
        mockExecuteUpdate();

        // WHEN
        int result = adapterProvider.getQueryAdapter(ID).executeUpdate(singletonList(Q));

        // THEN
        assertEquals(0, result, "wrong result");
        captureTransaction(tx -> {
            assertEquals(ISOLATION_DEFAULT, tx.getIsolationLevel(), "wrong isolation");
            assertEquals(PROPAGATION_REQUIRED, tx.getPropagationBehavior(), "wrong propagation");
            assertFalse(tx.isReadOnly(), "read-only");
        });
    }

    @SuppressWarnings("unchecked")
    void mockExecute() {
        when(envServiceMock.doWith(eq(ID), any(Function.class)))
                .thenAnswer(i -> i.getArgument(1, Function.class).apply(dataEnv));
        when(emfMock.isOpen()).thenReturn(true);
        when(emfMock.createEntityManager(SYNCHRONIZED)).thenReturn(emMock);
        when(emMock.createNativeQuery(Q).getResultList()).thenReturn(emptyList());
    }
    @SuppressWarnings("unchecked")
    void mockExecuteUpdate() {
        when(envServiceMock.doWith(eq(ID), any(Function.class)))
                .thenAnswer(i -> i.getArgument(1, Function.class).apply(dataEnv));
        when(emfMock.isOpen()).thenReturn(true);
        when(emfMock.createEntityManager(SYNCHRONIZED)).thenReturn(emMock);
        when(emMock.createNativeQuery(Q).executeUpdate()).thenReturn(0);
    }

    void captureTransaction(Consumer<TransactionDefinition> consumer) {
        ArgumentCaptor<TransactionDefinition> tx = ArgumentCaptor.forClass(TransactionDefinition.class);
        verify(tmMock, times(1)).getTransaction(tx.capture());
        consumer.accept(tx.getValue());
    }

    @Configuration
    @EnableAutoConfiguration
    @ComponentScan(value =
            "ru.serge2nd.octopussy.config",
            excludeFilters = @Filter(type = ASSIGNABLE_TYPE, value = WebConfig.class))
    static class Config {}
}