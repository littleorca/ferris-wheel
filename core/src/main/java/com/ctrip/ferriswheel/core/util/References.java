package com.ctrip.ferriswheel.core.util;

import com.ctrip.ferriswheel.common.variant.ErrorCodes;
import com.ctrip.ferriswheel.core.ref.CellRef;
import com.ctrip.ferriswheel.core.ref.PositionRef;
import com.ctrip.ferriswheel.core.ref.RangeRef;

public class References {
    /**
     * Parse cell reference such as 'A1' or '$A$1'
     *
     * @param refStr
     * @return
     */
    public static CellRef parseSimpleCellRef(String refStr) {
        CellRef cellRef = parseRangeEndRef(refStr);

        if (cellRef.getRowIndex() == -1 || cellRef.getColumnIndex() == -1) {
            cellRef.setValid(false);
            cellRef.setRowIndex(-1);
            cellRef.setColumnIndex(-1);
        }

        return cellRef;
    }

    /**
     * Parse cell reference such as 'A1' or '$A$1'
     *
     * @param refStr
     * @return
     */
    public static CellRef parseRangeEndRef(String refStr) {
        CellRef cellRef = new CellRef();
        int pos = 0;

        int col = -1;
        for (; pos < refStr.length(); pos++) {
            char ch = refStr.charAt(pos);
            if (ch == '$') {
                if (col != -1) {
                    break;
                }
                cellRef.setColumnAbsolute(true);
            } else if (ch >= 'A' && ch <= 'Z') {
                col = col == -1 ? (ch - 'A' + 1) : col * 26 + (ch - 'A' + 1);
            } else {
                break;
            }
        }

        if (col == -1) {
            cellRef.setColumnAbsolute(false); // clear
            pos = 0;
        }

        int row = -1;
        for (; pos < refStr.length(); pos++) {
            char ch = refStr.charAt(pos);
            if (ch == '$') {
                if (row != -1) {
                    break;
                }
                cellRef.setRowAbsolute(true);
            } else if (ch >= '0' && ch <= '9') {
                row = row == -1 ? (ch - '0') : row * 10 + (ch - '0');
            } else {
                break;
            }
        }

        cellRef.setColumnIndex(col == -1 ? col : col - 1);
        cellRef.setRowIndex(row == -1 ? row : row - 1);

        return cellRef;
    }

    public static CellRef shift(CellRef originRef, int rowDrift, int columnDrift) {
        CellRef newRef = new CellRef(originRef);
        if (!originRef.isRowAbsolute()) {
            newRef.setRowIndex(originRef.getRowIndex() + rowDrift);
        }
        if (!originRef.isColumnAbsolute()) {
            newRef.setColumnIndex(originRef.getColumnIndex() + columnDrift);
        }
        return newRef;
    }

    public static RangeRef shift(RangeRef originRef, int nShiftRows, int nShiftCols) {
        CellRef newUpperLeft = shift(originRef.getUpperLeft(), nShiftRows, nShiftCols);
        CellRef newLowerRight = shift(originRef.getLowerRight(), nShiftRows, nShiftCols);
        return new RangeRef(newUpperLeft, newLowerRight);
    }

    public static String toColumnCode(int columnIndex) {
        if (columnIndex < 0) {
            throw new IllegalArgumentException();
        }
        // column code's radix is 26 but without zero
        // think that for radix = 10, 0 eq 00, but here A not eq to AA
        StringBuilder sb = new StringBuilder();
        while (columnIndex >= 0) {
            int n = columnIndex % 26;
            sb.insert(0, (char) ('A' + n));
            if (columnIndex < 26) {
                break;
            }
            columnIndex = columnIndex / 26 - 1;
        }
        return sb.toString();
    }

    public static int toColumnIndex(String columnCode) {
        CellRef ref = parseRangeEndRef(columnCode);
        return ref.getColumnIndex();
    }

    public static String toRowCode(int rowIndex) {
        if (rowIndex < 0) {
            throw new IllegalArgumentException();
        }
        return String.valueOf(rowIndex + 1);
    }

    public static String toFormula(CellRef cellRef) {
        return toFormula(cellRef, 0, 0);
    }

    public static String toFormula(CellRef cellRef, int nShiftRows, int nShiftCols) {
        if (!cellRef.isValid()) {
            return ErrorCodes.ILLEGAL_REF.getFullName();
        }

        StringBuilder sb = new StringBuilder();
        appendQualifier(sb, cellRef.getSheetName(), cellRef.getTableName());
        appendPosition(sb, cellRef, nShiftRows, nShiftCols);
        return sb.toString();
    }

    public static String toFormula(RangeRef rangeRef) {
        return toFormula(rangeRef, 0, 0);
    }

    public static String toFormula(RangeRef rangeRef, int nShiftRows, int nShiftCols) {
        if (!rangeRef.isValid()) {
            return ErrorCodes.ILLEGAL_REF.getFullName();

        } else if (rangeRef.getTop() == -1 && rangeRef.getLeft() == -1) {
            throw new IllegalArgumentException();

        } else if (rangeRef.getTop() == -1) {
            if (rangeRef.getBottom() != -1 || rangeRef.getRight() == -1) {
                throw new IllegalArgumentException();
            }

        } else if (rangeRef.getLeft() == -1) {
            if (rangeRef.getRight() != -1 || rangeRef.getBottom() == -1) {
                throw new IllegalArgumentException();
            }

        } else if (rangeRef.getRight() == -1 || rangeRef.getBottom() == -1) {
            throw new IllegalArgumentException();
        }
        // TODO above check could be move to RangeRef itself.

        StringBuilder sb = new StringBuilder();
        CellRef upperLeft = rangeRef.getUpperLeft();
        CellRef lowerRight = rangeRef.getLowerRight();

        if ((upperLeft.getRowIndex() == -1 && upperLeft.getColumnIndex() == -1)
                || (lowerRight.getRowIndex() == -1 && lowerRight.getColumnIndex() == -1)) {
            throw new IllegalArgumentException();
        } else if (upperLeft.getRowIndex() == -1) {

        } else if (upperLeft.getColumnIndex() == -1) {

        }

        appendQualifier(sb, upperLeft.getSheetName(), upperLeft.getTableName());
        appendPosition(sb, upperLeft, nShiftRows, nShiftCols, true);
        sb.append(":");
        appendPosition(sb, lowerRight, nShiftRows, nShiftCols, true);
        return sb.toString();
    }

    static StringBuilder appendQualifier(StringBuilder sb, String sheetName, String tableName) {
        if (tableName != null) {
            if (sheetName != null) {
                sb.append(EscapeHelper.escapeNameIfNeeded(sheetName)).append('!');
            }
            sb.append(EscapeHelper.escapeNameIfNeeded(tableName)).append('!');
        }
        return sb;
    }

    static StringBuilder appendPosition(StringBuilder sb, PositionRef positionRef, int nShiftRows, int nShiftCols) {
        return appendPosition(sb, positionRef, nShiftRows, nShiftCols, false);
    }

    static StringBuilder appendPosition(StringBuilder sb, PositionRef positionRef, int nShiftRows, int nShiftCols, boolean allowPartial) {
        if (positionRef instanceof CellRef &&
                !((CellRef) positionRef).isValid()) {
            sb.append("#REF!");
            return sb;
        }

        if (positionRef.getColumnIndex() == -1 && positionRef.getRowIndex() == -1) {
            throw new IllegalArgumentException();
        }

        if (!allowPartial && (positionRef.getColumnIndex() == -1 || positionRef.getRowIndex() == -1)) {
            throw new IllegalArgumentException();
        }

        if (positionRef.getColumnIndex() != -1) {
            if (positionRef.isColumnAbsolute()) {
                sb.append('$').append(toColumnCode(positionRef.getColumnIndex()));
            } else {
                sb.append(toColumnCode(positionRef.getColumnIndex() + nShiftCols));
            }
        }

        if (positionRef.getRowIndex() != -1) {
            if (positionRef.isRowAbsolute()) {
                sb.append('$').append(toRowCode(positionRef.getRowIndex()));
            } else {
                sb.append(toRowCode(positionRef.getRowIndex() + nShiftRows));
            }
        }
        return sb;
    }
}
