package com.ctrip.ferriswheel.core.asset;

import com.ctrip.ferriswheel.core.intf.Asset;
import com.ctrip.ferriswheel.core.intf.AssetManager;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

// TODO make asset node a tree node for asset ownership and a graph node for dependency graph
// TODO make asset node able to subscribe/publish events of ownership change/dependency change.
abstract class AssetNode implements Asset {
    private final AssetManager assetManager;
    private final long assetId;
    private AssetNode parentAsset;
    private Set<AssetNode> children = new LinkedHashSet<>(); // bound children
    private Set<Long> dependencies;
    private long lastUpdateSequenceNumber = 0; // revision sequence number
    private boolean valid = true;

    protected AssetNode(AssetManager assetManager) {
        this.assetManager = assetManager;
        this.assetId = assetManager.nextAssetId();
    }

    protected AssetNode(AssetNode parentAsset) {
        this(parentAsset.getAssetManager());
        parentAsset.bindChild(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssetNode assetNode = (AssetNode) o;
        return assetId == assetNode.assetId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(assetId);
    }

    protected boolean isEmployed() {
        return assetManager.exists(assetId);
    }

    protected void bindChild(AssetNode child) {
        children.add(child);
        child.parentAsset = this;
        if (isEmployed()) {
            assetManager.employ(child);
            child.onEmployed();
        }
    }

    protected void unbindChild(AssetNode child) {
        children.remove(child);
        child.parentAsset = null;
        if (isEmployed()) {
            assetManager.dismiss(child);
            child.onDismissed();
        }
    }

    protected void onEmployed() {
        if (lastUpdateSequenceNumber == 0) {
            updateSequenceNumber();
        }
        for (AssetNode child : children) {
            assetManager.employ(child);
            child.onEmployed();
        }
        afterEmployed();
    }

    protected void onDismissed() {
        for (AssetNode child : children) {
            assetManager.dismiss(child);
            child.onDismissed();
        }
        afterDismissed();
    }

    // overridable
    protected void afterEmployed() {
    }

    // overridable
    protected void afterDismissed() {
    }

    protected <T extends AssetNode> T parent(Class<T> parentClass) {
        AssetNode parent = getParentAsset();
        while (parent != null && !parentClass.isInstance(parent)) {
            parent = parent.getParentAsset();
        }
        if (parent != null && parentClass.isInstance(parent)) {
            return (T) parent;
        } else {
            return null;
        }
    }

    protected void clearDependencies() {
        this.dependencies = null;
    }

    protected boolean addDependencies(long targetAssetId) {
        if (dependencies == null) {
            dependencies = new HashSet<>();
        }
        return dependencies.add(targetAssetId);
    }

    protected long getLastUpdateSequenceNumber() {
        return lastUpdateSequenceNumber;
    }

    protected void setLastUpdateSequenceNumber(long lastUpdateSequenceNumber) {
        this.lastUpdateSequenceNumber = lastUpdateSequenceNumber;
        if (parentAsset != null) {
            parentAsset.afterChildUpdate(this);
        }
    }

    protected void updateSequenceNumber() {
        // TODO dependency to workbook may be not necessary.
        DefaultWorkbook wb = parent(DefaultWorkbook.class);
        if (wb != null) {
            setLastUpdateSequenceNumber(wb.nextSequenceNumber());
        }
    }

    /**
     * Overridable
     *
     * @param child
     */
    protected void afterChildUpdate(AssetNode child) {
    }

    protected boolean needUpdate() {
        if (getLastUpdateSequenceNumber() == 0) {
            return true;
        }
        if (getDependencies() == null) {
            return false;
        }
        for (long dependencyId : getDependencies()) {
            AssetNode dependencyAsset = (AssetNode) assetManager.get(dependencyId);
            if (dependencyAsset.getLastUpdateSequenceNumber() > getLastUpdateSequenceNumber()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public long getAssetId() {
        return assetId;
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    protected void setValid(boolean valid) {
        this.valid = valid;
    }

    protected AssetManager getAssetManager() {
        return assetManager;
    }

    protected AssetNode getParentAsset() {
        return parentAsset;
    }

    protected void setParentAsset(AssetNode parentAsset) {
        this.parentAsset = parentAsset;
    }

    protected Set<Long> getDependencies() {
        return dependencies;
    }

    protected void setDependencies(Set<Long> dependencies) {
        this.dependencies = dependencies;
    }

    protected Set<AssetNode> getChildren() {
        return children;
    }

    protected void setChildren(Set<AssetNode> children) {
        this.children = children;
    }
}
