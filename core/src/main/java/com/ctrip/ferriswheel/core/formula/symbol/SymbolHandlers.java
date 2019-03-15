package com.ctrip.ferriswheel.core.formula.symbol;

import com.ctrip.ferriswheel.core.formula.*;
import com.ctrip.ferriswheel.core.ref.CellRef;
import com.ctrip.ferriswheel.core.ref.RangeRef;
import com.ctrip.ferriswheel.core.util.References;
import com.ctrip.ferriswheel.quarks.Symbol;
import com.ctrip.ferriswheel.quarks.Token;
import com.ctrip.ferriswheel.quarks.token.DefaultToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is strongly related to formula.bnf, please refer it.
 */
public class SymbolHandlers {
    private static final Logger LOG = LoggerFactory.getLogger(SymbolHandler.class);
    private static final Map<String, SymbolHandler> HANDLERS = new ConcurrentHashMap<>();

    static {
        r(new TransparentHandler());
        r(new SimpleReferenceHandler());
        r(new RangeHandler());
        r(new ReferenceHandler());
        r(new PositiveHandler());
        r(new NegativeHandler());
        r(new PercentHandler());
        r(new ParenthesisHandler());
        r(new ConcatHandler());
        r(new AddHandler());
        r(new SubHandler());
        r(new MulHandler());
        r(new DivHandler());
        r(new PowHandler());
        r(new CompareHandler());
        r(new ParamsHandler());
        r(new FuncHandler());
    }

    static void r(SymbolHandler handler) {
        Handle s = handler.getClass().getAnnotation(Handle.class);
        if (s == null || s.value() == null || s.value().length == 0) {
            throw new RuntimeException("Symbol handler class must be correctly annotated.");
        }
        for (String symbol : s.value()) {
            register(symbol, handler);
        }
    }

    static void register(String symbol, SymbolHandler handler) {
        LOG.info("Register symbol handler \"" + symbol + "\" => \"" + handler.getClass().getName() + "\"");
        SymbolHandler old = HANDLERS.put(symbol, handler);
        if (old != null) {
            LOG.warn("Duplicated handler of symbol \""
                    + symbol + "\", former one \""
                    + old.getClass().getName() + "\" was overrode by \""
                    + handler.getClass().getName() + "\"");
        }
    }

    public static SymbolHandler getHandler(String symbol) {
        SymbolHandler handler = HANDLERS.get(symbol);
        if (handler == null) {
            throw new RuntimeException("Symbol handler of \"" + symbol + "\" not found!");
        }
        return handler;
    }

    @Handle({"quarks", "expression", "cmp", "additive", "unary", "factor",
            "qualifier", "local_reference", "term1", "term2", "term3"})
    public static class TransparentHandler implements SymbolHandler {
        @Override
        public void reduce(Stack<FormulaElement> stack, Symbol handle, List<Symbol> sequence) {
            // do nothing
        }
    }

    //<reference> ::= <qualifier> "!" <qualifier> "!" <local_reference>
    //              | <qualifier> "!" <local_reference>
    //              | <local_reference>
    //<qualifier> ::= <identifier>
    //              | <string>
    //<local_reference> ::= <range_reference>
    //                    | <cell_reference>
    //<range_reference> ::= <identifier> ":" <identifier>
    //                    | <number> ":" <number>
    //<cell_reference> ::= <identifier>

    @Handle("simple_reference")
    public static class SimpleReferenceHandler implements SymbolHandler {
        //<cell_reference> ::= <identifier>
        @Override
        public void reduce(Stack<FormulaElement> stack, Symbol handle, List<Symbol> sequence) {
            FormulaElement identifier = stack.pop();
            SimpleReferenceElement elem = new SimpleReferenceElement(References.parseSimpleCellRef(identifier.getTokenString()));
            elem.setSlices(1);
            elem.setToken(identifier.getToken());
            stack.push(elem);
        }
    }

    @Handle("range_reference")
    public static class RangeHandler implements SymbolHandler {
        //<range_reference> ::= <identifier> ":" <identifier>
        //                    | <number> ":" <number>
        @Override
        public void reduce(Stack<FormulaElement> stack, Symbol handle, List<Symbol> sequence) {
            FormulaElement to = stack.pop();
            stack.pop(); // ":"
            FormulaElement from = stack.pop();

            RangeRef rangeRef = new RangeRef();

            rangeRef.setUpperLeft(References.parseRangeEndRef(from.getTokenString()));
            rangeRef.setLowerRight(References.parseRangeEndRef(to.getTokenString()));

            if (!rangeRef.isValid()) {
                throw new FormulaParserException("Invalid range reference(1).");
            }

            // either of row or column index must be set
            if (rangeRef.getLeft() == -1 && rangeRef.getTop() == -1) {
                throw new FormulaParserException("Invalid range reference(2).");
            }

            // row count part
            if ((rangeRef.getTop() == -1 && rangeRef.getBottom() != -1)
                    || (rangeRef.getTop() != -1 && rangeRef.getBottom() == -1)) {
                throw new FormulaParserException("Invalid range reference(3).");
            }

            // column count part
            if ((rangeRef.getLeft() == -1 && rangeRef.getRight() != -1)
                    || (rangeRef.getLeft() != -1 && rangeRef.getRight() == -1)) {
                throw new FormulaParserException("Invalid range reference(4).");
            }

            RangeReferenceElement elem = new RangeReferenceElement(rangeRef);
            elem.setSlices(1);
            elem.setToken(mergeToken(from.getToken(), to.getToken(), null, null));
            stack.push(elem);
        }
    }

    @Handle("reference")
    public static class ReferenceHandler implements SymbolHandler {
        //<reference> ::= <qualifier> "!" <qualifier> "!" <local_reference>
        //              | <qualifier> "!" <local_reference>
        //              | <local_reference>
        //<qualifier> ::= <identifier>
        //              | <string>
        //<local_reference> ::= <range_reference>
        //                    | <cell_reference>
        //<range_reference> ::= <identifier> ":" <identifier>
        //                    | <number> ":" <number>
        //<cell_reference> ::= <identifier>
        @Override
        public void reduce(Stack<FormulaElement> stack, Symbol handle, List<Symbol> sequence) {
            FormulaElement q1 = null, q2 = null;
            ReferenceElement elem = (ReferenceElement) stack.pop();
            if (sequence.size() >= 3) {
                stack.pop(); // "!"
                q2 = stack.pop();

                if (sequence.size() == 5) {
                    stack.pop(); // "!"
                    q1 = stack.pop();
                } else if (sequence.size() != 3) {
                    throw new RuntimeException();
                }
            } else if (sequence.size() != 1) {
                throw new RuntimeException();
            }

            String sheetName = q1 == null ? null : q1.getTokenString();
            String tableName = q2 == null ? null : q2.getTokenString();
            FormulaElement startElement = q1 != null ? q1 : q2 != null ? q2 : null;

            if (elem instanceof SimpleReferenceElement) {
                CellRef cellRef = ((SimpleReferenceElement) elem).getCellRef();
                cellRef.setSheetName(sheetName);
                cellRef.setTableName(tableName);
            } else if (elem instanceof RangeReferenceElement) {
                RangeRef rangeRef = ((RangeReferenceElement) elem).getRangeRef();
                rangeRef.getUpperLeft().setSheetName(sheetName);
                rangeRef.getUpperLeft().setTableName(tableName);
                rangeRef.getLowerRight().setSheetName(sheetName);
                rangeRef.getLowerRight().setTableName(tableName);
            }

            if (startElement != null) {
                elem.setToken(mergeToken(startElement.getToken(), elem.getToken(), null, null));
            }

            stack.push(elem);
        }
    }
//
//    @Handle("qualified_cell")
//    public static class QualifiedCellHandler implements SymbolHandler {
//        @Override
//        public void reduce(Stack<FormulaElement> stack, Symbol handle, List<Symbol> sequence) {
//            if (sequence.size() == 1) {
//                return; // <qualified_cell> ::= <cell>
//            }
//            CellReferenceElement cell = (CellReferenceElement) stack.pop();
//            stack.pop(); // "!"
//            FormulaElement qualifier = stack.pop();
//            cell.getCellRef().setTableName(qualifier.getTokenString());
//            if (sequence.size() == 5) {
//                stack.pop(); // "!"
//                qualifier = stack.pop();
//                cell.getCellRef().setSheetName(qualifier.getTokenString());
//            }
//            cell.setToken(mergeToken(qualifier.getToken(), cell.getToken(), Token.Type.Identifier, null));
//            cell.setSlices(1);
//            stack.push(cell);
//        }
//    }
//
//    /**
//     * <qualified_range> ::= <qualified_cell> ":" <cell>
//     */
//    @Handle("qualified_range")
//    public static class QualifiedRangeHandler implements SymbolHandler {
//        @Override
//        public void reduce(Stack<FormulaElement> stack, Symbol handle, List<Symbol> sequence) {
//            CellReferenceElement cell = (CellReferenceElement) stack.pop();
//            stack.pop(); // ":"
//            CellReferenceElement qualifiedCell = (CellReferenceElement) stack.pop();
//            RangeReferenceElement range = new RangeReferenceElement(new RangeRef(qualifiedCell.getCellRef(), cell.getCellRef()));
//            range.setSlices(1);
//            range.setToken(mergeToken(qualifiedCell.getToken(), cell.getToken(), Token.Type.Identifier, null));
//            stack.push(range);
//        }
//    }

    public static abstract class PrefixUnaryHandler implements SymbolHandler {
        @Override
        public void reduce(Stack<FormulaElement> stack, Symbol handle, List<Symbol> sequence) {
            int pos = stack.size() - stack.peek().getSlices() - 1;
            FormulaElement op = stack.remove(pos);
            FormulaElement unary = createElement(mergeToken(op.getToken(), stack.peek().getToken(), null, null),
                    op.getTokenString());
            unary.setSlices(stack.peek().getSlices() + 1);
            stack.push(unary);
        }

        protected abstract FormulaElement createElement(Token opToken, String opTokenString);
    }

    @Handle("positive")
    public static class PositiveHandler extends PrefixUnaryHandler {
        @Override
        protected FormulaElement createElement(Token token, String tokenString) {
            return new UnaryElement.Positive(token, tokenString);
        }
    }

    @Handle("negative")
    public static class NegativeHandler extends PrefixUnaryHandler {
        @Override
        protected FormulaElement createElement(Token token, String tokenString) {
            return new UnaryElement.Negative(token, tokenString);
        }
    }

    public static abstract class SuffixUnaryHandler implements SymbolHandler {
        @Override
        public void reduce(Stack<FormulaElement> stack, Symbol handle, List<Symbol> sequence) {
            FormulaElement op = stack.pop();
            FormulaElement unary = createElement(mergeToken(stack.peek().getToken(), op.getToken(), null, null),
                    op.getTokenString());
            unary.setSlices(stack.peek().getSlices() + 1);
            stack.push(unary);
        }

        protected abstract FormulaElement createElement(Token opToken, String opTokenString);
    }

    @Handle("percent")
    public static class PercentHandler extends SuffixUnaryHandler {
        @Override
        protected FormulaElement createElement(Token token, String tokenString) {
            return new UnaryElement.Percent(token, tokenString);
        }
    }

    static abstract class BinaryHandler implements SymbolHandler {
        @Override
        public void reduce(Stack<FormulaElement> stack, Symbol handle, List<Symbol> sequence) {
            int pos = stack.size() - 1;
            int slices = stack.get(pos).getSlices();
            pos -= slices; // pos points to "+"/etc
            stack.remove(pos);
            FormulaElement first = stack.get(--pos);
            slices += first.getSlices();
            FormulaElement binary = createElement(stack, handle, sequence);
            binary.setToken(mergeToken(first.getToken(), stack.peek().getToken(), null, null));
            if (binary.getTokenString() == null) {
                binary.setTokenString(handle.getSymbol());
            }
            binary.setSlices(slices + 1);
            stack.push(binary);
        }

        protected abstract FormulaElement createElement(Stack<FormulaElement> stack, Symbol handle, List<Symbol> sequence);
    }

    @Handle("concat")
    public static class ConcatHandler extends BinaryHandler {
        @Override
        protected FormulaElement createElement(Stack<FormulaElement> stack, Symbol handle, List<Symbol> sequence) {
            return new BinaryElement.Concat();
        }
    }

    @Handle("add")
    public static class AddHandler extends BinaryHandler {
        @Override
        protected FormulaElement createElement(Stack<FormulaElement> stack, Symbol handle, List<Symbol> sequence) {
            return new BinaryElement.Add();
        }
    }

    @Handle("sub")
    public static class SubHandler extends BinaryHandler {
        @Override
        protected FormulaElement createElement(Stack<FormulaElement> stack, Symbol handle, List<Symbol> sequence) {
            return new BinaryElement.Subtract();
        }
    }

    @Handle("mul")
    public static class MulHandler extends BinaryHandler {
        @Override
        protected FormulaElement createElement(Stack<FormulaElement> stack, Symbol handle, List<Symbol> sequence) {
            return new BinaryElement.Multiply();
        }
    }

    @Handle("div")
    public static class DivHandler extends BinaryHandler {
        @Override
        protected FormulaElement createElement(Stack<FormulaElement> stack, Symbol handle, List<Symbol> sequence) {
            return new BinaryElement.Divide();
        }
    }

    @Handle("pow")
    public static class PowHandler extends BinaryHandler {
        @Override
        protected FormulaElement createElement(Stack<FormulaElement> stack, Symbol handle, List<Symbol> sequence) {
            return new BinaryElement.Power();
        }
    }

    @Handle({"eq", "lt", "gt", "le", "ge", "ne"})
    public static class CompareHandler extends BinaryHandler {
        private static Map<String, Class<? extends BinaryElement.Compare>> handlerMapping = new ConcurrentHashMap<>();

        static {
            handlerMapping.putIfAbsent("eq", BinaryElement.Equal.class);
            handlerMapping.putIfAbsent("lt", BinaryElement.LessThan.class);
            handlerMapping.putIfAbsent("gt", BinaryElement.GreaterThan.class);
            handlerMapping.putIfAbsent("le", BinaryElement.LessThanOrEqual.class);
            handlerMapping.putIfAbsent("ge", BinaryElement.GreaterThanOrEqual.class);
            handlerMapping.putIfAbsent("ne", BinaryElement.NotEqual.class);
        }

        @Override
        protected FormulaElement createElement(Stack<FormulaElement> stack, Symbol handle, List<Symbol> sequence) {
            try {
                return handlerMapping.get(handle.getSymbol()).newInstance();
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Handle("parenthesis")
    public static class ParenthesisHandler implements SymbolHandler {
        @Override
        public void reduce(Stack<FormulaElement> stack, Symbol handle, List<Symbol> sequence) {
            // "(" <expression> ")"
            stack.pop();
            int pos = stack.size() - stack.peek().getSlices() - 1;
            stack.remove(pos);
            // no push here
        }
    }

    // <params> "," <expression>
    //            | <expression>
    @Handle("params")
    public static class ParamsHandler implements SymbolHandler {
        @Override
        public void reduce(Stack<FormulaElement> stack, Symbol handle, List<Symbol> sequence) {
            if (sequence.size() == 1) { // <expression>
                FormulaElement exp = stack.peek();
                ParamsElement params = new ParamsElement(exp.getToken(), exp.getTokenString(), 1, exp.getSlices() + 1);
                stack.push(params);

            } else {
                int pos = stack.size() - stack.peek().getSlices() - 1;
                stack.remove(pos); // ","
                ParamsElement params = (ParamsElement) stack.remove(pos - 1); // Incomplete Function Element
                params.setSlices(params.getSlices() + stack.peek().getSlices());
                params.increaseCount();
                stack.push(params);
            }
        }
    }

    //<function> ::= <identifier> "(" <params> ")"
    //        | <identifier> "(" ")"
    @Handle("function")
    public static class FuncHandler implements SymbolHandler {
        @Override
        public void reduce(Stack<FormulaElement> stack, Symbol handle, List<Symbol> sequence) {
            if (sequence.size() == 3) { // <identifier> "(" ")"
                FormulaElement end = stack.pop();
                stack.pop();
                FormulaElement name = stack.pop();
                FuncElement func = new FuncElement(mergeToken(name.getToken(), end.getToken(), null, null),
                        name.getTokenString(), 0, 1);
                stack.push(func);

            } else {
                FormulaElement end = stack.pop(); // ")"
                ParamsElement params = (ParamsElement) stack.pop();
                int pos = stack.size() - params.getSlices();
                stack.remove(pos); // "("
                FormulaElement name = stack.remove(pos - 1); // <identifier>
                FuncElement func = new FuncElement(mergeToken(name.getToken(), end.getToken(), null, null),
                        name.getTokenString(), params.getCount(), params.getSlices()); // ParamsElement's slices = params' slices + 1 = FuncElement's slices.
                stack.push(func);
            }
        }

//        FuncElement createFunction(Token token, String name, FuncElement holder) {
//            Class<? extends FuncElement> funcClass = FormulaParser.FUNCTIONS.get(name.toUpperCase()); //  FIXME
//            if (funcClass == null) {
//                throw new RuntimeException("Unsupported function: " + name);
//            }
//
//            FuncElement func;
//            try {
//                func = funcClass.newInstance();
//            } catch (InstantiationException e) {
//                throw new RuntimeException(e);
//            } catch (IllegalAccessException e) {
//                throw new RuntimeException(e);
//            }
//
//            func.setToken(token);
//            func.setTokenString(name);
//            if (holder == null) { // <identifier> "(" ")"
//                func.setArgc(0);
//                func.setSlices(1);
//            } else { // function with args
//                func.setArgc(holder.getArgc());
//                func.setSlices(holder.getSlices());
//            }
//            return func;
//        }
    }

    private static DefaultToken mergeToken(Token first, Token second, Token.Type type, String tokenString) {
        return new DefaultToken(first.getSource(),
                first.getFrom(),
                second.getTo(),
                first.getLine(),
                type,
                tokenString);
    }
}
