package com.ctrip.ferriswheel.core.formula;

import junit.framework.TestCase;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TestDirectedAcyclicGraph extends TestCase {
    public void test() {
        DirectedAcyclicGraph graph = new DirectedAcyclicGraph();
        //graph.addEdges(new DependencyGraph.Node(0, null));
    }

    public void testSort() {
        List<Integer> ordered = new DirectedAcyclicGraph<Integer, Object>()
                .addEdges(1, 6)
                .addEdges(1, 7)
                .addEdges(7, 3)
                .addEdges(6, 7)
                .addEdges(6, 5)
                .addEdges(3, 2)
                .addEdges(2, 5)
                .addEdges(2, 4)
                .sort();
        System.out.print("DAG Order:");
        for (int i = 0; i < ordered.size(); i++) {
            Integer id = ordered.get(i);
            System.out.print("\t" + id);
        }
        System.out.println();
        assertTrue((ordered.get(0) == 4 && ordered.get(1) == 5)
                || (ordered.get(0) == 5 && ordered.get(1) == 4));
        assertEquals(Integer.valueOf(2), ordered.get(2));
        assertEquals(Integer.valueOf(3), ordered.get(3));
        assertEquals(Integer.valueOf(7), ordered.get(4));
        assertEquals(Integer.valueOf(6), ordered.get(5));
        assertEquals(Integer.valueOf(1), ordered.get(6));
    }

    public void testCircularException() {
        DirectedAcyclicGraph<Integer, Object> g = new DirectedAcyclicGraph<>();
        g.addEdges(1, 2).addEdges(2, 3);
        try {
            g.addEdges(3, 1);
            fail("IllegalArgumentException expected.");
        } catch (IllegalArgumentException e) {
            //
        }
    }

    /**
     * 1 -> 2, 3
     * 2 -> 4
     * 3 -> 2
     * 5 -> 3
     */
    public void testTrimOutboundToKeyNodes() {
        DirectedAcyclicGraph<Integer, Object> g = new DirectedAcyclicGraph<>();
        g.addEdges(1, 2, 3);
        g.addEdges(2, 4);
        g.addEdges(3, 2);
        g.addEdges(5, 3);

        Set<Integer> keyNodes = new HashSet<>();
        keyNodes.add(2);
        keyNodes.add(5);
        g.trimOutboundToKeyNodes(keyNodes);

        List<Integer> list = g.sort();
        assertEquals(4, list.size());
        assertEquals(Integer.valueOf(2), list.get(0));
        assertEquals(Integer.valueOf(3), list.get(1));
        assertEquals(Integer.valueOf(5), list.get(2));
        assertEquals(Integer.valueOf(1), list.get(3));
    }
}
