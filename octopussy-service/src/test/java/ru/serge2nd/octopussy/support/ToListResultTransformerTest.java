package ru.serge2nd.octopussy.support;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import ru.serge2nd.collection.Unmodifiable;
import ru.serge2nd.octopussy.support.ToListResultTransformer.LobFetchFailedException;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static java.lang.reflect.Array.get;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static ru.serge2nd.octopussy.support.ToListResultTransformer.INSTANCE;

@TestInstance(Lifecycle.PER_CLASS)
class ToListResultTransformerTest {

    @Test void testTransformTuple() throws SQLException {
        // GIVEN
        Clob c = mock(Clob.class); Blob b = mock(Blob.class);
        Object[] a = {
            Math.PI,
            mock(Time.class),
            mock(Date.class),
            mock(Timestamp.class),
            c, b,
            new Object[] {"nested"}};
        List<?> expected = Unmodifiable.of(
            Math.PI,
            LocalTime.now(),
            LocalDate.now(),
            LocalDateTime.now(),
            "clob998",
            "blob010".getBytes(),
            Unmodifiable.of(get(a[6], 0)));
        // AND
        when(((Time)a[1]).toLocalTime()).thenReturn((LocalTime)expected.get(1));
        when(((Date)a[2]).toLocalDate()).thenReturn((LocalDate)expected.get(2));
        when(((Timestamp)a[3]).toLocalDateTime()).thenReturn((LocalDateTime)expected.get(3));
        when(c.getSubString(1, 0)).thenReturn((String)expected.get(4));
        when(b.getBytes(1, 0)).thenReturn((byte[])expected.get(5));

        // WHEN
        Object result = INSTANCE.transform(a, null);

        /* THEN */ assertAll(() ->
        assertEquals(expected, result, "expected same data"), () ->
        assertSame(expected.getClass(), result.getClass(), "expected unmodifiable list"), () ->
        assertSame(expected.get(6).getClass(), ((List<?>)result).get(6).getClass(), "expected nested unmodifiable list"), () ->
        verify(c, times(1)).free(), () ->
        verify(b, times(1)).free());
    }
    @SuppressWarnings("ConstantConditions")
    @Test void testNullTuple() { assertThrows(IllegalArgumentException.class, ()->INSTANCE.transform(null, null)); }

    @Test void testTransformList() {
        // GIVEN
        List<?> l = asList("abc", 54);

        // WHEN
        List<?> result = INSTANCE.transform(l);

        /* THEN */ assertAll(() ->
        assertEquals(l, result), () ->
        assertTrue(result instanceof Unmodifiable, "expected unmodifiable list"));
    }
    @Test void testNullList() { assertThrows(IllegalArgumentException.class, ()->INSTANCE.transform(null)); }

    @Test void testClobError() throws SQLException {
        Clob[] c = {mock(Clob.class)};
        when(c[0].getSubString(1, 0)).thenThrow(SQLException.class);

        assertThrows(LobFetchFailedException.class, ()->INSTANCE.transform(c, null));
        verify(c[0], times(1)).free();
    }
    @Test void testBlobError() throws SQLException {
        Blob[] b = {mock(Blob.class)};
        when(b[0].getBytes(1, 0)).thenThrow(SQLException.class);

        assertThrows(LobFetchFailedException.class, ()->INSTANCE.transform(b, null));
        verify(b[0], times(1)).free();
    }
}