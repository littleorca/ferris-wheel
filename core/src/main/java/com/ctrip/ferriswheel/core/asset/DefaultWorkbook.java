package com.ctrip.ferriswheel.core.asset;

import com.ctrip.ferriswheel.common.*;
import com.ctrip.ferriswheel.common.action.ActionListener;
import com.ctrip.ferriswheel.core.action.AddSheet;
import com.ctrip.ferriswheel.core.action.MoveSheet;
import com.ctrip.ferriswheel.core.action.RemoveSheet;
import com.ctrip.ferriswheel.core.action.RenameSheet;
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
    private static final String DEFAULT_WORKBOOK_NAME = "Workbook";

    private final AtomicLong nextId = new AtomicLong(1);
    private final AtomicLong nextSequenceNumber = new AtomicLong(1);
    // package access for convenient of unit test.
    final Map<Long, AssetReference> assetMap = new ConcurrentHashMap<>();
    private DefaultTransaction transaction;
    private DefaultAssetEvaluator evaluator;
    private boolean skipWelding = false;

    // revision sequence number is used to mark last update (like last modified timestamp)
    private final Environment environment;
    private final NamedAssetList<DefaultSheet> sheets;
    private final ActionListenerChain listenerChain;
    private final DefaultReferenceMaintainer referenceMaintainer;

    DefaultWorkbook(Environment environment) {
        // FIXME does workbook really need a name?
        super(DEFAULT_WORKBOOK_NAME, 0);
        attach(this); // self-register
        this.environment = environment;
        this.sheets = new NamedAssetList<>(this);
        this.listenerChain = createListenerChain();
        this.referenceMaintainer = new DefaultReferenceMaintainer(this);
        this.evaluator = new DefaultAssetEvaluator(referenceMaintainer);
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
        listenerChain.publicly(new RenameSheet(sheet.getName(), newName),
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
                });

        // technically rename a sheet doesn't affect any cell value or chart property value,
        // it just affect formulas.
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
     */
    void batch(Consumer<Workbook> consumer) {
        this.skipWelding = true;
        try {
            consumer.accept(this);
        } finally {
            this.skipWelding = false;
            resettle();
        }
    }

    /**
     * Scan and update reference anchor.
     */
    void resettle() {
        LOG.debug("resettle");
        referenceMaintainer.resolveFormulas(this);
    }

    @Override
    public void refresh() {
        refresh(false);
    }

    @Override
    public void refresh(boolean force) {
        EvaluationMode mode = force ? EvaluationMode.Aggressive : EvaluationMode.Normal;
        LOG.debug("refresh: mode={}", mode);
        DefaultTransaction tx = getTransaction();
        tx.evaluate(mode);
        tx.commit();
    }

    @Override
    public void addListener(ActionListener listener) {
        listenerChain.addListener(listener);
    }

    @Override
    public boolean removeListener(ActionListener listener) {
        return listenerChain.removeListener(listener);
    }

    boolean isSkipWelding() {
        return skipWelding;
    }

    private void checkSheetName(String name) throws IllegalArgumentException {
        if (getSheet(name) != null) {
            throw new IllegalArgumentException("Duplicated sheet name!");
        }
    }

    public AssetManager getAssetManager() {
        return this;
    }

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
    public void attach(Asset asset) {
        AssetReference ref = assetMap.get(asset.getAssetId());
        if (ref == null) {
            ref = new AssetReference((AssetNode) asset);
            AssetReference previous = assetMap.putIfAbsent(asset.getAssetId(), ref);
            if (previous != null) {
                ref = previous;
            }
        }
        ref.referenceCount.incrementAndGet();
    }

    @Override
    public AssetNode get(long id) {
        AssetReference ref = assetMap.get(id);
        return ref == null ? null : ref.asset;
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
    public void detach(Asset asset) {
        AssetReference ref = assetMap.get(asset.getAssetId());
        if (ref == null) {
            throw new RuntimeException("This asset is not attached!");
        }
        if (ref.referenceCount.decrementAndGet() <= 0) {
            assetMap.remove(asset.getAssetId());
        }
    }

    @Override
    public TransactionManager getTransactionManager() {
        return this;
    }

    @Override
    public synchronized DefaultTransaction getTransaction() {
        if (transaction == null || transaction.getCurrentPhase() == TransactionPhase.Done) {
            transaction = new DefaultTransaction(this, evaluator, nextSequenceNumber());
        }
        return transaction;
    }

    long nextSequenceNumber() {
        return nextSequenceNumber.getAndIncrement();
    }

    class AssetReference {
        AssetNode asset;
        AtomicInteger referenceCount;

        AssetReference(AssetNode asset) {
            this.asset = asset;
            this.referenceCount = new AtomicInteger(0);
        }
    }
}
