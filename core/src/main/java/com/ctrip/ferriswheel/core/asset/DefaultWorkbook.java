package com.ctrip.ferriswheel.core.asset;

import com.ctrip.ferriswheel.common.*;
import com.ctrip.ferriswheel.common.action.ActionContextManager;
import com.ctrip.ferriswheel.common.action.ActionListener;
import com.ctrip.ferriswheel.core.action.AddSheet;
import com.ctrip.ferriswheel.core.action.MoveSheet;
import com.ctrip.ferriswheel.core.action.RemoveSheet;
import com.ctrip.ferriswheel.core.action.RenameSheet;
import com.ctrip.ferriswheel.core.formula.eval.ReferenceResolver;
import com.ctrip.ferriswheel.core.util.UUIDGen;
import com.ctrip.ferriswheel.core.util.UnmodifiableIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class DefaultWorkbook extends NamedAssetNode implements Workbook, AssetManager, TransactionManager {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultWorkbook.class);
    private static final VersionImpl VERSION = new VersionImpl(0, 0, 0);

    private final AtomicLong nextId = new AtomicLong(1);
    private final AtomicLong nextSequenceNumber = new AtomicLong(1);
    // package access for convenient of unit test.
    final Map<Long, AssetReference> assetMap = new ConcurrentHashMap<>();
    private DefaultTransaction transaction;
    private DefaultAssetEvaluator evaluator;

    // revision sequence number is used to mark last update (like last modified timestamp)
    private final Environment environment;
    private final NamedAssetList<DefaultSheet> sheets;
    private final ActionListenerChain listenerChain;
    private final ActionContextManager actionContextManager = new DefaultActionContextManager();
    private final DefaultReferenceMaintainer referenceMaintainer;

    DefaultWorkbook(Environment environment) {
        // FIXME workbook name
        super(UUIDGen.generate().toString(), 0);
        employ(this); // self-register
        this.environment = environment;
        this.sheets = new NamedAssetList<>(this);
        this.listenerChain = createListenerChain();
        this.referenceMaintainer = new DefaultReferenceMaintainer(this);
        this.evaluator = new DefaultAssetEvaluator(referenceMaintainer, this);
    }

    @Override
    public void setName(String name) {
        super.setName(name);
    }

    @Override
    public VersionImpl getVersion() {
        return VERSION;
    }

    @Override
    public int getSheetCount() {
        return sheets.size();
    }

    @Override
    public DefaultSheet getSheet(int index) {
        return sheets.get(index);
    }

    @Override
    public DefaultSheet getSheet(String name) {
        return sheets.get(name);
    }

    @Override
    public DefaultSheet addSheet(String name) {
        checkSheetName(name);
        return listenerChain.publicly(new AddSheet(name, sheets.size()), () -> {
            DefaultSheet sheet = createSheet(name);
            sheets.add(sheet);
            return sheet;
        });
    }

    @Override
    public DefaultSheet addSheet(int index, String name) {
        checkSheetName(name);
        return listenerChain.publicly(new AddSheet(name, index), () -> {
            DefaultSheet sheet = createSheet(name);
            sheets.add(index, sheet);
            return sheet;
        });
    }

    private DefaultSheet createSheet(String name) {
        return new DefaultSheet(name, getAssetManager(), listenerChain);
    }

    @Override
    public void renameSheet(String oldName, String newName) {
        DefaultSheet sheet = getSheet(oldName);
        if (sheet == null) {
            throw new IllegalArgumentException("Invalid sheet name: " + oldName);
        }
        if (newName.equals(sheet.getName())) {
            return; // nothing changed
        }
        checkSheetName(newName);
        withoutRefresh(() -> listenerChain.publicly(new RenameSheet(sheet.getName(), newName),
                () -> {
                    if (!sheets.rename(oldName, newName)) {
                        throw new RuntimeException("Failed to rename sheet.");
                    }
                    for (SheetAsset asset : sheet) {
                        if (asset instanceof DefaultTable) {
                            DefaultTable table = (DefaultTable) asset;
                            table.onTableUpdate();
                        }
                    }
                }));

        // technically rename a sheet doesn't affect any cell value or chart property value,
        // it just affect formulas.
        refreshIfNeeded();
    }

    @Override
    public void moveSheet(String name, int index) {
        DefaultSheet sheet = getSheet(name);
        if (sheet == null) {
            throw new IllegalArgumentException("Invalid sheet name: " + name);
        }
        listenerChain.publicly(new MoveSheet(name, index), () -> {
            sheets.remove(sheet.getName());
            sheets.add(index, sheet);
        });
    }

    @Override
    public DefaultSheet removeSheet(int index) {
        return doRemoveSheet(getSheet(index));
    }

    @Override
    public DefaultSheet removeSheet(String name) {
        return doRemoveSheet(getSheet(name));
    }

    private DefaultSheet doRemoveSheet(DefaultSheet sheet) {
        if (sheet == null) {
            return null;
        }
        return listenerChain.publicly(new RemoveSheet(sheet.getName()), () -> {
            sheets.remove(sheet.getName());
            return sheet;
        });
    }

    /**
     * A batch ops manipulate workbook without settle formula references down
     * immediately. It is useful in some scenarios like filling data in
     * arbitrary order which may put a formula with cell references that
     * not resolvable yet.
     *
     * @param consumer
     * @param forceRefresh
     */
    void batch(Consumer<Workbook> consumer, boolean forceRefresh) {
        actionContextManager.withContext(new DefaultActionContext(true, isSkipRefresh(), isForceRefresh()),
                this, consumer);
        resettle();
        refresh(forceRefresh);
    }

    /**
     * Scan and update reference anchor.
     * TODO currently dependencies also been update during this procedure, need to split them.
     */
    void resettle() {
        LOG.debug("resettle");

        withoutRefresh(() -> {
            referenceMaintainer.resolveFormulas(this);
        });
    }

    @Override
    public void refresh() {
        refresh(false);
    }

    @Override
    public void refresh(boolean force) {
        actionContextManager.withContext(new DefaultActionContext(isSkipWelding(), isSkipRefresh(), force),
                () -> {
                    LOG.debug("refresh");
                    EvaluationMode mode = isForceRefresh() ? EvaluationMode.Aggressive : EvaluationMode.Normal;
                    evaluator.evaluate(this, mode);
                });
    }

    @Override
    public void addListener(ActionListener listener) {
        listenerChain.addListener(listener);
    }

    @Override
    public boolean removeListener(ActionListener listener) {
        return listenerChain.removeListener(listener);
    }

    protected void refreshIfNeeded() {
        if (!isSkipWelding() && !isSkipRefresh()) {
            refresh();
        }
    }

    boolean isSkipRefresh() {
        return actionContextManager.isSkipRefresh();
    }

    boolean isSkipWelding() {
        return actionContextManager.isSkipWelding();
    }

    boolean isForceRefresh() {
        return actionContextManager.isForceRefresh();
    }

    private void checkSheetName(String name) throws IllegalArgumentException {
        if (getSheet(name) != null) {
            throw new IllegalArgumentException("Duplicated sheet name!");
        }
    }

    public AssetManager getAssetManager() {
        return this;
    }

    //// ------------------------------------------------------------------------------------
    //// callbacks for update dependencies and refresh formula results after certain changes.
    //// ------------------------------------------------------------------------------------

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Sheet sheet : this) {
            ((DefaultSheet) sheet).toString(sb);
        }
        return sb.toString();
    }

    private ActionListenerChain createListenerChain(ActionListener... extraActionListeners) {
        List<ActionListener> listeners = new ArrayList<>();
        if (extraActionListeners != null) {
            for (ActionListener extraListener : extraActionListeners) {
                listeners.add(extraListener);
            }
        }
        return new ActionListenerChain(listeners);
    }

    void withoutRefresh(Runnable runnable) {
        actionContextManager.withContext(new DefaultActionContext(isSkipWelding(), true, isForceRefresh()), runnable);
    }

    @Override
    public Iterator<Sheet> iterator() {
        return new UnmodifiableIterator(sheets.iterator());
    }

    Environment getEnvironment() {
        return environment;
    }

    @Override
    protected EvaluationState doEvaluate(EvaluationContext context) {
        // anything to do?
        return EvaluationState.DONE;
    }


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
    }

    @Override
    public Asset get(long id) {
        AssetReference ref = assetMap.get(id);
        return ref == null ? null : ref.asset;
    }

    @Override
    public ReferenceResolver getReferenceResolver() {
        return referenceMaintainer;
    }

    @Override
    public ReferenceMaintainer getReferenceMaintainer() {
        return referenceMaintainer;
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
    }

    @Override
    public synchronized DefaultTransaction getTransaction() {
        if (transaction == null || transaction.getCurrentPhase() == TransactionPhase.Done) {
            transaction = new DefaultTransaction(nextSequenceNumber());
        }
        return transaction;
    }

    long nextSequenceNumber() {
        return nextSequenceNumber.getAndIncrement();
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
