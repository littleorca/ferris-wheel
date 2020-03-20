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

import com.ctrip.ferriswheel.core.dom.ElementSnapshot;
import com.ctrip.ferriswheel.core.dom.NodeSnapshot;
import com.ctrip.ferriswheel.core.dom.impl.AbstractNodeSnapshot;
import com.ctrip.ferriswheel.core.dom.impl.ElementSnapshotImpl;
import com.ctrip.ferriswheel.core.dom.impl.TextNodeSnapshotImpl;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

public class RandomTestOfDiffAndPatch {
    public static void main(String[] args) {
        new RandomTestOfDiffAndPatch().runTests();
    }

    private static final int TOTAL_ROUND = 1000;
    private Random random = new Random();

    public void runTests() {
        System.out.println("# starting random diff & patch test for " +
                TOTAL_ROUND + " times...");

        for (int i = 1; i <= TOTAL_ROUND; i++) {
            System.out.println("# round " + i + "/" + TOTAL_ROUND);
            try {
                runRandomCase();
            } catch (RuntimeException e) {
                e.printStackTrace();
                System.err.println("# Failed to complete random test!");
                System.exit(1);
            }
        }

        System.out.println("# done!");
    }

    public void runRandomCase() {
        AtomicInteger idSeq = new AtomicInteger(0);
        ElementSnapshotImpl.Builder negativeTreeBuilder = createRandomTree(idSeq);
        ElementSnapshotImpl negativeTree = negativeTreeBuilder.build();
        System.out.println("# negative tree:");
        System.out.println(negativeTree);
        NodeSnapshot positiveTree = createRandomUpdatedTree(negativeTreeBuilder, idSeq);
        System.out.println("# positive tree:");
        System.out.println(positiveTree);
        createAndCheckPatch(negativeTree, positiveTree);
        System.out.println("# checked.");
    }

    private ElementSnapshotImpl.Builder createRandomTree(AtomicInteger idSeq) {
        ElementSnapshotImpl.Builder tree = new ElementSnapshotImpl.Builder().setTagName("E")
                .setAttribute("id", String.valueOf(idSeq.getAndIncrement()));
        List<ElementSnapshotImpl.Builder> elements = new LinkedList<>();
        elements.add(tree);

        int count = random.nextInt(100);
        for (int i = 0; i < count; i++) {
            ElementSnapshotImpl.Builder luckyElem = elements.get(random.nextInt(elements.size()));
            AbstractNodeSnapshot.Builder newChild = createRandomNode(idSeq);
            if (newChild instanceof ElementSnapshotImpl.Builder) {
                elements.add((ElementSnapshotImpl.Builder) newChild);
            }
            luckyElem.addChild(newChild);
        }

        return tree;
    }

    private AbstractNodeSnapshot.Builder createRandomNode(AtomicInteger idSeq) {
        if (random.nextInt(100) < 80) {
            return new ElementSnapshotImpl.Builder().setTagName("E")
                    .setAttribute("id", String.valueOf(idSeq.getAndIncrement()));
        } else {
            return new TextNodeSnapshotImpl.Builder().setData("blahblah..." + idSeq.getAndIncrement());
        }
    }

    private NodeSnapshot createRandomUpdatedTree(ElementSnapshotImpl.Builder builder, AtomicInteger idSeq) {
        int count = random.nextInt(20);
        List<ElementSnapshotImpl.Builder> elements = new LinkedList<>();
        elements.add(builder);
        Stack<ElementSnapshotImpl.Builder> stack = new Stack<>();
        stack.push(builder);
        while (!stack.isEmpty()) {
            ElementSnapshotImpl.Builder el = stack.pop();
            for (int i = 0; i < el.getChildCount(); i++) {
                AbstractNodeSnapshot.Builder child = el.getChild(i);
                if (child instanceof ElementSnapshotImpl.Builder) {
                    elements.add((ElementSnapshotImpl.Builder) child);
                    stack.push((ElementSnapshotImpl.Builder) child);
                }
            }
        }

        for (int i = 1; i < elements.size(); i++) {
            if (elements.get(i).getParent() == null) {
                System.out.println("!!!");
            }
        }

        for (int i = 0; i < count; i++) {
            switch (random.nextInt(4)) {
                case 0: // remove
                    while (elements.size() > 1) {
                        ElementSnapshotImpl.Builder el = elements.get(random.nextInt(elements.size()));
                        if (el.getChildCount() > 0) {
                            int idx = random.nextInt(el.getChildCount());
                            AbstractNodeSnapshot.Builder child = el.getChild(idx);
                            el.removeChild(idx);
                            if (child instanceof ElementSnapshotImpl.Builder) {
                                elements.remove(child);
                            }
                            break;
                        }
                    }
                    break;
                case 1: // create
                    ElementSnapshotImpl.Builder el = elements.get(random.nextInt(elements.size()));
                    AbstractNodeSnapshot.Builder newNode = createRandomNode(idSeq);
                    el.addChild(random.nextInt(el.getChildCount() + 1), newNode);
                    if (newNode instanceof ElementSnapshotImpl.Builder) {
                        elements.add((ElementSnapshotImpl.Builder) newNode);
                    }
                    break;
                case 2: // move
                    ElementSnapshotImpl.Builder acceptor = elements.get(random.nextInt(elements.size()));
                    AbstractNodeSnapshot.Builder source;
                    while (true) {
                        ElementSnapshotImpl.Builder tmpElement = elements.get(random.nextInt(elements.size()));
                        if (tmpElement.getChildCount() == 0) {
                            source = tmpElement;
                        } else {
                            source = tmpElement.getChild(random.nextInt(tmpElement.getChildCount()));
                        }
                        if (source != acceptor && !isAncestorOf(source, acceptor)) {
                            break;
                        }
                    }
                    ((ElementSnapshotImpl.Builder) source.getParent()).removeChild(source);
                    acceptor.addChild(random.nextInt(acceptor.getChildCount() + 1), source);
                    break;
                case 3: // update
                    ElementSnapshotImpl.Builder update = elements.get(random.nextInt(elements.size()));
                    if (update.getChildCount() > 0) {
                        update.removeChild(random.nextInt(update.getChildCount()));
                        break;
                    }
// TODO
                    break;
            }
        }
        return builder.build();
    }

    private boolean isAncestorOf(AbstractNodeSnapshot.Builder possibleAncestor,
                                 ElementSnapshotImpl.Builder possibleDescendant) {
        AbstractNodeSnapshot.Builder p = possibleDescendant.getParent();
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
            List<NodeSnapshot> children = ((ElementSnapshot) node).getChildren();
            if (children.isEmpty()) {
                node = tree;
            } else {
                node = children.get(random.nextInt(children.size()));
            }
        }
        return node;
    }

    private void createAndCheckPatch(NodeSnapshot negativeTree, NodeSnapshot positiveTree) {

    }

}
