package com.ctrip.ferriswheel.quarks.syntax.lr;

import com.ctrip.ferriswheel.quarks.Symbol;
import com.ctrip.ferriswheel.quarks.exception.QuarksSyntaxException;

import java.io.Serializable;
import java.util.*;

public class DefaultParsingTable implements ParsingTable, Serializable {
    private static final long serialVersionUID = 1L;

    public static final int MAX_STATES = 16384;

    class DefaultInstruction implements Instruction, Serializable {
        private static final long serialVersionUID = 1L;

        Action action;
        int state;
        Closure.Item rule;

        public DefaultInstruction(Action action, int state, Closure.Item rule) {
            this.action = action;
            this.state = state;
            this.rule = rule;
        }

        @Override
        public Action getAction() {
            return action;
        }

        @Override
        public int getState() {
            return state;
        }

        @Override
        public Closure.Item getRule() {
            return rule;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(action.toString());
            sb.append("/").append(state).append("/").append(rule);
            return sb.toString();
        }
    }

    private DefaultInstruction[][] instructions;
    private Map<Symbol, Integer> symbols;
    private transient Map<Integer, Symbol> symbolIndexes;

    public DefaultParsingTable() {
    }

    public DefaultParsingTable(Closure root) {
        Set<Closure> processed = new HashSet<>();
        Stack<Closure> stack = new Stack<>();
        stack.add(root);

        symbols = new HashMap<>();
        int symbolId = 0;

        while (!stack.isEmpty()) {
            Closure c = stack.pop();
            processed.add(c);
            for (Map.Entry<Symbol, Closure> entry : c.getTransitions()
                    .entrySet()) {
                if (!symbols.containsKey(entry.getKey()))
                    symbols.put(entry.getKey(), symbolId++);
                if (!processed.contains(entry.getValue()))
                    stack.push(entry.getValue());
            }
            for (Closure.Item item : c.getItems()) {
                for (Symbol s : item.searchSet) {
                    if (!symbols.containsKey(s))
                        symbols.put(s, symbolId++);
                }
            }
        }

        instructions = new DefaultInstruction[processed.size()][symbols.size()];
        processed.clear();
        stack.add(root);

        while (!stack.isEmpty()) {
            Closure c = stack.pop();
            processed.add(c);

            int i = c.getId();
            int j;

            for (Map.Entry<Symbol, Closure> entry : c.getTransitions()
                    .entrySet()) {
                if (!processed.contains(entry.getValue()))
                    stack.push(entry.getValue());

                j = symbols.get(entry.getKey());
                if (instructions[i][j] == null) {
                    instructions[i][j] = new DefaultInstruction(Action.Shift,
                            entry.getValue().getId(), null);
                }
            }
            for (Closure.Item item : c.items) {
                if (item.kernel.isTerminate()) {
                    for (Symbol s : item.searchSet) {
                        j = symbols.get(s);
                        if (instructions[i][j] == null) {
                            instructions[i][j] = new DefaultInstruction(
                                    Action.Reduce, 0, item);
                        }
                    }
                }
            }
        }
    }

    @Override
    public Instruction getInstruction(int state, Symbol input) throws QuarksSyntaxException {
        if (state >= instructions.length)
            throw new QuarksSyntaxException("Unknown state: " + state);

        Integer i = symbols.get(input);
        if (i == null)
            throw new QuarksSyntaxException("Unknown input symbol: " + input);

        return instructions[state][i];
    }

    @Override
    public List<Closure.Item> getAcceptableRules(int state) {
        DefaultInstruction[] ins = instructions[state];
        if (ins == null)
            return null;
        List<Closure.Item> rules = new ArrayList<Closure.Item>();
        for (DefaultInstruction i : ins) {
            if (i != null && i.rule != null)
                rules.add(i.rule);
        }
        return rules;
    }

    @Override
    public List<Symbol> getAcceptableSymbols(int state) {
        DefaultInstruction[] ins = instructions[state];
        if (ins == null)
            return null;

        Map<Integer, Symbol> si = getSymbolIndexes();

        List<Symbol> symbolList = new ArrayList<Symbol>();
        for (int i = 0; i < ins.length; i++) {
            if (ins[i] != null && ins[i].action != null) {
                symbolList.add(si.get(i));
            }
        }
        return symbolList;
    }

    Map<Integer, Symbol> getSymbolIndexes() {
        if (symbolIndexes == null) {
            symbolIndexes = new HashMap<>(symbols.size());
            for (Map.Entry<Symbol, Integer> entry : symbols.entrySet()) {
                symbolIndexes.put(entry.getValue(), entry.getKey());
            }
        }
        return symbolIndexes;
    }

}
