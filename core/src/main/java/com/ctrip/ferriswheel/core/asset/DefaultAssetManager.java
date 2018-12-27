package com.ctrip.ferriswheel.core.asset;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

class DefaultAssetManager implements AssetManager {
    private final AtomicLong nextId = new AtomicLong(0);
    final Map<Long, AssetReference> assetMap = new ConcurrentHashMap<>();
    final List<AssetChangeCallback> callbacks = new LinkedList<>();

    @Override
    public long nextAssetId() {
        return nextId.getAndIncrement();
    }

    @Override
    public void employ(Asset asset) {
        AssetReference ref = assetMap.get(asset.getAssetId());
        if (ref == null) {
            ref = new AssetReference(asset);
            AssetReference previous = assetMap.putIfAbsent(asset.getAssetId(), ref);
            if (previous != null) {
                ref = previous;
            }
        }
        ref.referenceCount.incrementAndGet();

        for (AssetChangeCallback callback : callbacks) {
            callback.onAssetEmployed(asset);
        }
    }

    @Override
    public Asset get(long id) {
        AssetReference ref = assetMap.get(id);
        return ref == null ? null : ref.asset;
    }

    @Override
    public boolean exists(long id) {
        return assetMap.containsKey(id);
    }

    @Override
    public void dismiss(Asset asset) {
        AssetReference ref = assetMap.get(asset.getAssetId());
        if (ref == null) {
            throw new RuntimeException("This asset is not employed!");
        }
        if (ref.referenceCount.decrementAndGet() <= 0) {
            assetMap.remove(asset.getAssetId());
        }

        for (AssetChangeCallback callback : callbacks) {
            callback.onAssetDismissed(asset);
        }
    }

    @Override
    public void subscribe(AssetChangeCallback callback) {
        if (callback != null) {
            callbacks.add(callback);
        }
    }

    class AssetReference {
        Asset asset;
        AtomicInteger referenceCount;

        AssetReference(Asset asset) {
            this.asset = asset;
            this.referenceCount = new AtomicInteger(0);
        }
    }
}
