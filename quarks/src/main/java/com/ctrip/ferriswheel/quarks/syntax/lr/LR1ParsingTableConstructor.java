package com.ctrip.ferriswheel.quarks.syntax.lr;

import com.ctrip.ferriswheel.quarks.Symbol;
import com.ctrip.ferriswheel.quarks.exception.QuarksLexicalException;
import com.ctrip.ferriswheel.quarks.exception.QuarksSyntaxException;
import com.ctrip.ferriswheel.quarks.syntax.bnf.BNFParser;
import com.ctrip.ferriswheel.quarks.syntax.bnf.BNFRule;
import com.ctrip.ferriswheel.quarks.syntax.bnf.BNFSymbol;
import com.ctrip.ferriswheel.quarks.syntax.bnf.BuiltinSymbols;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;

public class LR1ParsingTableConstructor {

    private HashMap<Closure, Closure> closures = new HashMap<>();
    private int closureIdSeq = 0;
    private BNFParser parser = new BNFParser();
    private Closure rootClosure;

    /**
     * Construct by using input stream as source.
     * 
     * @param is
     * @return
     * @throws IOException
     * @throws QuarksSyntaxException
     * @throws QuarksLexicalException
     */
    public ParsingTable construct(InputStream is) throws IOException,
            QuarksLexicalException, QuarksSyntaxException {
        return construct(new InputStreamReader(is));
    }

    /**
     * Construct by using string as source.
     * 
     * @param in
     * @return
     * @throws QuarksSyntaxException
     * @throws QuarksLexicalException
     */
    public ParsingTable construct(Reader in) throws QuarksLexicalException,
            QuarksSyntaxException {
        BNFRule rootRule = null;
        Map<Symbol, BNFRule> ruleDict = new HashMap<Symbol, BNFRule>();
        parser.setInput(in);
        BNFRule rule;
        while ((rule = parser.next()) != null) {
            if (rootRule == null)
                rootRule = rule;
            ruleDict.put(rule.getSymbol(), rule);
        }
        checkRuleDict(ruleDict);
        return construct(rootRule, ruleDict);
    }

    private void checkRuleDict(Map<Symbol, BNFRule> ruleDict)
            throws QuarksSyntaxException {
        HashSet<Symbol> connectedSymbols = new HashSet<Symbol>();
        Stack<Symbol> stack = new Stack<Symbol>();
        stack.push(BNFSymbol.nonTerminalSymbol(BuiltinSymbols.QUARKS));

        LinkedList<Symbol> undefinedNonTerminalSymbols = new LinkedList<Symbol>();

        while (!stack.isEmpty()) {
            Symbol symbol = stack.pop();
            if (connectedSymbols.contains(symbol))
                continue;

            connectedSymbols.add(symbol);
            if (ruleDict.get(symbol) == null) {
                undefinedNonTerminalSymbols.add(symbol);
                continue;
            }

            for (List<Symbol> choice : ruleDict.get(symbol).getChoices()) {
                for (Symbol s : choice) {
                    if (!s.isTerminal() && !connectedSymbols.contains(s)) {
                        stack.push(s);
                    }
                }
            }
        }

        ArrayList<BNFRule> aloneRules = new ArrayList<BNFRule>();
        for (BNFRule rule : ruleDict.values()) {
            if (!connectedSymbols.contains(rule.getSymbol()))
                aloneRules.add(rule);
        }

        if (!undefinedNonTerminalSymbols.isEmpty() || !aloneRules.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            if (!undefinedNonTerminalSymbols.isEmpty()) {
                sb.append("Undefined non-terminal symbols (")
                        .append(undefinedNonTerminalSymbols.size())
                        .append("): \n");
                for (Symbol s : undefinedNonTerminalSymbols) {
                    sb.append(" ").append(s);
                }
                sb.append("\n");
            }

            if (!aloneRules.isEmpty()) {
                sb.append("Alone rules (").append(aloneRules.size())
                        .append("):\n------------\n");
                for (BNFRule rule : aloneRules) {
                    sb.append(rule).append("\n");
                }
            }

            throw new QuarksSyntaxException(sb.toString());
        }
    }

    /**
     * Construct by using rule dictionary as source.
     * 
     * @param ruleDict
     * @return
     * @throws QuarksSyntaxException
     */
    public ParsingTable construct(BNFRule root, Map<Symbol, BNFRule> ruleDict)
            throws QuarksSyntaxException {
        Map<Symbol, Set<Symbol>> firstSets = getFirstSets(ruleDict);
        Closure closure = new Closure();
        List<Closure.Item> items = closure.getItems();
        Set<Symbol> searchSet = new HashSet<Symbol>();
        searchSet.add(BNFSymbol.terminator());
        for (List<Symbol> choice : root.getChoices()) {
            items.add(new Closure.Item(root.getSymbol(), choice, 0, searchSet));
        }

        completeClosure(closure, firstSets, ruleDict);

        this.rootClosure = closure;
        return genParsingTable(closure);
    }

    public Closure getRootClosure() {
        return rootClosure;
    }

    private Map<Symbol, Set<Symbol>> getFirstSets(Map<Symbol, BNFRule> ruleDict)
            throws QuarksSyntaxException {
        HashMap<Symbol, Set<Symbol>> firstSets = new HashMap<Symbol, Set<Symbol>>();
        HashSet<Symbol> pendingSymbols = new HashSet<Symbol>();
        for (Symbol symbol : ruleDict.keySet()) {
            genFirstSet(pendingSymbols, symbol, firstSets, ruleDict);
        }
        return firstSets;
    }

    private void genFirstSet(HashSet<Symbol> pendingSymbols, Symbol symbol,
            HashMap<Symbol, Set<Symbol>> firstSets,
            Map<Symbol, BNFRule> ruleDict) throws QuarksSyntaxException {
        Set<Symbol> set = firstSets.get(symbol);
        if (set != null)
            return;

        pendingSymbols.add(symbol);
        set = new HashSet<Symbol>();

        BNFRule rule = ruleDict.get(symbol);
        for (List<Symbol> symbols : rule.getChoices()) {
            Symbol first = symbols.get(0);
            if (!ruleDict.containsKey(first)) {
                set.add(first);

            } else {
                if (!firstSets.containsKey(first)) {
                    if (pendingSymbols.contains(first)) {
                        // System.err.println("Ignore recursive dependency: "
                        // + first);

                    } else {
                        genFirstSet(pendingSymbols, first, firstSets, ruleDict);
                    }
                }

                Set<Symbol> s = firstSets.get(first);
                if (s != null)
                    set.addAll(s);
            }
        }

        firstSets.put(symbol, set);
        pendingSymbols.remove(symbol);
    }

    private Closure completeClosure(Closure closure,
            Map<Symbol, Set<Symbol>> firstSets, Map<Symbol, BNFRule> ruleDict) {
        // expand first symbols if they are non-terminal.
        // calculate search sets.
        fillClosure(closure, firstSets, ruleDict);

        // make closure formal
        Closure formal = addClosure(closure);

        if (formal != closure) {
            return formal;
        }

        // System.out.print(closure.toGraphvizDot());

        // generate goto closures
        generateGotoClosures(closure, firstSets, ruleDict);

        // complete following closures
        for (Map.Entry<Symbol, Closure> entry : closure.getTransitions()
                .entrySet()) {
            formal = completeClosure(entry.getValue(), firstSets, ruleDict);
            entry.setValue(formal);
        }

        return closure;
    }

    private void fillClosure(Closure closure,
            Map<Symbol, Set<Symbol>> firstSets, Map<Symbol, BNFRule> ruleDict) {
        // search map keep index of all symbols and there search set.
        // the key means a handle, the value is it's search set.
        Map<Symbol, Set<Symbol>> searchMap = new HashMap<Symbol, Set<Symbol>>();
        // delivery map describe search set delivery relationship.
        // the key Symbol means delivery from,
        // the value Set<Symbole> means delivery to them
        Map<Closure.Kernel, Set<Closure.Kernel>> deliveryMap = new HashMap<Closure.Kernel, Set<Closure.Kernel>>();
        // used for exist item lookup.
        Map<Closure.Kernel, Closure.Item> items = new HashMap<Closure.Kernel, Closure.Item>();
        // task keep items that need to be processed.
        LinkedList<Closure.Item> task = new LinkedList<Closure.Item>();

        for (Closure.Item item : closure.getItems()) {
            items.put(item.getKernel(), item); // initial items should not have
                                                // collision
            task.offer(item);
        }

        while (!task.isEmpty()) {
            Closure.Item item = task.poll();

            // add to search map
            searchMap.put(item.kernel.handle, item.searchSet);

            if (item.kernel.pos >= item.kernel.sequence.size())
                continue;

            Symbol currentSymbol = item.kernel.sequence.get(item.kernel.pos);

            if (currentSymbol.isTerminal())
                continue;

            Set<Symbol> newSearchSet = new HashSet<Symbol>();
            Set<Closure.Kernel> newDeliveryTo = null;

            if (item.kernel.pos + 1 < item.kernel.sequence.size()) { // has next
                Symbol nextSymbol = item.kernel.sequence
                        .get(item.kernel.pos + 1);
                if (nextSymbol.isTerminal())
                    newSearchSet.add(nextSymbol);
                else
                    newSearchSet.addAll(firstSets.get(nextSymbol));

            } else if (item.kernel.pos + 1 == item.kernel.sequence.size()) { // last
                                                                                // one
                newSearchSet.addAll(item.searchSet);

                newDeliveryTo = deliveryMap.get(item.getKernel());
                if (newDeliveryTo == null) {
                    newDeliveryTo = new HashSet<Closure.Kernel>();
                    deliveryMap.put(item.getKernel(), newDeliveryTo);
                }
            }

            BNFRule rule = ruleDict.get(currentSymbol);
            for (List<Symbol> choice : rule.getChoices()) {
                Closure.Item newItem = new Closure.Item(rule.getSymbol(), choice, 0,
                        newSearchSet);
                if (newDeliveryTo != null)
                    newDeliveryTo.add(newItem.getKernel());

                Closure.Item existItem = items.get(newItem.getKernel());

                if (existItem != null) {
                    existItem.searchSet.addAll(newSearchSet);

                } else {
                    items.put(newItem.getKernel(), newItem);
                    searchMap.put(newItem.kernel.handle, newItem.searchSet);
                    task.offer(newItem);
                }
            }
        }

        // delivery

        int countBefore = 0, countAfter = 0;
        for (Closure.Item item : items.values()) {
            countAfter += item.searchSet.size();
        }

        do {
            countBefore = countAfter;

            // delivery once.
            for (Map.Entry<Closure.Kernel, Set<Closure.Kernel>> entry : deliveryMap.entrySet()) {
                Set<Symbol> source = items.get(entry.getKey()).searchSet;
                for (Closure.Kernel dest : entry.getValue()) {
                    items.get(dest).searchSet.addAll(source);
                }
            }

            countAfter = 0;
            for (Closure.Kernel k : deliveryMap.keySet()) {
                countAfter += items.get(k).searchSet.size();
            }

        } while (countAfter != countBefore);

        // add all items
        closure.getItems().clear();
        closure.getItems().addAll(items.values());
    }

    private void generateGotoClosures(Closure closure,
            Map<Symbol, Set<Symbol>> firstSets, Map<Symbol, BNFRule> ruleDict) {
        for (Closure.Item item : closure.getItems()) {
            if (item.kernel.pos >= item.kernel.sequence.size())
                continue;

            Symbol symbol = item.kernel.sequence.get(item.kernel.pos);
            Closure c = closure.getTransitions().get(symbol);
            if (c == null) {
                c = new Closure();
                closure.getTransitions().put(symbol, c);
            }
            c.addOrMergeItem(item.kernel.handle, item.kernel.sequence,
                    item.kernel.pos + 1, item.searchSet);
        }
    }

    private Closure addClosure(Closure closure) {
        if (closures.containsKey(closure))
            return closures.get(closure);
        closure.setId(closureIdSeq++);
        closures.put(closure, closure);
        return closure;
    }

    private ParsingTable genParsingTable(Closure closure) {
        // Map<Closure, Integer> closures = new HashMap<Closure, Integer>();
        // Map<Symbol, Integer> inputs = new HashMap<Symbol, Integer>();
        // int stateCount = 0;
        // int inputCount = 0;
        // Stack<Closure> stack = new Stack<Closure>();
        // stack.push(closure);
        //
        // while (!stack.isEmpty()) {
        // Closure c = stack.pop();
        // closures.put(c, stateCount++);
        // for (Map.Entry<Symbol, Closure> entry : c.transitions.entrySet()) {
        // if (!inputs.containsKey(entry.getKey()))
        // inputs.put(entry.getKey(), inputCount++);
        // if (!closures.containsKey(entry.getValue()))
        // stack.push(entry.getValue());
        // }
        // }
        //
        // System.out.println("digraph G {");
        // for (Closure c : closures.keySet()) {
        // System.out.print(c.toGraphvizDot());
        // }
        // System.out.println("}");

        return new DefaultParsingTable(closure);
    }
}
