package com.ctrip.ferriswheel.quarks;

import java.io.Serializable;

/**
 * Defines most lexical rules, including identifier pattern, identifier quote
 * (for SQL), string quote, operators, delimiters, keywords.
 * <p>
 * Note that the number format is not included here.
 *
 * @author liuhaifeng
 */
public interface LexContext extends Serializable {

    /**
     * Determine if the given character is a leading character of identifier.
     *
     * @param ch
     * @return
     */
    boolean isIdentifierStart(char ch);

    /**
     * Determine if the given character is an acceptable character of
     * identifier.
     *
     * @param pendingChar
     * @param identifierStart
     * @return
     */
    boolean isIdentifierPart(char pendingChar, char identifierStart);

    /**
     * Get identifier decoder.
     *
     * @return
     */
    TokenDecoder getQuotedIdentifierDecoder();

    /**
     * Get string decoder that takes care of escaped characters.
     *
     * @return
     */
    TokenDecoder getStringDecoder();

    /**
     * Determine if the given token is line comment delimiter.
     *
     * @param token
     * @return
     */
    boolean isLineComment(String token);

    /**
     * Determine if the given token is begin delimiter of block comment.
     *
     * @param token
     * @return
     */
    boolean isBlockComment(String token);

    /**
     * Get terminate token of block comment.
     *
     * @return
     */
    String getBlockCommentTerminator();

    /**
     * Get the full list of operators.
     *
     * @return
     */
    String[] getOperators();

    /**
     * Get the full list of delimiters.
     *
     * @return
     */
    String[] getDelimiters();

    /**
     * Get the full list of keywords.
     *
     * @return
     */
    String[] getKeywords();

    /**
     * Get the full list of literals, such as 'true', 'null', etc.
     *
     * @return
     */
    String[] getLiterals();

}
