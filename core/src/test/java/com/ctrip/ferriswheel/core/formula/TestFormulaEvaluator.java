package com.ctrip.ferriswheel.core.formula;

import com.ctrip.ferriswheel.common.Environment;
import com.ctrip.ferriswheel.common.Sheet;
import com.ctrip.ferriswheel.common.Workbook;
import com.ctrip.ferriswheel.common.table.Table;
import com.ctrip.ferriswheel.common.variant.ErrorCodes;
import com.ctrip.ferriswheel.common.variant.Value;
import com.ctrip.ferriswheel.common.variant.Variant;
import com.ctrip.ferriswheel.core.asset.DefaultTable;
import com.ctrip.ferriswheel.core.asset.FilingClerk;
import com.ctrip.ferriswheel.core.bean.DefaultEnvironment;
import com.ctrip.ferriswheel.core.formula.eval.FormulaEvaluationContext;
import com.ctrip.ferriswheel.core.formula.eval.FormulaEvaluator;
import com.ctrip.ferriswheel.core.formula.eval.ReferenceResolver;
import com.ctrip.ferriswheel.core.ref.CellReference;
import junit.framework.TestCase;

public class TestFormulaEvaluator extends TestCase {
    private Environment environment;

    @Override
    protected void setUp() throws Exception {
        environment = new DefaultEnvironment.Builder().build();
    }

    public void testEvaluator() {
        Workbook book = new FilingClerk(environment).createWorkbook("test-workbook");
        Table table = book.addSheet("sheet1").addAsset(Table.class, "test");
        table.addRows(2);
        table.addColumns(2);
        ResolverMock resolver = new ResolverMock(book);
        FormulaEvaluator evaluator = new FormulaEvaluator(resolver);

        FormulaElement[] elements = FormulaParser.parse("1+1");
        Variant value = evaluator.evaluate(elements);
        System.out.println(value);
        assertEquals(2, value.intValue());

        elements = FormulaParser.parse("\"abc\"+\"def\"");
        value = evaluator.evaluate(elements);
        assertEquals(ErrorCodes.VALUE, value.errorValue());

        elements = FormulaParser.parse("\"abc\"&\"def\" ");
        value = evaluator.evaluate(elements);
        assertEquals("abcdef", value.strValue());

        table.setCellValue(0, 0, new Value.DecimalValue(3.14));
        table.setCellValue(0, 1, new Value.DecimalValue(100.));

        elements = FormulaParser.parse("A1+B1");
        evaluator.setCurrentAsset(table);
        value = evaluator.evaluate(elements);
        System.out.println(value);

        elements = FormulaParser.parse("test!A1*test!B1");
        evaluator.setCurrentSheet(((DefaultTable) table).getSheet());
        evaluator.setCurrentAsset(null);
        value = evaluator.evaluate(elements);
        System.out.println(value);

        elements = FormulaParser.parse("10*3^2*3&10*3^(2*3)"); // = String.valueOf(10*pow(3,2)*3) + String.valueOf(10*pow(3, 2*3))
        evaluator.setCurrentSheet(null);
        evaluator.setCurrentAsset(null);
        value = evaluator.evaluate(elements);
        assertEquals("2707290", value.strValue());

        elements = FormulaParser.parse("75%");
        evaluator.setCurrentSheet(null);
        evaluator.setCurrentAsset(null);
        value = evaluator.evaluate(elements);
        assertEquals(0.75, value.floatValue(), 0.00001);

        elements = FormulaParser.parse("-75%");
        evaluator.setCurrentSheet(null);
        evaluator.setCurrentAsset(null);
        value = evaluator.evaluate(elements);
        assertEquals(-0.75, value.floatValue(), 0.00001);

        elements = FormulaParser.parse("TODAY()-1");
        evaluator.setCurrentSheet(null);
        evaluator.setCurrentAsset(null);
        value = evaluator.evaluate(elements);
        System.out.println(value.dateValue());
    }

    class ResolverMock implements ReferenceResolver {
        Workbook workbook;

        public ResolverMock(Workbook workbook) {
            this.workbook = workbook;
        }

        @Override
        public Variant resolve(CellReferenceElement referenceElement, FormulaEvaluationContext context) {
            CellReference cellReference = referenceElement.getCellReference();
            Sheet sheet = cellReference.getSheetName() == null ?
                    context.getCurrentSheet() : workbook.getSheet(cellReference.getSheetName());
            Table table = cellReference.getAssetName() == null ?
                    (Table) context.getCurrentAsset() : sheet.getAsset(cellReference.getAssetName());

            return table.getCell(cellReference.getPositionRef().getRowIndex(),
                    cellReference.getPositionRef().getColumnIndex()).getData();
        }

        @Override
        public Variant resolve(NameReferenceElement referenceElement, FormulaEvaluationContext context) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Table resolveTable(String sheetName, String tableName, FormulaEvaluationContext context) {
            if (sheetName == null && tableName == null) {
                return (Table) context.getCurrentAsset();
            }
            Sheet sheet = sheetName == null ? context.getCurrentSheet() : workbook.getSheet(sheetName);
            if (sheet == null) {
                return null;
            }
            return sheet.getAsset(tableName);
        }

    }
}
