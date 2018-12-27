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

import com.ctrip.ferriswheel.api.*;
import com.ctrip.ferriswheel.api.chart.Chart;
import com.ctrip.ferriswheel.api.table.Cell;
import com.ctrip.ferriswheel.api.table.QueryAutomaton;
import com.ctrip.ferriswheel.api.query.QueryTemplate;
import com.ctrip.ferriswheel.api.table.Table;
import com.ctrip.ferriswheel.core.asset.*;
import com.ctrip.ferriswheel.core.formula.CalcChain;
import com.ctrip.ferriswheel.core.formula.DirectedAcyclicGraph;
import com.ctrip.ferriswheel.core.ref.CellRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Stack;

/**
 * @author liuhaifeng
 */
public class GraphHelper {
    private static final Logger LOG = LoggerFactory.getLogger(GraphHelper.class);

    public static DirectedAcyclicGraph<Long, Asset> buildGraph(Asset root) {
        DirectedAcyclicGraph<Long, Asset> graph = new DirectedAcyclicGraph<>();
        Stack<Asset> stack = new Stack<>();
        stack.push(root);
        while (!stack.isEmpty()) {
            Asset node = stack.pop();
            if (node.getDependencies() != null && !node.getDependencies().isEmpty()) {
                Long[] dependencies = new Long[node.getDependencies().size()];
                int i = 0;
                for (Asset dependency : node.getDependencies()) {
                    dependencies[i++] = dependency.getAssetId();
                }
                graph.addEdges(node.getAssetId(), dependencies);
            }
            for (Asset child : node.getChildren()) {
                stack.push(child);
            }
        }
        return graph;
    }

    public static CalcChain buildCalcChain(Asset root) {
        DirectedAcyclicGraph<Long, Asset> graph = buildGraph(root);
        List<Long> ordered = graph.sort();
        CalcChain calcChain = new CalcChain(ordered);

        //// debug
        if (LOG.isDebugEnabled()) {
            LOG.debug(graph.toDot(null));
        }

        return calcChain;
    }

    public static String graphToDot(DefaultWorkbook workbook, AssetManager assetManager) {
        return graphToDot(buildGraph(workbook), assetManager);
    }

    public static String graphToDot(DirectedAcyclicGraph<Long, Asset> graph, AssetManager assetManager) {
        return graph.toDot(id -> {
            Asset asset = assetManager.get(id);
            if (asset instanceof Cell) {
                return id +
                        "\\n" +
                        References.toFormula(new CellRef((DefaultCell) asset, false, false));
            }
            if (asset instanceof Table) {
                return id +
                        "\\n" +
                        EscapeHelper.escape(((Table) asset).getSheet().getName()) +
                        "!" +
                        EscapeHelper.escape(((Table) asset).getName());
            }
            if (asset instanceof AbstractTableAutomaton) {
                Table table = ((AbstractTableAutomaton) asset).getTable();
                return id +
                        "\\n" +
                        EscapeHelper.escape(table.getSheet().getName()) +
                        "!" +
                        EscapeHelper.escape(table.getName()) +
                        "@" +
                        asset.getClass().getSimpleName();
            }
            if (asset instanceof Chart) {
                return id +
                        "\\n" +
                        EscapeHelper.escape(((DefaultChart) asset).getSheet().getName()) +
                        "#" +
                        EscapeHelper.escape(((Chart) asset).getName());
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