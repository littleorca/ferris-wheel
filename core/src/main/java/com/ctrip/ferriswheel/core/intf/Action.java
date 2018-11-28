package com.ctrip.ferriswheel.core.intf;

import java.io.Serializable;

/**
 * Represents an action that manipulates a workbook. An action can be internal.
 *
 * @see Instruction
 */
public interface Action extends Serializable {
    /**
     * Get the identifiable action code.
     *
     * @return
     */
    default String getActionCode() {
        return getClass().getSimpleName();
    }

    /**
     * Apply the action to the specified workbook.
     *
     * @param workbook
     */
    void apply(Workbook workbook);
}
