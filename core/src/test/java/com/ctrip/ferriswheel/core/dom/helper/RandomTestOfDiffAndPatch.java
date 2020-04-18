/*
 * MIT License
 *
 * Copyright (c) 2018-2020 Ctrip.com
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

package com.ctrip.ferriswheel.core.dom.helper;

import com.ctrip.ferriswheel.core.dom.*;
import com.ctrip.ferriswheel.core.dom.diff.Patch;
import com.ctrip.ferriswheel.core.dom.impl.AttributeSnapshotImpl;
import com.ctrip.ferriswheel.core.dom.impl.ElementSnapshotImpl;
import com.ctrip.ferriswheel.core.dom.impl.TextNodeSnapshotImpl;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class RandomTestOfDiffAndPatch {
    public static void main(String[] args) {
        new RandomTestOfDiffAndPatch().runTests();
    }

    private static final int TOTAL_ROUND = 10000;
    private static final int INITIAL_TREE_COMPLEXITY = 100;
    private static final int UPDATE_COMPLEXITY = 30;
    private static final long SLEEP_EVERY_HUNDRED_ROUND = 50L;

    private Random random = new Random();
    private DiffHelper diffHelper = new DiffHelper();
    private PatchHelper patchHelper = new PatchHelper();

    public void runTests() {
        System.out.println("# Starting random diff & patch test for " +
                TOTAL_ROUND + " times...");

        for (int i = 1; i <= TOTAL_ROUND; i++) {
            System.out.println("# Round " + i + "/" + TOTAL_ROUND);
            try {
                runRandomCase();
                Thread.sleep(SLEEP_EVERY_HUNDRED_ROUND);
            } catch (RuntimeException | InterruptedException e) {
                e.printStackTrace();
                System.err.println("# Failed to complete random test!");
                System.exit(1);
            } finally {
                System.out.println("# End of " + i + "/" + TOTAL_ROUND + "\n");
            }
        }

        System.out.println("# done!");
    }

    public void runRandomCase() {
        AtomicInteger idSeq = new AtomicInteger(0);
        ElementSnapshotBuilder negativeTreeBuilder = createRandomTree(idSeq);
        ElementSnapshotImpl negativeTree = negativeTreeBuilder.build();
        System.out.println("# negative tree:");
        System.out.println(negativeTree);
        ElementSnapshot positiveTree = createRandomUpdatedTree(negativeTreeBuilder, idSeq);
        System.out.println("# positive tree:");
        System.out.println(positiveTree);
        createAndCheckPatch(negativeTree, positiveTree);
        System.out.println("# checked.");
    }

    private ElementSnapshotBuilder createRandomTree(AtomicInteger idSeq) {
        ElementSnapshotBuilder tree = new ElementSnapshotBuilder().setTagName("E")
                .setAttribute("id", String.valueOf(idSeq.getAndIncrement()));
        List<ElementSnapshotBuilder> elements = new LinkedList<>();
        elements.add(tree);

        int count = random.nextInt(INITIAL_TREE_COMPLEXITY);
        for (int i = 0; i < count; i++) {
            ElementSnapshotBuilder luckyElem = elements.get(random.nextInt(elements.size()));
            AbstractNodeSnapshotBuilder newChild = createRandomNode(idSeq);
            if (newChild instanceof ElementSnapshotBuilder) {
                elements.add((ElementSnapshotBuilder) newChild);
            }
            luckyElem.addChild(newChild);
        }

        return tree;
    }

    private AbstractNodeSnapshotBuilder createRandomNode(AtomicInteger idSeq) {
        if (random.nextInt(100) < 80) {
            ElementSnapshotBuilder elemBuilder = new ElementSnapshotBuilder().setTagName("E")
                    .setAttribute("id", String.valueOf(idSeq.getAndIncrement()));
            setRandomAttributes(elemBuilder, random.nextInt(5));
            return elemBuilder;
        } else {
            return new TextNodeSnapshotBuilder().setData(randomText());
        }
    }

    private ElementSnapshot createRandomUpdatedTree(ElementSnapshotBuilder builder, AtomicInteger idSeq) {
        int count = random.nextInt(UPDATE_COMPLEXITY);
        List<ElementSnapshotBuilder> elements = new LinkedList<>();
        elements.add(builder);
        Stack<ElementSnapshotBuilder> stack = new Stack<>();
        stack.push(builder);
        while (!stack.isEmpty()) {
            ElementSnapshotBuilder el = stack.pop();
            for (int i = 0; i < el.getChildCount(); i++) {
                AbstractNodeSnapshotBuilder child = el.getChild(i);
                if (child instanceof ElementSnapshotBuilder) {
                    elements.add((ElementSnapshotBuilder) child);
                    stack.push((ElementSnapshotBuilder) child);
                }
            }
        }

        final int wRemove = 1, wMove = 3, wCreate = 5, wUpdate = 5;

        for (int i = 0; i < count; i++) {
            int hint = random.nextInt(wRemove + wMove + wCreate + wUpdate);
            if (hint < wRemove) { // remove
                doRandomRemove(elements);
            } else if (hint < wRemove + wMove) { // move
                doRandomMove(elements);
            } else if (hint < wRemove + wMove + wCreate) { // create
                doRandomCreate(elements, idSeq);
            } else { // update
                doRandomUpdate(elements);
            }
        }
        return builder.build();
    }

    private void doRandomRemove(List<ElementSnapshotBuilder> elements) {
        while (elements.size() > 1) {
            int max = (elements.size() * (elements.size() + 1)) >> 1;
            int rnd = (int) Math.floor(Math.sqrt(2 * random.nextInt(max) + .25) - .5);
            ElementSnapshotBuilder el = elements.get(rnd);
            if (el.getChildCount() > 0) {
                int idx = random.nextInt(el.getChildCount());
                AbstractNodeSnapshotBuilder child = el.getChild(idx);
                el.removeChild(child);
                if (child instanceof ElementSnapshotBuilder) {
                    Stack<ElementSnapshotBuilder> stack = new Stack<>();
                    stack.push((ElementSnapshotBuilder) child);
                    while (!stack.isEmpty()) {
                        ElementSnapshotBuilder e = stack.pop();
                        elements.remove(e);
                        for (int i = 0; i < e.getChildCount(); i++) {
                            AbstractNodeSnapshotBuilder c = e.getChild(i);
                            if (c instanceof ElementSnapshotBuilder) {
                                stack.push((ElementSnapshotBuilder) c);
                            }
                        }
                    }
                }
            }
        }
    }

    private void doRandomCreate(List<ElementSnapshotBuilder> elements, AtomicInteger idSeq) {
        ElementSnapshotBuilder el = elements.get(random.nextInt(elements.size()));
        AbstractNodeSnapshotBuilder newNode = createRandomNode(idSeq);
        el.addChild(random.nextInt(el.getChildCount() + 1), newNode);
        if (newNode instanceof ElementSnapshotBuilder) {
            elements.add((ElementSnapshotBuilder) newNode);
        }
    }

    private void doRandomMove(List<ElementSnapshotBuilder> elements) {
        ElementSnapshotBuilder acceptor = elements.get(random.nextInt(elements.size()));
        AbstractNodeSnapshotBuilder source = null;
        // there is a chance that no node can be moved, hence the
        // max try times is limited to avoid dead-loop.
        final int maxTryTimes = 20;
        int tryTimes;
        for (tryTimes = 0; tryTimes < maxTryTimes; tryTimes++) {
            ElementSnapshotBuilder tmpElement = elements.get(random.nextInt(elements.size()));
            if (tmpElement.getChildCount() == 0) {
                source = tmpElement;
            } else {
                source = tmpElement.getChild(random.nextInt(tmpElement.getChildCount()));
            }
            if (source != acceptor && !isAncestorOf(source, acceptor)) {
                break;
            }
        }
        if (tryTimes < maxTryTimes) {
            source.getParent().removeChild(source);
            acceptor.addChild(random.nextInt(acceptor.getChildCount() + 1), source);
        }
    }

    private void doRandomUpdate(List<ElementSnapshotBuilder> elements) {
        ElementSnapshotBuilder candidate = elements.get(random.nextInt(elements.size()));
        AbstractNodeSnapshotBuilder update = (candidate.getChildCount() > 0) ?
                candidate.getChild(random.nextInt(candidate.getChildCount())) : candidate;
        if (update instanceof ElementSnapshotBuilder) {
            setRandomAttributes(((ElementSnapshotBuilder) update), 1 + random.nextInt(5));
        } else if (update instanceof TextNodeSnapshotBuilder) {
            setRandomText((TextNodeSnapshotBuilder) update);
        } else {
            throw new RuntimeException();
        }
    }

    private void setRandomAttributes(ElementSnapshotBuilder elemBuilder, int times) {
        for (int i = 0; i < times; i++) {
            setRandomAttribute(elemBuilder);
        }
    }

    private void setRandomAttribute(ElementSnapshotBuilder elemBuilder) {
        elemBuilder.setAttribute(String.valueOf(randomLetter()),
                String.valueOf(random.nextInt(100)));
    }

    private void setRandomText(TextNodeSnapshotBuilder text) {
        text.setData(randomText());
    }

    private String randomText() {
        int lines = random.nextInt(15);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines; i++) {
            sb.append(randomLetter()).append("\n");
        }
        return sb.toString();
    }

    private char randomLetter() {
        return (char) ('a' + random.nextInt(26));
    }

    private boolean isAncestorOf(AbstractNodeSnapshotBuilder possibleAncestor,
                                 ElementSnapshotBuilder possibleDescendant) {
        AbstractNodeSnapshotBuilder p = possibleDescendant.getParent();
        while (p != null && p != possibleAncestor) {
            p = p.getParent();
        }
        return p == possibleAncestor;
    }

    private NodeSnapshot getRandomNode(ElementSnapshot tree) {
        NodeSnapshot node = tree;
        int steps = random.nextInt(100);
        while (steps-- > 0) {
            if (!(node instanceof ElementSnapshot)) {
                return node;
            }
            List<? extends NodeSnapshot> children = ((ElementSnapshot) node).getChildren();
            if (children.isEmpty()) {
                node = tree;
            } else {
                node = children.get(random.nextInt(children.size()));
            }
        }
        return node;
    }

    private void createAndCheckPatch(ElementSnapshot negativeTree, ElementSnapshot positiveTree) {
        Patch patch = diffHelper.diff(negativeTree, positiveTree);
        System.out.println("# patch:");
        System.out.println(patch);

        ElementSnapshot patchedTree = patchHelper.applyPatch(negativeTree, patch);
        System.out.println("# patched tree:");
        System.out.println(patchedTree);
        TreeSnapshotUtil.assertTreeEquals(positiveTree, patchedTree);
    }

    abstract class AbstractNodeSnapshotBuilder {
        private ElementSnapshotBuilder parent;
        private NodeSnapshot previousNode;
        private NodeSnapshot latestBuild;
        private boolean dirty = false;

        public ElementSnapshotBuilder getParent() {
            return parent;
        }

        protected void setParent(ElementSnapshotBuilder parent) {
            this.parent = parent;
        }

        public AbstractNodeSnapshotBuilder setPreviousNode(NodeSnapshot previousNode) {
            if (this.previousNode != previousNode) {
                markDirty();
            }
            this.previousNode = previousNode;
            return this;
        }

        public NodeSnapshot getPreviousNode() {
            return previousNode;
        }

        public NodeSnapshot getLatestBuild() {
            return latestBuild;
        }

        protected void setLatestBuild(NodeSnapshot latestBuild) {
            this.latestBuild = latestBuild;
        }

        public boolean isDirty() {
            return dirty;
        }

        protected void setDirty(boolean dirty) {
            this.dirty = dirty;
        }

        protected void markDirty() {
            setDirty(true);
            if (getParent() != null) {
                getParent().markDirty();
            }
        }

        public abstract NodeSnapshot build();
    }

    class ElementSnapshotBuilder extends AbstractNodeSnapshotBuilder {
        private String tagName;
        private Map<String, String> attributes = new HashMap<>();
        private List<AbstractNodeSnapshotBuilder> children = new LinkedList<>();

        public ElementSnapshotBuilder setTagName(String tagName) {
            if (this.tagName != tagName) {
                this.tagName = tagName;
                markDirty();
            }
            return this;
        }

        public String getTagName() {
            return tagName;
        }

        public ElementSnapshotBuilder setAttribute(String name, String value) {
            attributes.put(name, value);
            markDirty();
            return this;
        }

        public String getAttribute(String name) {
            return attributes.get(name);
        }

//        public Map<String, String> getAttributes() {
//            return attributes;
//        }

        public ElementSnapshotBuilder removeAttribute(String name) {
            if (attributes.remove(name) != null) {
                markDirty();
            }
            return this;
        }

        public ElementSnapshotBuilder clearAttributes() {
            if (!attributes.isEmpty()) {
                attributes.clear();
                markDirty();
            }
            return this;
        }
//
//        public Builder addChild(NodeSnapshot child) {
//            children.add(child);
//            markDirty();
//            return this;
//        }
//
//        public Builder addChild(int index, NodeSnapshot child) {
//            children.add(index, child);
//            markDirty();
//            return this;
//        }

        public ElementSnapshotBuilder addChild(AbstractNodeSnapshotBuilder child) {
            children.add(child);
            child.setParent(this);
            markDirty();
            return this;
        }

        public ElementSnapshotBuilder addChild(int index, AbstractNodeSnapshotBuilder child) {
            children.add(index, child);
            child.setParent(this);
            markDirty();
            return this;
        }

        public int getChildCount() {
            return children.size();
        }

        public AbstractNodeSnapshotBuilder getChild(int index) {
            return children.get(index);
        }

//        public List<Object> getChildren() {
//            return children;
//        }

        public ElementSnapshotBuilder removeChild(int index) {
            AbstractNodeSnapshotBuilder child = children.remove(index);
            child.setParent(null);
            markDirty();
            return this;
        }

        public ElementSnapshotBuilder removeChild(AbstractNodeSnapshotBuilder child) {
            if (children.remove(child)) {
                child.setParent(null);
                markDirty();
            }
            return this;
        }

        public ElementSnapshotBuilder clearChildren() {
            if (!children.isEmpty()) {
                for (AbstractNodeSnapshotBuilder child : children) {
                    child.setParent(null);
                }
                children.clear();
                markDirty();
            }
            return this;
        }

        public ElementSnapshotBuilder setPreviousNode(ElementSnapshot previousNode) {
            return (ElementSnapshotBuilder) super.setPreviousNode(previousNode);
        }

        public ElementSnapshot getPreviousNode() {
            return (ElementSnapshot) super.getPreviousNode();
        }

        @Override
        public ElementSnapshotImpl build() {
            if (!isDirty() && getLatestBuild() != null) {
                return (ElementSnapshotImpl) getLatestBuild();
            }
            List<AttributeSnapshot> attrList = new ArrayList<>(attributes.size());
            for (Map.Entry<String, String> attr : attributes.entrySet()) {
                attrList.add(new AttributeSnapshotImpl(attr.getKey(), attr.getValue(), null));
            }
            List<NodeSnapshot> childList = new ArrayList<>(children.size());
            for (AbstractNodeSnapshotBuilder child : children) {
                childList.add(child.build());
            }
            ElementSnapshotImpl node = new ElementSnapshotImpl(tagName,
                    Collections.unmodifiableList(attrList),
                    Collections.unmodifiableList(childList),
                    getPreviousNode());
            setPreviousNode(null);
            setLatestBuild(node);
            setDirty(false);
            return node;
        }
    }

    class TextNodeSnapshotBuilder extends AbstractNodeSnapshotBuilder {
        private String data;

        public TextNodeSnapshotBuilder setData(String data) {
            if (this.data != data) {
                this.data = data;
                markDirty();
            }
            return this;
        }

        public String getData() {
            return data;
        }

        public TextNodeSnapshotBuilder setPreviousNode(TextNodeSnapshot previousNode) {
            return (TextNodeSnapshotBuilder) super.setPreviousNode(previousNode);
        }

        public TextNodeSnapshot getPreviousNode() {
            return (TextNodeSnapshot) super.getPreviousNode();
        }

        @Override
        public TextNodeSnapshot build() {
            if (!isDirty() && getLatestBuild() != null) {
                return (TextNodeSnapshot) getLatestBuild();
            }
            TextNodeSnapshotImpl node = new TextNodeSnapshotImpl(data, getPreviousNode());
            setPreviousNode(null);
            setLatestBuild(node);
            setDirty(false);
            return node;
        }
    }
}
