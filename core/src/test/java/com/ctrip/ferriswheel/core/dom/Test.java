/*
 * MIT License
 *
 * Copyright (c) 2018-2019 Ctrip.com
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
 */

package com.ctrip.ferriswheel.core.dom;

import com.ctrip.ferriswheel.common.variant.DynamicValue;
import com.ctrip.ferriswheel.common.variant.Value;
import com.ctrip.ferriswheel.core.dom.helper.NodeSnapshotMapper;
import com.ctrip.ferriswheel.core.dom.helper.Serializer;
import com.ctrip.ferriswheel.core.dom.helper.SnapshotHelper;
import com.ctrip.ferriswheel.core.dom.impl.WorkbookDocumentImpl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Test {
    public static void main(String[] args) {
        WorkbookDocument d = new WorkbookDocumentImpl();
        WorkbookElement w = d.getDocumentElement();
        w.setAttribute("version", "0.1");
        System.out.println(Serializer.serialize(w));

        SheetElement s1 = d.createElement(SheetElement.class);
        s1.setAttribute("name", "Sheet1");
        s1.setAttribute("layout", "0 0 100 100");
        w.appendChild(s1);
        ChartElement c = d.createElement(ChartElement.class);
        c.setType("line");
        c.setTitle(new DynamicValue("t1!A1", Value.str("foobar")));
        c.setCategories(new DynamicValue("t1!B1:D1", Value.list(Arrays.asList(
                Value.str("C1"), Value.str("C1"), Value.str("C3")))));
        s1.appendChild(c);
        TableElement t = d.createElement(TableElement.class);
        s1.appendChild(t);

        SheetElement s2 = d.createElement(SheetElement.class);
        s2.setAttribute("name", "Sheet-2");
        s2.setAttribute("layout", "100 0 100 100");
        w.appendChild(s2);

        System.out.println(Serializer.serialize(w));

        SheetElement s3 = d.createElement(SheetElement.class);
        s3.setAttribute("name", "Sheet 3");
        w.insertChild(s3, s2);

        System.out.println(Serializer.serialize(w));

        s2.removeAttribute("layout");
        System.out.println(Serializer.serialize(w));

        w.removeChild(s3);
        System.out.println(Serializer.serialize(w));

        NodeSnapshotMapper mapper = new NodeSnapshotMapper() {
            private Map<Node, NodeSnapshot> map = new HashMap<>();

            @Override
            public void map(Node node, NodeSnapshot snapshot) {
                map.put(node, snapshot);
            }

            @Override
            public NodeSnapshot map(Node node) {
                return map.get(node);
            }
        };
        NodeSnapshot tree = SnapshotHelper.snapshotTree(w, mapper);
        System.out.println(tree);

        ChartElement chart = d.createElement(ChartElement.class);
        chart.setType("line");
        chart.setTitle(new DynamicValue("=t1!A1", Value.str("Line chart 1")));

        System.out.println(Serializer.serialize(w));
        NodeSnapshot tree2 = SnapshotHelper.snapshotTree(w, mapper);
        System.out.println(tree2);
    }
}
