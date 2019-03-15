package com.ctrip.ferriswheel.quarks;

public interface TokenDecoder {
    /**
     * Determine if the given character is a start.
     *
     * @param ch
     * @return
     */
    boolean isStartChar(char ch);

    /**
     * Prepare decoder for a task.
     *
     * @param quoteStart
     */
    void start(char quoteStart);

    /**
     * Feed character, return true if accepted, false if meet the string's end.
     * The start quote character should not be fed (refer {@link #start(char)}),
     * and the end quote must be fed.
     *
     * @param ch
     * @return
     */
    boolean feed(char ch);

    /**
     * Determine if the process is terminable.
     *
     * @return
     */
    boolean isTerminable();

    /**
     * Finish the process and get decoded string.
     *
     * @return
     */
    String finish();
}
