package ru.serge2nd.octopussy.service;

import org.hamcrest.Matcher;
import ru.serge2nd.octopussy.spi.DataEnvironment;
import ru.serge2nd.test.match.builder.MatcherBuilder;

import static ru.serge2nd.test.match.CommonMatch.sameAs;
import static ru.serge2nd.test.match.MatchAssist.not;

public class Matchers {

    public static Matcher<DataEnvironment> isClosed() {
        return new MatcherBuilder<DataEnvironment>(){}.matchIf(DataEnvironment::isClosed).append("data environment is closed").alert("was open").build();
    }
    public static Matcher<DataEnvironment> isOpen() {
        return new MatcherBuilder<DataEnvironment>(){}.matchIf(not(DataEnvironment::isClosed)).append("data environment is open").alert("was closed").build();
    }

    public static Matcher<DataEnvironmentProxy> hasTarget(DataEnvironment expected) {
        return new MatcherBuilder<DataEnvironmentProxy>(){}
                .then(proxy -> proxy.target, sameAs(expected))
                .build();
    }
    public static Matcher<DataEnvironmentProxy> noTarget() { return hasTarget(null); }

    public static Matcher<DataEnvironmentProxy> extractsTarget(DataEnvironment expected) {
        return new MatcherBuilder<DataEnvironmentProxy>(){}
                .then(DataEnvironmentProxy::getTarget, sameAs(expected))
                .build();
    }
}
