package com.ctrip.ferriswheel.core.util;

import com.ctrip.ferriswheel.common.variant.ErrorCodes;
import com.ctrip.ferriswheel.core.ref.CellReference;
import com.ctrip.ferriswheel.core.ref.NameReference;
import com.ctrip.ferriswheel.core.ref.PositionRef;
import com.ctrip.ferriswheel.core.ref.RangeReference;

public class References {
    /**
     * Parse position reference such as 'A1' or '$A$1'
     *
     * @param refStr
     * @return
     */
    public static PositionRef parsePositionRef(String refStr) {
        PositionRef positionRef = parseRangeEndRef(refStr);

        if (positionRef.getRowIndex() == -1 || positionRef.getColumnIndex() == -1) {
//            positionRef.setAlive(false);
            positionRef.setRowIndex(-1);
            positionRef.setColumnIndex(-1);
        }

        return positionRef;
    }

    /**
     * Parse cell reference such as 'A1' or '$A$1'
     *
     * @param refStr
     * @return
     */
    public static PositionRef parseRangeEndRef(String refStr) {
        PositionRef posRef = new PositionRef();
        int pos = 0;

        int col = -1;
        for (; pos < refStr.length(); pos++) {
            char ch = refStr.charAt(pos);
            if (ch == '$') {
                if (col != -1) {
                    break;
                }
                posRef.setColumnAbsolute(true);
            } else if (ch >= 'A' && ch <= 'Z') {
                col = col == -1 ? (ch - 'A' + 1) : col * 26 + (ch - 'A' + 1);
            } else {
                break;
            }
        }

        if (col == -1) {
            posRef.setColumnAbsolute(false); // clear
            pos = 0;
        }

        int row = -1;
        for (; pos < refStr.length(); pos++) {
            char ch = refStr.charAt(pos);
            if (ch == '$') {
                if (row != -1) {
                    break;
                }
                posRef.setRowAbsolute(true);
            } else if (ch >= '0' && ch <= '9') {
                row = row == -1 ? (ch - '0') : row * 10 + (ch - '0');
            } else {
                break;
            }
        }

        posRef.setColumnIndex(col == -1 ? col : col - 1);
        posRef.setRowIndex(row == -1 ? row : row - 1);

        return posRef;
    }

    public static PositionRef shift(PositionRef originRef, int rowDrift, int columnDrift) {
        PositionRef newRef = new PositionRef(originRef);
        if (!originRef.isRowAbsolute()) {
            newRef.setRowIndex(originRef.getRowIndex() + rowDrift);
        }
        if (!originRef.isColumnAbsolute()) {
            newRef.setColumnIndex(originRef.getColumnIndex() + columnDrift);
        }
        return newRef;
    }

    public static RangeReference shift(RangeReference originRef, int nShiftRows, int nShiftCols) {
        PositionRef newUpperLeft = shift(originRef.getUpperLeftRef(), nShiftRows, nShiftCols);
        PositionRef newLowerRight = shift(originRef.getLowerRightRef(), nShiftRows, nShiftCols);
        return new RangeReference(originRef.getSheetName(), originRef.getAssetName(), newUpperLeft, newLowerRight);
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
        PositionRef ref = parseRangeEndRef(columnCode);
        return ref.getColumnIndex();
    }

    public static String toRowCode(int rowIndex) {
        if (rowIndex < 0) {
            throw new IllegalArgumentException();
        }
        return String.valueOf(rowIndex + 1);
    }

    public static String toFormula(PositionRef positionRef) {
        StringBuilder sb = new StringBuilder();
        appendPosition(sb, positionRef, 0, 0);
        return sb.toString();
    }

    public static String toFormula(CellReference cellReference) {
        return toFormula(cellReference, 0, 0);
    }

    public static String toFormula(CellReference cellReference, int nShiftRows, int nShiftCols) {
        if (!cellReference.isAlive()) {
            return ErrorCodes.REF.getName();
        }

        StringBuilder sb = new StringBuilder();
        appendQualifier(sb, cellReference.getSheetName(), cellReference.getAssetName());
        appendPosition(sb, cellReference.getPositionRef(), nShiftRows, nShiftCols);
        return sb.toString();
    }

    public static String toFormula(RangeReference rangeReference) {
        return toFormula(rangeReference, 0, 0);
    }

    public static String toFormula(RangeReference rangeReference, int nShiftRows, int nShiftCols) {
        if (!rangeReference.isAlive()) {
            return ErrorCodes.REF.getName();

        } else if (rangeReference.getTop() == -1 && rangeReference.getLeft() == -1) {
            throw new IllegalArgumentException();

        } else if (rangeReference.getTop() == -1) {
            if (rangeReference.getBottom() != -1 || rangeReference.getRight() == -1) {
                throw new IllegalArgumentException();
            }

        } else if (rangeReference.getLeft() == -1) {
            if (rangeReference.getRight() != -1 || rangeReference.getBottom() == -1) {
                throw new IllegalArgumentException();
            }

        } else if (rangeReference.getRight() == -1 || rangeReference.getBottom() == -1) {
            throw new IllegalArgumentException();
        }
        // TODO above check could be move to RangeReference itself.

        StringBuilder sb = new StringBuilder();
        PositionRef upperLeft = rangeReference.getUpperLeftRef();
        PositionRef lowerRight = rangeReference.getLowerRightRef();

        if ((upperLeft.getRowIndex() == -1 && upperLeft.getColumnIndex() == -1)
                || (lowerRight.getRowIndex() == -1 && lowerRight.getColumnIndex() == -1)) {
            throw new IllegalArgumentException();
        } else if (upperLeft.getRowIndex() == -1) {

        } else if (upperLeft.getColumnIndex() == -1) {

        }

        appendQualifier(sb, rangeReference.getSheetName(), rangeReference.getAssetName());
        appendPosition(sb, upperLeft, nShiftRows, nShiftCols, true);
        sb.append(":");
        appendPosition(sb, lowerRight, nShiftRows, nShiftCols, true);
        return sb.toString();
    }

    public static String toFormula(NameReference nameReference) {
        if (!nameReference.isValid()) {
            return ErrorCodes.REF.getName();
        }

        StringBuilder sb = new StringBuilder();
        appendQualifier(sb, nameReference.getSheetName(), nameReference.getAssetName());
        sb.append(EscapeHelper.escapeName(nameReference.getTargetName()));
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
//        if (positionRef instanceof CellReference &&
//                !((CellReference) positionRef).isAlive()) {
//            sb.append("#REF!");
//            return sb;
//        }

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
