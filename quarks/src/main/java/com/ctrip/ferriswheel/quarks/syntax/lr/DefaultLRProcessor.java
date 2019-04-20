package com.ctrip.ferriswheel.quarks.syntax.lr;

import com.ctrip.ferriswheel.quarks.Symbol;
import com.ctrip.ferriswheel.quarks.SyntaxContext;
import com.ctrip.ferriswheel.quarks.Tokenizer;
import com.ctrip.ferriswheel.quarks.exception.QuarksLexicalException;
import com.ctrip.ferriswheel.quarks.exception.QuarksSyntaxException;
import com.ctrip.ferriswheel.quarks.syntax.DefaultSyntaxContext;
import com.ctrip.ferriswheel.quarks.syntax.bnf.BNFSymbol;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class DefaultLRProcessor implements LRProcessor {

    private ParsingTable table;
    private Tokenizer tokenizer;
    private Symbol input;
    private SyntaxContext syntaxContext;// FIXME

    public DefaultLRProcessor() {
        this(null, null, DefaultSyntaxContext.getDefaultInstance());
    }

    public DefaultLRProcessor(ParsingTable table, Tokenizer tokenizer) {
        this(table, tokenizer, DefaultSyntaxContext.getDefaultInstance());
    }

    public DefaultLRProcessor(ParsingTable table, Tokenizer tokenizer,
                              SyntaxContext syntaxContext) {
        this.table = table;
        this.tokenizer = tokenizer;
        this.syntaxContext = syntaxContext;
    }

    @Override
    public boolean process(LREventListener listener) throws QuarksSyntaxException, QuarksLexicalException {
        Deque<Integer> states = new ArrayDeque<Integer>();
        states.push(0);

        if (input == null)
            input = nextInput();
        if (input == null || input.equals(BNFSymbol.terminator()))
            return false;

        listener.onBegin();
        Symbol lastReducedHandle = null;

        while (true) {
            ParsingTable.Instruction instruction;
            try {
                instruction = table.getInstruction(states.peek(), input);
            } catch (QuarksSyntaxException e) {
                throw new QuarksSyntaxException(e, tokenizer);
            }

            if (instruction == null) {
                StringBuilder sb = new StringBuilder("Illegal syntax!");

                List<Closure.Item> rules = table.getAcceptableRules(states.peek());
                if (rules != null && rules.size() > 0) {
                    sb.append(" Acceptable rules:");
                    for (Closure.Item item : rules) {
                        sb.append(" {").append(item).append("}");
                    }
                }

                List<Symbol> symbols = table
                        .getAcceptableSymbols(states.peek());
                if (symbols != null && symbols.size() > 0) {
                    sb.append(" Acceptable symbols:");
                    for (Symbol symbol : symbols) {
                        if (symbol.isTerminal())
                            sb.append(" ").append(symbol);
                    }
                }

                throw new QuarksSyntaxException(input + " : " + sb.toString(), tokenizer);
            }

            if (ParsingTable.Action.Shift.equals(instruction.getAction())) {
                states.push(instruction.getState());
                listener.onShift(input, tokenizer.getToken());
                nextInput();

            } else if (ParsingTable.Action.Reduce.equals(instruction.getAction())) {
                Closure.Item rule = instruction.getRule();
                for (int i = 0; i < rule.kernel.sequence.size(); i++) {
                    states.pop();
                }

                listener.onReduce(rule.kernel.handle, rule.kernel.sequence);
                lastReducedHandle = rule.kernel.handle;

                if (syntaxContext.getRootSymbol().equals(lastReducedHandle)) {
                    listener.onFinish();
                    return true;
                }

                instruction = table.getInstruction(states.peek(), rule.kernel.handle);
                if (instruction == null || instruction.getAction() != ParsingTable.Action.Shift) {
                    throw new QuarksSyntaxException("Expected shift operation.", tokenizer);
                }

                states.push(instruction.getState());

            } else if (syntaxContext.getRootSymbol().equals(lastReducedHandle)) {
                listener.onFinish();
                return true;

            } else {
                throw new QuarksSyntaxException("Unknown error!", tokenizer);
            }
        }
    }

    Symbol nextInput() throws QuarksLexicalException {
        if (tokenizer.next()) {
            switch (tokenizer.getType()) {
                case Number:
                    input = syntaxContext.getNumberSymbol();
                    break;
                case String:
                    input = syntaxContext.getStringSymbol();
                    break;
                case Identifier:
                    input = syntaxContext.getIdentifierSymbol();
                    break;
                case QuotedIdentifier:
                    input = syntaxContext.getQuotedIdentifierSymbol();
                    break;
                default:
                    input = BNFSymbol.terminalSymbol(tokenizer.getString());
            }

        } else {
            input = syntaxContext.getTerminatorSymbol();
        }

        return input;
    }

    void setParsingTable(ParsingTable table) {
        this.table = table;
    }

    ParsingTable getParsingTable() {
        return table;
    }

    void setSyntaxContext(SyntaxContext syntaxContext) {
        this.syntaxContext = syntaxContext;
    }

    SyntaxContext getSyntaxContext() {
        return syntaxContext;
    }

    void setTokenizer(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
        this.input = null;
    }

    Tokenizer getTokenizer() {
        return tokenizer;
    }
}
