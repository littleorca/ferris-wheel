package com.ctrip.ferriswheel.quarks;

import com.ctrip.ferriswheel.quarks.exception.QuarksLexicalException;
import com.ctrip.ferriswheel.quarks.exception.QuarksSyntaxException;


/**
 * Syntax parser interface.
 * 
 * @author liuhaifeng
 * 
 */
public interface SyntaxParser {

    /**
     * Set syntax context.
     * 
     * @param syntaxContext
     */
    void setSyntaxContext(SyntaxContext syntaxContext);

    /**
     * Set tokenizer.
     * 
     * @param tokenizer
     */
    void setTokenizer(Tokenizer tokenizer);

    /**
     * Parse tokens to abstract syntax tree.
     * 
     * @return
     * @throws QuarksSyntaxException
     * @throws QuarksLexicalException
     */
    <V> AbstractSyntaxTree<V> next() throws QuarksSyntaxException,
            QuarksLexicalException;

}
