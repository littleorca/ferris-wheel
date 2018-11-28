package com.ctrip.ferriswheel.example.web;

public interface RequestActionHandler {
    com.ctrip.ferriswheel.proto.v1.EditResponse handle(long txId,
                                                       com.ctrip.ferriswheel.proto.v1.Action action,
                                                       WorkContext workContext);
}
