package ru.serge2nd.test;

import org.opentest4j.AssertionFailedError;

import java.lang.reflect.Array;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class CustomAssertions {

    public static void assertStrictlyEquals(Object expected, Object actual) {
        StrictlyEqualsAssertion assertion = new StrictlyEqualsAssertion().apply(expected, actual);
        if (assertion.errors.size() != 0)
            throw new AssertionFailedError("messages:\n" + String.join("\n", assertion.errors), expected, actual);
    }

    static class StrictlyEqualsAssertion {
        final Deque<Integer> path = new LinkedList<>();
        final List<String> errors = new LinkedList<>();

        StrictlyEqualsAssertion apply(Object expected, Object actual) {
            if (expected == actual) return this;
            if (expected == null) return addAssertionError("expected null, actual " + actual);
            if (actual == null) return addAssertionError("actual is null but expected " + expected);

            Class<?> expectedClass = expected.getClass();
            Class<?> actualClass = actual.getClass();
            String notEqualsMsg = format(
                    "expected %s(%s), actual %s(%s)",
                    friendlyName(expectedClass),
                    expectedClass.isArray() ? "" : expected,
                    friendlyName(actualClass),
                    actualClass.isArray() ? "" : actual);

            if (expectedClass.isArray() && actualClass.isArray()) {
                if (!checkArrayEquals(expected, actual, expectedClass.getComponentType(), actualClass.getComponentType()))
                    return addAssertionError(notEqualsMsg);

            } else if (expected instanceof Iterable && actual instanceof Iterable) {
                if (!checkIterableEquals(expected, actual))
                    return addAssertionError(notEqualsMsg);

            } else if (expectedClass != actualClass || !Objects.equals(expected, actual))
                return addAssertionError(notEqualsMsg);

            return this;
        }

        boolean checkIterableEquals(Object expected, Object actual) {
            // check both are ordered or not
            if (expected instanceof List != actual instanceof List)
                return false;

            Iterator<?> expectedIt = ((Iterable<?>)expected).iterator();
            Iterator<?> actualIt = ((Iterable<?>)actual).iterator();

            for (int i = 0; expectedIt.hasNext() && actualIt.hasNext() && path.offerLast(i++); path.pollLast())
                this.apply(expectedIt.next(), actualIt.next());

            return expectedIt.hasNext() == actualIt.hasNext();
        }

        boolean checkArrayEquals(Object expected, Object actual,
                                 Class<?> expectedComponent, Class<?> actualComponent) {
            int expectedLength = Array.getLength(expected);
            int actualLength = Array.getLength(actual);
            if (expectedLength != actualLength) {
                addAssertionError(format(
                        "expected %s[%d], actual %s[%d]",
                        friendlyName(expectedComponent),
                        expectedLength,
                        friendlyName(actualComponent),
                        actualLength));
                return true;
            }

            if (expectedComponent.isPrimitive()) {
                if (expectedComponent != actualComponent) return false;
                checkPrimitiveArrayEquals(expected, actual, expectedComponent);
                return true;
            }

            if (actualComponent.isPrimitive()) return false;
            Object[] expectedArray = (Object[]) expected;
            Object[] actualArray = (Object[]) actual;

            for (int i = -1; ++i < expectedLength && path.offerLast(i); path.pollLast())
                this.apply(expectedArray[i], actualArray[i]);

            return true;
        }

        void checkPrimitiveArrayEquals(Object expected, Object actual, Class<?> componentType) {
            try {
                if (boolean.class == componentType)
                    assertArrayEquals((boolean[])expected, (boolean[])actual);
                else if (char.class == componentType)
                    assertArrayEquals((char[])expected, (char[])actual);
                else if (byte.class == componentType)
                    assertArrayEquals((byte[])expected, (byte[])actual);
                else if (short.class == componentType)
                    assertArrayEquals((short[])expected, (short[])actual);
                else if (int.class == componentType)
                    assertArrayEquals((int[])expected, (int[])actual);
                else if (long.class == componentType)
                    assertArrayEquals((long[])expected, (long[])actual);
                else if (float.class == componentType)
                    assertArrayEquals((float[])expected, (float[])actual);
                else if (double.class == componentType)
                    assertArrayEquals((double[])expected, (double[])actual);
                else throw new IllegalArgumentException(componentType.getName());
            } catch (AssertionFailedError assertionError) {
                addAssertionError(assertionError.getMessage());
            }
        }

        StrictlyEqualsAssertion addAssertionError(String assertionError) {
            errors.add(path.stream()
                    .map(i -> format("[%d]", i))
                    .collect(joining())
                    + ": " + assertionError);
            return this;
        }
    }

    public static String friendlyName(Class<?> clazz) {
        if (clazz.isArray())
            return friendlyName(clazz.getComponentType()) + "[]";
        return clazz.getName();
    }
}
