package com.ctrip.ferriswheel.quarks;

import javax.annotation.Nonnull;

/**
 * Base token.
 *
 * @author liuhaifeng
 */
public interface Token {

    enum Type {
        Identifier, QuotedIdentifier, Keyword, Literal, String, Number, Operator, Delimiter, Comment
    }

    /**
     * Get source string.
     *
     * @return
     */
    @Nonnull
    String getSource();

    /**
     * Get the start position of the current token in the origin string.
     *
     * @return
     */
    int getFrom();

    /**
     * Get the end position of the current token in the origin string.
     *
     * @return
     */
    int getTo();

    /**
     * Get the line number
     *
     * @return
     */
    int getLine();

    /**
     * Get token type.
     *
     * @return
     */
    @Nonnull
    Type getType();

    /**
     * Get token string.
     *
     * @return
     */
    @Nonnull
    String getString();

    /**
     * Get token length.
     *
     * @return
     */
    int length();

    /**
     * Judge if the current token equals to the given string.
     *
     * @param s
     * @return
     */
    boolean equalsTo(String s);

    /**
     * Judge if the current token equals to the given string.
     *
     * @param s
     * @param from
     * @param to
     * @return
     */
    boolean equalsTo(String s, int from, int to);

    /**
     * Judge if the current token equals to the given string in case-insensitive
     * mode.
     *
     * @param s
     * @return
     */
    boolean equalsToIgnoreCase(String s);

    /**
     * Judge if the current token equals to the given string in case-insensitive
     * mode.
     *
     * @param s
     * @param from
     * @param to
     * @return
     */
    boolean equalsToIgnoreCase(String s, int from, int to);

    /**
     * Judge if the current token starts with the given string.
     *
     * @param s
     * @return
     */
    boolean startsWith(String s);

    /**
     * Judge if the current token starts with the given string in case-sensitive
     * mode.
     *
     * @param s
     * @return
     */
    boolean startsWithIgnoreCase(String s);
}
