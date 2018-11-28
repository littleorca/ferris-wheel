package com.ctrip.ferriswheel.core.intf;

public enum VariantType {
    /**
     * Do NOT change their order!
     * Make sure item's ordinal is reliable.
     */

    ERROR,   // 0
    BLANK,   // 1
    DECIMAL, // 2
    BOOL,    // 3
    DATE,    // 4
    STRING,  // 5
    LIST;    // 6

    /*
     * Define type transition table.
     * TYPE_ERROR not included, leave that to error handlers.
     */
    private static final int[][] MATH_TRANSITION = new int[][]{
            /* ------------- ER,BL,DE,BO,DA,ST,LI */
            /* 0. ERROR   */ {0, 0, 0, 0, 0, 0, 0},
            /* 1. BLANK   */ {0, 1, 2, 3, 4, 5, 6},
            /* 2. DECIMAL */ {0, 2, 2, 2, 4, 5, 6},
            /* 3. BOOLEAN */ {0, 3, 2, 3, 4, 5, 6},
            /* 4. DATE    */ {0, 4, 4, 4, 2, 5, 6},
            /* 5. STRING  */ {0, 5, 5, 5, 5, 5, 6},
            /* 6. LIST    */ {0, 6, 6, 6, 6, 6, 6}
    };

    public static VariantType valueOf(int ordinal) {
        if (ordinal < 0 || ordinal >= values().length) {
            throw new ArrayIndexOutOfBoundsException("Invalid ordinal.");
        }
        return values()[ordinal];
    }

    public static VariantType compatible(VariantType type1, VariantType type2) {
        return valueOf(MATH_TRANSITION[type1.ordinal()][type2.ordinal()]);
    }
}
