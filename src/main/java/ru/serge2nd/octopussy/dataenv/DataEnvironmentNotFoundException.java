package ru.serge2nd.octopussy.dataenv;

import lombok.Getter;

import static java.lang.String.format;

@Getter
public class DataEnvironmentNotFoundException extends RuntimeException {
    private final String envId;

    public DataEnvironmentNotFoundException(String envId) {
        super(format("data environment %s not found", envId));
        this.envId = envId;
    }
}
