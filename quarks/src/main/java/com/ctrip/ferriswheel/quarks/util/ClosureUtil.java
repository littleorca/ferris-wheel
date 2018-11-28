package com.ctrip.ferriswheel.quarks.util;

import com.ctrip.ferriswheel.quarks.syntax.lr.Closure;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class ClosureUtil {

    public static String dumpGraphvizDot(Closure root) {
        StringBuilder sb = new StringBuilder("digraph G {");
        Stack<Closure> closures = new Stack<>();
        Set<Closure> processed = new HashSet<>();
        closures.add(root);
        while (!closures.isEmpty()) {
            Closure c = closures.pop();
            processed.add(c);
            sb.append(c.toGraphvizDot());
            for (Closure follow : c.getTransitions().values()) {
                if (!processed.contains(follow))
                    closures.push(follow);
            }
        }
        sb.append("}");
        return sb.toString();
    }

    public static void dumpGraphvizDot(Closure root, OutputStream os)
            throws IOException {
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
        bw.write("digraph G {\n");
        Stack<Closure> closures = new Stack<>();
        Set<Closure> processed = new HashSet<>();
        closures.add(root);
        while (!closures.isEmpty()) {
            Closure c = closures.pop();
            processed.add(c);
            bw.write(c.toGraphvizDot());
            for (Closure follow : c.getTransitions().values()) {
                if (!processed.contains(follow))
                    closures.push(follow);
            }
        }
        bw.write("}");
        bw.close();
    }

    public static void dump(Closure root) {
        StringBuilder sb = new StringBuilder();
        HashSet<Closure> processed = new HashSet<Closure>();
        Stack<Closure> stack = new Stack<Closure>();
        stack.push(root);
        while (!stack.isEmpty()) {
            Closure c = stack.pop();
            sb.append(c);
            processed.add(c);
            for (Closure child : c.getTransitions().values()) {
                if (!processed.contains(child)) {
                    stack.push(child);
                }
            }
        }
        System.out.println(sb);
    }
}
