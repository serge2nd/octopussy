package ru.serge2nd.octopussy.support;

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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.*;
import static ru.serge2nd.octopussy.service.Matchers.isClosed;
import static ru.serge2nd.octopussy.service.Matchers.isOpen;
import static ru.serge2nd.octopussy.support.DataEnvironmentDefinitionTest.DEF;
import static ru.serge2nd.test.Asserting.assertEach;
import static ru.serge2nd.test.matcher.AssertThat.assertThat;
import static ru.serge2nd.test.matcher.CommonMatch.fails;
import static ru.serge2nd.test.matcher.CommonMatch.illegalArgument;
import static ru.serge2nd.test.matcher.CommonMatch.notNullValue;
import static ru.serge2nd.test.matcher.CommonMatch.sameAs;

@TestInstance(Lifecycle.PER_CLASS)
class JpaEnvironmentImplTest {
    final Supplier<DataSource>                       dataSourceMock = mock(DataSourceProvider.class, RETURNS_DEEP_STUBS);
    final Function<DataSource, EntityManagerFactory> emfMock = mock(EmfProvider.class, RETURNS_DEEP_STUBS);

    @BeforeEach void setUp() { reset((Object)dataSourceMock); }

    @Test void testInitialization() {
        // WHEN
        JpaEnvironmentImpl result = new JpaEnvironmentImpl(DEF, dataSourceMock, emfMock);

        /* THEN */ assertThat(
        result.getDefinition()          , sameAs(DEF),
        result.getDataSource()          , notNullValue("data source"),
        result.getEntityManagerFactory(), notNullValue("entity manager factory"), () ->
        verify(dataSourceMock, times(1)).get(), () ->
        verify(emfMock, times(1)).apply(same(result.getDataSource())));
    }
    @Test void testFailedInitialization() {
        // GIVEN
        Throwable expected = new ArrayStoreException();
        when(dataSourceMock.get()).thenThrow(expected);
        boolean[] closed = new boolean[1];

        /* THEN */ assertThat(
        ()->new JpaEnvironmentImpl(DEF, dataSourceMock, emfMock) {
            public void close() { closed[0] = true; }
        }, fails(expected), () ->
        assertTrue(closed[0], "must call close() on error"));
    }

    @Test void testUnwrap() {
        // WHEN
        JpaEnvironmentImpl jpaEnv = new JpaEnvironmentImpl(DEF, dataSourceMock, emfMock);

        /* THEN */ assertThat(
        jpaEnv.unwrap(JpaEnvironmentImpl.class)           , sameAs(jpaEnv),
        jpaEnv.unwrap(JpaEnvironment.class)               , sameAs(jpaEnv),
        jpaEnv.unwrap(DataEnvironment.class)              , sameAs(jpaEnv),
        jpaEnv.unwrap(DataSource.class)                   , sameAs(jpaEnv.getDataSource()),
        jpaEnv.unwrap(EntityManagerFactory.class)         , sameAs(jpaEnv.getEntityManagerFactory()), () -> assertThat(
        ()->jpaEnv.unwrap(DataEnvironmentDefinition.class), illegalArgument()));
    }

    @Test void testIsClosedNullEmf() { assertThat(new JpaEnvironmentImpl(DEF, (DataSource)null, null), isOpen()); }
    @Test void testIsClosed()        { assertThat(new JpaEnvironmentImpl(DEF, null, mock(EntityManagerFactory.class)), isClosed()); }

    @Test void testClose() {
        // GIVEN
        DataSource closeableMock = mock(DataSource.class, withSettings().extraInterfaces(Closeable.class));
        EntityManagerFactory emfMock = mock(EntityManagerFactory.class);
        when(emfMock.isOpen()).thenReturn(true);
        JpaEnvironmentImpl dataEnv = new JpaEnvironmentImpl(DEF, closeableMock, emfMock);

        // WHEN
        dataEnv.close();

        /* THEN */ assertEach(() ->
        verify(emfMock, times(1)).close(), () ->
        verify((Closeable)closeableMock, times(1)).close());
    }

    @Test void testCloseNotCloseable() {
        // GIVEN
        EntityManagerFactory emfMock = mock(EntityManagerFactory.class);
        when(emfMock.isOpen()).thenReturn(true);
        JpaEnvironmentImpl dataEnv = new JpaEnvironmentImpl(DEF, mock(DataSource.class), emfMock);

        // WHEN
        dataEnv.close();

        // THEN
        verify(emfMock, times(1)).close();
    }

    @Test void testCloseNullEmf() throws IOException {
        // GIVEN
        DataSource closeableMock = mock(DataSource.class, withSettings().extraInterfaces(Closeable.class));
        JpaEnvironmentImpl dataEnv = new JpaEnvironmentImpl(DEF, closeableMock, null);

        // WHEN
        dataEnv.close();

        // THEN
        verify((Closeable)closeableMock, times(1)).close();
    }

    interface DataSourceProvider extends Supplier<DataSource> {}
    interface EmfProvider extends Function<DataSource, EntityManagerFactory> {}
}