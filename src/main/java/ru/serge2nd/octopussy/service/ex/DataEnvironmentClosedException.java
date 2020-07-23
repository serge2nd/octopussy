package ru.serge2nd.octopussy.service.ex;

import lombok.Getter;

import static java.lang.String.format;

@Getter
public class DataEnvironmentClosedException extends RuntimeException {
    private final String envId;

    public DataEnvironmentClosedException(String envId) {
        super(format("data environment %s closed", envId));
        this.envId = envId;
    }
}
