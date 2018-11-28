package com.ctrip.ferriswheel.core.intf;

public interface Aggregator {

    AggregateType getType();

    void feed(Variant variant);

    Variant getResult();

}
