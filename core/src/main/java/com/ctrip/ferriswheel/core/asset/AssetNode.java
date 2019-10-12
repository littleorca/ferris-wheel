package com.ctrip.ferriswheel.core.asset;

import java.util.*;

abstract class AssetNode implements Asset {
    private final AssetManager assetManager;
    private final long assetId;
    private AssetNode parent;
    private Set<AssetNode> children = new LinkedHashSet<>(); // bound children
    private Set<AssetNode> dependencies = new HashSet<>();
    private Set<AssetNode> dependents = new HashSet<>();
    private volatile long currentRevision = 0;
    private volatile long evaluatedRevision = 0;
    private boolean valid = true;

    protected AssetNode(AssetManager assetManager) {
        this.assetManager = assetManager;
        this.assetId = assetManager.nextAssetId();
    }

    /**
     * Special constructor for asset implementation that also implemented
     * {@link AssetManager}, in which case the subclass cannot pass itself
     * to super constructor. This kind of asset should override
     * {@link #getAssetManager()} to reveal itself as asset manager.
     *
     * @param assetId
     */
    AssetNode(long assetId) {
        this.assetManager = null; // TODO looks bad
        this.assetId = assetId;
    }

    @Override
    public EvaluationState evaluate(EvaluationContext context) {
        long maxDependencyRevision = scanMaxDependencyRevision();
        boolean dependenciesChanged = maxDependencyRevision > getCurrentRevision();
        boolean selfChanged = getCurrentRevision() > getEvaluatedRevision();
        EvaluationState resultState = EvaluationState.DONE;
        if (dependenciesChanged
                || selfChanged
                || isVolatile()
                || EvaluationMode.Aggressive == context.getEvaluationMode()) {
            setCurrentRevision(getAssetManager().getTransactionManager().getTransaction().getTransactionId());
            beforeEvaluating(context);
            resultState = doEvaluate(context);
            afterEvaluating(context);
            // TODO FIXME for async evaluation, the job may still running,
            //  shall we update eval revision after it really done?
            setEvaluatedRevision(getCurrentRevision());
        }
        return resultState;
    }

    protected long scanMaxDependencyRevision() {
        long maxRevision = 0;
        for (AssetNode dependency : dependencies) {
            if (dependency.getCurrentRevision() > maxRevision) {
                maxRevision = dependency.getCurrentRevision();
            }
        }
        for (AssetNode child : children) {
            if (child.getCurrentRevision() > maxRevision) {
                maxRevision = child.getCurrentRevision();
            }
        }
        return maxRevision;
    }

    /**
     * Overridable.
     *
     * @param context
     */
    protected void beforeEvaluating(EvaluationContext context) {
        // dummy
    }

    protected abstract EvaluationState doEvaluate(EvaluationContext context);

    /**
     * Overridable.
     *
     * @param context
     */
    protected void afterEvaluating(EvaluationContext context) {
        // dummy
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

    protected boolean isAttached() {
        return getAssetManager().exists(assetId);
    }

    protected void bindChild(AssetNode child) {
        children.add(child);
        child.parent = this;
        if (isAttached()) {
            getAssetManager().attach(child);
            child.onAttached();
        }
        markDirty();
    }

    protected void unbindChild(AssetNode child) {
        children.remove(child);
        child.parent = null;
        if (isAttached()) {
            getAssetManager().detach(child);
            child.onDetached();
        }
        markDirty();
    }

    protected void onAttached() {
        if (currentRevision == 0) {
            markDirty();
        }
        for (AssetNode child : children) {
            getAssetManager().attach(child);
            child.onAttached();
        }
        afterAttached();
    }

    protected void onDetached() {
        // mark all dependent nodes as dirty.
        // TODO mark dirty is not enough, should fix reference
        for (AssetNode node : getDependents()) {
            node.onExternalDependencyChange();
        }

        clearDependencies();
        for (AssetNode child : children) {
            getAssetManager().detach(child);
            child.onDetached();
        }
        afterDetached();
    }

    // overridable
    protected void afterAttached() {
    }

    // overridable
    protected void afterDetached() {
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

    protected long getCurrentRevision() {
        return currentRevision;
    }

    protected void setCurrentRevision(long currentRevision) {
        this.currentRevision = currentRevision;
    }

    protected long getEvaluatedRevision() {
        return evaluatedRevision;
    }

    protected void setEvaluatedRevision(long evaluatedRevision) {
        this.evaluatedRevision = evaluatedRevision;
    }

    protected void markDirty() {
        Transaction tx = getTransaction();
        setCurrentRevision(tx.getTransactionId());
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
    public boolean isVolatile() {
        return false;
    }

    @Override
    public boolean isPhantom() {
        return false;
    }

    @Override
    public boolean isDirty() {
        return getCurrentRevision() > getEvaluatedRevision();
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

    void addDependency(AssetNode dependency) {
        requireTransactionPhase(TransactionPhase.Polluting);
        this.dependencies.add(dependency);
        dependency.addDependent(this);
    }

    private void requireTransactionPhase(TransactionPhase phase) {
        Transaction tx = getTransaction();
        if (!phase.equals(tx.getCurrentPhase())) {
            throw new IllegalStateException("Operation require transaction phase " +
                    phase + " but current phase is " + tx.getCurrentPhase());
        }
    }

    private Transaction getTransaction() {
        return getAssetManager().getTransactionManager().getTransaction();
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
    }

    @Override
    public Set<AssetNode> getDependents() {
        return Collections.unmodifiableSet(dependents);
    }

    /**
     * Do not call for self instance.
     *
     * @param dependent
     */
    void addDependent(AssetNode dependent) {
        this.dependents.add(dependent);
        onExternalDependentChange();
    }

    /**
     * Do not call for self instance.
     *
     * @param dependent
     */
    private void removeDependent(AssetNode dependent) {
        if (this.dependents.remove(dependent)) {
            if (this.dependents.isEmpty()) {
                this.dependents = new HashSet<>();
            }
            onExternalDependentChange();
        }
    }

    /**
     * Overridable
     */
    protected void onExternalDependencyChange() {
        markDirty();
    }

    /**
     * Overridable
     */
    protected void onExternalDependentChange() {
        // dummy
    }

    @Override
    public List<AssetNode> getChildren() {
        return Collections.unmodifiableList(new ArrayList<>(children));
    }

}
