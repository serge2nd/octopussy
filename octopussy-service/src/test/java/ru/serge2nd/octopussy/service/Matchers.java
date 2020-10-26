package ru.serge2nd.octopussy.service;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import ru.serge2nd.octopussy.spi.DataEnvironment;

import static lombok.AccessLevel.PACKAGE;
import static org.hamcrest.CoreMatchers.not;

public class Matchers {

    public static Matcher<DataEnvironment> isClosed() { return IsClosed.INSTANCE; }
    public static Matcher<DataEnvironment> isOpen()   { return not(IsClosed.INSTANCE); }

    public static Matcher<DataEnvironmentProxy> hasTarget(DataEnvironment expected) { return new HasTarget(expected); }
    public static Matcher<DataEnvironmentProxy> noTarget()                          { return new HasTarget(null); }

    public static Matcher<DataEnvironmentProxy> extractsTarget(DataEnvironment expected) { return new ExtractsTarget(expected); }

    @NoArgsConstructor(access = PACKAGE)
    static class IsClosed extends TypeSafeMatcher<DataEnvironment> {
        static final IsClosed INSTANCE = new IsClosed();
        @Override
        public boolean matchesSafely(DataEnvironment actual) { return actual.isClosed(); }
        @Override
        public void describeTo(Description description) { description.appendText("a closed data environment"); }
        @Override
        protected void describeMismatchSafely(DataEnvironment item, Description description) { description.appendText("not closed"); }
    }

    @RequiredArgsConstructor(access = PACKAGE)
    static class HasTarget extends TypeSafeMatcher<DataEnvironmentProxy> {
        final DataEnvironment expected;
        @Override
        public boolean matchesSafely(DataEnvironmentProxy actual) { return actual.target == expected; }
        @Override
        public void describeTo(Description description) { description.appendText("a proxy target"); }
    }

    static class ExtractsTarget extends HasTarget {
        ExtractsTarget(DataEnvironment expected) { super(expected); }
        @Override
        public boolean matchesSafely(DataEnvironmentProxy actual) { return actual.getTarget() == expected; }
    }
}
