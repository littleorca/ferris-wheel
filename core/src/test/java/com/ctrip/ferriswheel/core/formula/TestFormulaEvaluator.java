package com.ctrip.ferriswheel.core.formula;

import com.ctrip.ferriswheel.core.ref.CellRef;
import com.ctrip.ferriswheel.core.asset.FilingClerk;
import com.ctrip.ferriswheel.core.bean.DefaultEnvironment;
import com.ctrip.ferriswheel.core.bean.TableData;
import com.ctrip.ferriswheel.core.bean.Value;
import com.ctrip.ferriswheel.core.formula.eval.FormulaEvaluationContext;
import com.ctrip.ferriswheel.core.formula.eval.FormulaEvaluator;
import com.ctrip.ferriswheel.core.formula.eval.ReferenceResolver;
import com.ctrip.ferriswheel.core.intf.*;
import com.ctrip.ferriswheel.core.ref.CellRef;
import junit.framework.TestCase;

public class TestFormulaEvaluator extends TestCase {
    private Environment environment;

    @Override
    protected void setUp() throws Exception {
        environment = new DefaultEnvironment.Builder().build();
    }

    public void testEvaluator() {
        Workbook book = new FilingClerk(environment).createWorkbook("test-workbook");
        Table table = book.addSheet("sheet1").addTable("test", new TableData());
        ResolverMock resolver = new ResolverMock(book);
        FormulaEvaluator evaluator = new FormulaEvaluator(resolver);

        FormulaElement[] elements = FormulaParser.parse("1+1");
        Variant value = evaluator.evaluate(elements);
        System.out.println(value);
        assertEquals(2, value.intValue());

        elements = FormulaParser.parse("\"abc\"+\"def\"");
        value = evaluator.evaluate(elements);
        assertEquals(ErrorCode.ILLEGAL_VALUE, value.errorValue());

        elements = FormulaParser.parse("\"abc\"&\"def\"");
        value = evaluator.evaluate(elements);
        assertEquals("abcdef", value.strValue());

        table.setCellValue(0, 0, new Value.DecimalValue(3.14));
        table.setCellValue(0, 1, new Value.DecimalValue(100.));

        elements = FormulaParser.parse("A1+B1");
        evaluator.setCurrentTable(table);
        value = evaluator.evaluate(elements);
        System.out.println(value);

        elements = FormulaParser.parse("test!A1*test!B1");
        evaluator.setCurrentSheet(table.getSheet());
        evaluator.setCurrentTable(null);
        value = evaluator.evaluate(elements);
        System.out.println(value);

        elements = FormulaParser.parse("10*3^2*3&10*3^(2*3)"); // = String.valueOf(10*pow(3,2)*3) + String.valueOf(10*pow(3, 2*3))
        evaluator.setCurrentSheet(null);
        evaluator.setCurrentTable(null);
        value = evaluator.evaluate(elements);
        assertEquals("2707290", value.strValue());

        elements = FormulaParser.parse("75%");
        evaluator.setCurrentSheet(null);
        evaluator.setCurrentTable(null);
        value = evaluator.evaluate(elements);
        assertEquals(0.75, value.floatValue(), 0.00001);

        elements = FormulaParser.parse("-75%");
        evaluator.setCurrentSheet(null);
        evaluator.setCurrentTable(null);
        value = evaluator.evaluate(elements);
        assertEquals(-0.75, value.floatValue(), 0.00001);

        elements = FormulaParser.parse("TODAY()-1");
        evaluator.setCurrentSheet(null);
        evaluator.setCurrentTable(null);
        value = evaluator.evaluate(elements);
        System.out.println(value.dateValue());
    }

    class ResolverMock implements ReferenceResolver {
        Workbook workbook;

        public ResolverMock(Workbook workbook) {
            this.workbook = workbook;
        }

        @Override
        public Variant resolve(CellRef cellRef, FormulaEvaluationContext context) {
            Sheet sheet = cellRef.getSheetName() == null ?
                    context.getCurrentSheet() : workbook.getSheet(cellRef.getSheetName());
            Table table = cellRef.getTableName() == null ?
                    context.getCurrentTable() : sheet.getTable(cellRef.getTableName());

            return table.getCell(cellRef.getRowIndex(), cellRef.getColumnIndex());
        }

        @Override
        public Table resolveTable(String sheetName, String tableName, FormulaEvaluationContext context) {
            if (sheetName == null && tableName == null) {
                return context.getCurrentTable();
            }
            Sheet sheet = sheetName == null ? context.getCurrentSheet() : workbook.getSheet(sheetName);
            if (sheet == null) {
                return null;
            }
            return sheet.getTable(tableName);
        }

    }
}
