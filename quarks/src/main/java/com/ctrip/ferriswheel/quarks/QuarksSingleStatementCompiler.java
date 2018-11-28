package com.ctrip.ferriswheel.quarks;

import com.ctrip.ferriswheel.quarks.exception.QuarksException;

import java.io.Reader;

public interface QuarksSingleStatementCompiler<V> extends IQuarksCompiler<V>  {

    /**
     * Compile with default optimize level.
     * 
     * @param in
     * @return
     * @throws QuarksException
     */
    AbstractSyntaxTree<V> compileSingleStatement(String in)
            throws QuarksException;

    /**
     * Compile with default optimize level.
     * 
     * @param in
     * @return
     * @throws QuarksException
     */
    AbstractSyntaxTree<V> compileSingleStatement(Reader in)
            throws QuarksException;

    /**
     * Compile with specified optimize level.
     * 
     * @param in
     * @param optimize
     * @return
     * @throws QuarksException
     */
    AbstractSyntaxTree<V> compileSingleStatement(Reader in, int optimize)
            throws QuarksException;

}
