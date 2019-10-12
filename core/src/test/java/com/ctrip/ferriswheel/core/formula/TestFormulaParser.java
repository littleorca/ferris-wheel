package com.ctrip.ferriswheel.core.formula;

import com.ctrip.ferriswheel.core.ref.Anchor;
import com.ctrip.ferriswheel.core.ref.NameReference;
import com.ctrip.ferriswheel.core.ref.RangeReference;
import junit.framework.TestCase;

import java.util.Arrays;
import java.util.Stack;

import static com.ctrip.ferriswheel.core.formula.BinaryElement.*;

public class TestFormulaParser extends TestCase {
    public void testSimpleSum() {
        FormulaElement[] el = FormulaParser.parse("SUM('table-1'!A:B, 'a\" ''b'' c'!A:Z)");
        assertEquals(3, el.length);

        assertTrue(el[0] instanceof RangeReferenceElement);
        RangeReferenceElement ref = (RangeReferenceElement) el[0];
        RangeReference rangeRef = ref.getRangeReference();
        assertEquals("table-1", rangeRef.getAssetName());
        assertEquals(new Anchor(0, false), rangeRef.getLeftAnchor());
        assertNull(rangeRef.getTopAnchor());
        assertEquals(new Anchor(1, false), rangeRef.getRightAnchor());
        assertNull(rangeRef.getBottom());
        assertEquals(1, ref.getSlices());

        assertTrue(el[1] instanceof RangeReferenceElement);
        ref = (RangeReferenceElement) el[1];
        assertEquals("a\" 'b' c", ref.getRangeReference().getAssetName());
        assertEquals(new Anchor(0, false), ref.getRangeReference().getLeftAnchor());
        assertNull(ref.getRangeReference().getTopAnchor());
        assertEquals(new Anchor(25, false), ref.getRangeReference().getRightAnchor());
        assertNull(ref.getRangeReference().getBottomAnchor());
        assertEquals(1, ref.getSlices());

        assertTrue(el[2] instanceof FuncElement);
        FuncElement fun = (FuncElement) el[2];
        assertEquals("SUM", fun.getFunction().getName());
        assertEquals(2, fun.getArgc());
        assertEquals(3, fun.getSlices());
    }

    public void testCalculateFieldFormula() {
        FormulaElement[] el = FormulaParser.parse("foo + 'foo bar'*'比例'");
        assertEquals(5, el.length);
        assertTrue(el[0] instanceof ReferenceElement);
    }

    public void testErrorToken() {
        FormulaElement[] elements = FormulaParser.parse("#REF!");
        checkAndPrintElements("#REF!", elements);

        elements = FormulaParser.parse("A1+#REF!");
        checkAndPrintElements("A1+#REF!", elements);
    }

    public void testMultilineFormula() {
        String f = "\"让我猜猜，表格里foo-good对应的值为：\\n\\t\"\n" +
                "&t1!B2\n" +
                "&\"\\n\\n\\t--猜于：\"\n" +
                "&NOW()\n" +
                "&\"\\n\\n楼上猜对了！\"";

        FormulaElement[] elements = FormulaParser.parse(f);
        checkAndPrintElements(f, elements);

        String reassembly = FormulaParser.assemble(Arrays.asList(elements), 0, 0);
        assertEquals(f, reassembly);

        reassembly = FormulaParser.assemble(Arrays.asList(elements), 1, -1);
        assertEquals(f.replaceAll("B2", "A3"), reassembly);
    }

    public void testParser() {
        FormulaElement[] elements = FormulaParser.parse("A1");
        checkAndPrintElements("A1", elements);

        elements = FormulaParser.parse("true");
        checkAndPrintElements("true", elements);

        elements = FormulaParser.parse("false");
        checkAndPrintElements("false", elements);

        elements = FormulaParser.parse("table!$A$1");
        checkAndPrintElements("table!$A$1", elements);

        elements = FormulaParser.parse("table!'foo'");
        checkAndPrintElements("table!'foo", elements);
        assertEquals(1, elements.length);
        assertTrue(elements[0] instanceof NameReferenceElement);
        NameReference nameReference = ((NameReferenceElement) elements[0]).getNameReference();
        assertNull(nameReference.getSheetName());
        assertEquals("table", nameReference.getAssetName());
        assertEquals("foo", nameReference.getTargetName());

        elements = FormulaParser.parse("sheet!table!$A$1");
        checkAndPrintElements("sheet!table!$A$1", elements);

        elements = FormulaParser.parse("$A$1:$C$5");
        checkAndPrintElements("$A$1:$C$5", elements);

        elements = FormulaParser.parse("table!$A$1:$C$5");
        checkAndPrintElements("table!$A$1:$C$5", elements);

        elements = FormulaParser.parse("sheet!table!$A$1:$C$5");
        checkAndPrintElements("sheet!table!$A$1:$C$5", elements);

        elements = FormulaParser.parse("sheet!table!'foo'");
        checkAndPrintElements("sheet!table!'foo", elements);
        assertEquals(1, elements.length);
        assertTrue(elements[0] instanceof NameReferenceElement);
        nameReference = ((NameReferenceElement) elements[0]).getNameReference();
        assertEquals("sheet", nameReference.getSheetName());
        assertEquals("table", nameReference.getAssetName());
        assertEquals("foo", nameReference.getTargetName());

        elements = FormulaParser.parse("A1+B1*C1");
        checkAndPrintElements("A1+B1*C1", elements);

        elements = FormulaParser.parse("(A1+B1)*C1");
        checkAndPrintElements("(A1+B1)*C1", elements);

        elements = FormulaParser.parse("SUM(A1,B1)");
        checkAndPrintElements("SUM(A1,B1)", elements);
        assertEquals(3, elements.length);
        assertTrue(elements[2] instanceof FuncElement);
        FuncElement sum = (FuncElement) elements[2];
        assertEquals(2, sum.getArgc());
        assertEquals(3, sum.getSlices());
        assertNotNull(sum.getFunction());
        assertSame(FormulaParser.getFunction("SUM"), sum.getFunction());

        elements = FormulaParser.parse("IF(A1+B1, C1*D1, SUM(C1:D2))");
        checkAndPrintElements("IF(A1+B1, C1*D1, SUM(C1:D2))", elements);

        elements = FormulaParser.parse("-A1");
        checkAndPrintElements("-A1", elements);

        elements = FormulaParser.parse("-A1%");
        checkAndPrintElements("-A1%", elements);

        elements = FormulaParser.parse("18%");
        checkAndPrintElements("18%", elements);

        elements = FormulaParser.parse("NOW()");
        checkAndPrintElements("NOW()", elements);

        elements = FormulaParser.parse("A1^10&B2^(2*2)");
        checkAndPrintElements("A1^10", elements);

        elements = FormulaParser.parse("A1=B2");
        checkAndPrintElements("A1=B2", elements);
        assertTrue(elements[2] instanceof Equal);

        elements = FormulaParser.parse("A1<B2");
        checkAndPrintElements("A1<B2", elements);
        assertTrue(elements[2] instanceof LessThan);

        elements = FormulaParser.parse("A1>B2");
        checkAndPrintElements("A1>B2", elements);
        assertTrue(elements[2] instanceof GreaterThan);

        elements = FormulaParser.parse("A1<=B2");
        checkAndPrintElements("A1<B2", elements);
        assertTrue(elements[2] instanceof LessThanOrEqual);

        elements = FormulaParser.parse("A1>=B2");
        checkAndPrintElements("A1>B2", elements);
        assertTrue(elements[2] instanceof GreaterThanOrEqual);

        elements = FormulaParser.parse("A1<>B2");
        checkAndPrintElements("A1<>B2", elements);
        assertTrue(elements[2] instanceof NotEqual);
    }

    private void checkAndPrintElements(String title, FormulaElement[] elements) {
        printElements(title, elements);

        Stack<FormulaElement[]> pendingSequence = new Stack<>();
        pendingSequence.push(elements);
        while (!pendingSequence.isEmpty()) {
            FormulaElement[] seq = pendingSequence.pop();
            printElements("\tcheck slices", seq);
            int pos = seq.length - 1;
            FormulaElement elem = seq[pos];
            assertEquals(seq.length, elem.getSlices());

            pos--;
            while (pos > -1) {
                elem = seq[pos];
                assertTrue(elem.getSlices() > 0);
                if (elem.getSlices() > 1) {
                    pendingSequence.push(Arrays.copyOfRange(seq, pos + 1 - elem.getSlices(), pos + 1));
                }
                pos -= elem.getSlices();
                assertTrue(pos >= -1);
            }
            assertEquals(-1, pos);
        }
    }

    private void printElements(String title, FormulaElement[] elements) {
        System.out.print(title + "\t=> ");
        for (FormulaElement element : elements) {
            System.out.print(" ");
            System.out.print(element);
        }
        System.out.println();
    }

    public void testParseRangeReference() {
        FormulaElement[] f = FormulaParser.parse("table!A:B");
        assertEquals(1, f.length);
        assertTrue(f[0] instanceof RangeReferenceElement);
        RangeReferenceElement r = (RangeReferenceElement) f[0];
        assertEquals(0, r.getToken().getFrom());
        assertEquals(9, r.getToken().getTo());
        RangeReference rr = r.getRangeReference();
        assertTrue(rr.isAlive());
        assertEquals(new Anchor(0, false), rr.getLeftAnchor());
        assertEquals(new Anchor(1, false), rr.getRightAnchor());
        assertNull(rr.getTopAnchor());
        assertNull(rr.getBottomAnchor());
        assertEquals("table", rr.getAssetName());
        assertNull(rr.getSheetName());

        f = FormulaParser.parse("table!1:3");
        assertEquals(1, f.length);
        assertTrue(f[0] instanceof RangeReferenceElement);
        r = (RangeReferenceElement) f[0];
        assertEquals(0, r.getToken().getFrom());
        assertEquals(9, r.getToken().getTo());
        rr = r.getRangeReference();
        assertTrue(rr.isAlive());
        assertNull(rr.getLeftAnchor());
        assertNull(rr.getRightAnchor());
        assertEquals(new Anchor(0, false), rr.getTopAnchor());
        assertEquals(new Anchor(2, false), rr.getBottomAnchor());
        assertEquals("table", rr.getAssetName());
        assertNull(rr.getSheetName());

        f = FormulaParser.parse("A:B");
        assertEquals(1, f.length);
        assertTrue(f[0] instanceof RangeReferenceElement);
        r = (RangeReferenceElement) f[0];
        assertEquals(0, r.getToken().getFrom());
        assertEquals(3, r.getToken().getTo());
        rr = r.getRangeReference();
        assertTrue(rr.isAlive());
        assertEquals(new Anchor(0, false), rr.getLeftAnchor());
        assertEquals(new Anchor(1, false), rr.getRightAnchor());
        assertNull(rr.getTopAnchor());
        assertNull(rr.getBottomAnchor());
        assertNull(rr.getAssetName());
        assertNull(rr.getSheetName());

        f = FormulaParser.parse("1:3");
        assertEquals(1, f.length);
        assertTrue(f[0] instanceof RangeReferenceElement);
        r = (RangeReferenceElement) f[0];
        assertEquals(0, r.getToken().getFrom());
        assertEquals(3, r.getToken().getTo());
        rr = r.getRangeReference();
        assertTrue(rr.isAlive());
        assertNull(rr.getLeftAnchor());
        assertNull(rr.getRightAnchor());
        assertEquals(new Anchor(0, false), rr.getTopAnchor());
        assertEquals(new Anchor(2, false), rr.getBottomAnchor());
        assertNull(rr.getAssetName());
        assertNull(rr.getSheetName());
    }

    public void testParseIllegalFormula() {
        try {
            FormulaParser.parse("");
            fail();
        } catch (FormulaParserException e) {
        }
        try {
            FormulaParser.parse("some malformed formula");
            fail();
        } catch (FormulaParserException e) {
        }

    }
}
