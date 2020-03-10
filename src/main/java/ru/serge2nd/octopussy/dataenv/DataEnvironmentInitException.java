package ru.serge2nd.octopussy.dataenv;

import lombok.Getter;

import static java.lang.String.format;

@Getter
public class DataEnvironmentInitException extends RuntimeException {
    private final String envId;

    public DataEnvironmentInitException(String envId, Throwable cause) {
        super(format("data environment(s) %s initialization failed", envId), cause);
        this.envId = envId;
    }
}
