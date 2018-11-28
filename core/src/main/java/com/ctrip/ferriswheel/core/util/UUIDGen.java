package com.ctrip.ferriswheel.core.util;


import java.util.UUID;


public class UUIDGen {
    public static UUID generate() {
        return UUID.fromString(new com.eaio.uuid.UUID().toString());
    }
}
