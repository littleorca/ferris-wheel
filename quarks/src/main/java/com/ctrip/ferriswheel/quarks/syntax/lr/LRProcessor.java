package com.ctrip.ferriswheel.quarks.syntax.lr;

import com.ctrip.ferriswheel.quarks.exception.QuarksLexicalException;
import com.ctrip.ferriswheel.quarks.exception.QuarksSyntaxException;

/**
 * Low level LR processor which provides callback event.
 */
public interface LRProcessor {
    /**
     * Process one statement.
     *
     * @param listener
     * @return false if already met the end, true otherwise
     * @throws QuarksSyntaxException
     * @throws QuarksLexicalException
     */
    boolean process(LREventListener listener) throws QuarksSyntaxException, QuarksLexicalException;
}
