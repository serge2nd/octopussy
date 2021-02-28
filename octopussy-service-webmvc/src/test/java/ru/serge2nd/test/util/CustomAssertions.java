package ru.serge2nd.test.util;

import org.opentest4j.AssertionFailedError;

import java.lang.reflect.Array;
import java.util.*;

import static java.lang.String.format;
import static java.lang.String.join;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static ru.serge2nd.stream.util.Collecting.collect;

public class CustomAssertions {

    public static void assertStrictlyEquals(Object expected, Object actual) {
        StrictlyEqualsAssertion assertion = new StrictlyEqualsAssertion().apply(expected, actual);
        if (!assertion.errors.isEmpty())
            throw new AssertionFailedError("messages:\n" + join("\n", assertion.errors), expected, actual);
    }

    static class StrictlyEqualsAssertion {
        final Deque<Integer> path = new LinkedList<>();
        final List<String> errors = new LinkedList<>();

        StrictlyEqualsAssertion apply(Object expected, Object actual) {
            if (expected == actual) return this;
            if (expected == null)   return addAssertionError("expected null, actual " + actual);
            if (actual == null)     return addAssertionError("actual is null but expected " + expected);

            Class<?> expectedClass = expected.getClass();
            Class<?> actualClass = actual.getClass();
            String notEqualMsg = format(
                    "expected %s(%s), actual %s(%s)",
                    expectedClass.getTypeName(),
                    expectedClass.isArray() ? "" : expected,
                    actualClass.getTypeName(),
                    actualClass.isArray() ? "" : actual);

            if (expectedClass.isArray() && actualClass.isArray()) {
                if (!checkArrayEquals(expected, actual, expectedClass.getComponentType(), actualClass.getComponentType()))
                    return addAssertionError(notEqualMsg);

            } else if (expected instanceof Iterable && actual instanceof Iterable) {
                if (!checkIterableEquals(expected, actual))
                    return addAssertionError(notEqualMsg);

            } else if (expectedClass != actualClass || !Objects.equals(expected, actual))
                return addAssertionError(notEqualMsg);

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
                        expectedComponent.getTypeName(),
                        expectedLength,
                        actualComponent.getTypeName(),
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

        void checkPrimitiveArrayEquals(Object expected, Object actual, Class<?> component) {
            try {
                if (boolean.class == component)
                    assertArrayEquals((boolean[])expected, (boolean[])actual);
                else if (char.class == component)
                    assertArrayEquals((char[])expected, (char[])actual);
                else if (byte.class == component)
                    assertArrayEquals((byte[])expected, (byte[])actual);
                else if (short.class == component)
                    assertArrayEquals((short[])expected, (short[])actual);
                else if (int.class == component)
                    assertArrayEquals((int[])expected, (int[])actual);
                else if (long.class == component)
                    assertArrayEquals((long[])expected, (long[])actual);
                else if (float.class == component)
                    assertArrayEquals((float[])expected, (float[])actual);
                else if (double.class == component)
                    assertArrayEquals((double[])expected, (double[])actual);
                else addAssertionError(component.getName() + " not a primitive");
            } catch (AssertionError error) {
                addAssertionError(error.getMessage());
            }
        }

        StrictlyEqualsAssertion addAssertionError(String msg) {
            errors.add(collect(path, new StringBuilder(),
                (sb, i) -> sb.append("[").append(i).append("]"))
                .append(": ").append(msg)
                .toString());
            return this;
        }
    }
}
