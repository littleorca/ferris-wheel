package com.ctrip.ferriswheel.core.intf;

public interface ProviderManager {

    void register(DataProvider provider);

    DataProvider getProvider(DataQuery query);

}
