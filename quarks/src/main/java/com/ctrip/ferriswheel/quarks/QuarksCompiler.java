package com.ctrip.ferriswheel.quarks;

import com.ctrip.ferriswheel.quarks.exception.QuarksException;

import java.io.Reader;
import java.util.List;

public interface QuarksCompiler<V> extends IQuarksCompiler<V> {

    /**
     * Compile with default optimize level.
     * 
     * @param in
     * @return
     * @throws QuarksException
     */
    List<AbstractSyntaxTree<V>> compile(String in)
            throws QuarksException;

    /**
     * Compile with default optimize level.
     * 
     * @param in
     * @return
     * @throws QuarksException
     */
    List<AbstractSyntaxTree<V>> compile(Reader in)
            throws QuarksException;

    /**
     * Compile with specified optimize level.
     * 
     * @param in
     * @param optimize
     * @return
     * @throws QuarksException
     */
    List<AbstractSyntaxTree<V>> compile(Reader in, int optimize)
            throws QuarksException;

}
