package ru.serge2nd.octopussy.service.ex;

import lombok.Getter;

import static java.lang.String.format;

@Getter
public class DataEnvironmentExistsException extends RuntimeException {
    private final String envId;

    public DataEnvironmentExistsException(String envId) {
        super(format("data environment %s already exists", envId));
        this.envId = envId;
    }
}
