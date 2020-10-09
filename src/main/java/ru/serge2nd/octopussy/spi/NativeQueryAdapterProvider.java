package ru.serge2nd.octopussy.spi;

public interface NativeQueryAdapterProvider {

    default NativeQueryAdapter getQueryAdapter(DataEnvironment dataEnv) {
        return this.getQueryAdapter(dataEnv.getDefinition().getEnvId());
    }

    NativeQueryAdapter getQueryAdapter(String envId);
}
