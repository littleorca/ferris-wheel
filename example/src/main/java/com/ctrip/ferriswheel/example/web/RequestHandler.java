package com.ctrip.ferriswheel.example.web;

public interface RequestHandler {
    com.ctrip.ferriswheel.proto.v1.EditResponse handle(com.ctrip.ferriswheel.proto.v1.EditRequest request,
                                                       WorkContext workContext);
}
