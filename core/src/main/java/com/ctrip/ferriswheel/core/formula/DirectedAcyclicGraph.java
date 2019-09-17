package com.ctrip.ferriswheel.core.formula;

import java.util.*;
import java.util.function.Function;

/**
 * Directed acyclic graph
 *
 * @param <Id>   Node Id
 * @param <Data> Node data
 */
public class DirectedAcyclicGraph<Id, Data> {
    private final Map<Id, Node<Id, Data>> nodes = new HashMap<>();

    public DirectedAcyclicGraph<Id, Data> addEdges(Id from, Id... toList) {
        Node<Id, Data> node = nodes.get(from);
        if (node == null) {
            node = new Node(from, null);
            nodes.put(node.id, node);
        } else if (isReachable(Arrays.asList(toList), from)) {
            throw new IllegalArgumentException("Circular edges found.");
        }
        for (Id toItem : toList) {
            Node<Id, Data> target = nodes.get(toItem);
            if (target == null) {
                target = new Node(toItem, null);
                nodes.put(target.id, target);
            }
            node.addOutbound(target);
        }
        return this;
    }

    /**
     * Return true if any of the <em>from</em> node set can reach to <em>to</em> node.
     *
     * @param from
     * @param to
     * @return
     */
    private boolean isReachable(Collection<Id> from, Id to) {
        Set<Id> pending = new HashSet<>();
        pending.addAll(from);
        while (!pending.isEmpty()) {
            if (pending.contains(to)) {
                return true;
            }
            HashSet<Id> tmp = new HashSet<>();
            for (Id id : pending) {
                Node<Id, Data> node = nodes.get(id);
                if (node != null) {
                    tmp.addAll(node.outbounds.keySet());
                }
            }
            pending = tmp;
        }
        return false;
    }

    public Data getData(Id id) {
        Node<Id, Data> node = nodes.get(id);
        return node == null ? null : node.data;
    }

    public Data setData(Id id, Data data) {
        Data oldData = null;
        Node<Id, Data> node = nodes.get(id);
        if (node == null) {
            nodes.put(id, new Node<>(id, data));
        } else {
            oldData = node.data;
            node.data = data;
        }
        return oldData;
    }

    public Data setDataIfAbsent(Id id, Data data) {
        Data oldData = null;
        Node<Id, Data> node = nodes.get(id);
        if (node == null) {
            nodes.put(id, new Node<>(id, data));
        } else {
            oldData = node.data;
            if (oldData == null) {
                node.data = data;
            }
        }
        return oldData;
    }

    public boolean hasNode(Id id) {
        return nodes.containsKey(id);
    }

    public void removeEdges(Id from, Id... toList) {
        Node<Id, Data> node = nodes.get(from);
        if (node == null) {
            return;
        }
        for (Id toItem : toList) {
            Node<Id, Data> target = nodes.get(toItem);
            if (target != null) {
                node.removeOutbound(target);
            }
        }
    }

    public void removeNode(Id id) {
        Node node = nodes.get(id);
        if (node == null) {
            return;
        }
        node.clearInbounds();
        node.clearOutbounds();
        nodes.remove(id);
    }

    /**
     * Get DAG order
     *
     * @return
     */
    public List<Id> sort() {
        List<Id> ordered = new ArrayList<>();
        TreeMap<Id, Set<Id>> inDegrees = getInboundMap();
        TreeMap<Id, Set<Id>> outDegrees = getOutboundMap();
        int lastSize = outDegrees.size();
        while (!outDegrees.isEmpty()) {
            Iterator<Map.Entry<Id, Set<Id>>> it = outDegrees.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Id, Set<Id>> entry = it.next();
                if (!entry.getValue().isEmpty()) {
                    continue;
                }
                Id id = entry.getKey();
                ordered.add(id);
                it.remove();
                Set<Id> inSet = inDegrees.remove(id);
                for (Id in : inSet) {
                    Set<Id> outSet = outDegrees.get(in);
                    outSet.remove(id);
                }
            }
            if (outDegrees.size() == lastSize) {
                throw new RuntimeException("Circular dependency found!");
            } else {
                lastSize = outDegrees.size();
            }
        }
        return ordered;
    }

    public TreeMap<Id, Set<Id>> getOutboundMap() {
        TreeMap<Id, Set<Id>> map = new TreeMap<>();
        nodes.forEach((id, node) -> map.put(id, new TreeSet(node.outbounds.keySet())));
        return map;
    }

    public TreeMap<Id, Set<Id>> getInboundMap() {
        TreeMap<Id, Set<Id>> map = new TreeMap<>();
        nodes.forEach((id, node) -> map.put(id, new TreeSet(node.inbounds.keySet())));
        return map;
    }

    public void clear() {
        nodes.clear();
    }

    public String toDot(Function<Id, String> mapper) {
        StringBuilder sb = new StringBuilder("digraph G {\n");
        if (mapper != null) {
            nodes.values().forEach(node -> sb.append("  ")
                    .append(node.id)
                    .append("[label=\"")
                    .append(filterLabel(mapper.apply(node.id), "" + node.id))
                    .append("\"]\n"));
            sb.append("\n");
        }
        nodes.values().forEach(node -> {
            if (!node.outbounds.isEmpty()) {
                sb.append("  ").append(node.id).append("->");
                node.outbounds.values().forEach(o -> sb.append(o.id).append(","));
                sb.delete(sb.length() - 1, sb.length()).append("\n");
            }
        });
        return sb.append("}\n").toString();
    }

    private String filterLabel(String label, String defVal) {
        if (label == null) {
            return defVal;
        }
        return label.replaceAll("\"", "\\\"");
    }

    public Set<Id> collectOutboundEnds() {
        Set<Id> outboundEnds = new HashSet<>();
        for (Map.Entry<Id, Node<Id, Data>> entry : nodes.entrySet()) {
            if (entry.getValue().outbounds.isEmpty()) {
                outboundEnds.add(entry.getKey());
            }
        }
        return outboundEnds;
    }

    public boolean isEmpty() {
        return nodes.isEmpty();
    }

    public void trimOutboundToKeyNodes(Set<Id> keyNodes) {
        while (true) {
            Set<Id> outboundEnds = collectOutboundEnds();
            if (outboundEnds.isEmpty() || keyNodes.containsAll(outboundEnds)) {
                break;
            }
            for (Id id : outboundEnds) {
                if (keyNodes.contains(id)) {
                    continue;
                }
                removeNode(id);
            }
        }
    }

    /**
     * Graph node
     *
     * @param <Id>
     * @param <Data>
     */
    final class Node<Id, Data> {
        final Id id;
        Data data;
        final Map<Id, Node<Id, Data>> outbounds = new HashMap<>();
        final Map<Id, Node<Id, Data>> inbounds = new HashMap<>();

        Node(Id id, Data data) {
            this.id = id;
            this.data = data;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || !(o instanceof Node)) return false;
            Node node = (Node) o;
            return Objects.equals(id, node.id);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(id);
        }

        boolean hasOutbound(Node node) {
            return outbounds.containsKey(node.id);
        }

        boolean hasInbound(Node node) {
            return inbounds.containsKey(node.id);
        }

        boolean hasOutbounds() {
            return !outbounds.isEmpty();
        }

        boolean hasInbounds() {
            return !inbounds.isEmpty();
        }

        void addOutbound(Node<Id, Data> node) {
            if (outbounds.containsKey(node.id)) {
                return;
            }
            outbounds.put(node.id, node);
            node.inbounds.put(id, this);
        }

        void removeOutbound(Node<Id, Data> node) {
            if (!outbounds.containsKey(node.id)) {
                return;
            }
            outbounds.remove(node.id);
            node.inbounds.remove(id);
        }

        void setOutbounds(Node<Id, Data>... nodes) {
            for (Node formerOutbound : outbounds.values()) {
                formerOutbound.inbounds.remove(id);
            }
            outbounds.clear();
            if (nodes == null) {
                return;
            }
            for (Node newDep : nodes) {
                addOutbound(newDep);
            }
        }

        void clearInbounds() {
            // construct a new set to avoid concurrent modification exception.
            Set<Node<Id, Data>> nodes = new HashSet<>(inbounds.values());
            for (Node<Id, Data> node : nodes) {
                node.removeOutbound(this);
            }
        }

        void clearOutbounds() {
            setOutbounds();
        }
    }

}
