package com.ctrip.ferriswheel.core.util;

import com.ctrip.ferriswheel.common.variant.ErrorCodes;
import com.ctrip.ferriswheel.core.ref.*;

public class References {
    /**
     * Parse position reference such as 'A1' or '$A$1'
     *
     * @param refStr
     * @return
     */
    public static PositionRef parsePositionRef(String refStr) {
        PositionRef positionRef = parseRangeEndRef(refStr);

        if (positionRef.getRowAnchor() == null || positionRef.getColumnAnchor() == null) {
//            throw new IllegalArgumentException();
//            positionRef.setAlive(false);
            positionRef.setRowAnchor(null);
            positionRef.setColumnAnchor(null);
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
        int pos = 0;

        Anchor columnAnchor = null;
        Anchor rowAnchor = null;

        boolean absolute = false;
        for (; pos < refStr.length(); pos++) {
            char ch = refStr.charAt(pos);
            if (ch == '$') {
                if (columnAnchor != null) {
                    break;
                }
                absolute = true;
            } else if (ch >= 'A' && ch <= 'Z') {
                if (columnAnchor == null) {
                    columnAnchor = new Anchor(ch - 'A' + 1, absolute);
                } else {
                    columnAnchor.setIndex(columnAnchor.getIndex() * 26 + (ch - 'A' + 1));
                }
            } else {
                break;
            }
        }

        if (columnAnchor == null) {
            pos = 0;
        }

        absolute = false;
        for (; pos < refStr.length(); pos++) {
            char ch = refStr.charAt(pos);
            if (ch == '$') {
                if (rowAnchor != null) {
                    break;
                }
                absolute = true;
            } else if (ch >= '0' && ch <= '9') {
                if (rowAnchor == null) {
                    rowAnchor = new Anchor(ch - '0', absolute);
                } else {
                    rowAnchor.setIndex(rowAnchor.getIndex() * 10 + (ch - '0'));
                }
            } else {
                break;
            }
        }

        if (rowAnchor != null) {
            rowAnchor.setIndex(rowAnchor.getIndex() - 1);
        }
        if (columnAnchor != null) {
            columnAnchor.setIndex(columnAnchor.getIndex() - 1);
        }

        return new PositionRef(rowAnchor, columnAnchor);
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

        } else if (rangeReference.getTopAnchor() == null && rangeReference.getLeftAnchor() == null) {
            throw new IllegalArgumentException();

        } else if (rangeReference.getTopAnchor() == null) {
            if (rangeReference.getBottomAnchor() != null || rangeReference.getRightAnchor() == null) {
                throw new IllegalArgumentException();
            }

        } else if (rangeReference.getLeftAnchor() == null) {
            if (rangeReference.getRightAnchor() != null || rangeReference.getBottomAnchor() == null) {
                throw new IllegalArgumentException();
            }

        } else if (rangeReference.getRightAnchor() == null || rangeReference.getBottomAnchor() == null) {
            throw new IllegalArgumentException();
        }
        // TODO above check could be move to RangeReference itself.

        StringBuilder sb = new StringBuilder();

        appendQualifier(sb, rangeReference.getSheetName(), rangeReference.getAssetName());
        appendPosition(sb,
                rangeReference.getLeftAnchor(),
                rangeReference.getTopAnchor(),
                nShiftRows,
                nShiftCols,
                true);
        sb.append(":");
        appendPosition(sb,
                rangeReference.getRightAnchor(),
                rangeReference.getBottomAnchor(),
                nShiftRows,
                nShiftCols,
                true);
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
        return appendPosition(sb,
                positionRef.getColumnAnchor(),
                positionRef.getRowAnchor(),
                nShiftRows,
                nShiftCols,
                false);
    }

    static StringBuilder appendPosition(StringBuilder sb,
                                        Anchor columnAnchor,
                                        Anchor rowAnchor,
                                        int nShiftRows,
                                        int nShiftCols,
                                        boolean allowPartial) {
//        if (positionRef instanceof CellReference &&
//                !((CellReference) positionRef).isAlive()) {
//            sb.append("#REF!");
//            return sb;
//        }

        if (columnAnchor == null && rowAnchor == null) {
            throw new IllegalArgumentException();
        }

        if (!allowPartial && (columnAnchor == null || rowAnchor == null)) {
            throw new IllegalArgumentException();
        }

        if (columnAnchor != null) {
            if (columnAnchor.isAbsolute()) {
                sb.append('$').append(toColumnCode(columnAnchor.getIndex()));
            } else {
                sb.append(toColumnCode(columnAnchor.getIndex() + nShiftCols));
            }
        }

        if (rowAnchor != null) {
            if (rowAnchor.isAbsolute()) {
                sb.append('$').append(toRowCode(rowAnchor.getIndex()));
            } else {
                sb.append(toRowCode(rowAnchor.getIndex() + nShiftRows));
            }
        }
        return sb;
    }
}
