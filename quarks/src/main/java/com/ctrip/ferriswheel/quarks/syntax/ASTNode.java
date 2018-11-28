package com.ctrip.ferriswheel.quarks.syntax;

import com.ctrip.ferriswheel.quarks.*;
import com.ctrip.ferriswheel.quarks.context.DefaultEvaluationContext;
import com.ctrip.ferriswheel.quarks.exception.QuarksEvaluationException;
import com.ctrip.ferriswheel.quarks.exception.QuarksSyntaxException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * a default AbstractSyntaxTree implement.
 */
public class ASTNode<V> implements AbstractSyntaxTree<V> {

    protected Symbol handle;

    protected Token token;

    protected BaseEvaluator<V> evaluator;

    protected ASTNode<V> parent;

    protected final int expectedChildCount;

    protected List<ASTNode<V>> children;

    private IQuarksCompiler<V> compiler;

    public ASTNode(Symbol handle) {
        this(handle, 0);
    }

    public ASTNode(Symbol handle, Token token) {
        this(handle, token, 0);
    }

    public ASTNode(int expectedChildCount) {
        this(null, null, expectedChildCount);
    }

    public ASTNode(Symbol handle, int expectedChildCount) {
        this(handle, null, expectedChildCount);
    }

    public ASTNode(Symbol handle, Token token, int expectedChildCount) {
        this.handle = handle;
        this.token = token;
        this.expectedChildCount = expectedChildCount;
        if (this.expectedChildCount > 0)
            this.children = new ArrayList<>(this.expectedChildCount);
        else
            this.children = new LinkedList<>();
    }

    public void setHandle(Symbol handle) {
        this.handle = handle;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public void setEvaluator(BaseEvaluator<V> evaluator) {
        this.evaluator = evaluator;
    }

    public ASTNode<V> getParent() {
        return parent;
    }

    public void setParent(ASTNode<V> parent) {
        this.parent = parent;
    }

    public int getExpectedChildCount() {
        return expectedChildCount;
    }

    public int getChildCount() {
        return children.size();
    }

    @Override
    public Symbol getHandle() {
        return handle;
    }

    @Override
    public Token getToken() {
        return token;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E extends Evaluator<V>> E getEvaluator() {
        return ((E) evaluator);
    }

    @Override
    public V evaluate() throws QuarksEvaluationException {
        return this.evaluate(new DefaultEvaluationContext<V>());
    }


    @Override
    public V evaluate(EvaluationContext<V> context) throws QuarksEvaluationException {
        return evaluator.evaluate(getCompiler().getInternalContextBuilder().build(this, context));
    }

    @Override
    public ASTNode<V> getChild(int index) {
        if (index < 0 || index >= children.size())
            return null;
        return children.get(index);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterator<ASTNode<V>> childrenIterator() {
        return children.iterator();
    }

    @Override
    public String toString() {
        return handle.toString();
    }

    /**
     * add child
     *
     * @param child
     * @throws QuarksSyntaxException
     */
    public void addChild(ASTNode<V> child) throws QuarksSyntaxException {
        addChild(children.size(), child);
    }

    /**
     * add child ASTNode to specially index.
     *
     * @param index
     * @param child
     * @throws QuarksSyntaxException
     */
    public void addChild(int index, ASTNode<V> child) throws QuarksSyntaxException {
        if (expectedChildCount > 0 && children.size() >= expectedChildCount) {
            ASTNode<V> node = child;
            Token token = getToken();
            while (token == null && node != null && node.getChildCount() > 0) {
                node = node.getChild(0);
                token = node.getToken();
            }
            if (token != null) {
                throw new QuarksSyntaxException(
                        new QuarksSyntaxException("Too many child node(s)."), token);
            } else {
                throw new QuarksSyntaxException("Too many child node(s).");
            }
        }

        child.setParent(this);

        if (index == 0 && children instanceof LinkedList) {
            ((LinkedList<ASTNode<V>>) children).addFirst(child);
        } else if (index == children.size() && children instanceof LinkedList) {
            ((LinkedList<ASTNode<V>>) children).addLast(child);
        } else {
            children.add(index, child);
        }

    }

    /**
     * remove child ASTNode according to specially index.
     *
     * @param index
     * @return
     */
    public ASTNode<V> removeChild(int index) {
        if (index < 0 || index >= children.size())
            return null;

        ASTNode<V> child;

        if (index == 0 && children instanceof LinkedList) {
            child = ((LinkedList<ASTNode<V>>) children).removeFirst();
        } else if ((index + 1) == children.size() && children instanceof LinkedList) {
            child = ((LinkedList<ASTNode<V>>) children).removeLast();
        } else {
            child = children.remove(index);
        }

        if (child != null)
            child.setParent(null);

        return child;
    }

    /**
     * empty all children
     */
    public void emptyChildren() {
        Iterator<ASTNode<V>> childIter = children.iterator();
        while (childIter.hasNext()) {
            ASTNode<V> child = childIter.next();
            child.setParent(null);
            childIter.remove();
        }
    }

    @Override
    public IQuarksCompiler<V> getCompiler() {
        return compiler;
    }

    public void setCompiler(IQuarksCompiler<V> compiler) {
        this.compiler = compiler;
    }
}
