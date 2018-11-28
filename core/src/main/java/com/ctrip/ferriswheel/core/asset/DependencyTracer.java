package com.ctrip.ferriswheel.core.asset;

import com.ctrip.ferriswheel.core.formula.CalcChain;
import com.ctrip.ferriswheel.core.formula.DirectedAcyclicGraph;
import com.ctrip.ferriswheel.core.ref.CellRef;
import com.ctrip.ferriswheel.core.formula.CalcChain;
import com.ctrip.ferriswheel.core.formula.DirectedAcyclicGraph;
import com.ctrip.ferriswheel.core.intf.Asset;
import com.ctrip.ferriswheel.core.intf.AssetManager;
import com.ctrip.ferriswheel.core.intf.Table;
import com.ctrip.ferriswheel.core.intf.VariantNode;
import com.ctrip.ferriswheel.core.ref.CellRef;
import com.ctrip.ferriswheel.core.util.EscapeHelper;
import com.ctrip.ferriswheel.core.util.References;
import com.ctrip.ferriswheel.core.view.Rectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * TODO since range tracer merged into this class, maybe something can be done more friendly.
 */
class DependencyTracer {
    private static final Logger LOG = LoggerFactory.getLogger(DependencyTracer.class);
    private AssetManager assetManager;
    private DirectedAcyclicGraph<Long, Object> graph = new DirectedAcyclicGraph();
    private CalcChain calcChain;
    private Map<Range, Set<Long>> rangeToNodes = new HashMap<>();
    private Map<Long, Set<Range>> nodeToRanges = new HashMap<>();

    DependencyTracer(AssetManager assetManager) {
        this.assetManager = assetManager;
        this.assetManager.subscribe(new AssetManager.AssetChangeCallback() {
            @Override
            public void onAssetEmployed(Asset asset) {
            }

            @Override
            public void onAssetUpdate(Asset asset) {
                //TODO
            }

            @Override
            public void onAssetDismissed(Asset asset) {
                if (asset instanceof VariantNode) {
                    if (!assetManager.exists(asset.getAssetId())) {
                        clearRangeDependencies(asset.getAssetId());
                    }
                }
            }
        });
    }

    /**
     * Get current calculating chain.
     *
     * @return
     */
    public CalcChain getCalcChain() {
        return calcChain;
    }

    /**
     * Clear dependency graph.
     */
    public void clearDependencyGraph() {
        graph.clear();
        calcChain = null;
    }

    /**
     * Add dependencies.
     *
     * @param from ID of the node which depends on the <code>to</code> nodes.
     * @param to   IDs of nodes that the <code>from</code> node depends on.
     * @return
     */
    public DependencyTracer addDependencies(Long from, Long... to) {
        graph.addEdges(from, to);
        return this;
    }

    /**
     * Rebuild calculating chain based on current dependency graph.
     *
     * @return
     */
    public CalcChain rebuildCalcChain() {
        List<Long> ordered = graph.sort();
        this.calcChain = new CalcChain(ordered);

        //// debug
        if (LOG.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder("## rebuilt calc chain ##");
            for (int i = 0; i < calcChain.size(); i++) {
                Long id = calcChain.get(i);
                Asset asset = assetManager.get(id);
                sb.append(" > ").append(asset.getAssetId()).append(":");
                if (asset instanceof DefaultCell) {
                    DefaultCell cell = (DefaultCell) asset;
                    sb.append(References.toFormula(new CellRef(cell, false, false)));
                } else if (asset instanceof DefaultTable) {
                    DefaultTable table = (DefaultTable) asset;
                    sb.append(EscapeHelper.escape(table.getSheet().getName()))
                            .append("!")
                            .append(EscapeHelper.escape(table.getName()));
                } else if (asset instanceof ValueNode) {
                    sb.append("vn@").append(((ValueNode) asset).getParentAsset().getClass().getSimpleName());
                } else {
                    sb.append(asset.getClass().getSimpleName());
                }
            }
            LOG.debug(sb.toString());
        }

        return calcChain;
    }

    /**
     * Trace one-node range dependency.
     * If dependency is one certain node, consider it as a range that has one by one node.
     *
     * @param table    In which table the one-node range residents.
     * @param rowIndex Row index.
     * @param colIndex Column index.
     * @param nodeId   ID of the node which depends on the one-node range.
     * @see #traceRange(DefaultTable, int, int, int, int, long)
     */
    public void traceRange(DefaultTable table, int rowIndex, int colIndex, long nodeId) {
        traceRange(table, colIndex, rowIndex, colIndex, rowIndex, nodeId);
    }

    /**
     * Trace range dependency.
     *
     * @param table  In which table the range residents.
     * @param left   Left index of the range
     * @param top    Top index of the range.
     * @param right  Right index of the range.
     * @param bottom Bottom index of the range.
     * @param nodeId ID of the node which depends on the specified range.
     * @see #traceRange(DefaultTable, int, int, long)
     */
    public void traceRange(DefaultTable table, int left, int top, int right, int bottom, long nodeId) {
        if (left == -1) {
            left = 0;
        }
        if (top == -1) {
            top = 0;
        }
        if (right == -1) {
            right = Integer.MAX_VALUE;
        }
        if (bottom == -1) {
            bottom = Integer.MAX_VALUE;
        }
        Range range = new Range(table.getAssetId(), left, top, right, bottom);
        Set<Long> nodeIds = rangeToNodes.get(range);
        if (nodeIds == null) {
            nodeIds = new HashSet<>();
            rangeToNodes.put(range, nodeIds);
        }
        nodeIds.add(nodeId);
        Set<Range> ranges = nodeToRanges.get(nodeId);
        if (ranges == null) {
            ranges = new HashSet<>();
            nodeToRanges.put(nodeId, ranges);
        }
        ranges.add(range);
    }

    /**
     * Clear range dependencies related to the specified node ID.
     *
     * @param nodeId
     */
    public void clearRangeDependencies(long nodeId) {
        Set<Range> ranges = nodeToRanges.remove(nodeId);
        if (ranges == null) {
            return;
        }
        for (Range r : ranges) {
            Set<Long> nodeIds = rangeToNodes.get(r);
            nodeIds.remove(nodeId);
            if (nodeIds.isEmpty()) {
                rangeToNodes.remove(r);
            }
        }
    }

    /**
     * Find overlapped ranges.
     *
     * @param table
     * @param left
     * @param top
     * @param right
     * @param bottom
     * @return
     */
    public List<Range> findOverlappedRanges(Table table,
                                            Integer left,
                                            Integer top,
                                            Integer right,
                                            Integer bottom) {
        // 先用笨方法实现功能
        List<Range> ranges = new LinkedList<>();
        for (Range range : rangeToNodes.keySet()) {
            if (range.tableId != table.getAssetId()) {
                continue;
            }
            if (range.isOverlap(left, top, right, bottom)) {
                ranges.add(range);
            }
        }
        return ranges;
    }

    /**
     * Get node IDs which depend on the specified range.
     *
     * @param range
     * @return
     */
    public Set<Long> getDependencies(Range range) {
        Set<Long> set = rangeToNodes.get(range);
        return set == null ? Collections.emptySet() : set;
    }

    class Range extends Rectangle {
        long tableId;

        public Range() {
        }

        public Range(long tableId, int left, int top, int right, int bottom) {
            super(left, top, right, bottom);
            this.tableId = tableId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            Range range = (Range) o;
            return Objects.equals(tableId, range.tableId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), tableId);
        }

        public boolean isOverlap(Integer left, Integer top, Integer right, Integer bottom) {
            return isRowOverlap(top, bottom) && isColumnOverlap(left, right);
        }

        private boolean isRowOverlap(Integer top, Integer bottom) {
            return (top == null || top <= this.getBottom()) && (bottom == null || bottom >= this.getTop());
        }

        private boolean isColumnOverlap(Integer left, Integer right) {
            return (left == null || left <= this.getRight()) && (right == null || right >= this.getLeft());
        }
    }

}
