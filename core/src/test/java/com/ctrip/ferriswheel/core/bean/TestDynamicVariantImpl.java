package com.ctrip.ferriswheel.core.bean;

import com.ctrip.ferriswheel.core.formula.FormulaParserException;
import junit.framework.TestCase;

public class TestDynamicVariantImpl extends TestCase {
    public void testIllegalFormula() {
//        try {
//            new DynamicValue((String) null);
//            fail();
//        } catch (IllegalArgumentException e) {
//        }
//        try {
//            new DynamicValue(null, null);
//            fail();
//        } catch (IllegalArgumentException e) {
//        }
        try {
            new DynamicVariantImpl("");
            fail();
        } catch (FormulaParserException e) {
        }
        try {
            new DynamicVariantImpl("some malformed formula");
            fail();
        } catch (FormulaParserException e) {
        }
//        try {
//            new DynamicValue("A1");
//            fail();
//        } catch (IllegalArgumentException e) {
//        }
//        try {
//            new DynamicValue("A1:B1");
//            fail();
//        } catch (IllegalArgumentException e) {
//        }
    }
}
