package ru.serge2nd.octopussy.service.ex;

import lombok.Getter;

import static java.lang.String.format;

public class DataEnvironmentException extends RuntimeException {
    private final @Getter String envId;

    public static DataEnvironmentException errDataEnvClosed(String envId)   { return new Closed(envId); }
    public static DataEnvironmentException errDataEnvExists(String envId)   { return new Exists(envId); }
    public static DataEnvironmentException errDataEnvNotFound(String envId) { return new NotFound(envId); }

    public DataEnvironmentException(String msg, String envId) {
        super(format(msg, envId));
        this.envId = envId;
    }

    public static final class Closed extends DataEnvironmentException {
        public Closed(String envId) { super("data environment %s is closed", envId); }
    }
    public static final class Exists extends DataEnvironmentException {
        public Exists(String envId) { super("data environment %s already exists", envId); }
    }
    public static final class NotFound extends DataEnvironmentException {
        public NotFound(String envId) { super("data environment %s not found", envId); }
    }
}
