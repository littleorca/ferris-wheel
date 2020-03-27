package com.ctrip.ferriswheel.core.dom.diff;

import java.util.Collections;
import java.util.List;

public class TextNodeDiff extends Diff {
    private List<LineDiff> lines;

    public TextNodeDiff(NodeLocation negativeLocation, NodeLocation positiveLocation) {
        super(negativeLocation, positiveLocation);
    }

    @Override
    public boolean hasContent() {
        return hasTextChanges();
    }

    @Override
    protected void doMerge(Diff another) {
        if (!(another instanceof TextNodeDiff)) {
            throw new IllegalArgumentException("Cannot merge between different classes.");
        }
        TextNodeDiff anotherTextNodeDiff = (TextNodeDiff) another;
        if (hasTextChanges()) {
            if (anotherTextNodeDiff.hasTextChanges()) {
                throw new RuntimeException("Cannot merge text changes, this is probably a bug!");
            }
        } else if (anotherTextNodeDiff.hasTextChanges()) {
            setLines(anotherTextNodeDiff.lines);
        }
    }

    public boolean hasTextChanges() {
        return lines != null && !lines.isEmpty();
    }

    public List<LineDiff> getLines() {
        return lines == null ? Collections.emptyList() : lines;
    }

    public void setLines(List<LineDiff> lines) {
        this.lines = lines;
    }
}
