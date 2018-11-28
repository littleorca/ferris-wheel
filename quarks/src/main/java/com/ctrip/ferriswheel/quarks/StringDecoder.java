package com.ctrip.ferriswheel.quarks;

public interface StringDecoder {

    /**
     * Determine if the given character is a string quote start.
     *
     * @param ch
     * @return
     */
    boolean isQuoteStart(char ch);

    /**
     * Determine if the given character is the counterpart of the specified
     * string quote start.
     *
     * @param ch
     * @return
     */
    boolean isQuoteEnd(char ch, char quoteStart);

    /**
     * Prepare decoder for a task.
     *
     * @param quoteStart
     */
    void start(char quoteStart);

    /**
     * Feed character, return true if accepted, false if meet the string's end.
     * The start quote character should not be fed, and the end quote will be
     * treated as terminate symbol and results false return value.
     *
     * @param ch
     * @return
     */
    boolean feed(char ch);

    /**
     * Determine if the process is ended.
     *
     * @return
     */
    boolean isEnded();

    /**
     * Finish the process and get decoded string.
     *
     * @return
     */
    String finish();
}
