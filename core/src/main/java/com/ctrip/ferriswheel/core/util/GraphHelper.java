/*
 * MIT License
 *
 * Copyright (c) 2018 Ctrip.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.ctrip.ferriswheel.core.util;

import com.ctrip.ferriswheel.common.Workbook;
import com.ctrip.ferriswheel.common.automaton.QueryAutomaton;
import com.ctrip.ferriswheel.common.chart.Chart;
import com.ctrip.ferriswheel.common.query.QueryTemplate;
import com.ctrip.ferriswheel.common.table.Cell;
import com.ctrip.ferriswheel.common.table.Table;
import com.ctrip.ferriswheel.core.asset.*;
import com.ctrip.ferriswheel.core.formula.DirectedAcyclicGraph;
import com.ctrip.ferriswheel.core.ref.CellReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.Stack;

/**
 * @author liuhaifeng
 */
public class GraphHelper {
    private static final Logger LOG = LoggerFactory.getLogger(GraphHelper.class);

    public static DirectedAcyclicGraph<Long, Asset> buildGraph(Asset root,
                                                               Set<Long> dirtyNodeCollector,
                                                               Set<Long> volatileNodeCollector) {
        DirectedAcyclicGraph<Long, Asset> graph = new DirectedAcyclicGraph<>();
        Stack<Asset> stack = new Stack<>();
        stack.push(root);
        while (!stack.isEmpty()) {
            Asset node = stack.pop();
            if (dirtyNodeCollector != null && node.isDirty()) {
                dirtyNodeCollector.add(node.getAssetId());
            }
            if (volatileNodeCollector != null && node.isVolatile()) {
                volatileNodeCollector.add(node.getAssetId());
            }
            if (node.getDependencies() != null && !node.getDependencies().isEmpty()) {
                Long[] dependencies = new Long[node.getDependencies().size()];
                int i = 0;
                for (Asset dependency : node.getDependencies()) {
                    dependencies[i++] = dependency.getAssetId();
                }
                graph.addEdges(node.getAssetId(), dependencies);
            }
            if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                Long[] dependencies = new Long[node.getChildren().size()];
                int i = 0;
                for (Asset child : node.getChildren()) {
                    dependencies[i++] = child.getAssetId();
                    stack.push(child);
                }
                graph.addEdges(node.getAssetId(), dependencies);
            }
        }
        return graph;
    }

    public static String graphToDot(DefaultWorkbook workbook, AssetManager assetManager) {
        return graphToDot(buildGraph(workbook, null, null), assetManager);
    }

    public static String graphToDot(DirectedAcyclicGraph<Long, Asset> graph, AssetManager assetManager) {
        return graph.toDot(id -> {
            Asset asset = assetManager.get(id);
            if (asset instanceof Cell) {
                return id +
                        "\\n" +
                        References.toFormula(new CellReference((DefaultCell) asset, false, false));
            }
            if (asset instanceof Table) {
                return id +
                        "\\n" +
                        EscapeHelper.escapeNameIfNeeded(((DefaultTable) asset).getSheet().getName()) +
                        "!" +
                        EscapeHelper.escapeNameIfNeeded(((Table) asset).getName());
            }
            if (asset instanceof AbstractAutomaton) {
                Table table = (Table) asset.getParent();
                return id +
                        "\\n" +
                        EscapeHelper.escapeNameIfNeeded(((DefaultTable) table).getSheet().getName()) +
                        "!" +
                        EscapeHelper.escapeNameIfNeeded(table.getName()) +
                        "@" +
                        asset.getClass().getSimpleName();
            }
            if (asset instanceof Chart) {
                return id +
                        "\\n" +
                        EscapeHelper.escapeNameIfNeeded(((DefaultChart) asset).getSheet().getName()) +
                        "#" +
                        EscapeHelper.escapeNameIfNeeded(((Chart) asset).getName());
            }
            StringBuilder sb = new StringBuilder();
            while (asset != null && !(asset instanceof Workbook)) {
                sb.insert(0, ">");
                if (asset instanceof NamedAsset) {
                    sb.insert(0, ((NamedAsset) asset).getName());
                } else if (asset instanceof QueryAutomaton) {
                    sb.insert(0, "query");
                } else if (asset instanceof DefaultPivotAutomaton) {
                    sb.insert(0, "pivot");
                } else if (asset instanceof QueryTemplate) {
                    sb.insert(0, "template");
                } else if (asset instanceof ValueNode) {
                    sb.insert(0, "vn");
                } else {
                    sb.insert(0, asset.getClass().getSimpleName());
                }
                asset = asset.getParent();
            }
            return sb.insert(0, "\\n").insert(0, String.valueOf(id)).toString();
        });
    }
}
