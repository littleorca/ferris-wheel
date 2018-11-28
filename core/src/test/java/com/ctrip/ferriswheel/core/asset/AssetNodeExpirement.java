package com.ctrip.ferriswheel.core.asset;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class AssetNodeExpirement {
    static class Node {
        Node parent;
        Map<Serializable, Node> children = new HashMap<>();
        Map<String, Object> payload = new HashMap<>();

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            toString(sb, 0);
            return sb.toString();
        }

        StringBuilder toString(StringBuilder sb, int indent) {
            children.forEach((key, value) -> {
                indent(sb, indent).append(key).append(": ").append(value.payload).append("\n");
                value.toString(sb, indent + 1);
            });
            return sb;
        }

        StringBuilder indent(StringBuilder sb, int indent) {
            for (int i = 0; i < indent; i++) {
                sb.append("    ");
            }
            return sb;
        }
    }

    static class Root {
        Node committed;
        Node current;
    }

    public static void main(String[] args) {
        Root root = new Root();
        root.committed = new Node();
        root.committed.parent = root.committed;

        Node s1 = new Node();
        s1.parent = root.committed;
        root.committed.children.put("s1", s1);

        Node s2 = new Node();
        s2.parent = root.committed;
        root.committed.children.put("s2", s2);

        Node t21 = new Node();
        t21.parent = s2;
        s2.children.put("t2-1", t21);

        Node r1 = new Node();
        r1.parent = t21;
        t21.children.put(0, r1);

        Node cell1 = new Node();
        cell1.parent = r1;
        cell1.payload.put("intValue", 1024);

        Node cell2 = new Node();
        cell2.parent = r1;
        cell2.payload.put("formulaString", "A1*2");

        r1.children.put(0, cell1);
        r1.children.put(1, cell2);

        System.out.println(root.committed);
    }
}
