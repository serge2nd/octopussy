package ru.serge2nd.octopussy.data;

import org.hibernate.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
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
import ru.serge2nd.octopussy.dataenv.DataEnvironment;
import ru.serge2nd.octopussy.dataenv.DataEnvironmentService;

import javax.persistence.EntityManagerFactory;
import javax.persistence.SynchronizationType;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;
import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;
import static org.springframework.transaction.TransactionDefinition.*;

@SpringBootTest(classes = {
        NativeQueryAdapterProviderTest.Config.class,
        NativeQueryAdapterProvider.class},
        webEnvironment = NONE)
@ActiveProfiles("test")
class NativeQueryAdapterProviderTest {
    private static final String ID = "5000";
    private static final String Q = "not executed";

    @Autowired
    private NativeQueryAdapterProvider adapterProvider;
    @Value("#{cacheManager.getCache('nativeQueryAdapters')}")
    private Cache queryAdaptersCache;

    @MockBean
    private DataEnvironmentService envServiceMock;
    @InjectMocks
    private DataEnvironment dataEnvMock;
    @Mock
    private EntityManagerFactory emfMock;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Session emMock;
    @Mock
    private PlatformTransactionManager tmMock;

    @BeforeEach
    void setUp() {
        queryAdaptersCache.clear();

    }

    @Test
    void testGetQueryAdapter() {
        // GIVEN
        when(envServiceMock.get(ID)).thenReturn(dataEnvMock);

        // WHEN
        NativeQueryAdapter queryAdapter = adapterProvider.getQueryAdapter(ID);

        // THEN
        assertTrue(queryAdapter instanceof NativeQueryAdapterImpl, "expected adapter impl");
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
        TransactionDefinition txDef = captureTransaction();
        assertEquals(ISOLATION_DEFAULT, txDef.getIsolationLevel(), "wrong isolation");
        assertEquals(PROPAGATION_SUPPORTS, txDef.getPropagationBehavior(), "wrong propagation");
        assertTrue(txDef.isReadOnly(), "not read-only");
    }

    @Test
    void testExecuteUpdateTransactional() {
        // GIVEN
        mockExecuteUpdate();

        // WHEN
        int result = adapterProvider.getQueryAdapter(ID).executeUpdate(singletonList(Q));

        // THEN
        assertEquals(0, result, "wrong result");
        TransactionDefinition txDef = captureTransaction();
        assertEquals(ISOLATION_DEFAULT, txDef.getIsolationLevel(), "wrong isolation");
        assertEquals(PROPAGATION_REQUIRED, txDef.getPropagationBehavior(), "wrong propagation");
        assertFalse(txDef.isReadOnly(), "read-only");
    }

    private void mockExecute() {
        when(envServiceMock.get(ID)).thenReturn(dataEnvMock);
        when(emfMock.createEntityManager(SynchronizationType.SYNCHRONIZED)).thenReturn(emMock);
        when(emMock.createNativeQuery(Q).getResultList()).thenReturn(emptyList());
    }

    private void mockExecuteUpdate() {
        when(envServiceMock.get(ID)).thenReturn(dataEnvMock);
        when(emfMock.createEntityManager(SynchronizationType.SYNCHRONIZED)).thenReturn(emMock);
        when(emMock.createNativeQuery(Q).executeUpdate()).thenReturn(0);
    }

    private TransactionDefinition captureTransaction() {
        ArgumentCaptor<TransactionDefinition> tx = ArgumentCaptor.forClass(TransactionDefinition.class);
        verify(tmMock, times(1)).getTransaction(tx.capture());
        return tx.getValue();
    }

    @Configuration
    @EnableAutoConfiguration
    @ComponentScan(value =
            "ru.serge2nd.octopussy.config",
            excludeFilters = @Filter(type = ASSIGNABLE_TYPE, value = WebConfig.class))
    static class Config {}
}