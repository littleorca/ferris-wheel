package com.ctrip.ferriswheel.core.formula;

import com.ctrip.ferriswheel.core.formula.func.*;
import com.ctrip.ferriswheel.core.formula.func.date.*;
import com.ctrip.ferriswheel.core.formula.func.text.StringFunctions;
import com.ctrip.ferriswheel.core.formula.symbol.SymbolHandler;
import com.ctrip.ferriswheel.core.formula.symbol.SymbolHandlers;
import com.ctrip.ferriswheel.core.ref.CellReference;
import com.ctrip.ferriswheel.core.ref.NameReference;
import com.ctrip.ferriswheel.core.ref.RangeReference;
import com.ctrip.ferriswheel.core.util.References;
import com.ctrip.ferriswheel.quarks.LexContext;
import com.ctrip.ferriswheel.quarks.Symbol;
import com.ctrip.ferriswheel.quarks.Token;
import com.ctrip.ferriswheel.quarks.exception.QuarksException;
import com.ctrip.ferriswheel.quarks.exception.QuarksLexicalException;
import com.ctrip.ferriswheel.quarks.exception.QuarksSyntaxException;
import com.ctrip.ferriswheel.quarks.syntax.DefaultSyntaxContext;
import com.ctrip.ferriswheel.quarks.syntax.lr.DefaultLRProcessor;
import com.ctrip.ferriswheel.quarks.syntax.lr.LREventListener;
import com.ctrip.ferriswheel.quarks.syntax.lr.ParsingTable;
import com.ctrip.ferriswheel.quarks.token.DefaultTokenizer;
import com.ctrip.ferriswheel.quarks.util.BNFTableFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

// TODO make it non-static, move configurations to Environment.
public class FormulaParser {
    private static final Logger LOG = LoggerFactory.getLogger(FormulaParser.class);

    private static final Map<String, Function> FUNCTIONS = new ConcurrentHashMap<>();

    public static void registerFunction(Function function) {
        FUNCTIONS.putIfAbsent(function.getName().toUpperCase(), function);
    }

    public static Function getFunction(String name) {
        return FUNCTIONS.get(name);
    }

    static {
        // register functions
        registerFunction(new AggregatorFunctions.Average());
        registerFunction(new AggregatorFunctions.Sum());
        registerFunction(new AggregatorFunctions.Count());
        registerFunction(new AggregatorFunctions.Max());
        registerFunction(new AggregatorFunctions.Min());
        registerFunction(new AggregatorFunctions.StDev());
        registerFunction(new AggregatorFunctions.StDevP());
        registerFunction(new AggregatorFunctions.Var());
        registerFunction(new AggregatorFunctions.VarP());

        registerFunction(new If());
        registerFunction(new VLookup());
        // bool
        registerFunction(new And());
        registerFunction(new Or());
        registerFunction(new Not());
        // date
        registerFunction(new DateDif());
        registerFunction(new DateFields.Year());
        registerFunction(new DateFields.Month());
        registerFunction(new DateFields.Day());
        registerFunction(new DateFields.Hour());
        registerFunction(new DateFields.Minute());
        registerFunction(new DateFields.Second());
        registerFunction(new DateFunc());
        registerFunction(new DateParser.DateValue());
        registerFunction(new DateParser.TimeValue());
        registerFunction(new Now());
        registerFunction(new TimeFunc());
        registerFunction(new Today());
        registerFunction(new WeekDay());
        registerFunction(new WeekNum());
        // text
        registerFunction(new StringFunctions.Mid());
    }


    protected DefaultTokenizer tokenizer;
    protected DefaultLRProcessor processor;
    protected Stack<FormulaElement> stack;
    protected Listener listener;

    private static FormulaLexContext createLexContext() {
        return FormulaLexContext.getDefaultInstance();
    }

    FormulaParser() {
        this(BNFTableFactory.INSTANCE.getParsingTable("formula.bnf"));
    }

    private FormulaParser(ParsingTable table) {
        this(table, createLexContext(), new DefaultSyntaxContext());
    }

    private FormulaParser(ParsingTable table, LexContext lexContext, DefaultSyntaxContext syntaxContext) {
        this(table, syntaxContext, new DefaultTokenizer(null, lexContext));
    }

    private FormulaParser(ParsingTable table, DefaultSyntaxContext syntaxContext, DefaultTokenizer tokenizer) {
        this.tokenizer = tokenizer;
        this.processor = new DefaultLRProcessor(table, tokenizer, syntaxContext);
        this.stack = new Stack<>();
        this.listener = new Listener();
    }

    public static FormulaElement[] parse(String formulaString) throws FormulaParserException {
        if (formulaString == null) {
            throw new IllegalArgumentException();
        }
        FormulaParser fp = new FormulaParser();
        try {
            fp.doParse(formulaString);
        } catch (QuarksException e) {
            throw new FormulaParserException(e);
        }
        return fp.getFormulaElements();
    }

    protected void doParse(String formulaString) throws QuarksSyntaxException, QuarksLexicalException {
        // use setInput(String) to ensure all tokens share the same source string.
        tokenizer.setInput(formulaString);
        if (!processor.process(listener)) {
            throw new FormulaParserException();
        }
    }

    protected FormulaElement[] getFormulaElements() {
        FormulaElement[] elements = new FormulaElement[stack.size()];
        for (int i = 0; i < stack.size(); i++) {
            elements[i] = stack.get(i);
        }
        return elements;
    }

    public static String assemble(Iterable<FormulaElement> formulaElements,
                                  int nShiftRows,
                                  int nShiftCols) {
        FormulaElement topElement = null;
        Iterator<FormulaElement> it = formulaElements.iterator();
        while (it.hasNext()) {
            topElement = it.next();
        }
        if (topElement == null) {
            throw new IllegalArgumentException("No valid formula element.");
        }
        Token token = topElement.getToken();
        StringBuilder newFormula = new StringBuilder();
        int pos = topElement.getToken().getFrom();

        for (FormulaElement elem : formulaElements) {
            if (elem.getToken().getSource() != token.getSource()) {
                throw new IllegalArgumentException();
            }

            if (elem instanceof ReferenceElement) {
                if (pos < elem.getToken().getFrom()) {
                    newFormula.append(token.getSource(), pos, elem.getToken().getFrom());
                }

                if (elem instanceof CellReferenceElement) {
                    CellReference cellReference = ((CellReferenceElement) elem).getCellReference();
                    newFormula.append(References.toFormula(cellReference, nShiftRows, nShiftCols));

                } else if (elem instanceof RangeReferenceElement) {
                    RangeReference rangeReference = ((RangeReferenceElement) elem).getRangeReference();
                    newFormula.append(References.toFormula(rangeReference, nShiftRows, nShiftCols));

                } else if (elem instanceof NameReferenceElement) {
                    NameReference nameReference = ((NameReferenceElement) elem).getNameReference();
                    newFormula.append(References.toFormula(nameReference));
                }

                pos = elem.getToken().getTo();
            }
        }

        if (pos < token.getTo()) {
            newFormula.append(token.getSource(), pos, token.getTo());
        }

        return newFormula.toString();
    }

    class Listener implements LREventListener {
        @Override
        public void onBegin() {
            LOG.debug(" # BEGIN");
        }

        @Override
        public void onShift(Symbol handle, Token token) throws QuarksSyntaxException {
            LOG.debug(" # SHIFT: " + handle + ":" + token);
            FormulaElement element;
            switch (token.getType()) {
                case Number:
                    element = new NumericElement(token, new BigDecimal(token.getString()));
                    break;
                case String:
                    element = new StringElement(token, token.getString());
                    break;
                case Operator:
                    element = new OperatorElement(token, token.getString());
                    break;
                case Delimiter:
                    element = new DelimiterElement(token, token.getString());
                    break;
                case Identifier:
                    element = new IdentifierElement(token, token.getString());
                    break;
                case QuotedIdentifier:
                    element = new IdentifierElement(token, token.getString()); // TODO add QuotedIdentifierElement?
                    break;
                case Literal:
                    if (token.startsWith("#")) {
                        element = new ErrorElement(token, token.getString());
                    } else if (token.equalsToIgnoreCase(Boolean.TRUE.toString())) {
                        element = new BooleanElement(token, true);
                    } else if (token.equalsToIgnoreCase(Boolean.FALSE.toString())) {
                        element = new BooleanElement(token, false);
//                    } else if (token.equalsToIgnoreCase("null")) {
//                        element = new NullElement();
                    } else {
                        throw new QuarksSyntaxException("Unrecognized literal", tokenizer);
                    }
                    break;
                case Keyword:
                case Comment:
                default:
                    throw new QuarksSyntaxException(tokenizer);
            }
            stack.push(element);
        }

        @Override
        public void onReduce(Symbol handle, List<Symbol> sequence) throws QuarksSyntaxException {
            LOG.debug(" # REDUCE: " + handle + ":" + sequence);
            SymbolHandler handler = SymbolHandlers.getHandler(handle.getSymbol());
            if (handler == null) {
                throw new QuarksSyntaxException("Failed to get symbol handler:  " + handle.getSymbol());
            }
            handler.reduce(stack, handle, sequence);
        }

        @Override
        public void onFinish() {
            LOG.debug(" # FINISH");
        }

    }
}
