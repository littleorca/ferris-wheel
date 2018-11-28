package com.ctrip.ferriswheel.quarks.syntax.lr;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.ctrip.ferriswheel.quarks.Symbol;

import java.util.Set;

public class Closure implements Serializable {
    private static final long serialVersionUID = 1L;

    public static class Kernel implements Serializable {
        private static final long serialVersionUID = 1L;

        Symbol handle;
        List<Symbol> sequence;
        int pos;

        /**
         * Construct new kernel.
         * 
         * @param handle
         * @param sequence
         * @param pos
         */
        Kernel(Symbol handle, List<Symbol> sequence, int pos) {
            this.handle = handle;
            this.sequence = sequence;
            this.pos = pos;
        }

        public boolean isTerminate() {
            return pos == sequence.size();
        }

        @Override
        public int hashCode() {
            int h = handle.hashCode();
            h = h * 31 + sequence.hashCode();
            h = h * 31 + pos;
            return h;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || !(o instanceof Kernel))
                return false;
            Kernel k = (Kernel) o;
            return handle.equals(k.handle) && sequence.equals(k.sequence)
                    && pos == k.pos;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(handle).append(" ::= ");
            for (int i = 0; i < sequence.size(); i++) {
                if (i == pos)
                    sb.append("*");
                sb.append(sequence.get(i));
            }
            if (pos >= sequence.size())
                sb.append("*");
            return sb.toString();
        }
    }

    public static class Item implements Serializable {
        private static final long serialVersionUID = 1L;

        Kernel kernel;
        Set<Symbol> searchSet;

        /**
         * Construct new item.
         * 
         * @param handle
         * @param sequence
         * @param pos
         * @param searchSet
         *            the new item will add all symbols in the given search set
         *            as it's search set, and no reference to the give set will
         *            be kept.
         */
        public Item(Symbol handle, List<Symbol> sequence, int pos,
                Set<Symbol> searchSet) {
            this.kernel = new Kernel(handle, sequence, pos);
            this.searchSet = new HashSet<Symbol>();
            if (searchSet != null)
                this.searchSet.addAll(searchSet);
        }

        public Kernel getKernel() {
            return kernel;
        }

        @Override
        public int hashCode() {
            int h = kernel.hashCode();
            h = h * 31 + searchSet.hashCode();
            return h;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || !(o instanceof Item))
                return false;

            Item item = (Item) o;
            return kernel.equals(item.kernel)
                    && searchSet.equals(item.searchSet);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(kernel.toString()).append(",");
            for (Symbol s : searchSet) {
                sb.append(" ").append(s);
            }
            return sb.toString();
        }
    }

    int id;
    List<Item> items = new ArrayList<Item>();
    Map<Symbol, Closure> transitions = new HashMap<Symbol, Closure>();

    Closure() {
    }

    public Closure(Symbol handle, List<Symbol> sequence, int pos,
            Set<Symbol> searchSet) {
        items.add(new Item(handle, sequence, pos, searchSet));
    }

    public int getId() {
        return id;
    }

    void setId(int id) {
        this.id = id;
    }

    public void addOrMergeItem(Symbol handle, List<Symbol> sequence, int pos,
            Set<Symbol> searchSet) {
        Item item = new Item(handle, sequence, pos, searchSet);
        for (Item i : items) {
            if (i.kernel.handle.equals(item.kernel.handle)
                    && i.kernel.sequence.equals(item.kernel.sequence)
                    && i.kernel.pos == item.kernel.pos) {
                i.searchSet.addAll(item.searchSet);
                return;
            }
        }
        items.add(item);
    }

    public List<Item> getItems() {
        return items;
    }

    public Map<Symbol, Closure> getTransitions() {
        return transitions;
    }

    @Override
    public int hashCode() {
        return items.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Closure))
            return false;
        Closure c = (Closure) obj;
        return items.equals(c.items);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Closure: ");
        sb.append(id).append("\r\n");
        for (Item item : items) {
            sb.append("  ").append(item).append("\r\n");
        }
        if (transitions != null && !transitions.isEmpty()) {
            for (Entry<Symbol, Closure> entry : transitions.entrySet()) {
                sb.append("  ").append(entry.getKey()).append(" ==> ")
                        .append(entry.getValue().getId()).append("\r\n");
            }
        }
        return sb.toString();
    }

    /**
     * To graphviz dot segment.
     * 
     * @return
     */
    public String toGraphvizDot() {
        StringBuilder sb = new StringBuilder("C");
        sb.append(id).append(" [shape=box, label=\"").append("Closure: ")
                .append(id).append("\\r");
        for (Item item : items) {
            sb.append(item.toString().replaceAll("\"", "\\\\\"")).append("\\l");
        }
        sb.append("\"];\r\n");
        if (transitions != null && !transitions.isEmpty()) {
            for (Entry<Symbol, Closure> entry : transitions.entrySet()) {
                sb.append("C")
                        .append(id)
                        .append(" -> ")
                        .append("C")
                        .append(entry.getValue().getId())
                        .append(" [label=\"")
                        .append(entry.getKey().toString()
                                .replaceAll("\"", "\\\\\"")).append("\"];\r\n");
            }
        }

        return sb.toString();
    }
}
