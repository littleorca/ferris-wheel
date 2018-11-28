package com.ctrip.ferriswheel.core.bean;

import com.ctrip.ferriswheel.core.util.EscapeHelper;

import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class AssetIdentifier implements Serializable {
    private static final char LOCATOR_SEP = '!';
    private List<AssetLocator> locators;

    public AssetIdentifier() {
    }

    public AssetIdentifier(String pathname) throws IllegalArgumentException {
        int pos = 0;
        while (pos < pathname.length()) {
            char ch = pathname.charAt(pos);
            if (ch == LOCATOR_SEP) {
                pos++;
            } else if (pos != 0) {
                throw new RuntimeException();
            }
            pos = parseFragment(pathname, pos);
        }
    }

    private int parseFragment(String pathname, int pos) {
        if (pos >= pathname.length()) {
            return pos;
        }
        int start;
        char ch = pathname.charAt(pos);
        if (ch == '[') {
            start = ++pos;
            pos = pathname.indexOf(']', pos);
            if (pos == -1) {
                throw new IllegalArgumentException();
            }
            int n1 = Integer.parseInt(pathname.substring(start, pos));
            pos++;
            if (pos < pathname.length() && pathname.charAt(pos) == '[') {
                start = ++pos;
                pos = pathname.indexOf(']', pos);
                if (pos == -1) {
                    throw new IllegalArgumentException();
                }
                int n2 = Integer.parseInt(pathname.substring(start, pos));
                pos++;
                append(n1, n2);
            } else {
                append(n1);
            }
            return pos;

        } else {
            start = pos;
            if (pos < pathname.length() && pathname.charAt(pos) == '"') {
                pos = parseEscapedFragment(pathname, pos);
                if (pos < 0) {
                    throw new IllegalArgumentException();
                }
            } else {
                pos = pathname.indexOf(LOCATOR_SEP, pos);
                if (pos == -1) {
                    pos = pathname.length();
                }
                if (pos > start) {
                    append(pathname.substring(start, pos));
                }
            }
            return pos;
        }
    }

    private int parseEscapedFragment(String pathname, int pos) {
        StringBuilder sb = new StringBuilder();
        int end;
        try {
            end = EscapeHelper.unescape(pathname, pos, sb);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (end <= pos) {
            throw new IllegalArgumentException();
        }
        append(sb.toString());
        return end;
    }

    public AssetIdentifier(List<AssetLocator> locators) {
        this.locators = locators;
    }

    public AssetIdentifier append(String name) {
        return append(new NameLocator(name));
    }

    public AssetIdentifier append(int index) {
        return append(new ArrayLocator(index));
    }

    public AssetIdentifier append(int rowIndex, int columnIndex) {
        return append(new GridLocator(rowIndex, columnIndex));
    }

    public AssetIdentifier append(AssetLocator locator) {
        if (locators == null) {
            this.locators = new LinkedList<>();
        }
        this.locators.add(locator);
        return this;
    }

    public void clear() {
        if (locators != null) {
            locators.clear();
        }
    }

    @Override
    public String toString() {
        return serialize();
    }

    public String serialize() {
        if (locators == null) {
            return null;
        } else if (locators.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (AssetLocator locator : locators) {
            builder.append(LOCATOR_SEP).append(locator.serialize());
        }
        return (builder.length() > 0) ? builder.substring(1) : builder.toString();
    }

    public List<AssetLocator> getLocators() {
        return locators;
    }

    public void setLocators(List<AssetLocator> locators) {
        this.locators = locators;
    }

    static abstract class AssetLocator {
        private AssetLocator() {
            // prevent from extending subclasses out of this file.
        }

        abstract String serialize();
    }

    public static final class NameLocator extends AssetLocator {
        private String name;

        public NameLocator() {
        }

        public NameLocator(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return serialize();
        }

        @Override
        String serialize() {
            return EscapeHelper.escape(name);
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static final class ArrayLocator extends AssetLocator {
        private int index;

        public ArrayLocator() {
        }

        public ArrayLocator(int index) {
            this.index = index;
        }

        @Override
        public String toString() {
            return serialize();
        }

        @Override
        String serialize() {
            return "[" + index + "]";
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }
    }

    public static final class GridLocator extends AssetLocator {
        private int rowIndex;
        private int columnIndex;

        public GridLocator() {
        }

        public GridLocator(int rowIndex, int columnIndex) {
            this.rowIndex = rowIndex;
            this.columnIndex = columnIndex;
        }

        @Override
        public String toString() {
            return serialize();
        }

        @Override
        String serialize() {
            return "[" + rowIndex + "][" + columnIndex + "]";
        }

        public int getRowIndex() {
            return rowIndex;
        }

        public void setRowIndex(int rowIndex) {
            this.rowIndex = rowIndex;
        }

        public int getColumnIndex() {
            return columnIndex;
        }

        public void setColumnIndex(int columnIndex) {
            this.columnIndex = columnIndex;
        }
    }

}
