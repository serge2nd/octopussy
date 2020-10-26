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
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.serge2nd.octopussy.support.ToListResultTransformer.INSTANCE;
import static ru.serge2nd.test.matcher.AssertThat.assertThat;
import static ru.serge2nd.test.matcher.CommonMatch.equalTo;
import static ru.serge2nd.test.matcher.CommonMatch.fails;

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

        // THEN
        assertThat(
        result                  , instanceOf(expected.getClass()),
        result                  , equalTo(expected),
        ((List<?>)result).get(6), instanceOf(expected.get(6).getClass()), () ->
        verify(c, times(1)).free(), () ->
        verify(b, times(1)).free());
    }
    @SuppressWarnings("ConstantConditions")
    @Test void testNullTuple() { assertThat(()->INSTANCE.transform(null, null), fails(IllegalArgumentException.class)); }

    @Test void testTransformList() {
        // GIVEN
        List<?> expected = asList("abc", 54);

        // WHEN
        List<?> result = INSTANCE.transform(expected);

        // THEN
        assertThat(result, instanceOf(Unmodifiable.class), equalTo(expected));
    }
    @Test void testNullList() { assertThat(()->INSTANCE.transform(null), fails(IllegalArgumentException.class)); }

    @Test void testClobError() throws SQLException {
        Clob[] c = {mock(Clob.class)};
        when(c[0].getSubString(1, 0)).thenThrow(SQLException.class);

        assertThat(()->INSTANCE.transform(c, null), fails(LobFetchFailedException.class), () ->
        verify(c[0], times(1)).free());
    }
    @Test void testBlobError() throws SQLException {
        Blob[] b = {mock(Blob.class)};
        when(b[0].getBytes(1, 0)).thenThrow(SQLException.class);

        assertThat(()->INSTANCE.transform(b, null), fails(LobFetchFailedException.class), () ->
        verify(b[0], times(1)).free());
    }
}