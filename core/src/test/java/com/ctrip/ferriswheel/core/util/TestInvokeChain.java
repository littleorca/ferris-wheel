package com.ctrip.ferriswheel.core.util;

import junit.framework.TestCase;

public class TestInvokeChain extends TestCase {
    interface TestIntf {
        void hello();

        String world();
    }

    class TestImpl implements TestIntf {
        final StringBuilder sb;
        final String myid;

        TestImpl(StringBuilder sb, String myid) {
            this.sb = sb;
            this.myid = myid;
        }

        @Override
        public void hello() {
            sb.append('{').append(myid).append(":").append("hello}");
        }

        @Override
        public String world() {
            sb.append('{').append(myid).append(":").append("world}");
            return myid;
        }
    }

    public void testInvokeChain() {
        StringBuilder sb = new StringBuilder();
        TestIntf testIntf = new InvokeChain.Builder<>(TestIntf.class)
                .add(new TestImpl(sb, "first"))
                .add(new TestImpl(sb, "second"))
                .add(new TestImpl(sb, "third"))
                .add(new TestImpl(sb, "fourth"))
                .build();
        testIntf.hello();
        assertEquals("{first:hello}{second:hello}{third:hello}{fourth:hello}", sb.toString());
        sb.delete(0, sb.length());
        String ret = testIntf.world();
        assertEquals("{first:world}{second:world}{third:world}{fourth:world}", sb.toString());
        assertEquals("fourth", ret);
    }
}
