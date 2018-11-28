package com.ctrip.ferriswheel.quarks;

import java.io.Reader;

import com.ctrip.ferriswheel.quarks.exception.QuarksLexicalException;

/**
 * Lexical analysis tool.
 * 
 * @author liuhaifeng
 * 
 */
public interface Tokenizer extends Token {

    /**
     * Set lexical context.
     * 
     * @param lexContext
     */
    void setLexContext(LexContext lexContext);

    /**
     * Set input source.
     * 
     * @param in
     */
    void setInput(Reader in);

    /**
     * Next token. if succeed, new token information will be set, otherwise
     * empty information will be set.
     * 
     * @return true if successfully got the next token.
     * @throws QuarksLexicalException
     */
    boolean next() throws QuarksLexicalException;

    /**
     * Get the current token.
     * 
     * @return
     */
    Token getToken();

    /**
     * Determine if the tokenizer points to nothing.
     * 
     * @return
     */
    boolean isEmpty();

    /**
     * Get current line number.
     * 
     * @return
     */
    int getLine();

    /**
     * Get current column number.
     * 
     * @return
     */
    int getColumn();

}
