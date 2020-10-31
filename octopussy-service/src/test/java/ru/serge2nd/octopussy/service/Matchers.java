package ru.serge2nd.octopussy.service;

import org.hamcrest.Matcher;
import ru.serge2nd.octopussy.spi.DataEnvironment;
import ru.serge2nd.test.matcher.MatcherOf;

import static ru.serge2nd.test.matcher.MatchAssist.descriptor;
import static ru.serge2nd.test.matcher.MatchAssist.idDescriptor;
import static ru.serge2nd.test.matcher.MatchAssist.valueDescriptor;
import static ru.serge2nd.test.matcher.MatchAssist.valueIdDescriptor;

public class Matchers {

    public static Matcher<DataEnvironment> isClosed() {
        return new MatcherOf<DataEnvironment>(descriptor(()->"data environment is closed"), DataEnvironment::isClosed, valueDescriptor("was open")) {};
    }
    public static Matcher<DataEnvironment> isOpen() {
        return new MatcherOf<DataEnvironment>(descriptor(()->"data environment is open"), dataEnv -> !dataEnv.isClosed(), valueDescriptor("was closed")) {};
    }

    public static Matcher<DataEnvironmentProxy> hasTarget(DataEnvironment expected) {
        return new MatcherOf<DataEnvironmentProxy>(idDescriptor(expected), actual -> actual.target == expected, valueIdDescriptor(actual -> actual.target)) {};
    }
    public static Matcher<DataEnvironmentProxy> noTarget() { return hasTarget(null); }

    public static Matcher<DataEnvironmentProxy> extractsTarget(DataEnvironment expected) {
        return new MatcherOf<DataEnvironmentProxy>(idDescriptor(expected), actual -> actual.getTarget() == expected, valueIdDescriptor(DataEnvironmentProxy::getTarget)) {};
    }
}
