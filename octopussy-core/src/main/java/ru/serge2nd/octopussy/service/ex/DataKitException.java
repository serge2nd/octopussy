package ru.serge2nd.octopussy.service.ex;

import lombok.Getter;
import ru.serge2nd.octopussy.spi.DataKit;

/**
 * @see DataKit
 */
public class DataKitException extends RuntimeException {
    private final @Getter String kitId;

    public static DataKitException errDataKitClosed(String kitId)   { return new Closed(kitId); }
    public static DataKitException errDataKitExists(String kitId)   { return new Exists(kitId); }
    public static DataKitException errDataKitNotFound(String kitId) { return new NotFound(kitId); }

    public DataKitException(String msg, String kitId) {
        super(msg.replace(ID, kitId));
        this.kitId = kitId;
    }

    public static class Closed extends DataKitException {
        public Closed(String kitId) { super("the data kit " + ID + " is closed", kitId); }
    }
    public static class Exists extends DataKitException {
        public Exists(String kitId) { super("the data kit " + ID + " already exists", kitId); }
    }
    public static class NotFound extends DataKitException {
        public NotFound(String kitId) { super("the data kit " + ID + " not found", kitId); }
    }

    static final String ID = "#";
}
