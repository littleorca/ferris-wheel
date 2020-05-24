package com.ctrip.ferriswheel.core.dom.helper;

import com.ctrip.ferriswheel.core.dom.TreeRevision;
import com.ctrip.ferriswheel.core.util.LinkedItem;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class LinkedTreeRevisionHistory implements TreeRevisionHistory {
    private static final int DEFAULT_MAX_REVISIONS = 100;

    private final int maxRevisions;
    private LinkedItem<TreeRevision> current = null;

    public LinkedTreeRevisionHistory() {
        this(DEFAULT_MAX_REVISIONS);
    }

    public LinkedTreeRevisionHistory(int maxRevisions) {
        this.maxRevisions = maxRevisions;
    }

    @Override
    public TreeRevision backward() {
        if (current == null || current.getNext() == null) {
            return null;
        }
        current = current.getNext();
        return current.getData();
    }

    @Override
    public TreeRevision forward() {
        if (current == null || current.getPrevious() == null) {
            return null;
        }
        current = current.getPrevious();
        return current.getData();
    }

    @Override
    public TreeRevision go(int n) {
        if (n < 0) {
            for (int i = 0; i > n; i--) {
                if (backward() == null) {
                    break;
                }
            }

        } else if (n > 0) {
            for (int i = 0; i < n; i++) {
                if (forward() == null) {
                    break;
                }
            }
        }

        // if n == 0, no move needed.

        return current == null ? null : current.getData();
    }

    @Override
    public List<TreeRevision> backwardList() {
        if (current == null || current.getPrevious() == null) {
            return Collections.emptyList();
        }

        List<TreeRevision> list = new LinkedList<>();
        LinkedItem<TreeRevision> rev = current.getPrevious();
        while (rev != null) {
            list.add(rev.getData());
            rev = rev.getPrevious();
        }

        return list;
    }

    @Override
    public List<TreeRevision> forwardList() {
        if (current == null || current.getNext() == null) {
            return Collections.emptyList();
        }

        List<TreeRevision> list = new LinkedList<>();
        LinkedItem<TreeRevision> rev = current.getNext();
        while (rev != null) {
            list.add(rev.getData());
            rev = rev.getNext();
        }

        return list;
    }

    @Override
    public void push(TreeRevision revision) {
        LinkedItem<TreeRevision> linkedRevisionItem = new LinkedItem<>(revision, current, null);
        if (current != null) {
            current.setNext(linkedRevisionItem);
        }
        current = linkedRevisionItem;
    }

}
