package com.ctrip.ferriswheel.quarks.token;

import com.ctrip.ferriswheel.quarks.Token;
import junit.framework.TestCase;

public class TestDefaultToken extends TestCase {
    public void testEqualsToIgnoreCase() {
        DefaultToken token = new DefaultToken("hello world !", 6, 11, 1, Token.Type.String, null);
        assertTrue(token.equalsToIgnoreCase("World"));
        assertTrue(token.equalsToIgnoreCase("The World !", 4, 9));
    }
}
