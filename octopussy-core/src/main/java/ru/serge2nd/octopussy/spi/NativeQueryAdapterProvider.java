package ru.serge2nd.octopussy.spi;

/**
 * @see NativeQueryAdapter
 */
public interface NativeQueryAdapterProvider {

    NativeQueryAdapter getQueryAdapter(String kitId);

    default NativeQueryAdapter getQueryAdapter(DataKit dataKit) {
        return this.getQueryAdapter(dataKit.getDefinition().getKitId());
    }
}
