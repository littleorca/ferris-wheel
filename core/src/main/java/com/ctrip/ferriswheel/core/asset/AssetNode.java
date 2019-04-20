package com.ctrip.ferriswheel.core.asset;

import java.util.*;

// TODO make asset node be able to subscribe/publish events of ownership/dependency changes.
abstract class AssetNode implements Asset {
    private final AssetManager assetManager;
    private final long assetId;
    private AssetNode parent;
    private Set<AssetNode> children = new LinkedHashSet<>(); // bound children
    private Set<AssetNode> dependencies = new HashSet<>();
    private Set<AssetNode> dependents = new HashSet<>();
    private Map<DefaultTable, Set<DefaultTable.Range>> watchedRanges = new HashMap<>();
    private volatile long lastUpdateSequenceNumber = 0; // revision sequence number
    private boolean valid = true;
    private boolean ephemeral = false;

    protected AssetNode(AssetManager assetManager) {
        this.assetManager = assetManager;
        this.assetId = assetManager.nextAssetId();
    }

    protected AssetNode(AssetNode parent) {
        this(parent.getAssetManager());
        // parent.bindChild(this); // bind should be proceed while it is added to parent.
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
        child.parent = this;
        if (isEmployed()) {
            assetManager.employ(child);
            child.onEmployed();
        }
    }

    protected void unbindChild(AssetNode child) {
        children.remove(child);
        child.parent = null;
        if (isEmployed()) {
            assetManager.dismiss(child);
            child.onDismissed();
        }
    }

    protected void onEmployed() {
        // addDependency(getParentAsset()); // TODO review if it is needed
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
        clearDependencies();
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
        AssetNode parent = getParent();
        while (parent != null && !parentClass.isInstance(parent)) {
            parent = parent.getParent();
        }
        if (parent != null && parentClass.isInstance(parent)) {
            return (T) parent;
        } else {
            return null;
        }
    }

    protected long getLastUpdateSequenceNumber() {
        return lastUpdateSequenceNumber;
    }

    protected void setLastUpdateSequenceNumber(long lastUpdateSequenceNumber) {
        this.lastUpdateSequenceNumber = lastUpdateSequenceNumber;
        if (parent != null) {
            parent.afterChildUpdate(this);
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
        for (AssetNode dependency : getDependencies()) {
            if (dependency.getLastUpdateSequenceNumber() > getLastUpdateSequenceNumber()) {
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

    @Override
    public boolean isEphemeral() {
        return ephemeral;
    }

    protected void setEphemeral(boolean ephemeral) {
        this.ephemeral = ephemeral;
    }

    protected AssetManager getAssetManager() {
        return assetManager;
    }

    public AssetNode getParent() {
        return parent;
    }

    protected void setParent(AssetNode parent) {
        this.parent = parent;
    }

    @Override
    public Set<AssetNode> getDependencies() {
        return Collections.unmodifiableSet(dependencies);
    }

    private void setDependencies(Set<AssetNode> dependencies) {
        this.dependencies = dependencies;
    }

    void addDependency(AssetNode dependency) {
        this.dependencies.add(dependency);
        dependency.addDependent(this);
    }

    void removeDependency(AssetNode dependency) {
        if (this.dependencies.remove(dependency)) {
            if (this.dependencies.isEmpty()) {
                this.dependencies = new HashSet<>();
            }
        }
        dependency.removeDependent(this);
    }

    void clearDependencies() {
        for (AssetNode dependency : dependencies) {
            dependency.removeDependent(this);
        }
        this.dependencies = new HashSet<>();
        endWatch();
    }

    @Override
    public Set<AssetNode> getDependents() {
        return Collections.unmodifiableSet(dependents);
    }

    private void setDependents(Set<AssetNode> dependents) {
        this.dependents = dependents;
    }

    void addDependent(AssetNode dependent) {
        this.dependents.add(dependent);
    }

    private void removeDependent(AssetNode dependent) {
        if (this.dependents.remove(dependent)) {
            if (this.dependents.isEmpty()) {
                this.dependents = new HashSet<>();
            }
        }
    }

    void watchRange(DefaultTable table, int rowIndex, int columnIndex) {
        watchRange(table, columnIndex, rowIndex, columnIndex, rowIndex);
    }

    void watchRange(DefaultTable table, int left, int top, int right, int bottom) {
        DefaultTable.Range range = table.tableRange(left, top, right, bottom);
        table.subscribeRange(range, getAssetId());
        Set<DefaultTable.Range> rangeSet = watchedRanges.get(table);
        if (rangeSet == null) {
            rangeSet = new HashSet<>();
            watchedRanges.put(table, rangeSet);
        }
        rangeSet.add(range);
    }

    void endWatch() {
        for (Map.Entry<DefaultTable, Set<DefaultTable.Range>> entry : watchedRanges.entrySet()) {
            DefaultTable table = entry.getKey();
            table.clearRangeWatcher(getAssetId());
        }
        watchedRanges = new HashMap<>();
    }

    @Override
    public List<AssetNode> getChildren() {
        return Collections.unmodifiableList(new ArrayList<>(children));
    }

    protected void setChildren(Set<AssetNode> children) {
        this.children = children;
    }
}
