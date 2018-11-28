package com.ctrip.ferriswheel.quarks;

import com.ctrip.ferriswheel.quarks.exception.QuarksEvaluationException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;

public interface AbstractSyntaxTree<V> {

    /**
     * @return Symbol
     */
    @Nonnull
    Symbol getHandle();

    /**
     * @return
     */
    @Nullable
    Token getToken();

    /**
     * get the {@link Evaluator} that corresponding to current
     * AbstractSyntaxTree
     *
     * @return
     * @see #evaluate()
     * @see #evaluate(EvaluationContext)
     */
    <E extends Evaluator<V>> E getEvaluator();

    /**
     * evaluate the AbstractSyntaxTree and get the {@link V} that equality
     * following code :
     * <p>
     * <pre>
     * AbstractSyntaxTree ast;
     * ast.getEvaluator().evaluate(ast, context)
     * </pre>
     *
     * @return
     * @throws QuarksEvaluationException QuarksAssignException
     * @see #getEvaluator()
     * @see #evaluate(EvaluationContext)
     */
    V evaluate() throws QuarksEvaluationException;

    /**
     * evaluate the AbstractSyntaxTree and get the {@link V} that equality
     * following code :
     * <p>
     * <pre>
     * AbstractSyntaxTree ast;
     * ast.getEvaluator().evaluate(ast, context)
     * </pre>
     *
     * @return
     * @throws QuarksEvaluationException QuarksAssignException
     * @see #getEvaluator
     * @see #evaluate()
     */
    V evaluate(EvaluationContext<V> context) throws QuarksEvaluationException;

    /**
     * get parent node of current AbstractSyntaxTree, return <B>null</B> if
     * current node is the root
     *
     * @return
     */
    AbstractSyntaxTree<V> getParent();

    /**
     * get child count of current AbstractSyntaxTree
     *
     * @return
     */
    int getChildCount();

    /**
     * get child AbstractSyntaxTree Node
     *
     * @param index
     * @return
     */
    AbstractSyntaxTree<V> getChild(int index);

    /**
     * get children iterator
     *
     * @return
     */
    <T extends AbstractSyntaxTree<V>> Iterator<T> childrenIterator();

    /**
     * Get the compiler.
     *
     * @return
     */
    <T extends IQuarksCompiler<V>> T getCompiler();
}
