package com.ctrip.ferriswheel.quarks.compile;

import com.ctrip.ferriswheel.quarks.*;
import com.ctrip.ferriswheel.quarks.context.DefaultInternalContextBuilder;
import com.ctrip.ferriswheel.quarks.exception.QuarksCompileException;
import com.ctrip.ferriswheel.quarks.exception.QuarksEvaluationNotFoundException;
import com.ctrip.ferriswheel.quarks.exception.QuarksException;
import com.ctrip.ferriswheel.quarks.syntax.ASTNode;
import com.ctrip.ferriswheel.quarks.util.ASTUtil;

import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class BasicQuarksCompiler<V> implements
        QuarksSingleStatementCompiler<V>, QuarksCompiler<V> {

    protected Tokenizer tokenizer;

    protected SyntaxParser parser;

    protected InternalContextBuilder<V> internalContextBuilder;

    /**
     * @param tokenizer
     * @param parser
     */
    public BasicQuarksCompiler(Tokenizer tokenizer, SyntaxParser parser) {
        this(tokenizer, parser, new DefaultInternalContextBuilder<V>());
    }

    public BasicQuarksCompiler(Tokenizer tokenizer, SyntaxParser parser, InternalContextBuilder<V> internalContextBuilder) {
        this.tokenizer = tokenizer;
        this.parser = parser;
        this.internalContextBuilder = internalContextBuilder;
    }

    @Override
    public List<AbstractSyntaxTree<V>> compile(String in)
            throws QuarksException {
        return compile(new StringReader(in));
    }

    @Override
    public List<AbstractSyntaxTree<V>> compile(Reader in)
            throws QuarksException {
        return compile(in, 0);
    }

    @Override
    public List<AbstractSyntaxTree<V>> compile(Reader in, int optimize)
            throws QuarksException {
        tokenizer.setInput(in);
        parser.setTokenizer(tokenizer);

        List<AbstractSyntaxTree<V>> statements = new LinkedList<>();
        AbstractSyntaxTree<V> ast;
        while ((ast = parser.next()) != null) {
            try {
                initEachNode((ASTNode<V>) ast);
                statements.add(ast = optimize(ast, optimize));
            } catch (Exception e) {
                throw new QuarksCompileException("compile error : \n" + ASTUtil.dump(ast), e);
            }
        }
        return statements;
    }

    private void initEachNode(ASTNode<V> astNode) throws QuarksEvaluationNotFoundException {
        astNode.setCompiler(this);
        Iterator<ASTNode<V>> nodeIterator = astNode.childrenIterator();
        while (nodeIterator.hasNext()) {
            initEachNode(nodeIterator.next());
        }
    }

    @Override
    public AbstractSyntaxTree<V> compileSingleStatement(String in)
            throws QuarksException {
        List<AbstractSyntaxTree<V>> statements = compile(in);
        return statements == null || statements.size() < 1 ? null : statements
                .get(0);
    }

    @Override
    public AbstractSyntaxTree<V> compileSingleStatement(Reader in)
            throws QuarksException {
        List<AbstractSyntaxTree<V>> statements = compile(in);
        return statements == null || statements.size() < 1 ? null : statements
                .get(0);
    }

    @Override
    public AbstractSyntaxTree<V> compileSingleStatement(Reader in, int optimize)
            throws QuarksException {
        List<AbstractSyntaxTree<V>> statements = compile(in, optimize);
        return statements == null || statements.size() < 1 ? null : statements
                .get(0);
    }

    protected AbstractSyntaxTree<V> optimize(AbstractSyntaxTree<V> ast, int level) throws Exception {
        return ast;
    }

    @Override
    public InternalContextBuilder<V> getInternalContextBuilder() {
        return internalContextBuilder;
    }

    public void setInternalContextBuilder(InternalContextBuilder<V> internalContextBuilder) {
        this.internalContextBuilder = internalContextBuilder;
    }
}
