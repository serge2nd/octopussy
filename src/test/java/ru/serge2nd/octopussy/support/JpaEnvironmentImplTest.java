package ru.serge2nd.octopussy.support;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.transaction.PlatformTransactionManager;
import ru.serge2nd.octopussy.spi.DataEnvironment;
import ru.serge2nd.octopussy.spi.JpaEnvironment;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.io.Closeable;
import java.io.IOException;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.*;
import static ru.serge2nd.octopussy.support.DataEnvironmentDefinitionTest.DEF;

@TestInstance(Lifecycle.PER_CLASS)
class JpaEnvironmentImplTest {
    final Supplier<DataSource>                                       dataSourceMock = mock(DataSourceProvider.class, RETURNS_DEEP_STUBS);
    final Function<DataSource, EntityManagerFactory>                 emfMock = mock(EmfProvider.class, RETURNS_DEEP_STUBS);
    final Function<EntityManagerFactory, PlatformTransactionManager> tmMock = mock(TmProvider.class, RETURNS_DEEP_STUBS);

    @Test void testInitialization() {
        // WHEN
        JpaEnvironmentImpl result = new JpaEnvironmentImpl(DEF, dataSourceMock, emfMock, tmMock);

        /* THEN */ assertAll(() ->
        assertSame(DEF, result.getDefinition(), "null definition"), () ->
        assertNotNull(result.getDataSource(), "null data source"), () ->
        assertNotNull(result.getEntityManagerFactory(), "null entity manager factory"), () ->
        assertNotNull(result.getTransactionManager(), "null transaction manager"), () ->
        verify(dataSourceMock, times(1)).get(), () ->
        verify(emfMock, times(1)).apply(same(result.getDataSource())), () ->
        verify(tmMock, times(1)).apply(same(result.getEntityManagerFactory())));
    }

    @Test void testUnwrap() {
        // WHEN
        JpaEnvironmentImpl jpaEnv = new JpaEnvironmentImpl(DEF, dataSourceMock, emfMock, tmMock);

        /* THEN */ assertAll(() ->
        assertSame(jpaEnv, jpaEnv.unwrap(DataEnvironment.class), "must unwrap " + DataEnvironment.class.getName()), () ->
        assertSame(jpaEnv, jpaEnv.unwrap(JpaEnvironment.class), "must unwrap " + JpaEnvironment.class.getName()), () ->
        assertSame(jpaEnv, jpaEnv.unwrap(JpaEnvironmentImpl.class), "must unwrap " + JpaEnvironmentImpl.class.getName()), () ->
        assertSame(jpaEnv.getDataSource(), jpaEnv.unwrap(DataSource.class), "must unwrap " + DataSource.class.getName()), () ->
        assertSame(jpaEnv.getEntityManagerFactory(), jpaEnv.unwrap(EntityManagerFactory.class), "must unwrap " + EntityManagerFactory.class.getName()), () ->
        assertSame(jpaEnv.getTransactionManager(), jpaEnv.unwrap(PlatformTransactionManager.class), "must unwrap " + PlatformTransactionManager.class.getName()));
    }

    @Test void testIsClosedNullEmf() {
        assertFalse(new JpaEnvironmentImpl(DEF, (DataSource)null, null, null).isClosed(), "closed but no entity manager factory");
    }
    @Test void testIsClosed() {
        assertTrue(new JpaEnvironmentImpl(DEF, null, mock(EntityManagerFactory.class), null).isClosed(), "wrong status");
    }

    @Test void testClose() {
        // GIVEN
        DataSource closeableMock = mock(DataSource.class, withSettings().extraInterfaces(Closeable.class));
        EntityManagerFactory emfMock = mock(EntityManagerFactory.class);
        when(emfMock.isOpen()).thenReturn(true);
        JpaEnvironmentImpl dataEnv = new JpaEnvironmentImpl(DEF, closeableMock, emfMock, null);

        // WHEN
        dataEnv.close();

        /* THEN */ assertAll(() ->
        verify(emfMock, times(1)).close(), () ->
        verify((Closeable)closeableMock, times(1)).close());
    }

    @Test void testCloseNotCloseable() {
        // GIVEN
        EntityManagerFactory emfMock = mock(EntityManagerFactory.class);
        when(emfMock.isOpen()).thenReturn(true);
        JpaEnvironmentImpl dataEnv = new JpaEnvironmentImpl(DEF, mock(DataSource.class), emfMock, null);

        // WHEN
        dataEnv.close();

        // THEN
        verify(emfMock, times(1)).close();
    }

    @Test void testCloseNullEmf() throws IOException {
        // GIVEN
        DataSource closeableMock = mock(DataSource.class, withSettings().extraInterfaces(Closeable.class));
        JpaEnvironmentImpl dataEnv = new JpaEnvironmentImpl(DEF, closeableMock, null, null);

        // WHEN
        dataEnv.close();

        // THEN
        verify((Closeable)closeableMock, times(1)).close();
    }

    interface DataSourceProvider extends Supplier<DataSource> {}
    interface EmfProvider extends Function<DataSource, EntityManagerFactory> {}
    interface TmProvider extends Function<EntityManagerFactory, PlatformTransactionManager> {}
}