package com.ctrip.ferriswheel.quarks.syntax.lr;

import com.ctrip.ferriswheel.quarks.syntax.ASTNode;
import com.ctrip.ferriswheel.quarks.syntax.DefaultSyntaxContext;
import com.ctrip.ferriswheel.quarks.Symbol;
import com.ctrip.ferriswheel.quarks.SyntaxContext;
import com.ctrip.ferriswheel.quarks.Token;
import com.ctrip.ferriswheel.quarks.Tokenizer;
import com.ctrip.ferriswheel.quarks.exception.QuarksLexicalException;
import com.ctrip.ferriswheel.quarks.exception.QuarksSyntaxException;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class DefaultLRParser extends DefaultLRProcessor implements LRParser {
    public DefaultLRParser() {
        this(null, null, DefaultSyntaxContext.getDefaultInstance());
    }

    public DefaultLRParser(ParsingTable table, Tokenizer tokenizer) {
        super(table, tokenizer);
    }

    public DefaultLRParser(ParsingTable table, Tokenizer tokenizer,
                           SyntaxContext syntaxContext) {
        super(table, tokenizer, syntaxContext);
    }

    @Override
    public void setParsingTable(ParsingTable table) {
        super.setParsingTable(table);
    }

    @Override
    public void setSyntaxContext(SyntaxContext syntaxContext) {
        super.setSyntaxContext(syntaxContext);
    }

    @Override
    public void setTokenizer(Tokenizer tokenizer) {
        super.setTokenizer(tokenizer);
    }

    @Override
    public <V> ASTNode<V> next() throws QuarksSyntaxException, QuarksLexicalException {
        Listener<V> listener = new Listener<V>();
        if (!process(listener)) {
            return null;
        }
        return listener.getAST();
    }

    class Listener<V> implements LREventListener {
        Deque<ASTNode<V>> nodes = new ArrayDeque<>();

        @Override
        public void onBegin() {
        }

        @Override
        public void onShift(Symbol handle, Token token) {
            nodes.push(new ASTNode<V>(handle, token));
        }

        @Override
        public void onReduce(Symbol handle, List<Symbol> sequence) throws QuarksSyntaxException {
            ASTNode<V> node = new ASTNode<>(handle);
            for (int i = 0; i < sequence.size(); i++) {
                node.addChild(0, nodes.pop());
            }
            nodes.push(node);
        }

        @Override
        public void onFinish() throws QuarksSyntaxException {
            if (nodes.size() != 1) {
                throw new QuarksSyntaxException("ERRRRRRORRRRRRR!!", getTokenizer());
            }
        }

        ASTNode<V> getAST() {
            return nodes.peek();
        }
    }

}
