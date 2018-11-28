package com.ctrip.ferriswheel.core.bean;

import com.ctrip.ferriswheel.core.intf.Environment;
import com.ctrip.ferriswheel.core.intf.ProviderManager;

public class DefaultEnvironment implements Environment {
    public static class Builder {
        private ProviderManager providerManager;

        public DefaultEnvironment build() {
            return new DefaultEnvironment(providerManager);
        }

        public Builder setProviderManager(ProviderManager providerManager) {
            this.providerManager = providerManager;
            return this;
        }
    }

    private final ProviderManager providerManager;

    public DefaultEnvironment(ProviderManager providerManager) {
        this.providerManager = providerManager;
    }

    @Override
    public ProviderManager getProviderManager() {
        return providerManager;
    }
}
