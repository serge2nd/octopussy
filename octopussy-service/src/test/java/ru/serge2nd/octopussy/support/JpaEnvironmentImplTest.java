package ru.serge2nd.octopussy.support;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import ru.serge2nd.octopussy.spi.DataEnvironment;
import ru.serge2nd.octopussy.spi.JpaEnvironment;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.io.Closeable;
import java.io.IOException;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.*;

@TestInstance(Lifecycle.PER_CLASS)
class JpaEnvironmentImplTest {
    final Supplier<DataSource>                       dataSourceMock = mock(DataSourceProvider.class, RETURNS_DEEP_STUBS);
    final Function<DataSource, EntityManagerFactory> emfMock = mock(EmfProvider.class, RETURNS_DEEP_STUBS);

    @BeforeEach void setUp() { reset((Object)dataSourceMock); }

    @Test void testInitialization() {
        // WHEN
        JpaEnvironmentImpl result = new JpaEnvironmentImpl(DataEnvironmentDefinitionTest.DEF, dataSourceMock, emfMock);

        /* THEN */ assertAll(() ->
        Assertions.assertSame(DataEnvironmentDefinitionTest.DEF, result.getDefinition(), "null definition"), () ->
        assertNotNull(result.getDataSource(), "null data source"), () ->
        assertNotNull(result.getEntityManagerFactory(), "null entity manager factory"), () ->
        verify(dataSourceMock, times(1)).get(), () ->
        verify(emfMock, times(1)).apply(same(result.getDataSource())));
    }
    @Test void testFailedInitialization() {
        // GIVEN
        Throwable expected = new ArrayStoreException();
        when(dataSourceMock.get()).thenThrow(expected);
        boolean[] closed = new boolean[1];

        // WHEN
        Throwable error = catchThrowableOfType(()->new JpaEnvironmentImpl(DataEnvironmentDefinitionTest.DEF, dataSourceMock, emfMock) {
            public void close() { closed[0] = true; }
        }, expected.getClass());

        /* THEN */ assertAll(() ->
        assertNotNull(error, "expected an error"), () ->
        assertTrue(closed[0], "must call close() on error"));
    }

    @Test void testUnwrap() {
        // WHEN
        JpaEnvironmentImpl jpaEnv = new JpaEnvironmentImpl(DataEnvironmentDefinitionTest.DEF, dataSourceMock, emfMock);

        /* THEN */ assertAll(() ->
        assertSame(jpaEnv, jpaEnv.unwrap(JpaEnvironmentImpl.class), "must unwrap itself"), () ->
        assertSame(jpaEnv, jpaEnv.unwrap(JpaEnvironment.class), "must unwrap " + JpaEnvironment.class.getName()), () ->
        assertSame(jpaEnv, jpaEnv.unwrap(DataEnvironment.class), "must unwrap " + DataEnvironment.class.getName()), () ->
        assertSame(jpaEnv.getDataSource(), jpaEnv.unwrap(DataSource.class), "must unwrap " + DataSource.class.getName()), () ->
        assertSame(jpaEnv.getEntityManagerFactory(), jpaEnv.unwrap(EntityManagerFactory.class), "must unwrap " + EntityManagerFactory.class.getName()), () ->
        assertThrows(IllegalArgumentException.class, ()->jpaEnv.unwrap(DataEnvironmentDefinition.class)));
    }

    @Test void testIsClosedNullEmf() {
        assertFalse(new JpaEnvironmentImpl(DataEnvironmentDefinitionTest.DEF, (DataSource)null, null).isClosed(), "closed but no entity manager factory");
    }
    @Test void testIsClosed() {
        assertTrue(new JpaEnvironmentImpl(DataEnvironmentDefinitionTest.DEF, null, mock(EntityManagerFactory.class)).isClosed(), "wrong status");
    }

    @Test void testClose() {
        // GIVEN
        DataSource closeableMock = mock(DataSource.class, withSettings().extraInterfaces(Closeable.class));
        EntityManagerFactory emfMock = mock(EntityManagerFactory.class);
        when(emfMock.isOpen()).thenReturn(true);
        JpaEnvironmentImpl dataEnv = new JpaEnvironmentImpl(DataEnvironmentDefinitionTest.DEF, closeableMock, emfMock);

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
        JpaEnvironmentImpl dataEnv = new JpaEnvironmentImpl(DataEnvironmentDefinitionTest.DEF, mock(DataSource.class), emfMock);

        // WHEN
        dataEnv.close();

        // THEN
        verify(emfMock, times(1)).close();
    }

    @Test void testCloseNullEmf() throws IOException {
        // GIVEN
        DataSource closeableMock = mock(DataSource.class, withSettings().extraInterfaces(Closeable.class));
        JpaEnvironmentImpl dataEnv = new JpaEnvironmentImpl(DataEnvironmentDefinitionTest.DEF, closeableMock, null);

        // WHEN
        dataEnv.close();

        // THEN
        verify((Closeable)closeableMock, times(1)).close();
    }

    interface DataSourceProvider extends Supplier<DataSource> {}
    interface EmfProvider extends Function<DataSource, EntityManagerFactory> {}
}