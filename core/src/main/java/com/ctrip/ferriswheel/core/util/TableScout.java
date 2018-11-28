package com.ctrip.ferriswheel.core.util;

import com.ctrip.ferriswheel.core.intf.Cell;
import com.ctrip.ferriswheel.core.intf.Table;
import com.ctrip.ferriswheel.core.intf.VariantType;
import com.ctrip.ferriswheel.core.view.Rectangle;

public class TableScout {
    public static boolean isDecimalCompatible(Table table, int left, int top, int right, int bottom) {
        if (table == null) {
            throw new IllegalArgumentException("Sheet cannot be null.");
        }
        if (left < 0 || right < left || top < 0 || bottom < top) {
            throw new IllegalArgumentException("Invalid index.");
        }
        for (int row = top; row <= bottom; row++) {
            for (int col = left; col <= right; col++) {
                Cell cell = table.getCell(row, col);
                if (cell == null) {
                    continue;
                }
                if (cell.valueType() == VariantType.DECIMAL
                        || cell.valueType() == VariantType.BLANK
                        || cell.valueType() == VariantType.ERROR) {
                    continue;
                }
                return false;
            }
        }
        return true;
    }

    public static boolean isDecimalCompatible(Cell cell) {
        if (cell == null || cell.valueType() == VariantType.BLANK) {
            return true;
        }
        if (cell.valueType() == VariantType.ERROR) {
            return true;
        }
        if (cell.valueType() == VariantType.DECIMAL) {
            return true;
        }
        return false;
    }

    public static Rectangle getBiggestDecimalRectangleFromBottomRight(Table table,
                                                                      int left,
                                                                      int top,
                                                                      int right,
                                                                      int bottom) {
        if (table == null) {
            throw new IllegalArgumentException("Sheet cannot be null.");
        }
        if (left < 0 || right < left || top < 0 || bottom < top) {
            throw new IllegalArgumentException("Invalid index.");
        }

        /*
         * Obstacles example:
         * i.e. 1:
         *               @
         *         @ $ + +
         *     $ + + + + +
         *     + + + + + +
         *   @ + + + + + +
         * $ + + + + + + +
         * + + + + + + + +
         * i.e. 2:
         *               $
         *             @ +
         *         @ $ + +
         *     $ + + + + +
         *     + + + + + +
         *   @ + + + + + +
         * @ = obstacle
         * +/$ = decimal cell
         * $ = candidate best upper left corner
         */

        /**
         * obstacles index = column - left
         * obstacles[index] = row index of the obstacle, or null if no effective obstacle in that column.
         */
        Integer[] obstacles = new Integer[right - left + 1];
        int leftBound = left;
        for (int row = bottom; row >= top; row--) {
            for (int col = right; col >= leftBound; col--) {
                if (!isDecimalCompatible(table.getCell(row, col))) {
                    obstacles[col - left] = row;
                    leftBound = col + 1;
                    break;
                }
            }
            if (leftBound > right) {
                break;
            }
        }

        Rectangle rect = new Rectangle(right, bottom, right, bottom);
        int area = 0;
        int col = left;
        for (int i = 0; i < obstacles.length; i++) {
            if (obstacles[i] == null) {
                continue;
            }
            int row = obstacles[i] + 1;
            // maybe row is greater than bottom, but then newArea will be zero or negative,
            // thus the following 'if' statement will always fail, so it's OK.
            int newArea = (right - col + 1) * (bottom - row + 1);
            if (newArea > area) {
                rect.setLeft(col);
                rect.setTop(row);
                area = newArea;
            }
            col = i + left + 1;
            if (col > right) {
                break;
            }
        }

        if (col <= right) {
            int newArea = (right - col + 1) * (bottom - top + 1);
            if (newArea > area) {
                rect.setLeft(col);
                rect.setTop(top);
                area = newArea;
            }
        }

        return area == 0 ? null : rect;
    }

}
