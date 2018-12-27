package com.ctrip.ferriswheel.core.loader;

import com.ctrip.ferriswheel.api.query.DataProvider;
import com.ctrip.ferriswheel.api.query.DataQuery;
import com.ctrip.ferriswheel.api.ProviderManager;

import java.util.concurrent.CopyOnWriteArrayList;

public class DefaultProviderManager implements ProviderManager {
    private final CopyOnWriteArrayList<DataProvider> providers = new CopyOnWriteArrayList<>();

    @Override
    public void register(DataProvider provider) {
        providers.addIfAbsent(provider);
    }

    @Override
    public DataProvider getProvider(DataQuery query) {
        for (DataProvider provider : providers) {
            if (isProviderMatches(provider, query)) {
                return provider;
            }
        }
        return null;
    }

    private boolean isProviderMatches(DataProvider provider, DataQuery query) {
        return provider.acceptsQuery(query);
    }

}
