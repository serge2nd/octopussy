package ru.serge2nd.octopussy.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.serge2nd.octopussy.service.DataKitProxy;
import ru.serge2nd.octopussy.spi.DataKit;
import ru.serge2nd.octopussy.spi.DataKit.DataKitBuilder;
import ru.serge2nd.octopussy.spi.JpaKit;
import ru.serge2nd.octopussy.support.DataKitDefinition;
import ru.serge2nd.octopussy.support.JpaKitImpl;

import java.util.Map;

import static ru.serge2nd.collection.HardProperties.properties;
import static ru.serge2nd.octopussy.App.*;

@RequiredArgsConstructor
class JpaKitProto implements DataKit, DataKitBuilder {
    final JpaKitConfig cfg;

    @Getter DataKitDefinition definition;
    public JpaKitProto definition(DataKitDefinition definition) { this.definition = definition; return this; }

    public JpaKitProto toBuilder() { return new JpaKitProto(cfg).definition(definition); }
    public DataKit     build()     { return jpaKitProxy(definition); }
    public void        close()     { /* NO-OP */ }

    DataKit jpaKitProxy(DataKitDefinition definition) {
        return new DataKitProxy(definition, cfg.dataKitService(), this::newJpaKit) {
            @Override
            public void doClose(DataKitProxy proxy) {
                super.doClose(proxy);
                cfg.queryAdaptersCache.evictIfPresent(definition.getKitId());
            }
        };
    }

    JpaKit newJpaKit(DataKitDefinition definition) {
        Map<String, String> keys = cfg.propertyMappings();
        Map<String, Object> vals = definition.getProperties();
        return new JpaKitImpl(definition,
            () -> cfg.pUnits.getDataSource(properties(
                keys.get(DATA_KIT_DRIVER_CLASS), vals.get(DATA_KIT_DRIVER_CLASS),
                keys.get(DATA_KIT_URL)         , vals.get(DATA_KIT_URL),
                keys.get(DATA_KIT_LOGIN)       , vals.get(DATA_KIT_LOGIN),
                keys.get(DATA_KIT_PASSWORD)    , vals.get(DATA_KIT_PASSWORD)).toMap()),
            ds -> cfg.pUnits.getEntityManagerFactory(ds, properties(
                DATA_KIT_DB, vals.get(DATA_KIT_DB),
                DATA_KIT_ID, definition.getKitId()).toMap()));
    }
}
